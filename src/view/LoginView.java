package view;

import controller.LoginController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginView extends JFrame {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JButton registerButton;
    private final JLabel errorLabel;

    public LoginView() {
        setTitle("SmartBudget — Connexion");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(AppColors.BACKGROUND);
        setContentPane(root);

        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(16, AppColors.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 48, 40, 48));
        card.setPreferredSize(new Dimension(400, 460));

        // Logo / title
        JLabel logo = new JLabel("SmartBudget");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logo.setForeground(AppColors.PRIMARY);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Gestion de budget personnel");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(AppColors.TEXT_LIGHT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        usernameField = UIComponents.styledField(20);
        passwordField = UIComponents.styledPasswordField(20);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        loginButton = UIComponents.primaryButton("SE CONNECTER");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        registerButton = UIComponents.secondaryButton("Créer un compte");
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(AppColors.DANGER);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(logo);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(32));
        card.add(fieldLabel("Nom d'utilisateur"));
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(16));
        card.add(fieldLabel("Mot de passe"));
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(24));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(10));
        card.add(registerButton);
        card.add(Box.createVerticalStrut(12));
        card.add(errorLabel);

        root.add(card);
        pack();
        setLocationRelativeTo(null);

        // Wire controller
        new LoginController(this);

        // Enter key triggers login
        getRootPane().setDefaultButton(loginButton);
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(AppColors.TEXT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public String getUsername() { return usernameField.getText(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public void showError(String msg) { errorLabel.setText(msg); }
    public void addLoginListener(ActionListener l) { loginButton.addActionListener(l); }
    public void addRegisterListener(ActionListener l) { registerButton.addActionListener(l); }
}
