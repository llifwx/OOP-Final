package ui.menu;

import comparator.ResearchPaperCitationComparator;
import comparator.ResearchPaperDateComparator;
import comparator.ResearchPaperLengthComparator;
import enums.Language;
import enums.UrgencyLevel;
import exceptions.MarkException;
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
import ui.MenuPrinter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import static i18n.I18n.t;

public class TeacherMenu {
    private final AuthService authService;
    private final TeacherService teacherService;
    private final UserService userService;
    private final MessageService messageService;
    private final TechSupportService techSupportService;
    private final ResearchPaperService paperService;
    private final JournalService journalService;
    private final Scanner sc;

    public TeacherMenu(AuthService authService, TeacherService teacherService, UserService userService, MessageService messageService, TechSupportService techSupportService, ResearchPaperService paperService, ResearchProjectService projectService, JournalService journalService, Scanner sc) {
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
            MenuPrinter.print(t("teacher.title"), t("teacher.welcome", teacher.getFullName()), List.of("1.  " + t("teacher.courses"), "2.  " + t("teacher.students"), "3.  " + t("teacher.put_marks"), "4.  " + t("teacher.complaint"), "5.  " + t("teacher.messages"), "6.  " + t("teacher.publish_paper"), "7.  " + t("teacher.view_papers"), "8.  " + t("teacher.hindex"), "9.  " + t("teacher.journals"), "10. " + t("teacher.notifications"), "11. " + t("teacher.tech_support"), "12. " + t("teacher.switch_lang"), "0.  " + t("menu.logout")));

            switch (sc.nextLine().trim()) {
                case "1" -> printCourses(teacher.getCourses());
                case "2" -> viewStudents(teacher);
                case "3" -> putMark(teacher);
                case "4" -> sendComplaint(teacher);
                case "5" -> messagesMenu();
                case "6" -> publishPaper(teacher);
                case "7" -> viewPapersSorted(teacher);
                case "8" -> System.out.println(t("teacher.hindex_value", teacher.calculateHIndex()));
                case "9" -> journalsMenu(teacher);
                case "10" -> printNotifications(teacher);
                case "11" -> techSupportMenu();
                case "12" -> switchLanguage(teacher);
                case "0" -> {
                    authService.logout();
                    running = false;
                }
                default -> System.out.println(t("app.invalid"));
            }
        }
    }

    private void viewStudents(Teacher teacher) {
        Course course = selectOwnCourse(teacher);
        if (course == null) return;
        List<Student> students = teacherService.viewStudents(course);
        if (students.isEmpty()) System.out.println(t("teacher.no_students"));
        else students.forEach(System.out::println);
    }

    private void putMark(Teacher teacher) {
        Course course = selectOwnCourse(teacher);
        if (course == null) return;

        Student student = readStudentFromCourseByUsername(course);
        if (student == null) return;

        Mark mark = new Mark(student, course);

        try {
            mark.setFirstAttestation(readScore(t("prompt.first_attest")));
            mark.setSecondAttestation(readScore(t("prompt.second_attest")));
            mark.setFinalExam(readScore(t("prompt.final_exam")));
            System.out.println(teacherService.putMark(student, course, mark) ? t("teacher.mark_saved") : t("teacher.mark_rejected"));
        } catch (MarkException e) {
            System.out.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void sendComplaint(Teacher teacher) {
        Course course = selectOwnCourse(teacher);
        if (course == null) return;

        Student student = readStudentFromCourseByUsername(course);
        if (student == null) return;

        UrgencyLevel urgency = readUrgency();
        String text = promptRequired(t("prompt.complaint_text"));

        if (urgency != null && text != null && teacherService.sendComplaint(student, urgency, text) != null) {
            System.out.println(t("teacher.complaint_sent"));
        }
    }

    private void messagesMenu() {
        MenuPrinter.print(t("msg.title"), null, List.of("1. " + t("msg.send"), "2. " + t("msg.inbox"), "3. " + t("msg.sent"), "4. " + t("msg.open"), "5. " + t("msg.mark_read"), "0. " + t("menu.back")));
        switch (sc.nextLine().trim()) {
            case "1" -> sendMessage();
            case "2" -> messageService.printInbox();
            case "3" -> messageService.printSentMessages();
            case "4" -> openMessage();
            case "5" -> messageService.markAllRead();
            case "0" -> {}
            default -> System.out.println(t("app.invalid"));
        }
    }

    private void sendMessage() {
        Employee employee = readEmployeeByUsername();
        if (employee == null) return;

        String text = promptRequired(t("prompt.message"));
        if (text != null) messageService.sendMessage(employee, text);
    }

    private void openMessage() {
        int id = readInt(t("prompt.message_id"));
        if (id >= 0) System.out.println(messageService.openMessage(id));
    }

    private void publishPaper(Teacher teacher) {
        String title = promptRequired(t("prompt.title"));
        String journalName = promptRequired(t("prompt.journal_name"));
        int pages = readInt(t("prompt.pages"));
        String doi = promptRequired(t("prompt.doi"));
        int citations = readInt(t("prompt.citations"));
        if (title == null || journalName == null || doi == null || pages < 0 || citations < 0) return;

        Journal journal = paperService.findJournalByName(journalName);
        if (journal == null) {
            journal = new Journal(journalName);
            journalService.addJournal(journal);
        }
        List<Researcher> authors = new ArrayList<>();
        authors.add(teacher);
        paperService.publishPaper(teacher, new ResearchPaper(title, authors, journal, citations, pages, new Date(), doi), journal);
        System.out.println(t("teacher.paper_published"));
    }

    private void viewPapersSorted(Teacher teacher) {
        Comparator<ResearchPaper> comparator = readPaperComparator();
        if (comparator != null) paperService.printPapers(teacher, comparator);
    }

    private void journalsMenu(Teacher teacher) {
        MenuPrinter.print(t("journal.title"), null, List.of("1. " + t("journal.view"), "2. " + t("journal.subscribe"), "3. " + t("journal.unsubscribe"), "0. " + t("menu.back")));
        switch (sc.nextLine().trim()) {
            case "1" -> journalService.getAllJournals().forEach(journalService::printJournalInfo);
            case "2" -> {
                Journal j = readJournal();
                if (j != null) journalService.subscribe(teacher, j);
            }
            case "3" -> {
                Journal j = readJournal();
                if (j != null) journalService.unsubscribe(teacher, j);
            }
            case "0" -> {}
            default -> System.out.println(t("app.invalid"));
        }
    }

    private void techSupportMenu() {
        MenuPrinter.print(t("tech.title"), null, List.of("1. " + t("tech.submit"), "2. " + t("tech.my_requests"), "0. " + t("menu.back")));
        switch (sc.nextLine().trim()) {
            case "1" -> {
                String d = promptRequired(t("prompt.description"));
                if (d != null) techSupportService.submitRequest(d);
            }
            case "2" -> techSupportService.printMyRequests();
            case "0" -> {}
            default -> System.out.println(t("app.invalid"));
        }
    }

    private Course selectOwnCourse(Teacher teacher) {
        String code = promptRequired(t("prompt.course_code"));
        if (code == null) return null;
        for (Course course : teacher.getCourses()) {
            if (course.getCourseCode().equalsIgnoreCase(code)) return course;
        }
        System.out.println(t("teacher.not_assigned"));
        return null;
    }

    private void printCourses(List<Course> courses) {
        if (courses.isEmpty()) System.out.println(t("teacher.no_courses"));
        else courses.forEach(System.out::println);
    }

    private void printNotifications(User user) {
        List<String> notifications = user.getNotifications();
        if (notifications.isEmpty()) System.out.println(t("student.no_notifications"));
        else notifications.forEach(System.out::println);
    }

    private void switchLanguage(User user) {
        System.out.print(t("prompt.language") + ": ");
        try {
            userService.changeLanguage(user, Language.valueOf(sc.nextLine().trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            System.out.println(t("lang.invalid"));
        }
    }

    private Journal readJournal() {
        String name = promptRequired(t("prompt.journal_name"));
        if (name == null) return null;
        Journal journal = journalService.findJournalByName(name);
        if (journal == null) System.out.println(t("journal.not_found"));
        return journal;
    }

    private Comparator<ResearchPaper> readPaperComparator() {
        MenuPrinter.print(t("sort.title"), null, List.of("1. " + t("sort.date"), "2. " + t("sort.citations"), "3. " + t("sort.pages"), "0. " + t("menu.back")));
        return switch (sc.nextLine().trim()) {
            case "1" -> new ResearchPaperDateComparator();
            case "2" -> new ResearchPaperCitationComparator();
            case "3" -> new ResearchPaperLengthComparator();
            case "0" -> null;
            default -> {
                System.out.println(t("app.invalid"));
                yield null;
            }
        };
    }

    private Employee readEmployeeByUsername() {
        printEmployees(userService.getAllEmployees());

        String username = promptRequired(t("prompt.receiver_id"));
        if (username == null) return null;

        User user = userService.findByUsername(username);

        if (user instanceof Employee employee) {
            return employee;
        }

        System.out.println(t("msg.receiver_not_found"));
        return null;
    }

    private Student readStudentFromCourseByUsername(Course course) {
        if (course == null) return null;

        List<Student> students = course.getEnrolledStudents();

        if (students.isEmpty()) {
            System.out.println(t("teacher.no_students"));
            return null;
        }

        System.out.println("Students in course:");
        for (Student student : students) {
            System.out.println("- " + student.getUsername() + " | " + student.getFullName());
        }

        String username = promptRequired(t("prompt.student_id"));
        if (username == null) return null;

        for (Student student : students) {
            if (student.getUsername().equalsIgnoreCase(username)) {
                return student;
            }
        }

        System.out.println(t("teacher.student_not_found"));
        return null;
    }

    private void printEmployees(List<Employee> employees) {
        if (employees.isEmpty()) {
            System.out.println(t("msg.receiver_not_found"));
            return;
        }

        System.out.println("Available employees:");
        for (Employee employee : employees) {
            System.out.println("- " + employee.getUsername() + " | " + employee.getClass()
                    .getSimpleName() + " | " + employee.getFullName());
        }
    }

    private UrgencyLevel readUrgency() {
        System.out.print(t("prompt.urgency") + ": ");
        try {
            return UrgencyLevel.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println(t("invalid.urgency"));
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
            System.out.println(t("invalid.number"));
            return -1;
        }
    }

    private double readDouble(String label) {
        System.out.print(label + ": ");
        try {
            return Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println(t("invalid.number"));
            return -1;
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
}
