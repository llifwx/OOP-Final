package ui.menu;

import model.academic.Course;
import model.users.Teacher;
import services.AuthService;
import services.ResearchPaperService;
import utils.UserNamePadding;

import java.util.Scanner;

public class TeacherMenu {
    private final AuthService authService;
    private final ResearchPaperService paperService;
    private final Scanner sc;
    private final UserNamePadding padding = new UserNamePadding();

    public TeacherMenu(AuthService authService, ResearchPaperService paperService, Scanner sc) {
        this.authService = authService;
        this.paperService = paperService;
        this.sc = sc;
    }

    public void show() {
        Teacher teacher = (Teacher) authService.getCurrentUser();
        boolean running = true;

        while (running) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║                TEACHER               ║");
            System.out.println("║       " + padding.padRight("Welcome, " + teacher.getFullName(), 32) + "║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║       1. View my courses             ║");
            System.out.println("║       2. View my research papers     ║");
            System.out.println("║       3. View H-index                ║");
            System.out.println("║       0. Logout                      ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Your choice: ");

            switch (sc.nextLine().trim()) {
                case "1" -> printCourses(teacher);
                case "2" -> printPapers(teacher);
                case "3" -> System.out.println("H-index: " + teacher.calculateHIndex());
                case "0" -> {
                    authService.logout();
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void printCourses(Teacher teacher) {
        if (teacher.getCourses().isEmpty()) {
            System.out.println("No assigned courses.");
            return;
        }

        for (Course course : teacher.getCourses()) {
            System.out.println(course);
        }
    }

    private void printPapers(Teacher teacher) {
        paperService.printPapers(teacher, null);
    }
}
