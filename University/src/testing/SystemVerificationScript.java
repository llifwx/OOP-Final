package testing;

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
import exceptions.InvalidSupervisorEx;
import exceptions.MarkException;
import exceptions.NotResearcherException;
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
import model.users.Employee;
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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class SystemVerificationScript {
    private static final String PASSWORD = "TEST_password";
    private static final String COURSE_CODE = "TEST_CS101";
    private static final String JOURNAL_NAME = "TEST_Research_Journal";
    private static final String PAPER_TITLE = "TEST Research Paper for Verification";

    private static int passed;
    private static int failed;

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
    private static Teacher secondTeacher;
    private static Student student;
    private static GraduateStudent graduateStudent;
    private static TechSupportSpecialist support;
    private static Course mainCourse;
    private static Journal journal;
    private static ResearchPaper publishedPaper;
    private static ResearchProject researchProject;
    private static TechSupportReq acceptedSupportRequest;
    private static Message message;

    public static void main(String[] args) {
        runAllChecks();
    }

    private static void runAllChecks() {
        PrintStream originalOut = System.out;
        System.setOut(new VerificationPrintStream(originalOut));
        System.out.println("========== SYSTEM VERIFICATION ==========");
        File databaseFile = resolveDatabaseFile();
        boolean originalDatabaseExisted = databaseFile.exists();
        Path backupPath = null;

        try {
            if (originalDatabaseExisted) {
                backupPath = Files.createTempFile("university-database-backup-", ".ser");
                Files.copy(databaseFile.toPath(), backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            setupCleanDatabase();
            checkAuthentication();
            checkUserManagement();
            checkCourseRegistrationFlow();
            checkTeacherAssignmentFlow();
            checkMarksTranscriptAndGpaFlow();
            checkResearchFlow();
            checkNewsAndJournalFlow();
            checkTechSupportFlow();
            checkMessagingFlow();
            checkSerializationFlow();
        } catch (Throwable t) {
            fail("System verification bootstrap", t.getClass().getSimpleName() + ": " + t.getMessage());
        } finally {
            restoreDatabaseFile(databaseFile, backupPath, originalDatabaseExisted);
        }

        printSummary();
        System.setOut(originalOut);
    }

    private static void setupCleanDatabase() throws Exception {
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

        admin = UserFactory.createAdmin("TEST_ADMIN", PASSWORD, "TEST Admin", "test.admin@university.kz", Language.EN);
        manager = UserFactory.createManager("TEST_MANAGER", PASSWORD, "TEST Manager", "test.manager@university.kz", Language.EN,
                "TEST_M001", "Academic Department", 1000, ManagerType.OR);
        teacher = UserFactory.createTeacher("TEST_TEACHER", PASSWORD, "TEST Teacher", "test.teacher@university.kz", Language.EN,
                "TEST_T001", "SITE", 1000, TeacherType.PROFESSOR);
        secondTeacher = UserFactory.createTeacher("TEST_TEACHER_2", PASSWORD, "TEST Second Teacher", "test.teacher2@university.kz", Language.EN,
                "TEST_T002", "SITE", 1000, TeacherType.TUTOR);
        student = UserFactory.createStudent("TEST_STUDENT", PASSWORD, "TEST Student", "test.student@university.kz", Language.EN,
                "TEST_S001", "SITE", "Computer Science", 1);
        graduateStudent = UserFactory.createGraduateStudent("TEST_GRAD_STUDENT", PASSWORD, "TEST Graduate Student",
                "test.grad@university.kz", Language.EN, "TEST_G001", "SITE", "Computer Science", 1, DegreeType.MASTER);
        support = UserFactory.createTechSupportSpecialist("TEST_SUPPORT", PASSWORD, "TEST Support",
                "test.support@university.kz", Language.EN, "TEST_TS001", "Tech Support", 1000);

        database.addUser(admin);
        database.addUser(manager);
        database.addUser(teacher);
        database.addUser(secondTeacher);
        database.addUser(student);
        database.addUser(graduateStudent);
        database.addUser(support);
    }

    private static void checkAuthentication() {
        module("AUTH");
        assertTrue(authService.login("TEST_ADMIN", PASSWORD) == admin, "Admin login works", "expected TEST_ADMIN login to return admin user");
        assertTrue(authService.login("TEST_MANAGER", PASSWORD) == manager, "Manager login works", "expected TEST_MANAGER login to return manager user");
        assertTrue(authService.login("TEST_TEACHER", PASSWORD) == teacher, "Teacher login works", "expected TEST_TEACHER login to return teacher user");
        assertTrue(authService.login("TEST_STUDENT", PASSWORD) == student, "Student login works", "expected TEST_STUDENT login to return student user");
        assertTrue(authService.login("TEST_STUDENT", "wrong-password") == null, "Wrong password is rejected", "expected wrong password to return null");
        login("TEST_STUDENT");
        assertThrows(SecurityException.class, "Student cannot execute manager-only actions",
                () -> managerService.getPendingRegistrations(),
                "expected manager service call from student account to throw SecurityException");
        login("TEST_TEACHER");
        assertThrows(SecurityException.class, "Teacher cannot execute admin-only actions",
                () -> adminService.listAllUsers(),
                "expected admin service call from teacher account to throw SecurityException");
    }

    private static void checkUserManagement() {
        module("ADMIN");
        login("TEST_ADMIN");

        User newUser = UserFactory.createUser("student", "TEST_ADMIN_CREATED_STUDENT", PASSWORD,
                "TEST Admin Created Student", "test.created@university.kz");
        assertTrue(adminService.addUser(newUser), "Admin can add user", "expected addUser to return true");
        assertTrue(database.findUserByUsername("TEST_ADMIN_CREATED_STUDENT") != null, "Created user exists in database",
                "expected TEST_ADMIN_CREATED_STUDENT to be present in database");

        assertTrue(adminService.updateUserEmail("TEST_ADMIN_CREATED_STUDENT", "test.updated@university.kz"),
                "Admin can update email", "expected updateUserEmail to return true");
        assertTrue("test.updated@university.kz".equals(newUser.getEmail()), "Updated email is saved",
                "expected email to be test.updated@university.kz but was " + newUser.getEmail());

        assertTrue(adminService.updateUserFullName("TEST_ADMIN_CREATED_STUDENT", "TEST Updated Student"),
                "Admin can update full name", "expected updateUserFullName to return true");
        assertTrue("TEST Updated Student".equals(newUser.getFullName()), "Updated full name is saved",
                "expected full name to be TEST Updated Student but was " + newUser.getFullName());

        assertTrue(adminService.updateUserLanguage("TEST_ADMIN_CREATED_STUDENT", Language.RU),
                "Admin can update language", "expected updateUserLanguage to return true");
        assertTrue(newUser.getLanguage() == Language.RU, "Updated language is saved",
                "expected language RU but was " + newUser.getLanguage());

        assertTrue(adminService.listAllUsers().size() >= 8, "Admin can list users",
                "expected at least 8 users after admin-created test user");
        assertTrue(!adminService.viewAllLogs().isEmpty(), "Admin can view logs",
                "expected logs to be visible to admin");
        assertTrue(database.findLogsByUser(admin).stream().anyMatch(log -> log.toString().contains("updated")),
                "Logs are created for admin actions", "expected admin update actions to create log entries");
    }

    private static void checkCourseRegistrationFlow() {
        module("COURSE REGISTRATION");
        login("TEST_MANAGER");
        mainCourse = new Course(COURSE_CODE, "Test Introduction to OOP", 3, CourseType.MAJOR, Language.EN,
                "Computer Science", 1, true);

        managerService.addCourseForRegistration(mainCourse);
        assertTrue(database.findCourseByCode(COURSE_CODE) == mainCourse, "Manager can create course",
                "expected TEST_CS101 to be stored in database");
        assertTrue("Computer Science".equals(mainCourse.getIntendedMajor()) && mainCourse.getIntendedYear() == 1
                        && mainCourse.getType() == CourseType.MAJOR && mainCourse.getLanguage() == Language.EN
                        && mainCourse.isOpenForRegistration(),
                "Course registration metadata is correct",
                "expected major/year/type/language/open flag to match TEST_CS101 setup");

        login("TEST_STUDENT");
        assertTrue(studentService.getAvailableCourses().contains(mainCourse), "Student sees available course",
                "expected TEST_CS101 in available courses");
        assertTrue(studentService.registerForCourse(COURSE_CODE), "Student can request course registration",
                "expected registerForCourse(TEST_CS101) to return true");

        CourseRegistration registration = latestRegistrationFor(student, mainCourse);
        assertTrue(registration != null, "Registration request is created", "expected a CourseRegistration for TEST_STUDENT and TEST_CS101");
        assertTrue(registration != null && registration.getStatus() == RegistrationStatus.PENDING, "Request status is PENDING",
                "expected registration status PENDING");

        boolean duplicatePending = studentService.registerForCourse(COURSE_CODE);
        assertTrue(!duplicatePending, "Duplicate pending registration is rejected",
                "expected second request for same course to return false");

        login("TEST_MANAGER");
        assertTrue(registration != null && managerService.approveRegistration(registration.getId()),
                "Manager can approve registration", "expected approveRegistration to return true");
        assertTrue(registration.getStatus() == RegistrationStatus.APPROVED, "Request status becomes APPROVED",
                "expected registration status APPROVED but was " + registration.getStatus());
        assertTrue(student.getRegisteredCourses().contains(mainCourse), "Student has course in registered courses",
                "expected student registeredCourses to contain TEST_CS101");
        assertTrue(mainCourse.getEnrolledStudents().contains(student), "Course has student in enrolled students",
                "expected course enrolledStudents to contain TEST_STUDENT");
        assertTrue(student.getCredits() == mainCourse.getCredits(), "Student credits increase",
                "expected credits " + mainCourse.getCredits() + " but was " + student.getCredits());

        login("TEST_STUDENT");
        assertTrue(!studentService.registerForCourse(COURSE_CODE), "Duplicate approved registration is rejected",
                "expected registration for already enrolled course to return false");
        assertCreditLimit();
    }

    private static void checkTeacherAssignmentFlow() {
        module("TEACHER ASSIGNMENT");
        login("TEST_MANAGER");

        managerService.assignCourseToTeacher(COURSE_CODE, teacher.getId(), LessonType.LECTURE);
        assertTrue(mainCourse.getInstructorsByLessonType(LessonType.LECTURE).contains(teacher),
                "Manager can assign teacher to course as LECTURE",
                "expected teacher in TEST_CS101 lecture instructors");
        assertTrue(teacher.getCourses().contains(mainCourse), "Course is added to teacher courses",
                "expected TEST_CS101 in teacher courses");

        managerService.assignCourseToTeacher(COURSE_CODE, teacher.getId(), LessonType.PRACTICE);
        assertTrue(mainCourse.getInstructorsByLessonType(LessonType.PRACTICE).contains(teacher),
                "Manager can assign teacher to course as PRACTICE",
                "expected teacher in TEST_CS101 practice instructors");
        assertTrue(mainCourse.getInstructors().contains(teacher), "Teacher is added to course instructors",
                "expected teacher in all instructors list");

        int lectureCountBefore = mainCourse.getInstructorsByLessonType(LessonType.LECTURE).size();
        managerService.assignCourseToTeacher(COURSE_CODE, teacher.getId(), LessonType.LECTURE);
        assertTrue(mainCourse.getInstructorsByLessonType(LessonType.LECTURE).size() == lectureCountBefore,
                "Duplicate assignment for same lesson type is not duplicated",
                "expected lecture instructor count to remain " + lectureCountBefore);

        login("TEST_TEACHER");
        assertThrows(SecurityException.class, "Teacher cannot self-assign to course",
                () -> managerService.assignCourseToTeacher(COURSE_CODE, teacher.getId(), LessonType.LECTURE),
                "expected teacher using manager workflow to throw SecurityException");
    }

    private static void checkMarksTranscriptAndGpaFlow() {
        module("MARKS");
        Course unassignedCourse = new Course("TEST_UNASSIGNED", "Test Unassigned Course", 3, CourseType.FREE_ELECTIVE, Language.EN,
                "Computer Science", 1, true);
        database.addCourse(unassignedCourse);

        Student notEnrolledStudent = UserFactory.createStudent("TEST_NOT_ENROLLED", PASSWORD, "TEST Not Enrolled",
                "test.not.enrolled@university.kz", Language.EN, "TEST_S002", "SITE", "Computer Science", 1);
        database.addUser(notEnrolledStudent);

        login("TEST_TEACHER");
        Mark validMark = createMark(student, mainCourse, 80, 90, 85);
        double oldGpa = student.getGpa();
        assertTrue(teacherService.putMark(student, mainCourse, validMark), "Teacher can put mark for enrolled student",
                "expected putMark to return true for assigned course and enrolled student");
        assertTrue(Math.abs(validMark.getTotalScore() - 85.0) < 0.001, "Total score is calculated correctly",
                "expected 85.0 but was " + validMark.getTotalScore());
        assertTrue(student.getTranscript().getMarks().contains(validMark), "Transcript receives the mark",
                "expected transcript to contain TEST_CS101 mark");
        assertTrue(student.getGpa() > oldGpa, "GPA is recalculated",
                "expected GPA to increase from " + oldGpa + " but was " + student.getGpa());

        assertTrue(!teacherService.putMark(student, unassignedCourse, createMark(student, unassignedCourse, 70, 70, 70)),
                "Teacher cannot put mark for unassigned course",
                "expected putMark to return false for unassigned course");
        assertTrue(!teacherService.putMark(notEnrolledStudent, mainCourse, createMark(notEnrolledStudent, mainCourse, 70, 70, 70)),
                "Teacher cannot put mark for student not enrolled in course",
                "expected putMark to return false for not enrolled student");
        assertTrue(!teacherService.putMark(student, mainCourse, createMark(student, mainCourse, 90, 90, 90)),
                "Duplicate marks for same student/course are rejected",
                "expected duplicate mark for TEST_CS101 to return false");

        Course failCourse = new Course("TEST_FAIL_LIMIT", "Test Fail Limit", 3, CourseType.FREE_ELECTIVE, Language.EN,
                "Computer Science", 1, true);
        student.getTranscript().incrementFailedAttempts(failCourse);
        student.getTranscript().incrementFailedAttempts(failCourse);
        student.getTranscript().incrementFailedAttempts(failCourse);
        assertTrue(student.getTranscript().hasExceededFailedAttempts(failCourse), "Failed attempts limit works",
                "expected failed attempts limit to be exceeded after 3 attempts");

        assertThrows(MarkException.class, "Mismatched mark student/course is rejected",
                () -> teacherService.putMark(student, mainCourse, createMark(notEnrolledStudent, mainCourse, 90, 90, 90)),
                "expected MarkException for mark belonging to a different student");
    }

    private static void checkResearchFlow() {
        module("RESEARCH");
        assertTrue(graduateStudent instanceof Researcher, "Graduate student is a researcher",
                "expected GraduateStudent to implement Researcher");
        assertTrue(teacher instanceof Researcher, "Teacher/professor can be researcher",
                "expected Teacher to implement Researcher");

        Journal localJournal = new Journal("TEST_H_Index_Journal");
        ResearchPaper h1 = new ResearchPaper("TEST H Paper 1", List.of(teacher), localJournal, 5, 10, daysAgo(5), "TEST-H-1");
        ResearchPaper h2 = new ResearchPaper("TEST H Paper 2", List.of(teacher), localJournal, 4, 11, daysAgo(4), "TEST-H-2");
        ResearchPaper h3 = new ResearchPaper("TEST H Paper 3", List.of(teacher), localJournal, 3, 12, daysAgo(3), "TEST-H-3");
        teacher.addPaper(h1);
        teacher.addPaper(h2);
        teacher.addPaper(h3);
        assertTrue(teacher.calculateHIndex() == 3, "H-index is calculated",
                "expected teacher h-index 3 but was " + teacher.calculateHIndex());

        login("TEST_MANAGER");
        assertThrows(InvalidSupervisorEx.class, "Supervisor with h-index below 3 is rejected",
                () -> researchProjectService.assignSupervisor(graduateStudent, graduateStudent),
                "expected InvalidSupervisorEx for low h-index supervisor");
        try {
            researchProjectService.assignSupervisor(graduateStudent, teacher);
            assertTrue(graduateStudent.getSupervisor() == teacher, "Valid supervisor is accepted",
                    "expected graduate student's supervisor to be TEST_TEACHER");
        } catch (InvalidSupervisorEx e) {
            fail("Valid supervisor is accepted", e.getMessage());
        }

        journal = new Journal(JOURNAL_NAME);
        database.addJournal(journal);
        publishedPaper = new ResearchPaper(PAPER_TITLE, List.of(graduateStudent, teacher), journal, 7, 15, new Date(),
                "TEST-DOI-VERIFICATION");

        login("TEST_GRAD_STUDENT");
        researchPaperService.publishPaper(graduateStudent, publishedPaper, journal);
        assertTrue(database.getResearchPapers().contains(publishedPaper), "Researcher can publish paper",
                "expected paper to be in research paper database");
        assertTrue(graduateStudent.getPapers().contains(publishedPaper), "Research paper can be added to researcher",
                "expected graduate student papers to contain verification paper");

        researchProject = new ResearchProject("TEST Research Project");
        database.addResearchProject(researchProject);
        try {
            researchProjectService.joinProject(researchProject, graduateStudent);
            assertTrue(researchProject.getParticipants().contains(graduateStudent), "Research project accepts researchers",
                    "expected project participants to contain graduate student");
        } catch (NotResearcherException e) {
            fail("Research project accepts researchers", e.getMessage());
        }

        assertThrows(NotResearcherException.class, "Non-researcher project join throws custom exception",
                () -> researchService.joinProject(manager, researchProject),
                "expected NotResearcherException for manager joining research project");

        List<ResearchPaper> papers = new ArrayList<>(Arrays.asList(h1, h2, h3));
        papers.sort(new ResearchPaperDateComparator());
        assertTrue(papers.get(0) == h3, "Papers can be sorted by date", "expected newest paper first");
        papers.sort(new ResearchPaperCitationComparator());
        assertTrue(papers.get(0) == h1, "Papers can be sorted by citations", "expected highest cited paper first");
        papers.sort(new ResearchPaperLengthComparator());
        assertTrue(papers.get(0) == h3, "Papers can be sorted by pages", "expected longest paper first");
        assertTrue(researchPaperService.getCitation(publishedPaper, Format.PLAIN_TEXT).contains(PAPER_TITLE),
                "Citation generation works", "expected plain-text citation to contain paper title");
    }

    private static void checkNewsAndJournalFlow() {
        module("NEWS/JOURNALS");
        login("TEST_MANAGER");
        News news = new News("TEST Defense News", "TEST verification news content", NewsTopic.ACADEMIC);
        managerService.addNews(news);
        assertTrue(database.findNewsByTitle("TEST Defense News") == news, "Manager can add news",
                "expected TEST Defense News in database");

        News researchNews = database.findNewsByTitle("New Paper Published: " + PAPER_TITLE);
        assertTrue(researchNews != null && researchNews.isPinned(), "Research news is pinned/prioritized",
                "expected research publication news to exist and be pinned");

        journalService.subscribe(student, journal);
        assertTrue(journal.getSubscribers().contains(student), "User can subscribe to journal",
                "expected TEST_STUDENT in journal subscribers");
        journalService.unsubscribe(student, journal);
        assertTrue(!journal.getSubscribers().contains(student), "User can unsubscribe from journal",
                "expected TEST_STUDENT removed from journal subscribers");
        journalService.subscribe(student, journal);

        int notificationsBefore = student.getNotifications().size();
        ResearchPaper secondPaper = new ResearchPaper("TEST Journal Notification Paper", List.of(teacher), journal,
                2, 9, new Date(), "TEST-DOI-NOTIFY");
        journalService.publishPaper(journal, secondPaper);
        assertTrue(journal.getPapers().contains(secondPaper), "Publishing paper stores it in journal",
                "expected journal to contain second paper");
        assertTrue(student.getNotifications().size() == notificationsBefore + 1, "Publishing paper in journal notifies subscribers",
                "expected subscriber notification count to increase by 1");

        int paperCount = journal.getPapers().size();
        int notificationCount = student.getNotifications().size();
        journalService.publishPaper(journal, secondPaper);
        assertTrue(journal.getPapers().size() == paperCount, "Duplicate publication does not duplicate paper",
                "expected journal paper count to remain " + paperCount);
        assertTrue(student.getNotifications().size() == notificationCount, "Duplicate publication does not duplicate notification",
                "expected notification count to remain " + notificationCount);
        assertTrue(database.findNewsByTitle("New Paper Published: " + secondPaper.getTitle()) != null,
                "Paper publication creates research news", "expected research news for second paper");
    }

    private static void checkTechSupportFlow() {
        module("TECH SUPPORT");
        login("TEST_MANAGER");
        acceptedSupportRequest = techSupportService.submitRequest("TEST Need projector help");
        assertTrue(acceptedSupportRequest != null, "Employee submits tech support request",
                "expected support request to be created");
        assertTrue(acceptedSupportRequest != null && acceptedSupportRequest.getStatus() == RequestStatus.NEW,
                "Initial status is NEW", "expected NEW status");

        login("TEST_SUPPORT");
        assertTrue(techSupportService.viewNewRequests().contains(acceptedSupportRequest), "Tech support specialist can view new requests",
                "expected new requests to contain acceptedSupportRequest");
        TechSupportReq viewed = techSupportService.viewRequest(acceptedSupportRequest.getId());
        assertTrue(viewed == acceptedSupportRequest && acceptedSupportRequest.getStatus() == RequestStatus.VIEWED,
                "Viewing request changes status to VIEWED", "expected VIEWED status");
        assertTrue(techSupportService.acceptRequest(acceptedSupportRequest.getId()), "Specialist can accept request",
                "expected acceptRequest to return true");
        assertTrue(acceptedSupportRequest.getStatus() == RequestStatus.ACCEPTED, "Status becomes ACCEPTED",
                "expected ACCEPTED status");
        assertTrue(techSupportService.markAsDone(acceptedSupportRequest.getId()), "Specialist can mark request as done",
                "expected markAsDone to return true");
        assertTrue(acceptedSupportRequest.getStatus() == RequestStatus.DONE, "Status becomes DONE",
                "expected DONE status");

        login("TEST_MANAGER");
        TechSupportReq rejectedRequest = techSupportService.submitRequest("TEST Reject this request");
        login("TEST_SUPPORT");
        assertTrue(techSupportService.rejectRequest(rejectedRequest.getId(), "TEST invalid duplicate"),
                "Specialist can reject another request with reason", "expected rejectRequest to return true");
        assertTrue(rejectedRequest.getStatus() == RequestStatus.REJECTED && "TEST invalid duplicate".equals(rejectedRequest.getRejectionReason()),
                "Rejected request stores reason", "expected REJECTED status with rejection reason");

        login("TEST_MANAGER");
        assertThrows(SecurityException.class, "Non-specialist cannot accept/reject requests",
                () -> techSupportService.acceptRequest(rejectedRequest.getId()),
                "expected manager accepting request to throw SecurityException");
    }

    private static void checkMessagingFlow() {
        module("MESSAGING");
        login("TEST_MANAGER");
        message = messageService.sendMessage(teacher, "TEST Message for verification");
        assertTrue(message != null, "Employee can send message to another employee",
                "expected message from manager to teacher to be created");

        login("TEST_TEACHER");
        assertTrue(messageService.getInbox().contains(message), "Receiver can see message",
                "expected teacher inbox to contain verification message");
        assertTrue(message != null && message.getSender() == manager && message.getReceiver() == teacher
                        && message.getText().equals("TEST Message for verification") && message.getSentDate() != null,
                "Message contains sender, receiver, text, and date",
                "expected message fields to match sender/receiver/text/date");

        login("TEST_STUDENT");
        assertThrows(SecurityException.class, "Student cannot use employee-only messaging",
                () -> messageService.sendMessage(manager, "TEST forbidden message"),
                "expected student sending employee message to throw SecurityException");
    }

    private static void checkSerializationFlow() {
        module("SERIALIZATION");
        try {
            database.save();
            Database loaded = FileStorage.load();
            Database.setInstance(loaded);

            assertTrue(loaded != null, "Loaded database is not null", "expected FileStorage.load() to return database");
            assertTrue(loaded.findUserByUsername("TEST_ADMIN") != null, "Users persist after save/load",
                    "expected TEST_ADMIN after load");
            assertTrue(loaded.findCourseByCode(COURSE_CODE) != null, "Courses persist after save/load",
                    "expected TEST_CS101 after load");
            assertTrue(loaded.findRegistrationsByStudent(student).stream().anyMatch(r -> r.getCourse().equals(mainCourse)),
                    "Registrations persist after save/load", "expected TEST_STUDENT registration after load");
            Student loadedStudent = (Student) loaded.findUserByUsername("TEST_STUDENT");
            assertTrue(loadedStudent != null && !loadedStudent.getTranscript().getMarks().isEmpty(),
                    "Marks persist after save/load", "expected loaded student transcript to contain marks");
            assertTrue(loaded.findResearchPaperByTitle(PAPER_TITLE) != null, "Research papers persist after save/load",
                    "expected verification research paper after load");
            assertTrue(loaded.findJournalByName(JOURNAL_NAME) != null, "Journals persist after save/load",
                    "expected TEST_Research_Journal after load");
            Journal loadedJournal = loaded.findJournalByName(JOURNAL_NAME);
            assertTrue(loadedJournal != null && !loadedJournal.getSubscribers().isEmpty(),
                    "Journal subscriptions persist after save/load", "expected journal subscribers after load");
            assertTrue(loaded.findTechSupportReqById(acceptedSupportRequest.getId()) != null,
                    "Tech support requests persist after save/load", "expected accepted support request after load");
            assertTrue(!loaded.getLogs().isEmpty(), "Logs persist after save/load", "expected logs after load");

            int nextId = loaded.nextTechSupportReqId();
            int maxId = loaded.getTechSupportReqs().stream().map(TechSupportReq::getId).max(Comparator.naturalOrder()).orElse(0);
            assertTrue(nextId > maxId, "Generated IDs continue correctly after load",
                    "expected next tech support request ID greater than max existing ID " + maxId + " but was " + nextId);

            Database.setInstance(database);
        } catch (Throwable t) {
            fail("Serialization checks complete", t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }

    private static void assertCreditLimit() {
        login("TEST_MANAGER");
        Course c1 = new Course("TEST_LIMIT_1", "Test Credit Limit 1", 6, CourseType.FREE_ELECTIVE, Language.EN,
                "Computer Science", 1, true);
        Course c2 = new Course("TEST_LIMIT_2", "Test Credit Limit 2", 6, CourseType.FREE_ELECTIVE, Language.EN,
                "Computer Science", 1, true);
        Course c3 = new Course("TEST_LIMIT_3", "Test Credit Limit 3", 6, CourseType.FREE_ELECTIVE, Language.EN,
                "Computer Science", 1, true);
        Course c4 = new Course("TEST_LIMIT_4", "Test Credit Limit 4", 1, CourseType.FREE_ELECTIVE, Language.EN,
                "Computer Science", 1, true);
        managerService.addCourseForRegistration(c1);
        managerService.addCourseForRegistration(c2);
        managerService.addCourseForRegistration(c3);
        managerService.addCourseForRegistration(c4);

        login("TEST_STUDENT");
        boolean first = studentService.registerForCourse(c1.getCourseCode());
        boolean second = studentService.registerForCourse(c2.getCourseCode());
        boolean third = studentService.registerForCourse(c3.getCourseCode());
        boolean overLimit = studentService.registerForCourse(c4.getCourseCode());
        assertTrue(first && second && third && !overLimit, "21-credit limit is enforced",
                "expected three 6-credit pending requests after 3 approved credits to pass, then 1 extra credit to fail");
    }

    private static CourseRegistration latestRegistrationFor(Student targetStudent, Course targetCourse) {
        List<CourseRegistration> registrations = database.findRegistrationsByStudent(targetStudent);
        for (int i = registrations.size() - 1; i >= 0; i--) {
            CourseRegistration registration = registrations.get(i);
            if (registration.getCourse().equals(targetCourse)) {
                return registration;
            }
        }
        return null;
    }

    private static Mark createMark(Student targetStudent, Course targetCourse, double first, double second, double finalExam) {
        Mark mark = new Mark(targetStudent, targetCourse);
        mark.setFirstAttestation(first);
        mark.setSecondAttestation(second);
        mark.setFinalExam(finalExam);
        return mark;
    }

    private static void login(String username) {
        User loggedIn = authService.login(username, PASSWORD);
        if (loggedIn == null) {
            throw new IllegalStateException("Could not login as " + username);
        }
    }

    private static void module(String name) {
        System.out.println();
        System.out.println("[" + name + "]");
    }

    private static void pass(String testName) {
        passed++;
        System.out.println("[PASS] " + testName);
    }

    private static void fail(String testName, String reason) {
        failed++;
        System.out.println("[FAIL] " + testName);
        System.out.println("Reason: " + reason);
    }

    private static void assertTrue(boolean condition, String testName, String failReason) {
        try {
            if (condition) {
                pass(testName);
            } else {
                fail(testName, failReason);
            }
        } catch (Throwable t) {
            fail(testName, t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }

    private static void assertThrows(Class<? extends Throwable> expectedType, String testName, ThrowingRunnable action,
                                     String failReason) {
        try {
            action.run();
            fail(testName, failReason);
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                pass(testName);
            } else {
                fail(testName, "expected " + expectedType.getSimpleName() + " but got "
                        + t.getClass().getSimpleName() + ": " + t.getMessage());
            }
        }
    }

    private static void printSummary() {
        System.out.println();
        System.out.println("[SUMMARY]");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Result: " + (failed == 0 ? "SUCCESS" : "FAILED"));
    }

    private static Database createEmptyDatabase() throws Exception {
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
            fail("Restore original serialized database", e.getMessage());
        }
    }

    private static Date daysAgo(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return calendar.getTime();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Throwable;
    }

    private static class VerificationPrintStream extends PrintStream {
        private final PrintStream delegate;

        VerificationPrintStream(PrintStream delegate) {
            super(delegate);
            this.delegate = delegate;
        }

        @Override
        public void println(String line) {
            if (shouldPrint(line)) {
                delegate.println(line);
            }
        }

        @Override
        public void println(Object value) {
            println(String.valueOf(value));
        }

        private boolean shouldPrint(String line) {
            if (line == null || line.isEmpty()) return true;
            return line.startsWith("==========")
                    || line.startsWith("[PASS]")
                    || line.startsWith("[FAIL]")
                    || line.startsWith("Reason:")
                    || line.startsWith("[AUTH]")
                    || line.startsWith("[ADMIN]")
                    || line.startsWith("[COURSE REGISTRATION]")
                    || line.startsWith("[TEACHER ASSIGNMENT]")
                    || line.startsWith("[MARKS]")
                    || line.startsWith("[RESEARCH]")
                    || line.startsWith("[NEWS/JOURNALS]")
                    || line.startsWith("[TECH SUPPORT]")
                    || line.startsWith("[MESSAGING]")
                    || line.startsWith("[SERIALIZATION]")
                    || line.startsWith("[SUMMARY]")
                    || line.startsWith("Passed:")
                    || line.startsWith("Failed:")
                    || line.startsWith("Result:");
        }
    }
}
