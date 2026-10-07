import database.DatabaseInitializer;
import view.LoginView;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {

        // ── 1. Initialisation de la base de données ──────────────────────────
        try {
            DatabaseInitializer.initialize();
        } catch (Exception e) {
            showStartupError(e);
            System.exit(1);
        }

        // ── 2. Lancement de l'interface graphique ────────────────────────────
        SwingUtilities.invokeLater(() -> {
            applyLookAndFeel();
            new LoginView().setVisible(true);
        });
    }

    private static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Swing par défaut si le look natif est indisponible
        }
    }

    private static void showStartupError(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();

        // Affichage console pour le débogage
        System.err.println("=== ERREUR DE DÉMARRAGE ===");
        System.err.println(message);
        e.printStackTrace();

        // Affichage graphique pour l'utilisateur
        String userMessage =
            "SmartBudget n'a pas pu démarrer.\n\n" +
            message + "\n\n" +
            "Consultez la console pour les détails techniques.\n" +
            "Vérifiez le fichier : config/database.properties";

        // Utiliser un JTextArea pour les messages longs
        JTextArea textArea = new JTextArea(userMessage);
        textArea.setEditable(false);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textArea.setBackground(UIManager.getColor("OptionPane.background"));
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setPreferredSize(new Dimension(500, 200));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(null);

        JOptionPane.showMessageDialog(
            null,
            scroll,
            "Erreur de démarrage — SmartBudget",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
