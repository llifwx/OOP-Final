package ui.menu;

import factory.UserFactory;
import model.users.Admin;
import model.users.User;
import services.AdminService;
import services.AuthService;
import utils.LogRecord;
import utils.UserNamePadding;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final AdminService adminService;
    private final AuthService authService;
    private final Scanner sc;
    private final UserNamePadding padding = new UserNamePadding();

    public AdminMenu(AdminService adminService, AuthService authService, Scanner sc) {
        this.adminService = adminService;
        this.authService = authService;
        this.sc = sc;
    }

    public void show() {
        Admin admin = (Admin) authService.getCurrentUser();
        boolean running = true;

        while (running) {
            int usersCount = adminService.listAllUsers().size();
            int logsCount = adminService.viewAllLogs().size();

            System.out.println("\n+--------------------------------------+");
            System.out.println("|                ADMIN                 |");
            System.out.println("|         " + padding.padRight("Welcome, " + admin.getFullName(), 28) + " |");
            System.out.println("+--------------------------------------+");
            System.out.printf("|  1. Add user              (%s)       |%n", padding.padRight(String.valueOf(usersCount), 3));
            System.out.println("|  2. Remove user                     |");
            System.out.println("|  3. View all users                  |");
            System.out.printf("|  4. View all logs         (%s)       |%n", padding.padRight(String.valueOf(logsCount), 3));
            System.out.println("|  5. View logs by username           |");
            System.out.println("|  0. Logout                          |");
            System.out.println("+--------------------------------------+");

            System.out.print("Your choice: ");

            switch (sc.nextLine().trim()) {
                case "1" -> addUser();
                case "2" -> removeUser();
                case "3" -> adminService.printAllUsers();
                case "4" -> printLogs();
                case "5" -> printLogsForUser();
                case "0" -> {
                    authService.logout();
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Menu actions
    private void addUser() {
        System.out.println("Supported roles: " + UserFactory.getSupportedRoles());
        System.out.print("Role: ");
        String role = sc.nextLine().trim();
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();
        System.out.print("Full name: ");
        String fullName = sc.nextLine().trim();
        System.out.print("Email: ");
        String email = sc.nextLine().trim();

        User user = UserFactory.createUser(role, username, password, fullName, email);
        if (user == null) {
            System.out.println("Cannot create user with role: " + role);
            return;
        }

        adminService.addUser(user);
    }

    private void removeUser() {
        System.out.print("Enter username to remove: ");
        String username = sc.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        adminService.removeUser(username);
    }

    private void printLogs() {
        List<LogRecord> logs = adminService.viewAllLogs();

        if (logs.isEmpty()) {
            System.out.println("No logs found.");
            return;
        }

        printLogList(logs, "System logs");
    }

    private void printLogsForUser() {
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();

        User user = findUserByUsername(username);
        if (user == null) {
            System.out.println("User not found: " + username);
            return;
        }

        List<LogRecord> logs = adminService.viewLogsForUser(user);
        if (logs.isEmpty()) {
            System.out.println("No logs for user: " + username);
            return;
        }

        printLogList(logs, "Logs for " + username);
    }

    // Utils
    private User findUserByUsername(String username) {
        for (User user : adminService.listAllUsers()) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    private void printLogList(List<LogRecord> logs, String title) {
        System.out.println("\n--- " + title + " (" + logs.size() + ") ---");
        for (LogRecord log : logs) {
            System.out.println(log);
        }
        System.out.println("----------------------------------------");
    }
}
