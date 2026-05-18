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
import ui.MenuPrinter;

import java.util.List;
import java.util.Scanner;

import static i18n.I18n.t;

public class StudentMenu {
    protected final AuthService authService;
    protected final StudentService studentService;
    protected final UserService userService;
    protected final TranscriptService transcriptService;
    protected final JournalService journalService;
    protected final Scanner sc;

    public StudentMenu(AuthService authService, StudentService studentService, UserService userService, TranscriptService transcriptService, JournalService journalService, Scanner sc) {
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
            MenuPrinter.print(t("student.title"), t("student.welcome", student.getFullName()), List.of("1.  " + t("student.courses_available"), "2.  " + t("student.courses_register"), "3.  " + t("student.courses_registered"), "4.  " + t("student.teacher_info"), "5.  " + t("student.marks"), "6.  " + t("student.transcript"), "7.  " + t("student.rate_teacher"), "8.  " + t("student.organizations"), "9.  " + t("student.journals"), "10. " + t("student.notifications"), "11. " + t("student.switch_lang"), "12. " + t("student.tech_support"), "0.  " + t("menu.logout")));

            switch (sc.nextLine().trim()) {
                case "1" -> printCourses(studentService.getAvailableCourses(), t("student.courses.available"));
                case "2" -> registerForCourse();
                case "3" -> printCourses(student.getRegisteredCourses(), t("student.courses.mine"));
                case "4" -> viewTeacherInfo();
                case "5" -> printMarks(student.getTranscript().getMarks());
                case "6" -> transcriptService.printTranscript(student.getTranscript());
                case "7" -> rateTeacher();
                case "8" -> organizationsMenu(student);
                case "9" -> journalsMenu(student);
                case "10" -> printNotifications(student);
                case "11" -> switchLanguage(student);
                case "12" -> System.out.println(t("student.tech_support_msg"));
                case "0" -> {
                    authService.logout();
                    running = false;
                }
                default -> System.out.println(t("app.invalid"));
            }
        }
    }

    protected void registerForCourse() {
        String code = promptRequired(t("prompt.course_code"));
        if (code != null && studentService.registerForCourse(code)) {
            System.out.println(t("student.registered"));
        }
    }

    protected void viewTeacherInfo() {
        String code = promptRequired(t("prompt.course_code"));
        if (code == null) return;
        Teacher teacher = studentService.getTeacherInfo(code);
        System.out.println(teacher == null ? t("student.teacher_unavailable") : teacher);
    }

    protected void rateTeacher() {
        int teacherId = readInt(t("prompt.teacher_id"));
        if (teacherId < 0) return;
        double rating = readDouble(t("prompt.rating"));
        if (rating < 0 || rating > 5) {
            System.out.println(t("student.rating_invalid"));
            return;
        }
        if (studentService.rateTeacher(teacherId, rating)) {
            System.out.println(t("student.teacher_rated"));
        }
    }

    protected void organizationsMenu(Student student) {
        MenuPrinter.print(t("org.title"), null, List.of("1. " + t("org.view_all"), "2. " + t("org.view_mine"), "3. " + t("org.join"), "4. " + t("org.leave"), "0. " + t("menu.back")));
        switch (sc.nextLine().trim()) {
            case "1" -> printOrganizations(studentService.getAllOrganizations());
            case "2" -> printOrganizations(student.getOrganizations());
            case "3" -> {
                String name = promptRequired(t("prompt.org_name"));
                if (name != null) studentService.joinOrganization(name);
            }
            case "4" -> {
                String name = promptRequired(t("prompt.org_name"));
                if (name != null) studentService.leaveOrganization(name);
            }
            case "0" -> {}
            default -> System.out.println(t("app.invalid"));
        }
    }

    protected void journalsMenu(Student student) {
        MenuPrinter.print(t("journal.title"), null, List.of("1. " + t("journal.view"), "2. " + t("journal.subscribe"), "3. " + t("journal.unsubscribe"), "0. " + t("menu.back")));
        switch (sc.nextLine().trim()) {
            case "1" -> printJournals();
            case "2" -> subscribeJournal(student);
            case "3" -> unsubscribeJournal(student);
            case "0" -> {}
            default -> System.out.println(t("app.invalid"));
        }
    }

    protected void printCourses(List<Course> courses, String title) {
        if (courses.isEmpty()) {
            System.out.println(t("student.no_courses"));
            return;
        }
        System.out.println("--- " + title + " ---");
        courses.forEach(System.out::println);
    }

    protected void printMarks(List<Mark> marks) {
        if (marks.isEmpty()) {
            System.out.println(t("student.no_marks"));
            return;
        }
        marks.forEach(System.out::println);
    }

    protected void printNotifications(Student student) {
        List<String> notifications = student.getNotifications();
        if (notifications.isEmpty()) {
            System.out.println(t("student.no_notifications"));
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
            System.out.println(t("journal.none"));
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
        String name = promptRequired(t("prompt.journal_name"));
        if (name == null) return null;
        Journal journal = journalService.findJournalByName(name);
        if (journal == null) System.out.println(t("journal.not_found"));
        return journal;
    }

    protected void printOrganizations(List<StudentOrganization> organizations) {
        if (organizations.isEmpty()) {
            System.out.println(t("org.none"));
            return;
        }
        organizations.forEach(System.out::println);
    }

    protected Language readLanguage() {
        System.out.print(t("prompt.language") + ": ");
        try {
            return Language.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println(t("lang.invalid"));
            return null;
        }
    }

    protected int readInt(String label) {
        System.out.print(label + ": ");
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println(t("invalid.number"));
            return -1;
        }
    }

    protected double readDouble(String label) {
        System.out.print(label + ": ");
        try {
            return Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println(t("invalid.number"));
            return -1;
        }
    }

    protected String promptRequired(String label) {
        System.out.print(label + ": ");
        String value = sc.nextLine().trim();
        if (value.isEmpty()) {
            System.out.println(t("prompt.cannot_empty", label));
            return null;
        }
        return value;
    }
}