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
import ui.menu.MenuPrinter;
import ui.menu.StudentMenu;
import ui.menu.TeacherMenu;
import ui.menu.TechSupportSpecialistMenu;

import java.util.List;
import java.util.Scanner;

public class ConsoleApplication {
    private final Scanner sc;
    private final AuthService authService;

    private final AdminService adminService;
    private final ManagerService managerService;
    private final TechSupportService techSupportService;
    private final MessageService messageService;
    private final ReportService reportService;
    private final UserService userService;
    private final TeacherService teacherService;
    private final JournalService journalService;
    private final NewsService newsService;
    private final ResearchPaperService researchPaperService;
    private final ResearchProjectService researchProjectService;
    private final TranscriptService transcriptService;
    private final StudentService studentService;

    private final AdminMenu adminMenu;
    private final StudentMenu studentMenu;
    private final GraduateStudentMenu graduateStudentMenu;
    private final TeacherMenu teacherMenu;
    private final ManagerMenu managerMenu;
    private final TechSupportSpecialistMenu techSupportSpecialistMenu;

    public ConsoleApplication() {
        Database db = Database.getInstance();
        this.sc = new Scanner(System.in);

        this.authService = new AuthService(db);
        this.userService = new UserService(db, authService);
        this.reportService = new ReportService(db, authService);
        this.messageService = new MessageService(db, authService);
        this.techSupportService = new TechSupportService(db, authService);
        this.teacherService = new TeacherService(db, authService);
        this.journalService = new JournalService(db, authService);
        this.newsService = new NewsService(db, authService);
        this.researchPaperService = new ResearchPaperService(db, authService, journalService);
        this.researchProjectService = new ResearchProjectService(db, authService);
        this.transcriptService = new TranscriptService(db, authService);
        this.studentService = new StudentService(db, authService);
        this.adminService = new AdminService(db, authService, userService);
        this.managerService = new ManagerService(db, authService, reportService);

        this.adminMenu = new AdminMenu(adminService, userService, authService, sc);
        this.studentMenu = new StudentMenu(authService, studentService, userService, transcriptService, journalService, sc);
        this.graduateStudentMenu = new GraduateStudentMenu(authService, studentService, userService, transcriptService, researchPaperService, researchProjectService, journalService, sc);
        this.teacherMenu = new TeacherMenu(authService, teacherService, userService, messageService, techSupportService, researchPaperService, researchProjectService, journalService, sc);
        this.managerMenu = new ManagerMenu(managerService, userService, authService, messageService, techSupportService, researchProjectService, newsService, reportService, sc);
        this.techSupportSpecialistMenu = new TechSupportSpecialistMenu(techSupportService, authService, messageService, userService, sc);
    }

    public void run() {
        MenuPrinter.printPromptBox("UNIVERSITY MANAGEMENT SYSTEM");

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

    private void showLoginMenu() {
        MenuPrinter.print("LOGIN MENU", null, List.of(
                "1. Login",
                "0. Exit"
        ));
    }

    private boolean login() {
        MenuPrinter.printPromptBox("LOGIN");
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        User user = authService.login(username, password);
        return user != null;
    }

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
