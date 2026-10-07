package controller;

import database.UserDAO;
import model.User;
import utils.PasswordUtils;
import utils.SessionManager;
import view.LoginView;
import view.MainView;
import view.UIComponents;

import javax.swing.*;
import java.util.Optional;

public class LoginController {

    private final LoginView view;
    private final UserDAO userDAO = new UserDAO();

    public LoginController(LoginView view) {
        this.view = view;
        this.view.addLoginListener(e -> handleLogin());
        this.view.addRegisterListener(e -> handleRegister());
    }

    private void handleLogin() {
        String username = view.getUsername().trim();
        String password = view.getPassword();

        if (username.isEmpty() || password.isEmpty()) {
            view.showError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            Optional<User> opt = userDAO.authenticate(username, password);
            if (opt.isPresent()) {
                SessionManager.login(opt.get());
                view.dispose();
                SwingUtilities.invokeLater(() -> new MainView().setVisible(true));
            } else {
                view.showError("Identifiants incorrects.");
            }
        } catch (Exception ex) {
            view.showError("Erreur de connexion : " + ex.getMessage());
        }
    }

    private void handleRegister() {
        JTextField userField = UIComponents.styledField(20);
        JPasswordField passField = UIComponents.styledPasswordField(20);
        JPasswordField confirmField = UIComponents.styledPasswordField(20);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(new JLabel("Nom d'utilisateur :"));
        form.add(userField);
        form.add(Box.createVerticalStrut(10));
        form.add(new JLabel("Mot de passe :"));
        form.add(passField);
        form.add(Box.createVerticalStrut(10));
        form.add(new JLabel("Confirmer le mot de passe :"));
        form.add(confirmField);

        int result = JOptionPane.showConfirmDialog(view, form,
            "Créer un compte", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        String confirm  = new String(confirmField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            view.showError("Veuillez remplir tous les champs.");
            return;
        }
        if (!password.equals(confirm)) {
            view.showError("Les mots de passe ne correspondent pas.");
            return;
        }
        if (password.length() < 6) {
            view.showError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        try {
            if (userDAO.usernameExists(username)) {
                view.showError("Ce nom d'utilisateur est déjà pris.");
                return;
            }
            userDAO.register(username, PasswordUtils.hash(password));
            view.showError("");
            JOptionPane.showMessageDialog(view, "Compte créé ! Vous pouvez vous connecter.",
                "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            view.showError("Erreur : " + ex.getMessage());
        }
    }
}
