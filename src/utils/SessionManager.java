package utils;

import model.User;

/**
 * Conserve l'utilisateur actuellement connecté.
 * Toutes les requêtes DAO doivent utiliser getUserId() pour filtrer les données.
 */
public class SessionManager {

    private static User currentUser;

    private SessionManager() {}

    public static void login(User user)  { currentUser = user; }
    public static void logout()          { currentUser = null; }

    public static User    getCurrentUser() { return currentUser; }
    public static int     getUserId()      { return currentUser != null ? currentUser.getId() : -1; }
    public static String  getUsername()    { return currentUser != null ? currentUser.getUsername() : ""; }
    public static boolean isLoggedIn()     { return currentUser != null; }
}
