package app;

import enums.Language;
import i18n.I18n;
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
import ui.MenuPrinter;
import ui.menu.StudentMenu;
import ui.menu.TeacherMenu;
import ui.menu.TechSupportSpecialistMenu;

import java.util.List;
import java.util.Scanner;

import static i18n.I18n.t;

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

        I18n.setLanguage(Language.EN);

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
        MenuPrinter.printPromptBox(t("app.title"));

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
                case "2" -> {
                    switchLanguage();
                }
                case "0" -> {
                    System.out.println(t("app.exit"));
                    running = false;
                }
                default -> System.out.println(t("app.invalid"));
            }
        }

        sc.close();
    }

    private void showLoginMenu() {
        MenuPrinter.print(t("login.menu"), null, List.of("1. " + t("login.option"), "2. " + t("login.switch"), "0. " + t("login.exit")));
    }

    private boolean login() {
        MenuPrinter.printPromptBox(t("auth.login.prompt"));
        System.out.print(t("auth.username") + ": ");
        String username = sc.nextLine().trim();
        System.out.print(t("auth.password") + ": ");
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

    private void switchLanguage() {
        System.out.println("Choose language / Выберите язык / Тілді таңдаңыз:");
        System.out.println("1. English");
        System.out.println("2. Русский");
        System.out.println("3. Қазақша");
        System.out.print(t("menu.choice") + ": ");

        String choice = sc.nextLine().trim();

        switch (choice) {
            case "1" -> I18n.setLanguage(Language.EN);
            case "2" -> I18n.setLanguage(Language.RU);
            case "3" -> I18n.setLanguage(Language.KZ);
            default -> {
                System.out.println(t("lang.invalid"));
                return;
            }
        }


    }
}
