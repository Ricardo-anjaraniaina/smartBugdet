package controller;

import model.User;

/**
 * Façade vers utils.SessionManager.
 * Conservé pour compatibilité avec les imports existants dans les vues.
 */
public class SessionManager {

    private SessionManager() {}

    public static void   login(User user)  { utils.SessionManager.login(user); }
    public static void   logout()          { utils.SessionManager.logout(); }
    public static User   getCurrentUser()  { return utils.SessionManager.getCurrentUser(); }
    public static int    getUserId()       { return utils.SessionManager.getUserId(); }
    public static String getUsername()     { return utils.SessionManager.getUsername(); }
    public static boolean isLoggedIn()     { return utils.SessionManager.isLoggedIn(); }
}
