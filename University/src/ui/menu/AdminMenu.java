package ui.menu;

import enums.Language;
import factory.UserFactory;
import model.users.Admin;
import model.users.User;
import services.AdminService;
import services.AuthService;
import services.UserService;
import utils.LogRecord;
import utils.UserNamePadding;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final AdminService adminService;
    private final UserService userService;
    private final AuthService authService;
    private final Scanner sc;
    private final UserNamePadding padding = new UserNamePadding();

    public AdminMenu(AdminService adminService, UserService userService, AuthService authService, Scanner sc) {
        this.adminService = adminService;
        this.userService = userService;
        this.authService = authService;
        this.sc = sc;
    }

    public void show() {
        Admin admin = (Admin) authService.getCurrentUser();
        boolean running = true;
        while (running) {
            int usersCount = adminService.listAllUsers().size();
            int logsCount = adminService.viewAllLogs().size();

            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║                ADMIN                 ║");
            System.out.println("║         " + padding.padRight("Welcome, " + admin.getFullName(), 28) + " ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.printf("║  1. Add user              (%s)      ║%n", padding.padRight(String.valueOf(usersCount), 3));
            System.out.println("║  2. Remove user                      ║");
            System.out.println("║  3. Update user                      ║");
            System.out.println("║  4. View all users                   ║");
            System.out.printf("║  5. View all logs         (%s)      ║%n", padding.padRight(String.valueOf(logsCount), 3));
            System.out.println("║  6. View logs by username            ║");
            System.out.println("║  7. Switch my language               ║");
            System.out.println("║  0. Logout                           ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Your choice: ");

            switch (sc.nextLine().trim()) {
                case "1" -> addUser();
                case "2" -> removeUser();
                case "3" -> updateUser();
                case "4" -> adminService.printAllUsers();
                case "5" -> printLogs(adminService.viewAllLogs(), "System logs");
                case "6" -> printLogsForUser();
                case "7" -> switchLanguage(admin);
                case "0" -> {
                    authService.logout();
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void addUser() {
        System.out.println("Supported roles: " + UserFactory.getSupportedRoles());
        String role = promptRequired("Role");
        String username = promptRequired("Username");
        String password = promptRequired("Password");
        String fullName = promptRequired("Full name");
        String email = promptRequired("Email");
        if (role == null || username == null || password == null || fullName == null || email == null) return;

        User user = UserFactory.createUser(role, username, password, fullName, email);
        if (user == null) {
            System.out.println("Unsupported role.");
            return;
        }
        adminService.addUser(user);
    }

    private void removeUser() {
        String username = promptRequired("Username to remove");
        if (username != null) adminService.removeUser(username);
    }

    private void updateUser() {
        String username = promptRequired("Username to update");
        if (username == null) return;
        User user = userService.findByUsername(username);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }
        System.out.println("1. Full name");
        System.out.println("2. Email");
        System.out.println("3. Language");
        System.out.print("Field: ");
        switch (sc.nextLine().trim()) {
            case "1" -> {
                String value = promptRequired("New full name");
                if (value != null) System.out.println(adminService.updateUserFullName(username, value) ? "Updated." : "Update failed.");
            }
            case "2" -> {
                String value = promptRequired("New email");
                if (value != null) System.out.println(adminService.updateUserEmail(username, value) ? "Updated." : "Update failed.");
            }
            case "3" -> {
                Language language = readLanguage();
                if (language != null) System.out.println(adminService.updateUserLanguage(username, language) ? "Updated." : "Update failed.");
            }
            default -> System.out.println("Invalid field.");
        }
    }

    private void printLogsForUser() {
        String username = promptRequired("Username");
        if (username == null) return;
        User user = userService.findByUsername(username);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }
        printLogs(adminService.viewLogsForUser(user), "Logs for " + username);
    }

    private void switchLanguage(User user) {
        Language language = readLanguage();
        if (language != null) userService.changeLanguage(user, language);
    }

    private void printLogs(List<LogRecord> logs, String title) {
        if (logs.isEmpty()) {
            System.out.println("No logs found.");
            return;
        }
        System.out.println("--- " + title + " ---");
        logs.forEach(System.out::println);
    }

    private Language readLanguage() {
        System.out.print("Language (KZ, EN, RU): ");
        try {
            return Language.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid language.");
            return null;
        }
    }

    private String promptRequired(String label) {
        System.out.print(label + ": ");
        String value = sc.nextLine().trim();
        if (value.isEmpty()) {
            System.out.println(label + " cannot be empty.");
            return null;
        }
        return value;
    }
}
