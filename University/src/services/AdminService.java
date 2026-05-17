package services;

import enums.Language;
import model.users.Admin;
import model.users.User;
import storage.Database;
import utils.LogRecord;

import java.util.ArrayList;
import java.util.List;

public class AdminService {

    private final Database database;
    private final AuthService authService;
    private final UserService userService;

    public AdminService(Database database, AuthService authService, UserService userService) {
        this.database = database;
        this.authService = authService;
        this.userService = userService;
    }

    private boolean isAdmin() {
        return authService.hasRole(Admin.class);
    }

    private void requireAdmin() {
        if (!isAdmin()) {
            throw new SecurityException("[AdminService] Access denied: current user is not an Admin.");
        }
    }

    public boolean addUser(User user) {
        requireAdmin();
        return userService.registerUser(user);
    }

    public boolean removeUser(String username) {
        requireAdmin();
        return userService.removeUser(username);
    }

    public boolean updateUserEmail(String username, String newEmail) {
        requireAdmin();
        User user = userService.findByUsername(username);
        if (user == null || newEmail == null || newEmail.isBlank()) return false;
        user.setEmail(newEmail);
        log("Admin updated email for user: " + username);
        database.save();
        return true;
    }

    public boolean updateUserFullName(String username, String newFullName) {
        requireAdmin();
        User user = userService.findByUsername(username);
        if (user == null || newFullName == null || newFullName.isBlank()) return false;
        user.setFullName(newFullName);
        log("Admin updated full name for user: " + username);
        database.save();
        return true;
    }

    public boolean updateUserLanguage(String username, Language language) {
        requireAdmin();
        User user = userService.findByUsername(username);
        if (user == null || language == null) return false;
        user.setLanguage(language);
        log("Admin updated language for user: " + username);
        database.save();
        return true;
    }

    public List<LogRecord> viewAllLogs() {
        requireAdmin();
        return new ArrayList<>(database.getLogs());
    }

    public List<LogRecord> viewLogsForUser(User user) {
        requireAdmin();
        return database.findLogsByUser(user);
    }

    public void printAllLogs() {
        requireAdmin();
        List<LogRecord> logs = database.getLogs();
        if (logs.isEmpty()) {
            System.out.println("[AdminService] No logs found.");
            return;
        }

        System.out.println("----- System Logs -----");
        for (LogRecord log : logs) {
            System.out.println(log);
        }
        System.out.println("-----------------------");
    }

    public List<User> listAllUsers() {
        requireAdmin();
        return userService.getAllUsers();
    }

    public void printAllUsers() {
        requireAdmin();
        List<User> users = database.getUsers();
        if (users.isEmpty()) {
            System.out.println("[AdminService] No users in system.");
            return;
        }

        System.out.println("----- All Users -----");
        for (User user : users) {
            System.out.println("[" + user.getClass().getSimpleName() + "] " + user);
        }
        System.out.println("---------------------");
    }

    private void log(String action) {
        User actor = authService.getCurrentUser();
        if (actor != null) {
            database.addLog(new LogRecord(actor, action));
        }
    }
}
