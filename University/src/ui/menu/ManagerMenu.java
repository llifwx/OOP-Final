package ui.menu;

import enums.CourseType;
import enums.Format;
import enums.Language;
import enums.LessonType;
import enums.NewsTopic;
import exceptions.InvalidSupervisorEx;
import interfaces.Researcher;
import model.academic.Course;
import model.academic.Lesson;
import model.academic.Report;
import model.research.ResearchProject;
import model.social.News;
import model.users.Employee;
import model.users.GraduateStudent;
import model.users.Manager;
import model.users.Student;
import model.users.Teacher;
import model.users.User;
import services.AuthService;
import services.ManagerService;
import services.MessageService;
import services.NewsService;
import services.ReportService;
import services.ResearchProjectService;
import services.TechSupportService;
import services.UserService;

import java.util.List;
import java.util.Scanner;

public class ManagerMenu {
    private final ManagerService managerService;
    private final UserService userService;
    private final AuthService authService;
    private final MessageService messageService;
    private final TechSupportService techSupportService;
    private final ResearchProjectService projectService;
    private final NewsService newsService;
    private final ReportService reportService;
    private final Scanner sc;

    public ManagerMenu(ManagerService managerService, UserService userService, AuthService authService,
                       MessageService messageService, TechSupportService techSupportService,
                       ResearchProjectService projectService, NewsService newsService,
                       ReportService reportService, Scanner sc) {
        this.managerService = managerService;
        this.userService = userService;
        this.authService = authService;
        this.messageService = messageService;
        this.techSupportService = techSupportService;
        this.projectService = projectService;
        this.newsService = newsService;
        this.reportService = reportService;
        this.sc = sc;
    }

    public void show() {
        boolean running = true;
        while (running) {
            Manager manager = (Manager) authService.getCurrentUser();
            MenuPrinter.print("MANAGER", "Welcome, " + manager.getFullName(), List.of(
                    "1. Add course",
                    "2. Assign course to teacher",
                    "3. Approve registration",
                    "4. Unregister student",
                    "5. Add lesson to course",
                    "6. Reports",
                    "7. Manage news",
                    "8. Students sorted by GPA",
                    "9. Teachers alphabetically",
                    "10. Assign supervisor",
                    "11. Add research project",
                    "12. Employee requests",
                    "13. Messages",
                    "14. Switch language",
                    "0. Logout"
            ));

            switch (sc.nextLine().trim()) {
                case "1" -> addCourse();
                case "2" -> assignCourse();
                case "3" -> approveRegistration();
                case "4" -> unregisterStudent();
                case "5" -> addLesson();
                case "6" -> reportsMenu();
                case "7" -> newsMenu();
                case "8" -> printStudents(managerService.viewStudentsSortedByGpa(userService.getAllStudents()));
                case "9" -> printTeachers(managerService.viewTeachersAlphabetically(userService.getAllTeachers()));
                case "10" -> assignSupervisor();
                case "11" -> addResearchProject();
                case "12" -> techSupportService.printAllRequests();
                case "13" -> messagesMenu();
                case "14" -> switchLanguage(manager);
                case "0" -> {
                    authService.logout();
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void addCourse() {
        String code = promptRequired("Course code");
        String name = promptRequired("Course name");
        int credits = readInt("Credits");
        CourseType type = readCourseType();
        Language language = readLanguage();
        if (code == null || name == null || credits < 0 || type == null || language == null) return;
        managerService.addCourseForRegistration(new Course(code, name, credits, type, language));
    }

    private void assignCourse() {
        String code = promptRequired("Course code");
        int teacherId = readInt("Teacher ID");
        LessonType type = readLessonType();
        if (code != null && teacherId >= 0 && type != null) managerService.assignCourseToTeacher(code, teacherId, type);
    }

    private void approveRegistration() {
        Student student = readStudent();
        Course course = readCourse();
        if (student != null && course != null) managerService.approveRegistration(student, course);
    }

    private void unregisterStudent() {
        Student student = readStudent();
        Course course = readCourse();
        if (student != null && course != null) managerService.unregisterStudentFromCourse(student, course);
    }

    private void addLesson() {
        Course course = readCourse();
        if (course == null) return;
        int teacherId = readInt("Teacher ID");
        User user = userService.findById(teacherId);
        if (!(user instanceof Teacher teacher)) {
            System.out.println("Teacher not found.");
            return;
        }
        LessonType type = readLessonType();
        String day = promptRequired("Day of week");
        String time = promptRequired("Time slot");
        String room = promptRequired("Room");
        if (type == null || day == null || time == null || room == null) return;
        managerService.addLessonToCourse(course, new Lesson(type, day, time, room, course, teacher));
    }

    private void reportsMenu() {
        MenuPrinter.print("REPORTS", null, List.of(
                "1. Create academic report",
                "2. Create research report",
                "3. View all reports",
                "4. Print report",
                "5. Delete report",
                "0. Back"
        ));
        switch (sc.nextLine().trim()) {
            case "1" -> managerService.createAcademicReport(userService.getAllStudents());
            case "2" -> reportService.createResearchReport(userService.getAllTeachers());
            case "3" -> printReports(reportService.getAllReports());
            case "4" -> printReport();
            case "5" -> {
                String title = promptRequired("Report title");
                if (title != null) reportService.deleteReport(title);
            }
            case "0" -> { }
            default -> System.out.println("Invalid choice.");
        }
    }

    private void newsMenu() {
        MenuPrinter.print("NEWS", null, List.of(
                "1. Add news",
                "2. Remove news",
                "3. Pin news",
                "4. View news",
                "0. Back"
        ));
        switch (sc.nextLine().trim()) {
            case "1" -> addNews();
            case "2" -> {
                String title = promptRequired("Title");
                if (title != null) managerService.removeNews(title);
            }
            case "3" -> {
                String title = promptRequired("Title");
                if (title != null) managerService.pinNews(title);
            }
            case "4" -> printNews(newsService.getAllNews());
            case "0" -> { }
            default -> System.out.println("Invalid choice.");
        }
    }

    private void addNews() {
        String title = promptRequired("Title");
        String content = promptRequired("Content");
        NewsTopic topic = readNewsTopic();
        if (title != null && content != null && topic != null) managerService.addNews(new News(title, content, topic));
    }

    private void assignSupervisor() {
        User studentUser = userService.findById(readInt("Graduate student ID"));
        if (!(studentUser instanceof GraduateStudent student)) {
            System.out.println("Graduate student not found.");
            return;
        }
        User supervisorUser = userService.findById(readInt("Supervisor user ID"));
        if (!(supervisorUser instanceof Researcher supervisor)) {
            System.out.println("Supervisor must be a researcher.");
            return;
        }
        try {
            projectService.assignSupervisor(student, supervisor);
            System.out.println("Supervisor assigned.");
        } catch (InvalidSupervisorEx e) {
            System.out.println(e.getMessage());
        }
    }

    private void addResearchProject() {
        String topic = promptRequired("Project topic");
        if (topic == null) return;
        if (projectService.findProjectByTopic(topic) != null) {
            System.out.println("Project already exists.");
            return;
        }
        projectService.addProject(new ResearchProject(topic));
    }

    private void messagesMenu() {
        MenuPrinter.print("MESSAGES", null, List.of(
                "1. Send message",
                "2. View inbox",
                "3. View sent messages",
                "4. Open message",
                "0. Back"
        ));
        switch (sc.nextLine().trim()) {
            case "1" -> sendMessage();
            case "2" -> messageService.printInbox();
            case "3" -> messageService.printSentMessages();
            case "4" -> {
                int id = readInt("Message ID");
                if (id >= 0) System.out.println(messageService.openMessage(id));
            }
            case "0" -> { }
            default -> System.out.println("Invalid choice.");
        }
    }

    private void sendMessage() {
        User user = userService.findById(readInt("Receiver employee ID"));
        if (!(user instanceof Employee employee)) {
            System.out.println("Employee not found.");
            return;
        }
        String text = promptRequired("Message");
        if (text != null) messageService.sendMessage(employee, text);
    }

    private Student readStudent() {
        User user = userService.findById(readInt("Student ID"));
        if (user instanceof Student student) return student;
        System.out.println("Student not found.");
        return null;
    }

    private Course readCourse() {
        String code = promptRequired("Course code");
        if (code == null) return null;
        for (Course course : userService.getAllCourses()) {
            if (course.getCourseCode().equalsIgnoreCase(code)) return course;
        }
        System.out.println("Course not found.");
        return null;
    }

    private void printReport() {
        String title = promptRequired("Report title");
        if (title == null) return;
        Report report = reportService.getReportByTitle(title);
        if (report != null) reportService.printReport(report, Format.PLAIN_TEXT);
    }

    private void printStudents(List<Student> students) {
        if (students.isEmpty()) System.out.println("No students found.");
        else students.forEach(System.out::println);
    }

    private void printTeachers(List<Teacher> teachers) {
        if (teachers.isEmpty()) System.out.println("No teachers found.");
        else teachers.forEach(System.out::println);
    }

    private void printReports(List<Report> reports) {
        if (reports.isEmpty()) System.out.println("No reports found.");
        else reports.forEach(report -> System.out.println(report.getTitle()));
    }

    private void printNews(List<News> news) {
        if (news.isEmpty()) System.out.println("No news found.");
        else news.forEach(item -> System.out.println(item + "\n" + item.getContent()));
    }

    private void switchLanguage(User user) {
        Language language = readLanguage();
        if (language != null) userService.changeLanguage(user, language);
    }

    private CourseType readCourseType() {
        System.out.print("Course type (MAJOR, MINOR, FREE_ELECTIVE): ");
        try {
            return CourseType.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid course type.");
            return null;
        }
    }

    private LessonType readLessonType() {
        System.out.print("Lesson type (LECTURE, PRACTICE): ");
        try {
            return LessonType.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid lesson type.");
            return null;
        }
    }

    private NewsTopic readNewsTopic() {
        System.out.print("Topic (RESEARCH, ACADEMIC, EVENT, ANNOUNCEMENT): ");
        try {
            return NewsTopic.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid topic.");
            return null;
        }
    }

    private Language readLanguage() {
        System.out.print("Language (KZ, EN, RU): ");
        try {
            return Language.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid language.");
            return null;
        }
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
