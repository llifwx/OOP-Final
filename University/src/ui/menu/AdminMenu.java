package ui.menu;

import enums.DegreeType;
import enums.Language;
import factory.UserFactory;
import model.users.Admin;
import model.users.Student;
import model.users.User;
import services.AdminService;
import services.AuthService;
import services.UserService;
import ui.MenuPrinter;
import utils.LogRecord;
import utils.UserNamePadding;

import java.util.List;
import java.util.Scanner;

import static i18n.I18n.t;

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

            MenuPrinter.print(t("admin.title"), t("admin.welcome", admin.getFullName()), List.of("1. " + t("admin.add_user") + " (" + usersCount + ")", "2. " + t("admin.remove_user"), "3. " + t("admin.update_user"), "4. " + t("admin.view_users"), "5. " + t("admin.view_logs") + " (" + logsCount + ")", "6. " + t("admin.view_logs_user"), "7. " + t("admin.switch_lang"), "0. " + t("menu.logout")));

            switch (sc.nextLine().trim()) {
                case "1" -> addUser();
                case "2" -> removeUser();
                case "3" -> updateUser();
                case "4" -> adminService.printAllUsers();
                case "5" -> printLogs(adminService.viewAllLogs(), t("admin.logs.title"));
                case "6" -> printLogsForUser();
                case "7" -> switchLanguage(admin);
                case "0" -> {
                    authService.logout();
                    running = false;
                }
                default -> System.out.println(t("app.invalid"));
            }
        }
    }

    private void addUser() {
        System.out.println("Supported roles: " + UserFactory.getSupportedRoles());

        String role = promptRequired(t("prompt.role"));
        String username = promptRequired(t("prompt.username"));
        String password = promptRequired(t("prompt.password"));
        String fullName = promptRequired(t("prompt.fullname"));
        String email = promptRequired(t("prompt.email"));

        if (role == null || username == null || password == null || fullName == null || email == null) {
            return;
        }

        User user;

        switch (role.toLowerCase()) {
            case "student" -> user = createStudentFromInput(username, password, fullName, email);

            case "graduate", "graduatestudent" ->
                    user = createGraduateStudentFromInput(username, password, fullName, email);

            default -> user = UserFactory.createUser(role, username, password, fullName, email);
        }

        if (user == null) {
            System.out.println(t("admin.unsupported_role"));
            return;
        }

        adminService.addUser(user);
    }

    private void removeUser() {
        String username = promptRequired(t("prompt.username_remove"));
        if (username != null) adminService.removeUser(username);
    }

    private void updateUser() {
        String username = promptRequired(t("prompt.username_update"));
        if (username == null) return;
        User user = userService.findByUsername(username);
        if (user == null) {
            System.out.println(t("admin.user_not_found"));
            return;
        }
        if (user instanceof Student student) {
            updateStudentFields(student);
            return;
        }
        System.out.println("1. " + t("admin.field.name"));
        System.out.println("2. " + t("admin.field.email"));
        System.out.println("3. " + t("admin.field.language"));
        System.out.print(t("prompt.field") + ": ");
        switch (sc.nextLine().trim()) {
            case "1" -> {
                String value = promptRequired(t("admin.field.new_name"));
                if (value != null)
                    System.out.println(adminService.updateUserFullName(username, value) ? t("admin.updated") : t("admin.update_failed"));
            }
            case "2" -> {
                String value = promptRequired(t("admin.field.new_email"));
                if (value != null)
                    System.out.println(adminService.updateUserEmail(username, value) ? t("admin.updated") : t("admin.update_failed"));
            }
            case "3" -> {
                Language language = readLanguage();
                if (language != null)
                    System.out.println(adminService.updateUserLanguage(username, language) ? t("admin.updated") : t("admin.update_failed"));
            }
            default -> System.out.println(t("admin.invalid_field"));
        }
    }

    private void printLogsForUser() {
        String username = promptRequired(t("prompt.username"));
        if (username == null) return;
        User user = userService.findByUsername(username);
        if (user == null) {
            System.out.println(t("admin.user_not_found"));
            return;
        }
        printLogs(adminService.viewLogsForUser(user), t("admin.logs.for", username));
    }

    private void switchLanguage(User user) {
        Language language = readLanguage();
        if (language != null) userService.changeLanguage(user, language);
    }

    private void printLogs(List<LogRecord> logs, String title) {
        if (logs.isEmpty()) {
            System.out.println(t("admin.no_logs"));
            return;
        }
        System.out.println("--- " + title + " ---");
        logs.forEach(System.out::println);
    }

    private User createStudentFromInput(String username, String password, String fullName, String email) {
        Language language = readLanguage();
        if (language == null) return null;

        String studentId = promptRequired("Student ID");
        String school = promptRequired("School");
        String major = promptRequired("Major");
        int yearOfStudy = readInt("Year of study");

        if (studentId == null || school == null || major == null || yearOfStudy < 1) {
            return null;
        }

        return UserFactory.createStudent(username, password, fullName, email, language, studentId, school, major, yearOfStudy);
    }

    private User createGraduateStudentFromInput(String username, String password, String fullName, String email) {
        Language language = readLanguage();
        if (language == null) return null;

        String studentId = promptRequired("Student ID");
        String school = promptRequired("School");
        String major = promptRequired("Major");
        int yearOfStudy = readInt("Year of study");

        if (studentId == null || school == null || major == null || yearOfStudy < 1) {
            return null;
        }

        System.out.print("Degree type (MASTER, PHD): ");
        DegreeType degreeType;

        try {
            degreeType = DegreeType.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid degree type.");
            return null;
        }

        return UserFactory.createGraduateStudent(username, password, fullName, email, language, studentId, school, major, yearOfStudy, degreeType);
    }

    private void updateStudentFields(Student student) {
        System.out.println("1. " + t("admin.field.name"));
        System.out.println("2. " + t("admin.field.email"));
        System.out.println("3. " + t("admin.field.language"));
        System.out.println("4. School");
        System.out.println("5. Major");
        System.out.println("6. Year of study");

        System.out.print(t("prompt.field") + ": ");

        switch (sc.nextLine().trim()) {
            case "1" -> {
                String value = promptRequired(t("admin.field.new_name"));
                if (value != null) {
                    System.out.println(adminService.updateUserFullName(student.getUsername(), value) ? t("admin.updated") : t("admin.update_failed"));
                }
            }
            case "2" -> {
                String value = promptRequired(t("admin.field.new_email"));
                if (value != null) {
                    System.out.println(adminService.updateUserEmail(student.getUsername(), value) ? t("admin.updated") : t("admin.update_failed"));
                }
            }
            case "3" -> {
                Language language = readLanguage();
                if (language != null) {
                    System.out.println(adminService.updateUserLanguage(student.getUsername(), language) ? t("admin.updated") : t("admin.update_failed"));
                }
            }
            case "4" -> {
                String school = promptRequired("New school");
                if (school != null) {
                    student.setSchool(school);
                    System.out.println(t("admin.updated"));
                }
            }
            case "5" -> {
                String major = promptRequired("New major");
                if (major != null) {
                    student.setMajor(major);
                    System.out.println(t("admin.updated"));
                }
            }
            case "6" -> {
                int year = readInt("New year of study");
                if (year >= 1) {
                    student.setYearOfStudy(year);
                    System.out.println(t("admin.updated"));
                }
            }
            default -> System.out.println(t("admin.invalid_field"));
        }
    }

    private Language readLanguage() {
        System.out.print(t("prompt.language") + ": ");
        try {
            return Language.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println(t("lang.invalid"));
            return null;
        }
    }

    private String promptRequired(String label) {
        System.out.print(label + ": ");
        String value = sc.nextLine().trim();
        if (value.isEmpty()) {
            System.out.println(t("prompt.cannot_empty", label));
            return null;
        }
        return value;
    }

    private int readInt(String label) {
        System.out.print(label + ": ");
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println(t("invalid.number"));
            return -1;
        }
    }
}