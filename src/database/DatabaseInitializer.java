package database;

import utils.PasswordUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DatabaseInitializer {

    // ── Point d'entrée principal ─────────────────────────────────────────────
    public static void initialize() {
        ensureDatabaseExists();
        try (Connection conn = DatabaseConnection.getConnection()) {
            createTables(conn);
            insertDefaultCategories(conn);
            insertDemoUser(conn);
            System.out.println("[SmartBudget] Base de données prête.");
        } catch (SQLException e) {
            throw new RuntimeException(
                "Erreur lors de l'initialisation de la base :\n" + e.getMessage(), e);
        }
    }

    // ── Créer la base si elle n'existe pas ───────────────────────────────────
    private static void ensureDatabaseExists() {
        Properties props = loadRawConfig();
        String host   = props.getProperty("db.host",     "localhost");
        String port   = props.getProperty("db.port",     "3306");
        String dbName = props.getProperty("db.name",     "smartbudget");
        String user   = props.getProperty("db.username", "root");
        String pass   = props.getProperty("db.password", "");

        // Connexion sans spécifier la base pour pouvoir la créer
        String rootUrl = "jdbc:mysql://" + host + ":" + port
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
            + "&characterEncoding=UTF-8";

        try (Connection conn = DriverManager.getConnection(rootUrl, user, pass);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE DATABASE IF NOT EXISTS `" + dbName
                + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("[SmartBudget] Base '" + dbName + "' vérifiée/créée.");
        } catch (SQLException e) {
            // Si la base existe déjà et qu'on n'a pas les droits CREATE DATABASE,
            // on continue — DatabaseConnection.getConnection() échouera avec un message clair.
            System.err.println("[SmartBudget] Avertissement création base : " + e.getMessage());
        }
    }

    // ── Création des tables (idempotent) ─────────────────────────────────────
    private static void createTables(Connection conn) throws SQLException {
        String[] ddl = {
            """
            CREATE TABLE IF NOT EXISTS users (
                id              INT AUTO_INCREMENT PRIMARY KEY,
                username        VARCHAR(100) NOT NULL UNIQUE,
                password        VARCHAR(255) NOT NULL,
                initial_balance DECIMAL(15,2) DEFAULT 0,
                created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """,
            """
            CREATE TABLE IF NOT EXISTS categories (
                id   INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                type ENUM('INCOME','EXPENSE') NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """,
            """
            CREATE TABLE IF NOT EXISTS transactions (
                id               INT AUTO_INCREMENT PRIMARY KEY,
                user_id          INT NOT NULL,
                category_id      INT NOT NULL,
                amount           DECIMAL(15,2) NOT NULL,
                type             ENUM('INCOME','EXPENSE') NOT NULL,
                description      VARCHAR(255),
                transaction_date DATE NOT NULL,
                payment_method   VARCHAR(50),
                created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id)     REFERENCES users(id)      ON DELETE CASCADE,
                FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """,
            """
            CREATE TABLE IF NOT EXISTS budgets (
                id           INT AUTO_INCREMENT PRIMARY KEY,
                user_id      INT NOT NULL,
                category_id  INT NOT NULL,
                amount_limit DECIMAL(15,2) NOT NULL,
                month        INT NOT NULL,
                year         INT NOT NULL,
                FOREIGN KEY (user_id)     REFERENCES users(id)      ON DELETE CASCADE,
                FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """,
            """
            CREATE TABLE IF NOT EXISTS savings_goals (
                id             INT AUTO_INCREMENT PRIMARY KEY,
                user_id        INT NOT NULL,
                name           VARCHAR(150) NOT NULL,
                target_amount  DECIMAL(15,2) NOT NULL,
                current_amount DECIMAL(15,2) DEFAULT 0,
                deadline       DATE,
                created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """
        };
        try (Statement stmt = conn.createStatement()) {
            for (String sql : ddl) stmt.execute(sql);
        }
        // Migration : ajouter initial_balance si colonne absente (base existante)
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS initial_balance DECIMAL(15,2) DEFAULT 0");
        } catch (SQLException ignored) {}
    }

    // ── Catégories par défaut ────────────────────────────────────────────────
    private static void insertDefaultCategories(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM categories")) {
            rs.next();
            if (rs.getInt(1) > 0) return;
        }

        String[][] categories = {
            {"Salaire",      "INCOME"},
            {"Freelance",    "INCOME"},
            {"Alimentation", "EXPENSE"},
            {"Transport",    "EXPENSE"},
            {"Logement",     "EXPENSE"},
            {"Internet",     "EXPENSE"},
            {"Santé",        "EXPENSE"},
            {"Études",       "EXPENSE"},
            {"Loisirs",      "EXPENSE"},
            {"Autre",        "EXPENSE"}
        };

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO categories (name, type) VALUES (?, ?)")) {
            for (String[] cat : categories) {
                ps.setString(1, cat[0]);
                ps.setString(2, cat[1]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ── Compte de démonstration ──────────────────────────────────────────────
    private static void insertDemoUser(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE username = ?")) {
            ps.setString(1, "admin");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) return;
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (username, password) VALUES (?, ?)")) {
            ps.setString(1, "admin");
            ps.setString(2, PasswordUtils.hash("admin123"));
            ps.executeUpdate();
        }
        System.out.println("[SmartBudget] Compte démo créé : admin / admin123");
    }

    // ── Lecture brute du fichier de config (avant DatabaseConnection) ────────
    private static Properties loadRawConfig() {
        Properties p = new Properties();
        File f = new File("config/database.properties");
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                p.load(fis);
            } catch (IOException ignored) {}
        }
        return p;
    }
}
