package app;

import model.users.Admin;
import model.users.GraduateStudent;
import model.users.Manager;
import model.users.Student;
import model.users.Teacher;
import model.users.TechSupportSpecialist;
import model.users.User;
import services.*;
import storage.Database;
import ui.menu.AdminMenu;
import ui.menu.GraduateStudentMenu;
import ui.menu.ManagerMenu;
import ui.menu.StudentMenu;
import ui.menu.TeacherMenu;
import ui.menu.TechSupportSpecialistMenu;

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
    private final TeacherService teacherService;
    private final NewsService newsService;
    private final JournalService journalService;
    private final ResearchService researchService;
    private final ResearchPaperService researchPaperService;
    private final ResearchProjectService researchProjectService;
    private final TranscriptService transcriptService;

    // Menus
    private final AdminMenu adminMenu;
    private final StudentMenu studentMenu;
    private final GraduateStudentMenu graduateStudentMenu;
    private final TeacherMenu teacherMenu;
    private final ManagerMenu managerMenu;
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
        this.teacherService = new TeacherService(db, authService);
        this.newsService = new NewsService(db, authService);
        this.journalService = new JournalService(db, authService);
        this.researchService = new ResearchService(db, authService);
        this.researchPaperService = new ResearchPaperService(db, authService, journalService);
        this.researchProjectService = new ResearchProjectService(db, authService);
        this.transcriptService = new TranscriptService(authService);
        this.adminService = new AdminService(db, authService, userService);
        this.managerService = new ManagerService(db, authService, reportService);

        // Init menus
        this.adminMenu = new AdminMenu(adminService, authService, sc);
        this.studentMenu = new StudentMenu(authService, transcriptService, sc);
        this.graduateStudentMenu = new GraduateStudentMenu(authService, researchService, researchPaperService, researchProjectService, journalService, sc);
        this.teacherMenu = new TeacherMenu(authService, researchPaperService, sc);
        this.managerMenu = new ManagerMenu(managerService, userService, authService, sc);
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
            case GraduateStudent i -> graduateStudentMenu.show();
            case Student i -> studentMenu.show();
            case Teacher i -> teacherMenu.show();
            case Manager i -> managerMenu.show();
            case TechSupportSpecialist i -> techSupportSpecialistMenu.show();
            default -> System.out.println("Menu for your role is not implemented yet.");
        }
    }
}
