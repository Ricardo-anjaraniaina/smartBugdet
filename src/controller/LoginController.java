package controller;

import database.UserDAO;
import model.User;
import utils.SessionManager;
import view.LoginView;
import view.MainView;

import javax.swing.*;
import java.util.Optional;

public class LoginController {

    private final LoginView view;
    private final UserDAO userDAO = new UserDAO();

    public LoginController(LoginView view) {
        this.view = view;
        this.view.addLoginListener(e -> handleLogin());
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
}
