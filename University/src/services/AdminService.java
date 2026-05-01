package services;

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
        boolean result = userService.registerUser(user);
        if (result) {
            log("Admin added user: " + user.getUsername());
        }
        return result;
    }

    public boolean removeUser(String username) {
        requireAdmin();
        boolean result = userService.removeUser(username);
        if (result) {
            log("Admin removed user: " + username);
        }
        return result;
    }

    public void updateUser(User user) {
        requireAdmin();
        if (user == null) return;
        // The user object is mutated directly — this confirms the change and logs it.
        log("Admin updated user: " + user.getUsername());
        System.out.println("[AdminService] User '" + user.getUsername() + "' updated.");
    }

    public List<LogRecord> viewAllLogs() {
        requireAdmin();
        List<LogRecord> logs = new ArrayList<>(database.getLogs());
        System.out.println("[AdminService] Retrieved " + logs.size() + " log record(s).");
        return logs;
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
        System.out.println("─── System Logs ───────────────────────────────");
        for (LogRecord log : logs) {
            System.out.println(log);
        }
        System.out.println("───────────────────────────────────────────────");
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
        System.out.println("─── All Users ──────────────────────────────────");
        for (User u : users) {
            System.out.println("[" + u.getClass().getSimpleName() + "] " + u);
        }
        System.out.println("───────────────────────────────────────────────");
    }

    private void log(String action) {
        User actor = authService.getCurrentUser();
        if (actor != null) {
            database.addLog(new LogRecord(actor, action));
        }
    }
}
