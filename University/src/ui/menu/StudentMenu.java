package ui.menu;

import enums.Language;
import model.academic.Course;
import model.academic.Mark;
import model.academic.StudentOrganization;
import model.social.Journal;
import model.users.Student;
import model.users.Teacher;
import services.AuthService;
import services.JournalService;
import services.StudentService;
import services.TranscriptService;
import services.UserService;

import java.util.List;
import java.util.Scanner;

public class StudentMenu {
    protected final AuthService authService;
    protected final StudentService studentService;
    protected final UserService userService;
    protected final TranscriptService transcriptService;
    protected final JournalService journalService;
    protected final Scanner sc;

    public StudentMenu(AuthService authService, StudentService studentService, UserService userService,
                       TranscriptService transcriptService, JournalService journalService, Scanner sc) {
        this.authService = authService;
        this.studentService = studentService;
        this.userService = userService;
        this.transcriptService = transcriptService;
        this.journalService = journalService;
        this.sc = sc;
    }

    public void show() {
        boolean running = true;
        while (running) {
            Student student = (Student) authService.getCurrentUser();
            MenuPrinter.print("STUDENT", "Welcome, " + student.getFullName(), List.of(
                    "1. View available courses",
                    "2. Register for course",
                    "3. View registered courses",
                    "4. View teacher info",
                    "5. View marks",
                    "6. View transcript",
                    "7. Rate teacher",
                    "8. Student organizations",
                    "9. Journals",
                    "10. View notifications",
                    "11. Switch language",
                    "12. Tech support availability",
                    "0. Logout"
            ));

            switch (sc.nextLine().trim()) {
                case "1" -> printCourses(studentService.getAvailableCourses(), "Available courses");
                case "2" -> registerForCourse();
                case "3" -> printCourses(student.getRegisteredCourses(), "Registered courses");
                case "4" -> viewTeacherInfo();
                case "5" -> printMarks(student.getTranscript().getMarks());
                case "6" -> transcriptService.printTranscript(student.getTranscript());
                case "7" -> rateTeacher();
                case "8" -> organizationsMenu(student);
                case "9" -> journalsMenu(student);
                case "10" -> printNotifications(student);
                case "11" -> switchLanguage(student);
                case "12" -> System.out.println("Tech support requests are currently available only for employees in the service layer.");
                case "0" -> {
                    authService.logout();
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    protected void registerForCourse() {
        String code = promptRequired("Course code");
        if (code != null && studentService.registerForCourse(code)) {
            System.out.println("Registered for course.");
        }
    }

    protected void viewTeacherInfo() {
        String code = promptRequired("Course code");
        if (code == null) return;
        Teacher teacher = studentService.getTeacherInfo(code);
        System.out.println(teacher == null ? "Teacher info is not available for this course." : teacher);
    }

    protected void rateTeacher() {
        int teacherId = readInt("Teacher ID");
        if (teacherId < 0) return;
        double rating = readDouble("Rating (0-5)");
        if (rating < 0 || rating > 5) {
            System.out.println("Rating must be between 0 and 5.");
            return;
        }
        if (studentService.rateTeacher(teacherId, rating)) {
            System.out.println("Teacher rated.");
        }
    }

    protected void organizationsMenu(Student student) {
        MenuPrinter.print("ORGANIZATIONS", null, List.of(
                "1. View all organizations",
                "2. View my organizations",
                "3. Join organization",
                "4. Leave organization",
                "0. Back"
        ));
        switch (sc.nextLine().trim()) {
            case "1" -> printOrganizations(studentService.getAllOrganizations());
            case "2" -> printOrganizations(student.getOrganizations());
            case "3" -> {
                String name = promptRequired("Organization name");
                if (name != null) studentService.joinOrganization(name);
            }
            case "4" -> {
                String name = promptRequired("Organization name");
                if (name != null) studentService.leaveOrganization(name);
            }
            case "0" -> { }
            default -> System.out.println("Invalid choice.");
        }
    }

    protected void journalsMenu(Student student) {
        MenuPrinter.print("JOURNALS", null, List.of(
                "1. View journals",
                "2. Subscribe",
                "3. Unsubscribe",
                "0. Back"
        ));
        switch (sc.nextLine().trim()) {
            case "1" -> printJournals();
            case "2" -> subscribeJournal(student);
            case "3" -> unsubscribeJournal(student);
            case "0" -> { }
            default -> System.out.println("Invalid choice.");
        }
    }

    protected void printCourses(List<Course> courses, String title) {
        if (courses.isEmpty()) {
            System.out.println("No courses found.");
            return;
        }
        System.out.println("--- " + title + " ---");
        courses.forEach(System.out::println);
    }

    protected void printMarks(List<Mark> marks) {
        if (marks.isEmpty()) {
            System.out.println("No marks yet.");
            return;
        }
        marks.forEach(System.out::println);
    }

    protected void printNotifications(Student student) {
        List<String> notifications = student.getNotifications();
        if (notifications.isEmpty()) {
            System.out.println("No notifications.");
            return;
        }
        notifications.forEach(System.out::println);
    }

    protected void switchLanguage(Student student) {
        Language language = readLanguage();
        if (language != null) userService.changeLanguage(student, language);
    }

    protected void printJournals() {
        List<Journal> journals = journalService.getAllJournals();
        if (journals.isEmpty()) {
            System.out.println("No journals found.");
            return;
        }
        journals.forEach(journalService::printJournalInfo);
    }

    protected void subscribeJournal(Student student) {
        Journal journal = readJournal();
        if (journal != null) journalService.subscribe(student, journal);
    }

    protected void unsubscribeJournal(Student student) {
        Journal journal = readJournal();
        if (journal != null) journalService.unsubscribe(student, journal);
    }

    protected Journal readJournal() {
        String name = promptRequired("Journal name");
        if (name == null) return null;
        Journal journal = journalService.findJournalByName(name);
        if (journal == null) System.out.println("Journal not found.");
        return journal;
    }

    protected void printOrganizations(List<StudentOrganization> organizations) {
        if (organizations.isEmpty()) {
            System.out.println("No organizations found.");
            return;
        }
        organizations.forEach(System.out::println);
    }

    protected Language readLanguage() {
        System.out.print("Language (KZ, EN, RU): ");
        try {
            return Language.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid language.");
            return null;
        }
    }

    protected int readInt(String label) {
        System.out.print(label + ": ");
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return -1;
        }
    }

    protected double readDouble(String label) {
        System.out.print(label + ": ");
        try {
            return Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return -1;
        }
    }

    protected String promptRequired(String label) {
        System.out.print(label + ": ");
        String value = sc.nextLine().trim();
        if (value.isEmpty()) {
            System.out.println(label + " cannot be empty.");
            return null;
        }
        return value;
    }
}
