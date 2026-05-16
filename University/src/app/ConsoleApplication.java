package app;

import services.ReportService;
import model.users.Admin;
import model.users.TechSupportSpecialist;
import model.users.User;
import services.*;
import storage.Database;
import ui.AdminMenu;
import ui.TechSupportSpecialistMenu;

import java.util.Scanner;

public class ConsoleApplication {
    private final Scanner sc;
    private final AuthService authService;

    // Services
    private final AdminService adminService;
    private final ManagerService managerService;
    private final TechSupportService techSupportService;
    private final MessageService messageService;
    private final ReportService reportService;
    private final UserService userService;
    // todo add other services

    // Menus
    private final AdminMenu adminMenu;
    private final TechSupportSpecialistMenu techSupportSpecialistMenu;
    // todo add other menus

    public ConsoleApplication() {
        Database db = Database.getInstance();
        this.sc = new Scanner(System.in);

        // Init services
        this.authService = new AuthService(db);
        this.userService = new UserService(db, authService);
        this.reportService = new ReportService(db, authService);
        this.messageService = new MessageService(db, authService);
        this.techSupportService = new TechSupportService(db, authService);
        this.adminService = new AdminService(db, authService, userService);
        this.managerService = new ManagerService(db, authService, reportService);
        // todo add other service initializers

        // Init menus
        this.adminMenu = new AdminMenu(adminService, authService, sc);
        this.techSupportSpecialistMenu = new TechSupportSpecialistMenu(techSupportService, authService, sc);
    }

    public void run() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║    UNIVERSITY MANAGEMENT SYSTEM      ║");
        System.out.println("╚══════════════════════════════════════╝");

        boolean running = true;
        while (running) {
            showLoginMenu();
            String input = sc.nextLine().trim();

            switch (input) {
                case "1" -> {
                    if (login()) {
                        routeToMenu();
                    }
                }

                case "0" -> {
                    System.out.println("Exiting application. Goodbye!");
                    running = false;
                }

                default -> System.out.println("Invalid choice. Please try again.");
            }
        }

        sc.close();
    }

    // Login Menu
    private void showLoginMenu() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║  1. Login                            ║");
        System.out.println("║  0. Exit                             ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.print("Choice: ");
    }

    private boolean login() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║              LOGIN                   ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        User user = authService.login(username, password);
        return user != null;
    }

    // Go to specific menu
    private void routeToMenu() {
        User user = authService.getCurrentUser();

        switch (user) {
            case Admin i -> adminMenu.show();
            case TechSupportSpecialist i -> techSupportSpecialistMenu.show();
            // todo add other routes to roles
            default -> System.out.println("Menu for your role is not implemented yet.");
        }
    }
}
