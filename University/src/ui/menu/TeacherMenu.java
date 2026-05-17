package ui.menu;

import comparator.ResearchPaperCitationComparator;
import comparator.ResearchPaperDateComparator;
import comparator.ResearchPaperLengthComparator;
import enums.Language;
import enums.UrgencyLevel;
import interfaces.Researcher;
import model.academic.Course;
import model.academic.Mark;
import model.research.ResearchPaper;
import model.social.Journal;
import model.users.Employee;
import model.users.Student;
import model.users.Teacher;
import model.users.User;
import services.AuthService;
import services.JournalService;
import services.MessageService;
import services.ResearchPaperService;
import services.ResearchProjectService;
import services.TeacherService;
import services.TechSupportService;
import services.UserService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class TeacherMenu {
    private final AuthService authService;
    private final TeacherService teacherService;
    private final UserService userService;
    private final MessageService messageService;
    private final TechSupportService techSupportService;
    private final ResearchPaperService paperService;
    private final JournalService journalService;
    private final Scanner sc;

    public TeacherMenu(AuthService authService, TeacherService teacherService, UserService userService,
                       MessageService messageService, TechSupportService techSupportService,
                       ResearchPaperService paperService, ResearchProjectService projectService,
                       JournalService journalService, Scanner sc) {
        this.authService = authService;
        this.teacherService = teacherService;
        this.userService = userService;
        this.messageService = messageService;
        this.techSupportService = techSupportService;
        this.paperService = paperService;
        this.journalService = journalService;
        this.sc = sc;
    }

    public void show() {
        boolean running = true;
        while (running) {
            Teacher teacher = (Teacher) authService.getCurrentUser();
            MenuPrinter.print("TEACHER", "Welcome, " + teacher.getFullName(), List.of(
                    "1. View assigned courses",
                    "2. View students in course",
                    "3. Put marks",
                    "4. Send complaint",
                    "5. Messages",
                    "6. Publish research paper",
                    "7. View papers sorted",
                    "8. View H-index",
                    "9. Journals",
                    "10. View notifications",
                    "11. Tech support request",
                    "12. Switch language",
                    "0. Logout"
            ));

            switch (sc.nextLine().trim()) {
                case "1" -> printCourses(teacher.getCourses());
                case "2" -> viewStudents(teacher);
                case "3" -> putMark(teacher);
                case "4" -> sendComplaint(teacher);
                case "5" -> messagesMenu();
                case "6" -> publishPaper(teacher);
                case "7" -> viewPapersSorted(teacher);
                case "8" -> System.out.println("H-index: " + teacher.calculateHIndex());
                case "9" -> journalsMenu(teacher);
                case "10" -> printNotifications(teacher);
                case "11" -> techSupportMenu();
                case "12" -> switchLanguage(teacher);
                case "0" -> {
                    authService.logout();
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void viewStudents(Teacher teacher) {
        Course course = selectOwnCourse(teacher);
        if (course == null) return;
        List<Student> students = teacherService.viewStudents(course);
        if (students.isEmpty()) System.out.println("No students enrolled.");
        else students.forEach(System.out::println);
    }

    private void putMark(Teacher teacher) {
        Course course = selectOwnCourse(teacher);
        if (course == null) return;
        int studentId = readInt("Student ID");
        User user = userService.findById(studentId);
        if (!(user instanceof Student student)) {
            System.out.println("Student not found.");
            return;
        }
        Mark mark = new Mark(student, course);
        try {
            mark.setFirstAttestation(readScore("First attestation"));
            mark.setSecondAttestation(readScore("Second attestation"));
            mark.setFinalExam(readScore("Final exam"));
            System.out.println(teacherService.putMark(student, course, mark) ? "Mark saved." : "Mark rejected.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void sendComplaint(Teacher teacher) {
        int studentId = readInt("Student ID");
        User user = userService.findById(studentId);
        if (!(user instanceof Student student)) {
            System.out.println("Student not found.");
            return;
        }
        UrgencyLevel urgency = readUrgency();
        String text = promptRequired("Complaint text");
        if (urgency != null && text != null && teacherService.sendComplaint(student, urgency, text) != null) {
            System.out.println("Complaint sent.");
        }
    }

    private void messagesMenu() {
        MenuPrinter.print("MESSAGES", null, List.of(
                "1. Send message",
                "2. View inbox",
                "3. View sent messages",
                "4. Open message",
                "5. Mark all read",
                "0. Back"
        ));
        switch (sc.nextLine().trim()) {
            case "1" -> sendMessage();
            case "2" -> messageService.printInbox();
            case "3" -> messageService.printSentMessages();
            case "4" -> openMessage();
            case "5" -> messageService.markAllRead();
            case "0" -> { }
            default -> System.out.println("Invalid choice.");
        }
    }

    private void sendMessage() {
        int id = readInt("Receiver employee ID");
        User user = userService.findById(id);
        if (!(user instanceof Employee employee)) {
            System.out.println("Employee not found.");
            return;
        }
        String text = promptRequired("Message");
        if (text != null) messageService.sendMessage(employee, text);
    }

    private void openMessage() {
        int id = readInt("Message ID");
        if (id >= 0) System.out.println(messageService.openMessage(id));
    }

    private void publishPaper(Teacher teacher) {
        String title = promptRequired("Title");
        String journalName = promptRequired("Journal name");
        int pages = readInt("Pages");
        String doi = promptRequired("DOI");
        int citations = readInt("Citations");
        if (title == null || journalName == null || doi == null || pages < 0 || citations < 0) return;
        Journal journal = paperService.findJournalByName(journalName);
        if (journal == null) {
            journal = new Journal(journalName);
            journalService.addJournal(journal);
        }
        List<Researcher> authors = new ArrayList<>();
        authors.add(teacher);
        paperService.publishPaper(teacher, new ResearchPaper(title, authors, journal, citations, pages, new Date(), doi), journal);
        System.out.println("Research paper published.");
    }

    private void viewPapersSorted(Teacher teacher) {
        Comparator<ResearchPaper> comparator = readPaperComparator();
        if (comparator != null) paperService.printPapers(teacher, comparator);
    }

    private void journalsMenu(Teacher teacher) {
        MenuPrinter.print("JOURNALS", null, List.of(
                "1. View journals",
                "2. Subscribe",
                "3. Unsubscribe",
                "0. Back"
        ));
        switch (sc.nextLine().trim()) {
            case "1" -> journalService.getAllJournals().forEach(journalService::printJournalInfo);
            case "2" -> {
                Journal journal = readJournal();
                if (journal != null) journalService.subscribe(teacher, journal);
            }
            case "3" -> {
                Journal journal = readJournal();
                if (journal != null) journalService.unsubscribe(teacher, journal);
            }
            case "0" -> { }
            default -> System.out.println("Invalid choice.");
        }
    }

    private void techSupportMenu() {
        MenuPrinter.print("TECH SUPPORT", null, List.of(
                "1. Submit request",
                "2. View my requests",
                "0. Back"
        ));
        switch (sc.nextLine().trim()) {
            case "1" -> {
                String description = promptRequired("Description");
                if (description != null) techSupportService.submitRequest(description);
            }
            case "2" -> techSupportService.printMyRequests();
            case "0" -> { }
            default -> System.out.println("Invalid choice.");
        }
    }

    private Course selectOwnCourse(Teacher teacher) {
        String code = promptRequired("Course code");
        if (code == null) return null;
        for (Course course : teacher.getCourses()) {
            if (course.getCourseCode().equalsIgnoreCase(code)) return course;
        }
        System.out.println("You are not assigned to this course.");
        return null;
    }

    private void printCourses(List<Course> courses) {
        if (courses.isEmpty()) {
            System.out.println("No assigned courses.");
            return;
        }
        courses.forEach(System.out::println);
    }

    private void printNotifications(User user) {
        List<String> notifications = user.getNotifications();
        if (notifications.isEmpty()) System.out.println("No notifications.");
        else notifications.forEach(System.out::println);
    }

    private void switchLanguage(User user) {
        System.out.print("Language (KZ, EN, RU): ");
        try {
            userService.changeLanguage(user, Language.valueOf(sc.nextLine().trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid language.");
        }
    }

    private Journal readJournal() {
        String name = promptRequired("Journal name");
        if (name == null) return null;
        Journal journal = journalService.findJournalByName(name);
        if (journal == null) System.out.println("Journal not found.");
        return journal;
    }

    private Comparator<ResearchPaper> readPaperComparator() {
        MenuPrinter.print("SORT PAPERS", null, List.of(
                "1. Date",
                "2. Citations",
                "3. Pages",
                "0. Back"
        ));
        return switch (sc.nextLine().trim()) {
            case "1" -> new ResearchPaperDateComparator();
            case "2" -> new ResearchPaperCitationComparator();
            case "3" -> new ResearchPaperLengthComparator();
            case "0" -> null;
            default -> {
                System.out.println("Invalid choice.");
                yield null;
            }
        };
    }

    private UrgencyLevel readUrgency() {
        System.out.print("Urgency (LOW, MEDIUM, HIGH): ");
        try {
            return UrgencyLevel.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid urgency.");
            return null;
        }
    }

    private double readScore(String label) {
        double score = readDouble(label);
        if (score < 0 || score > 100) throw new IllegalArgumentException("Score must be between 0 and 100");
        return score;
    }

    private int readInt(String label) {
        System.out.print(label + ": ");
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return -1;
        }
    }

    private double readDouble(String label) {
        System.out.print(label + ": ");
        try {
            return Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return -1;
        }
    }

    private String promptRequired(String label) {
        System.out.print(label + ": ");
        String value = sc.nextLine().trim();
        if (value.isEmpty()) {
            System.out.println(label + " cannot be empty.");
            return null;
        }
        return value;
    }
}
