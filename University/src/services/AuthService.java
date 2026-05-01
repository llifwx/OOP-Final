package services;

import model.users.User;
import storage.Database;
import utils.LogRecord;

public class AuthService {

    private final Database database;
    private User currentUser;

    public AuthService(Database database) {
        this.database = database;
        this.currentUser = null;
    }

    public User login(String username, String password) {
        if (username == null || password == null) {
            System.out.println("[AuthService] Login failed: username or password is null.");
            return null;
        }

        User user = database.findUserByUsername(username);

        if (user == null) {
            System.out.println("[AuthService] Login failed: user '" + username + "' not found.");
            return null;
        }

        if (!user.login(password)) {
            System.out.println("[AuthService] Login failed: incorrect password for '" + username + "'.");
            return null;
        }

        this.currentUser = user;
        LogRecord log = new LogRecord(user, "Logged in");
        database.addLog(log);
        System.out.println("[AuthService] User '" + username + "' logged in successfully.");
        return user;
    }

    public void logout() {
        if (currentUser == null) {
            System.out.println("[AuthService] No user is currently logged in.");
            return;
        }

        LogRecord log = new LogRecord(currentUser, "Logged out");
        database.addLog(log);
        System.out.println("[AuthService] User '" + currentUser.getUsername() + "' logged out.");
        currentUser.logout();
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean hasRole(Class<?> roleClass) {
        if (currentUser == null) return false;
        return roleClass.isInstance(currentUser);
    }
}