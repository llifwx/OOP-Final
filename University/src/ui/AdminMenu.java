package ui;

import factory.UserFactory;
import model.users.User;
import services.AdminService;
import services.AuthService;
import services.UserService;
import storage.Database;
import utils.LogRecord;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner scanner;
    private final AdminService adminService;

    public AdminMenu(Database database, AuthService authService) {
        this.scanner = new Scanner(System.in);
        UserService userService = new UserService(database, authService);
        this.adminService = new AdminService(database, authService, userService);
    }

    public AdminMenu(AdminService adminService) {
        this.scanner = new Scanner(System.in);
        this.adminService = adminService;
    }

    public void start() {
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readInt("Choose option: ");

            switch (choice) {
                case 1:
                    addUser();
                    break;
                case 2:
                    removeUser();
                    break;
                case 3:
                    adminService.printAllUsers();
                    break;
                case 4:
                    printLogs();
                    break;
                case 5:
                    printLogsForUser();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("[AdminMenu] Unknown option.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("----- Admin Menu -----");
        System.out.println("1. Add user");
        System.out.println("2. Remove user");
        System.out.println("3. View all users");
        System.out.println("4. View all logs");
        System.out.println("5. View logs by username");
        System.out.println("0. Exit");
    }

    private void addUser() {
        System.out.println("Supported roles: " + UserFactory.getSupportedRoles());
        String role = readLine("Role: ");
        String username = readLine("Username: ");
        String password = readLine("Password: ");
        String fullName = readLine("Full name: ");
        String email = readLine("Email: ");

        User user = UserFactory.createUser(role, username, password, fullName, email);

        if (user == null) {
            System.out.println("[AdminMenu] Cannot create user with role '" + role + "'.");
            return;
        }

        adminService.addUser(user);
    }

    private void removeUser() {
        String username = readLine("Username to remove: ");
        adminService.removeUser(username);
    }

    private void printLogs() {
        List<LogRecord> logs = adminService.viewAllLogs();
        if (logs.isEmpty()) {
            System.out.println("[AdminMenu] No logs found.");
            return;
        }

        for (LogRecord log : logs) {
            System.out.println(log);
        }
    }

    private void printLogsForUser() {
        String username = readLine("Username: ");
        User user = null;

        for (User current : adminService.listAllUsers()) {
            if (current.getUsername().equalsIgnoreCase(username)) {
                user = current;
                break;
            }
        }

        if (user == null) {
            System.out.println("[AdminMenu] User '" + username + "' not found.");
            return;
        }

        List<LogRecord> logs = adminService.viewLogsForUser(user);
        if (logs.isEmpty()) {
            System.out.println("[AdminMenu] No logs for user '" + username + "'.");
            return;
        }

        for (LogRecord log : logs) {
            System.out.println(log);
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("[AdminMenu] Enter a number.");
            }
        }
    }
}
