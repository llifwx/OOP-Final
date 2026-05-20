package demo;

import comparator.ResearchPaperCitationComparator;
import comparator.ResearchPaperDateComparator;
import comparator.ResearchPaperLengthComparator;
import enums.CourseType;
import enums.DegreeType;
import enums.Format;
import enums.Language;
import enums.LessonType;
import enums.ManagerType;
import enums.NewsTopic;
import enums.RegistrationStatus;
import enums.RequestStatus;
import enums.TeacherType;
import exceptions.NotResearcherEx;
import factory.UserFactory;
import interfaces.Researcher;
import model.academic.Course;
import model.academic.CourseRegistration;
import model.academic.Mark;
import model.research.ResearchPaper;
import model.research.ResearchProject;
import model.social.Journal;
import model.social.Message;
import model.social.News;
import model.support.TechSupportReq;
import model.users.Admin;
import model.users.GraduateStudent;
import model.users.Manager;
import model.users.Student;
import model.users.Teacher;
import model.users.TechSupportSpecialist;
import model.users.User;
import services.AdminService;
import services.AuthService;
import services.JournalService;
import services.ManagerService;
import services.MessageService;
import services.NewsService;
import services.ReportService;
import services.ResearchPaperService;
import services.ResearchProjectService;
import services.ResearchService;
import services.StudentService;
import services.TeacherService;
import services.TechSupportService;
import services.UserService;
import storage.Database;
import storage.FileStorage;
import utils.LogRecord;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DemoScenario {
    private static final String PASSWORD = "demo123";
    private static final String COURSE_CODE = "DEMO_CS101";
    private static final String JOURNAL_NAME = "DEMO_University_Research_Journal";
    private static final String PAPER_TITLE = "DEMO Paper: Object-Oriented Design in University Systems";
    private static final String PROJECT_TOPIC = "DEMO Research Project: Smart University Workflow";

    private static Database database;
    private static AuthService authService;
    private static UserService userService;
    private static AdminService adminService;
    private static ManagerService managerService;
    private static StudentService studentService;
    private static TeacherService teacherService;
    private static JournalService journalService;
    private static NewsService newsService;
    private static ResearchService researchService;
    private static ResearchPaperService researchPaperService;
    private static ResearchProjectService researchProjectService;
    private static TechSupportService techSupportService;
    private static MessageService messageService;

    private static Admin admin;
    private static Manager manager;
    private static Teacher teacher;
    private static Student student;
    private static GraduateStudent graduateStudent;
    private static TechSupportSpecialist supportSpecialist;
    private static Course demoCourse;
    private static CourseRegistration registration;
    private static Journal journal;
    private static ResearchPaper publishedPaper;
    private static ResearchProject researchProject;
    private static TechSupportReq supportRequest;
    private static Message message;

    public static void main(String[] args) {
        runDemo();
    }

    private static void runDemo() {
        File databaseFile = resolveDatabaseFile();
        boolean originalDatabaseExisted = databaseFile.exists();
        Path backupPath = null;

        try {
            if (originalDatabaseExisted) {
                backupPath = Files.createTempFile("university-demo-database-backup-", ".ser");
                Files.copy(databaseFile.toPath(), backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("========== UNIVERSITY SYSTEM DEMO ==========");
            setupServices();
            createDemoUsers();
            demonstrateAuthentication();
            demonstrateAdminUserManagement();
            demonstrateCourseRegistration();
            demonstrateTeacherAssignment();
            demonstrateMarksAndTranscript();
            demonstrateResearchFlow();
            demonstrateNewsAndJournalFlow();
            demonstrateTechSupportFlow();
            demonstrateMessagingFlow();
            demonstrateLogsAndSerialization();
            System.out.println();
            System.out.println("========== DEMO FINISHED SUCCESSFULLY ==========");
        } catch (Throwable t) {
            System.out.println();
            System.out.println("[ERROR] Demo stopped unexpectedly: " + t.getClass()
                    .getSimpleName() + " - " + t.getMessage());
        } finally {
            restoreDatabaseFile(databaseFile, backupPath, originalDatabaseExisted);
        }
    }

    private static void setupServices() throws
            Exception {
        database = createEmptyDatabase();
        Database.setInstance(database);

        authService = new AuthService(database);
        userService = new UserService(database, authService);
        adminService = new AdminService(database, authService, userService);
        managerService = new ManagerService(database, authService, new ReportService(database, authService));
        studentService = new StudentService(database, authService);
        teacherService = new TeacherService(database, authService);
        journalService = new JournalService(database, authService);
        newsService = new NewsService(database, authService);
        researchService = new ResearchService(database, authService);
        researchPaperService = new ResearchPaperService(database, authService, journalService);
        researchProjectService = new ResearchProjectService(database, authService);
        techSupportService = new TechSupportService(database, authService);
        messageService = new MessageService(database, authService);
    }

    private static void createDemoUsers() {
        admin = UserFactory.createAdmin("DEMO_ADMIN", PASSWORD, "DEMO Admin", "demo.admin@university.kz", Language.EN);
        database.addUser(admin);
        login("DEMO_ADMIN");

        manager = UserFactory.createManager("DEMO_MANAGER", PASSWORD, "DEMO Manager", "demo.manager@university.kz", Language.EN, "DEMO_M001", "Academic Office", 300000, ManagerType.OR);
        teacher = UserFactory.createTeacher("DEMO_TEACHER", PASSWORD, "DEMO Teacher", "demo.teacher@university.kz", Language.EN, "DEMO_T001", "SITE", 350000, TeacherType.PROFESSOR);
        student = UserFactory.createStudent("DEMO_STUDENT", PASSWORD, "DEMO Student", "demo.student@university.kz", Language.EN, "DEMO_S001", "SITE", "Computer Science", 1);
        graduateStudent = UserFactory.createGraduateStudent("DEMO_GRAD_STUDENT", PASSWORD, "DEMO Graduate Student", "demo.grad@university.kz", Language.EN, "DEMO_G001", "SITE", "Computer Science", 1, DegreeType.MASTER);
        supportSpecialist = UserFactory.createTechSupportSpecialist("DEMO_SUPPORT_SPECIALIST", PASSWORD, "DEMO Support Specialist", "demo.support@university.kz", Language.EN, "DEMO_TS001", "Tech Support", 250000);

        quietly(() -> adminService.addUser(manager));
        quietly(() -> adminService.addUser(teacher));
        quietly(() -> adminService.addUser(student));
        quietly(() -> adminService.addUser(graduateStudent));
        quietly(() -> adminService.addUser(supportSpecialist));
        quietly(() -> authService.logout());
    }

    private static void demonstrateAuthentication() {
        printStep("[1] Authentication");
        printAction("Login as admin");
        User adminLogin = login("DEMO_ADMIN");
        printResult("[AUTH] Admin logged in successfully: " + adminLogin.getUsername());
        quietly(() -> authService.logout());

        printAction("Login as manager");
        User managerLogin = login("DEMO_MANAGER");
        printResult("[AUTH] Manager logged in successfully: " + managerLogin.getUsername());
        quietly(() -> authService.logout());

        printAction("Login as student");
        User studentLogin = login("DEMO_STUDENT");
        printResult("[AUTH] Student logged in successfully: " + studentLogin.getUsername());
        quietly(() -> authService.logout());
    }

    private static void demonstrateAdminUserManagement() {
        printStep("[2] Admin User Management");
        login("DEMO_ADMIN");

        printAction("Admin creates and verifies demo users");
        printResult("[ADMIN] Created demo manager: " + exists("DEMO_MANAGER"));
        printResult("[ADMIN] Created demo teacher: " + exists("DEMO_TEACHER"));
        printResult("[ADMIN] Created demo student: " + exists("DEMO_STUDENT"));

        printAction("Admin updates demo student email and language");
        boolean emailUpdated = quietly(() -> adminService.updateUserEmail("DEMO_STUDENT", "demo.student.updated@university.kz"));
        boolean languageUpdated = quietly(() -> adminService.updateUserLanguage("DEMO_STUDENT", Language.KZ));
        printResult("[ADMIN] Updated demo student email: " + emailUpdated);
        printResult("[ADMIN] Updated demo student language: " + languageUpdated);

        printAction("Admin prints users and system logs");
        List<User> users = adminService.listAllUsers();
        printResult("[ADMIN] System users displayed: " + users.size() + " users");
        printUsers(users);
        List<LogRecord> logs = adminService.viewAllLogs();
        printResult("[ADMIN] System logs displayed: " + logs.size() + " records so far");
        printLogPreview(logs, 5);
        quietly(() -> authService.logout());
    }

    private static void demonstrateCourseRegistration() {
        printStep("[3] Course Registration");
        login("DEMO_MANAGER");

        printAction("Manager creates DEMO_CS101 and opens it for registration");
        demoCourse = new Course(COURSE_CODE, "Object-Oriented Programming Demo Course", 3, CourseType.MAJOR, Language.EN, "Computer Science", 1, true);
//        quietly(() -> managerService.addCourseForRegistration(demoCourse));
        printResult("[COURSE] Manager added " + COURSE_CODE + " for registration");
        printResult("[COURSE] Type: " + demoCourse.getType() + ", major: " + demoCourse.getIntendedMajor() + ", year: " + demoCourse.getIntendedYear() + ", open: " + demoCourse.isOpenForRegistration());
        quietly(() -> authService.logout());

        login("DEMO_STUDENT");
        printAction("Student views available courses");
        List<Course> availableCourses = studentService.getAvailableCourses();
        printResult("[COURSE] Available courses for student: " + courseCodes(availableCourses));

        printAction("Student requests registration for DEMO_CS101");
        boolean requested = quietly(() -> studentService.registerForCourse(COURSE_CODE));
        registration = latestRegistrationFor(student, demoCourse);
        printResult("[COURSE] Student requested registration: " + requested);
        printResult("[COURSE] Registration request created with status " + safeStatus(registration));
        quietly(() -> authService.logout());

        login("DEMO_MANAGER");
        printAction("Manager views pending registrations and approves request");
        List<CourseRegistration> pending = managerService.getPendingRegistrations();
        printResult("[COURSE] Pending registration requests: " + pending.size());
        boolean approved = registration != null && quietly(() -> managerService.approveRegistration(registration.getId()));
        printResult("[COURSE] Manager approved registration: " + approved);
        printResult("[COURSE] Student is now enrolled in " + COURSE_CODE + ": " + student.getRegisteredCourses()
                .contains(demoCourse));
        printResult("[COURSE] Course enrolled students: " + userNames(demoCourse.getEnrolledStudents()));
        quietly(() -> authService.logout());
    }

    private static void demonstrateTeacherAssignment() {
        printStep("[4] Teacher Assignment");
        login("DEMO_MANAGER");

        printAction("Manager assigns demo teacher as LECTURE instructor");
//        quietly(() -> managerService.assignCourseToTeacher(COURSE_CODE, teacher.getId(), LessonType.LECTURE));
        printResult("[TEACHER ASSIGNMENT] Teacher assigned as LECTURE instructor");

        printAction("Manager assigns demo teacher as PRACTICE instructor");
//        quietly(() -> managerService.assignCourseToTeacher(COURSE_CODE, teacher.getId(), LessonType.PRACTICE));
        printResult("[TEACHER ASSIGNMENT] Teacher assigned as PRACTICE instructor");

        printAction("Print course instructors and teacher assigned courses");
        printResult("[TEACHER ASSIGNMENT] Lecture instructors: " + userNames(demoCourse.getInstructorsByLessonType(LessonType.LECTURE)));
        printResult("[TEACHER ASSIGNMENT] Practice instructors: " + userNames(demoCourse.getInstructorsByLessonType(LessonType.PRACTICE)));
        printResult("[TEACHER ASSIGNMENT] Teacher can see assigned course: " + courseCodes(teacher.getCourses()));
        quietly(() -> authService.logout());
    }

    private static void demonstrateMarksAndTranscript() {
        printStep("[5] Marks and Transcript");
        login("DEMO_TEACHER");

        printAction("Teacher puts first attestation, second attestation, and final exam mark");
        Mark mark = new Mark(student, demoCourse);
        mark.setFirstAttestation(88);
        mark.setSecondAttestation(91);
        mark.setFinalExam(86);
        boolean markSaved = quietly(() -> teacherService.putMark(student, demoCourse, mark));
        printResult("[MARKS] Teacher put mark for demo student: " + markSaved);
        printResult("[MARKS] Total score calculated: " + format(mark.getTotalScore()));
        quietly(() -> authService.logout());

        login("DEMO_STUDENT");
        printAction("Student views transcript and GPA");
        printResult("[TRANSCRIPT] Student transcript updated: " + student.getTranscript()
                .getMarks()
                .size() + " mark(s)");
        printResult("[TRANSCRIPT] GPA recalculated: " + format(student.getGpa()));
        printResult("[TRANSCRIPT] " + student.getTranscript());
        quietly(() -> authService.logout());
    }

    private static void demonstrateResearchFlow() {
        printStep("[6] Research");
        login("DEMO_GRAD_STUDENT");

        printAction("Graduate student acts as a researcher");
        boolean isResearcher = graduateStudent instanceof Researcher;
        printResult("[RESEARCH] Graduate student is a researcher: " + isResearcher);

        journal = new Journal(JOURNAL_NAME);
        quietly(() -> journalService.addJournal(journal));

        publishedPaper = new ResearchPaper(PAPER_TITLE, List.of(graduateStudent, teacher), journal, 8, 14, daysAgo(1), "DEMO-DOI-OOP-001");

        printAction("Researcher publishes a paper");
        quietly(() -> researchPaperService.publishPaper(graduateStudent, publishedPaper, journal));
        printResult("[RESEARCH] Research paper created: " + publishedPaper.getTitle());
        printResult("[RESEARCH] Paper added to researcher: " + graduateStudent.getPapers().contains(publishedPaper));
        printResult("[RESEARCH] H-index calculated: " + graduateStudent.calculateHIndex());

        printAction("Researcher joins a research project");
        researchProject = new ResearchProject(PROJECT_TOPIC);
        quietly(() -> researchProjectService.addProject(researchProject));
        try {
            quietlyThrows(() -> researchProjectService.joinProject(researchProject, graduateStudent));
            researchProjectService.addPaperToProject(researchProject, publishedPaper);
            printResult("[RESEARCH] Research project created: " + researchProject.getTopic());
            printResult("[RESEARCH] Project participants: " + researchProject.getParticipants().size());
        } catch (NotResearcherEx e) {
            printSkipped("Research project join failed: " + e.getMessage());
        }

        printAction("Research papers are sorted and citation is printed");
        ResearchPaper comparisonPaperA = new ResearchPaper("DEMO Paper: Teaching OOP with Services", List.of(graduateStudent), journal, 3, 8, daysAgo(10), "DEMO-DOI-OOP-002");
        ResearchPaper comparisonPaperB = new ResearchPaper("DEMO Paper: Research Metrics in Education", List.of(teacher), journal, 12, 20, daysAgo(20), "DEMO-DOI-OOP-003");
        List<ResearchPaper> papers = new ArrayList<>(Arrays.asList(publishedPaper, comparisonPaperA, comparisonPaperB));
        papers.sort(new ResearchPaperCitationComparator());
        printResult("[RESEARCH] Sorted by citations: " + paperTitles(papers));
        papers.sort(new ResearchPaperDateComparator());
        printResult("[RESEARCH] Sorted by date: " + paperTitles(papers));
        papers.sort(new ResearchPaperLengthComparator());
        printResult("[RESEARCH] Sorted by pages: " + paperTitles(papers));
        printResult("[RESEARCH] Paper citation printed: " + researchPaperService.getCitation(publishedPaper, Format.PLAIN_TEXT));
        quietly(() -> authService.logout());
    }

    private static void demonstrateNewsAndJournalFlow() {
        printStep("[7] News and Journals");

        printAction("Student subscribes to demo research journal");
        int notificationsBefore = student.getNotifications().size();
        quietly(() -> journalService.subscribe(student, journal));
        printResult("[JOURNAL] Student subscribed to research journal: " + journal.getName());

        printAction("Publish another paper in the journal");
        ResearchPaper journalPaper = new ResearchPaper("DEMO Paper: Observer Pattern for Research Journals", List.of(graduateStudent), journal, 5, 11, new Date(), "DEMO-DOI-OBS-001");
        quietly(() -> journalService.publishPaper(journal, journalPaper));
        printResult("[JOURNAL] Paper published in journal: " + journalPaper.getTitle());
        printResult("[NOTIFICATION] Student received journal notification: " + (student.getNotifications()
                .size() > notificationsBefore));

        printAction("Research news announcement is created and pinned");
        News researchNews = database.findNewsByTitle("New Paper Published: " + journalPaper.getTitle());
        printResult("[NEWS] Research news announcement created: " + (researchNews != null));
        printResult("[NEWS] Research news is prioritized: " + (researchNews != null && researchNews.isPinned()));

        login("DEMO_MANAGER");
        printAction("Manager creates additional defense news");
        News defenseNews = new News("DEMO Defense Showcase", "DEMO scenario is ready for final defense.", NewsTopic.ACADEMIC);
        quietly(() -> managerService.addNews(defenseNews));
        printResult("[NEWS] Academic news created: " + defenseNews.getTitle());
        quietly(() -> authService.logout());
    }

    private static void demonstrateTechSupportFlow() {
        printStep("[8] Tech Support");
        login("DEMO_MANAGER");

        printAction("Employee submits tech support request");
        supportRequest = quietly(() -> techSupportService.submitRequest("DEMO Projector does not work in room 301."));
        printResult("[SUPPORT] Employee submitted request: " + (supportRequest != null));
        printResult("[SUPPORT] Request status: " + safeSupportStatus(supportRequest));
        quietly(() -> authService.logout());

        login("DEMO_SUPPORT_SPECIALIST");
        printAction("Tech support specialist views request");
        boolean visible = quietly(() -> techSupportService.viewNewRequests()).contains(supportRequest);
        TechSupportReq viewed = supportRequest == null ? null : quietly(() -> techSupportService.viewRequest(supportRequest.getId()));
        printResult("[SUPPORT] Specialist viewed request: " + (visible && viewed != null));
        printResult("[SUPPORT] Request status: " + safeSupportStatus(supportRequest));

        printAction("Specialist accepts request");
        boolean accepted = supportRequest != null && quietly(() -> techSupportService.acceptRequest(supportRequest.getId()));
        printResult("[SUPPORT] Specialist accepted request: " + accepted);
        printResult("[SUPPORT] Request status: " + safeSupportStatus(supportRequest));

        printAction("Specialist marks request as done");
        boolean done = supportRequest != null && quietly(() -> techSupportService.markAsDone(supportRequest.getId()));
        printResult("[SUPPORT] Request marked as DONE: " + done);
        printResult("[SUPPORT] Request status: " + safeSupportStatus(supportRequest));
        quietly(() -> authService.logout());
    }

    private static void demonstrateMessagingFlow() {
        printStep("[9] Messaging");
        login("DEMO_MANAGER");

        printAction("Manager sends message to teacher");
        message = quietly(() -> messageService.sendMessage(teacher, "DEMO Please review the OOP demo course before defense."));
        printResult("[MESSAGE] Employee sent message: " + (message != null));
        quietly(() -> authService.logout());

        login("DEMO_TEACHER");
        printAction("Teacher opens inbox");
        List<Message> inbox = messageService.getInbox();
        printResult("[MESSAGE] Receiver can see message: " + inbox.contains(message));
        if (message != null) {
            printResult("[MESSAGE] Preview: " + messageService.getPreview(message));
        }
        quietly(() -> authService.logout());
    }

    private static void demonstrateLogsAndSerialization() {
        printStep("[10] Logs and Storage");
        login("DEMO_ADMIN");

        printAction("Print several log records after demo actions");
        List<LogRecord> logs = adminService.viewAllLogs();
        printResult("[LOGS] User actions were logged: " + logs.size() + " records");
        printLogPreview(logs, 8);

        printAction("Save database and load it again");
        quietly(() -> database.save());
        Database loaded = FileStorage.load();
        Database.setInstance(loaded);
        boolean userExists = loaded.findUserByUsername("DEMO_STUDENT") != null;
        boolean courseExists = loaded.findCourseByCode(COURSE_CODE) != null;
        boolean paperExists = loaded.findResearchPaperByTitle(PAPER_TITLE) != null;
        boolean requestExists = supportRequest != null && loaded.findTechSupportReqById(supportRequest.getId()) != null;
        printResult("[STORAGE] Database saved successfully");
        printResult("[STORAGE] Demo user exists after reload: " + userExists);
        printResult("[STORAGE] Demo course exists after reload: " + courseExists);
        printResult("[STORAGE] Demo research paper exists after reload: " + paperExists);
        printResult("[STORAGE] Demo support request exists after reload: " + requestExists);
        Database.setInstance(database);
        quietly(() -> authService.logout());
    }

    private static User login(String username) {
        User user = quietly(() -> authService.login(username, PASSWORD));
        if (user == null) {
            throw new IllegalStateException("Could not login as " + username);
        }
        return user;
    }

    private static boolean exists(String username) {
        return database.findUserByUsername(username) != null;
    }

    private static CourseRegistration latestRegistrationFor(Student targetStudent, Course targetCourse) {
        List<CourseRegistration> registrations = database.findRegistrationsByStudent(targetStudent);
        for (int i = registrations.size() - 1; i >= 0; i--) {
            CourseRegistration current = registrations.get(i);
            if (current.getCourse().equals(targetCourse)) {
                return current;
            }
        }
        return null;
    }

    private static void printStep(String title) {
        System.out.println();
        System.out.println(title);
    }

    private static void printAction(String text) {
        System.out.println("[STEP] " + text);
    }

    private static void printResult(String text) {
        System.out.println("[RESULT] " + text);
    }

    private static void printSkipped(String text) {
        System.out.println("[SKIPPED] " + text);
    }

    private static void printUsers(List<User> users) {
        for (User user : users) {
            System.out.println("  - " + user.getClass()
                    .getSimpleName() + ": " + user.getUsername() + " | " + user.getFullName() + " | " + user.getEmail());
        }
    }

    private static void printLogPreview(List<LogRecord> logs, int limit) {
        int count = Math.min(limit, logs.size());
        for (int i = Math.max(0, logs.size() - count); i < logs.size(); i++) {
            System.out.println("  - " + logs.get(i));
        }
    }

    private static String courseCodes(List<Course> courses) {
        List<String> codes = new ArrayList<>();
        for (Course course : courses) {
            codes.add(course.getCourseCode());
        }
        return codes.toString();
    }

    private static String userNames(List<? extends User> users) {
        List<String> names = new ArrayList<>();
        for (User user : users) {
            names.add(user.getUsername());
        }
        return names.toString();
    }

    private static String paperTitles(List<ResearchPaper> papers) {
        List<String> titles = new ArrayList<>();
        for (ResearchPaper paper : papers) {
            titles.add(paper.getTitle() + " (" + paper.getCitations() + " citations, " + paper.getPages() + " pages)");
        }
        return titles.toString();
    }

    private static String safeStatus(CourseRegistration value) {
        RegistrationStatus status = value == null ? null : value.getStatus();
        return status == null ? "N/A" : status.toString();
    }

    private static String safeSupportStatus(TechSupportReq request) {
        RequestStatus status = request == null ? null : request.getStatus();
        return status == null ? "N/A" : status.toString();
    }

    private static String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static Date daysAgo(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return calendar.getTime();
    }

    private static Database createEmptyDatabase() throws
            Exception {
        Constructor<Database> constructor = Database.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static File resolveDatabaseFile() {
        File moduleDatabase = new File("University", "database.ser");
        File parent = moduleDatabase.getParentFile();
        if (moduleDatabase.exists() || (parent != null && parent.exists())) {
            return moduleDatabase;
        }
        return new File("database.ser");
    }

    private static void restoreDatabaseFile(File databaseFile, Path backupPath, boolean originalDatabaseExisted) {
        try {
            if (backupPath != null && Files.exists(backupPath)) {
                Files.copy(backupPath, databaseFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(backupPath);
            } else if (!originalDatabaseExisted && databaseFile.exists()) {
                Files.delete(databaseFile.toPath());
            }
        } catch (IOException e) {
            System.out.println("[WARNING] Could not restore original database file: " + e.getMessage());
        }
    }

    private static void quietly(QuietRunnable action) {
        quietly(() -> {
            action.run();
            return null;
        });
    }

    private static <T> T quietly(QuietSupplier<T> action) {
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            return action.get();
        } finally {
            System.setOut(original);
        }
    }

    private static void quietlyThrows(ThrowingRunnable action) throws
            NotResearcherEx {
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            action.run();
        } finally {
            System.setOut(original);
        }
    }

    @FunctionalInterface
    private interface QuietRunnable {
        void run();
    }

    @FunctionalInterface
    private interface QuietSupplier<T> {
        T get();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws
                NotResearcherEx;
    }
}
