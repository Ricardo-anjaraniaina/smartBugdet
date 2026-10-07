package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final Properties props = new Properties();
    private static String url;

    static {
        loadConfig();
    }

    private DatabaseConnection() {}

    // ── Chargement de la configuration ──────────────────────────────────────
    private static void loadConfig() {
        // 1. Chercher config/database.properties relatif au répertoire de lancement
        File configFile = new File("config/database.properties");

        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            } catch (IOException e) {
                throw new RuntimeException(
                    "Impossible de lire config/database.properties : " + e.getMessage(), e);
            }
        } else {
            // 2. Fallback : chercher dans le classpath (utile si lancé depuis un IDE)
            try (InputStream is = DatabaseConnection.class
                    .getClassLoader().getResourceAsStream("database.properties")) {
                if (is != null) {
                    props.load(is);
                } else {
                    throw new RuntimeException(configNotFoundMessage());
                }
            } catch (IOException e) {
                throw new RuntimeException(configNotFoundMessage(), e);
            }
        }

        String host     = props.getProperty("db.host",     "localhost");
        String port     = props.getProperty("db.port",     "3306");
        String name     = props.getProperty("db.name",     "smartbudget");

        url = "jdbc:mysql://" + host + ":" + port + "/" + name
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
            + "&characterEncoding=UTF-8";
    }

    // ── Obtenir une connexion ────────────────────────────────────────────────
    public static Connection getConnection() throws SQLException {
        String user     = props.getProperty("db.username", "root");
        String password = props.getProperty("db.password", "");
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new SQLException(connectionErrorMessage(e), e);
        }
    }

    // ── Messages d'erreur lisibles ───────────────────────────────────────────
    private static String configNotFoundMessage() {
        return """
            Fichier de configuration introuvable.

            Créez le fichier : config/database.properties
            en vous basant sur : config/database.properties.example

            Contenu attendu :
              db.host=localhost
              db.port=3306
              db.name=smartbudget
              db.username=root
              db.password=
            """;
    }

    private static String connectionErrorMessage(SQLException e) {
        return """
            Impossible de se connecter à MySQL.

            Vérifiez que :
              - MySQL est installé et démarré
              - Le port configuré est correct (défaut : 3306)
              - Le nom d'utilisateur et le mot de passe sont corrects
              - La base de données 'smartbudget' existe

            Configurez vos accès dans : config/database.properties

            Détail technique : """ + e.getMessage();
    }
}
