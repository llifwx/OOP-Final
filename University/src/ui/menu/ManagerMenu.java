package ui.menu;

import enums.CourseType;
import enums.Format;
import enums.Language;
import enums.LessonType;
import enums.NewsTopic;
import exceptions.InvalidSupervisorEx;
import interfaces.Researcher;
import model.academic.Course;
import model.academic.CourseRegistration;
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
import ui.MenuPrinter;

import java.util.List;
import java.util.Scanner;

import static i18n.I18n.t;

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

    public ManagerMenu(ManagerService managerService, UserService userService, AuthService authService, MessageService messageService, TechSupportService techSupportService, ResearchProjectService projectService, NewsService newsService, ReportService reportService, Scanner sc) {
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
            MenuPrinter.print(t("manager.title"), t("manager.welcome", manager.getFullName()), List.of("1.  " + t("manager.add_course"), "2.  " + t("manager.assign_course"), "3.  " + t("manager.approve_reg"), "4.  " + t("manager.unregister"), "5.  " + t("manager.add_lesson"), "6.  " + t("manager.reports"), "7.  " + t("manager.news"), "8.  " + t("manager.students_gpa"), "9.  " + t("manager.teachers_alpha"), "10. " + t("manager.assign_supervisor"), "11. " + t("manager.add_project"), "12. " + t("manager.emp_requests"), "13. " + t("manager.messages"), "14. " + t("manager.switch_lang"), "0.  " + t("menu.logout")));

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
                default -> System.out.println(t("app.invalid"));
            }
        }
    }

    private void addCourse() {
        String code = promptRequired(t("prompt.course_code"));
        String name = promptRequired(t("prompt.course_name"));
        int credits = readInt(t("prompt.credits"));
        CourseType type = readCourseType();
        Language lang = readLanguage();
        if (code == null || name == null || credits < 0 || type == null || lang == null) return;
        managerService.addCourseForRegistration(new Course(code, name, credits, type, lang));
    }

    private void assignCourse() {
        String code = promptRequired(t("prompt.course_code"));
        int teacherId = readInt(t("prompt.teacher_id"));
        LessonType type = readLessonType();
        if (code != null && teacherId >= 0 && type != null) managerService.assignCourseToTeacher(code, teacherId, type);
    }

    private void approveRegistration() {
        List<CourseRegistration> pending = managerService.getPendingRegistrations();
        if (pending.isEmpty()) {
            System.out.println("[Manager Menu] No pending registration requests.");
            return;
        }
        pending.forEach(System.out::println);
        int id = readInt("Registration request ID");
        if (id < 0) return;
        String action = promptRequired("Action (approve/reject)");
        if (action == null) return;
        if (action.equalsIgnoreCase("approve")) {
            managerService.approveRegistration(id);
        } else if (action.equalsIgnoreCase("reject")) {
            String reason = promptRequired("Rejection reason");
            if (reason != null) managerService.rejectRegistration(id, reason);
        } else {
            System.out.println(t("app.invalid"));
        }
    }

    private void unregisterStudent() {
        Student student = readStudent();
        Course course = readCourse();
        if (student != null && course != null) managerService.unregisterStudentFromCourse(student, course);
    }

    private void addLesson() {
        Course course = readCourse();
        if (course == null) return;
        int teacherId = readInt(t("prompt.teacher_id"));
        User user = userService.findById(teacherId);
        if (!(user instanceof Teacher teacher)) {
            System.out.println(t("manager.teacher_not_found"));
            return;
        }
        LessonType type = readLessonType();
        String day = promptRequired(t("prompt.day_of_week"));
        String time = promptRequired(t("prompt.time_slot"));
        String room = promptRequired(t("prompt.room"));
        if (type == null || day == null || time == null || room == null) return;
        managerService.addLessonToCourse(course, new Lesson(type, day, time, room, course, teacher));
    }

    private void reportsMenu() {
        MenuPrinter.print(t("reports.title"), null, List.of("1. " + t("reports.create_academic"), "2. " + t("reports.create_research"), "3. " + t("reports.view_all"), "4. " + t("reports.print"), "5. " + t("reports.delete"), "0. " + t("menu.back")));
        switch (sc.nextLine().trim()) {
            case "1" -> managerService.createAcademicReport(userService.getAllStudents());
            case "2" -> reportService.createResearchReport(userService.getAllTeachers());
            case "3" -> printReports(reportService.getAllReports());
            case "4" -> printReport();
            case "5" -> {
                String title = promptRequired(t("prompt.report_title"));
                if (title != null) reportService.deleteReport(title);
            }
            case "0" -> {}
            default -> System.out.println(t("app.invalid"));
        }
    }

    private void newsMenu() {
        MenuPrinter.print(t("news.title"), null, List.of("1. " + t("news.add"), "2. " + t("news.remove"), "3. " + t("news.pin"), "4. " + t("news.view"), "0. " + t("menu.back")));
        switch (sc.nextLine().trim()) {
            case "1" -> addNews();
            case "2" -> {
                String title = promptRequired(t("prompt.news_title"));
                if (title != null) managerService.removeNews(title);
            }
            case "3" -> {
                String title = promptRequired(t("prompt.news_title"));
                if (title != null) managerService.pinNews(title);
            }
            case "4" -> printNews(newsService.getAllNews());
            case "0" -> {}
            default -> System.out.println(t("app.invalid"));
        }
    }

    private void addNews() {
        String title = promptRequired(t("prompt.news_title"));
        String content = promptRequired(t("prompt.news_content"));
        NewsTopic topic = readNewsTopic();
        if (title != null && content != null && topic != null) managerService.addNews(new News(title, content, topic));
    }

    private void assignSupervisor() {
        User studentUser = userService.findById(readInt(t("prompt.grad_student_id")));
        if (!(studentUser instanceof GraduateStudent student)) {
            System.out.println(t("manager.grad_not_found"));
            return;
        }
        User supervisorUser = userService.findById(readInt(t("prompt.supervisor_id")));
        if (!(supervisorUser instanceof Researcher supervisor)) {
            System.out.println(t("manager.supervisor_not_researcher"));
            return;
        }
        try {
            projectService.assignSupervisor(student, supervisor);
            System.out.println(t("manager.supervisor_assigned"));
        } catch (InvalidSupervisorEx e) {
            System.out.println(e.getMessage());
        }
    }

    private void addResearchProject() {
        String topic = promptRequired(t("prompt.project_topic"));
        if (topic == null) return;
        if (projectService.findProjectByTopic(topic) != null) {
            System.out.println(t("manager.project_exists"));
            return;
        }
        projectService.addProject(new ResearchProject(topic));
    }

    private void messagesMenu() {
        MenuPrinter.print(t("msg.title"), null, List.of("1. " + t("msg.send"), "2. " + t("msg.inbox"), "3. " + t("msg.sent"), "4. " + t("msg.open"), "0. " + t("menu.back")));
        switch (sc.nextLine().trim()) {
            case "1" -> sendMessage();
            case "2" -> messageService.printInbox();
            case "3" -> messageService.printSentMessages();
            case "4" -> {
                int id = readInt(t("prompt.message_id"));
                if (id >= 0) System.out.println(messageService.openMessage(id));
            }
            case "0" -> {}
            default -> System.out.println(t("app.invalid"));
        }
    }

    private void sendMessage() {
        User user = userService.findById(readInt(t("prompt.receiver_id")));
        if (!(user instanceof Employee employee)) {
            System.out.println(t("msg.receiver_not_found"));
            return;
        }
        String text = promptRequired(t("prompt.message"));
        if (text != null) messageService.sendMessage(employee, text);
    }

    private Student readStudent() {
        User user = userService.findById(readInt(t("prompt.student_id")));
        if (user instanceof Student student) return student;
        System.out.println(t("manager.student_not_found"));
        return null;
    }

    private Course readCourse() {
        String code = promptRequired(t("prompt.course_code"));
        if (code == null) return null;
        for (Course course : userService.getAllCourses()) {
            if (course.getCourseCode().equalsIgnoreCase(code)) return course;
        }
        System.out.println(t("manager.course_not_found"));
        return null;
    }

    private void printReport() {
        String title = promptRequired(t("prompt.report_title"));
        if (title == null) return;
        Report report = reportService.getReportByTitle(title);
        if (report != null) reportService.printReport(report, Format.PLAIN_TEXT);
    }

    private void printStudents(List<Student> students) {
        if (students.isEmpty()) System.out.println(t("manager.no_students"));
        else students.forEach(System.out::println);
    }

    private void printTeachers(List<Teacher> teachers) {
        if (teachers.isEmpty()) System.out.println(t("manager.no_teachers"));
        else teachers.forEach(System.out::println);
    }

    private void printReports(List<Report> reports) {
        if (reports.isEmpty()) System.out.println(t("manager.no_reports"));
        else reports.forEach(r -> System.out.println(r.getTitle()));
    }

    private void printNews(List<News> news) {
        if (news.isEmpty()) System.out.println(t("manager.no_news"));
        else news.forEach(item -> System.out.println(item + "\n" + item.getContent()));
    }

    private void switchLanguage(User user) {
        Language language = readLanguage();
        if (language != null) userService.changeLanguage(user, language);
    }

    private CourseType readCourseType() {
        System.out.print(t("prompt.course_type") + ": ");
        try {
            return CourseType.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println(t("invalid.course_type"));
            return null;
        }
    }

    private LessonType readLessonType() {
        System.out.print(t("prompt.lesson_type") + ": ");
        try {
            return LessonType.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println(t("invalid.lesson_type"));
            return null;
        }
    }

    private NewsTopic readNewsTopic() {
        System.out.print(t("prompt.news_topic") + ": ");
        try {
            return NewsTopic.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println(t("invalid.news_topic"));
            return null;
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

    private int readInt(String label) {
        System.out.print(label + ": ");
        try {
            return Integer.parseInt(sc.nextLine().trim());
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
