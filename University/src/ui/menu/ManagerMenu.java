package ui.menu;

import model.users.Student;
import model.users.Teacher;
import services.AuthService;
import services.ManagerService;
import services.UserService;
import utils.UserNamePadding;

import java.util.List;
import java.util.Scanner;

public class ManagerMenu {
    private final ManagerService managerService;
    private final UserService userService;
    private final AuthService authService;
    private final Scanner sc;
    private final UserNamePadding padding = new UserNamePadding();

    public ManagerMenu(ManagerService managerService, UserService userService, AuthService authService, Scanner sc) {
        this.managerService = managerService;
        this.userService = userService;
        this.authService = authService;
        this.sc = sc;
    }

    public void show() {
        boolean running = true;

        while (running) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║               MANAGER                ║");
            System.out.println("║                 Welcome              ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║     1. View students sorted by GPA   ║");
            System.out.println("║     2. View teachers alphabetically  ║");
            System.out.println("║     3. Create academic report        ║");
            System.out.println("║     0. Logout                        ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Your choice: ");

            switch (sc.nextLine().trim()) {
                case "1" -> printStudents(managerService.viewStudentsSortedByGpa(userService.getAllStudents()));
                case "2" -> printTeachers(managerService.viewTeachersAlphabetically(userService.getAllTeachers()));
                case "3" -> managerService.createAcademicReport(userService.getAllStudents());
                case "0" -> {
                    authService.logout();
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void printStudents(List<Student> students) {
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }

        for (Student student : students) {
            System.out.println(student);
        }
    }

    private void printTeachers(List<Teacher> teachers) {
        if (teachers.isEmpty()) {
            System.out.println("No teachers found.");
            return;
        }

        for (Teacher teacher : teachers) {
            System.out.println(teacher.getFullName() + " | " + teacher.getTeacherType());
        }
    }
}
