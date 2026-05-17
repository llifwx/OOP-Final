package ui.menu;

import model.academic.Course;
import model.academic.Mark;
import model.users.Student;
import services.AuthService;
import services.TranscriptService;
import utils.UserNamePadding;

import java.util.List;
import java.util.Scanner;

public class StudentMenu {
    private final AuthService authService;
    private final TranscriptService transcriptService;
    private final Scanner sc;
    private final UserNamePadding padding = new UserNamePadding();

    public StudentMenu(AuthService authService, TranscriptService transcriptService, Scanner sc) {
        this.authService = authService;
        this.transcriptService = transcriptService;
        this.sc = sc;
    }

    public void show() {
        Student student = (Student) authService.getCurrentUser();
        boolean running = true;

        while (running) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║                STUDENT               ║");
            System.out.println("║  " + padding.padRight("Welcome, " + student.getFullName(), 32) + "║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║        1. View my courses            ║");
            System.out.println("║        2. View my marks              ║");
            System.out.println("║        3. View transcript            ║");
            System.out.println("║        0. Logout                     ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Your choice: ");

            switch (sc.nextLine().trim()) {
                case "1" -> printCourses(student.getRegisteredCourses());
                case "2" -> printMarks(student.getTranscript().getMarks());
                case "3" -> transcriptService.printTranscript(student.getTranscript());
                case "0" -> {
                    authService.logout();
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void printCourses(List<Course> courses) {
        if (courses.isEmpty()) {
            System.out.println("You are not registered for any courses.");
            return;
        }

        for (Course course : courses) {
            System.out.println(course);
        }
    }

    private void printMarks(List<Mark> marks) {
        if (marks.isEmpty()) {
            System.out.println("No marks yet.");
            return;
        }

        for (Mark mark : marks) {
            System.out.println(mark);
        }
    }
}
