package services;

import comparator.StudentGpaComparator;
import comparator.TeacherNameComparator;
import enums.RegistrationStatus;
import enums.LessonType;
import model.academic.Course;
import model.academic.CourseRegistration;
import model.academic.Lesson;
import model.academic.Report;
import model.social.News;
import model.users.*;
import storage.Database;
import utils.LogRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManagerService {
    private static final int MAX_STUDENT_CREDITS = 21;

    private final Database database;
    private final AuthService authService;
    private final ReportService reportService;

    public ManagerService(Database database, AuthService authService, ReportService reportService) {
        this.database = database;
        this.authService = authService;
        this.reportService = reportService;
    }

    // Helper for auth checking.
    private Manager requireManager() {
        User current = authService.getCurrentUser();
        if (!(current instanceof Manager manager)) {
            throw new SecurityException("[Manager Service] : Access denied.");
        }
        return manager;
    }

    // Academic management.
    public void assignCourseToTeacher(String courseCode, String teacherUsername, LessonType lessonType) {
        requireManager();

        if (lessonType == null) {
            System.out.println("[Manager Service] : Lesson type is required.");
            return;
        }

        Course course = database.findCourseByCode(courseCode);
        if (course == null) {
            System.out.println("Course with code '" + courseCode + "' not found.");
            return;
        }

        User user = database.findUserByUsername(teacherUsername);
        if (!(user instanceof Teacher teacher)) {
            System.out.println("Teacher with username '" + teacherUsername + "' not found.");
            return;
        }

        if (course.getInstructorsByLessonType(lessonType).contains(teacher)) {
            System.out.println("[Manager Service] : Teacher " + teacher.getFullName() + " is already assigned to course " + course.getName() + " as " + lessonType + ".");
            return;
        }

        course.addInstructor(teacher, lessonType);
        teacher.addCourse(course);

        log("Assigned teacher " + teacher.getFullName() + " to course " + course.getName() + " as " + lessonType);

        database.save();

        System.out.println("[Manager Service] : Teacher " + teacher.getFullName() + " assigned to course " + course.getName() + " as " + lessonType + ".");
    }

    public List<CourseRegistration> getPendingRegistrations() {
        requireManager();
        return new ArrayList<>(database.findRegistrationsByStatus(RegistrationStatus.PENDING));
    }

    public boolean approveRegistration(int registrationId) {
        Manager manager = requireManager();

        CourseRegistration registration = database.findCourseRegistrationById(registrationId);
        if (registration == null) {
            System.out.println("[Manager Service] : Registration request not found.");
            return false;
        }
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            System.out.println("[Manager Service] : Registration request has already been reviewed.");
            return false;
        }

        Student student = registration.getStudent();
        Course course = registration.getCourse();
        if (student == null || course == null) {
            System.out.println("[Manager Service] : Approve failed. Student or course is null.");
            return false;
        }

        if (student.getCredits() + course.getCredits() > MAX_STUDENT_CREDITS) {
            System.out.println("[Manager Service] : Cannot approve registration. Credit limit exceeded for student " + student.getFullName() + ". Limit: " + MAX_STUDENT_CREDITS + ", Current: " + student.getCredits() + ", Course: " + course.getCredits());
            return false;
        }
        if (course.getEnrolledStudents().contains(student)) {
            System.out.println("[Manager Service] : Student " + student.getFullName() + " is already enrolled in course " + course.getName() + ".");
            return false;
        }
        if (course.getEnrolledStudents().size() >= course.getCapacity()) {
            System.out.println("[Manager Service] : Cannot approve registration. Course " + course.getName() + " is full. Capacity: " + course.getCapacity() + ".");
            return false;
        }

        registerStudentToCourse(student, course);

        student.setCredits(student.getCredits() + course.getCredits());
        registration.approve(manager);

        log("Approved registration for student " + student.getFullName() + " in course " + course.getName());
        database.save();
        System.out.println("[Manager Service] : Registration approved for student " + student.getFullName() + " in course " + course.getName() + ".");

        return true;
    }

    public boolean rejectRegistration(int registrationId, String reason) {
        Manager manager = requireManager();

        CourseRegistration registration = database.findCourseRegistrationById(registrationId);
        if (registration == null) {
            System.out.println("[Manager Service] : Registration request not found.");
            return false;
        }
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            System.out.println("[Manager Service] : Registration request has already been reviewed.");
            return false;
        }
        if (reason == null || reason.trim().isEmpty()) {
            System.out.println("[Manager Service] : Rejection reason cannot be blank.");
            return false;
        }

        registration.reject(manager, reason.trim());
        log("Rejected registration request " + registrationId + ": " + reason.trim());
        database.save();
        System.out.println("[Manager Service] : Registration request rejected.");
        return true;
    }


    public void createCourse(Course course) {
        requireManager();

        if (course == null) {
            System.out.println("[Manager Service] : Course is null.");
            return;
        }

        if (database.findCourseByCode(course.getCourseCode()) != null) {
            System.out.println("[Manager Service] : Course with code '" + course.getCourseCode() + "' already exists.");
            return;
        }

        course.setOpenForRegistration(false);

        database.addCourse(course);
        log("Created course: " + course.getName());
        database.save();

        System.out.println("[Manager Service] : Course '" + course.getName() + "' created.");
    }

    public void openCourseForRegistration(String courseCode, String intendedMajor, int intendedYear) {
        requireManager();

        Course course = database.findCourseByCode(courseCode);

        if (course == null) {
            System.out.println("[Manager Service] : Course with code '" + courseCode + "' not found.");
            return;
        }

        if (course.isOpenForRegistration()) {
            System.out.println("[Manager Service] : Course '" + course.getName() + "' is already open for registration.");
            return;
        }

        try {
            course.setIntendedMajor(intendedMajor);
            course.setIntendedYear(intendedYear);
            course.setOpenForRegistration(true);
        } catch (IllegalArgumentException e) {
            System.out.println("[Manager Service] : " + e.getMessage());
            return;
        }

        log("Opened course for registration: " + course.getName());
        database.save();

        System.out.println("[Manager Service] : Course '" + course.getName() + "' opened for registration.");
    }

    public boolean addLessonToCourse(Course course, Lesson lesson) {
        requireManager();
        if (course == null || lesson == null) {
            System.out.println("[Manager Service] : Course or lesson is null.");
            return false;
        }
        if (hasLessonConflict(course, lesson)) {
            System.out.println("[Manager Service] : Lesson conflicts with existing course schedule.");
            return false;
        }

        course.addLesson(lesson);
        database.addLesson(lesson);
        database.save();
        log("Added lesson to course " + course.getCourseCode());
        return true;
    }

    private boolean hasLessonConflict(Course course, Lesson lesson) {
        return course.getLessons()
                .stream()
                .anyMatch(existing -> Objects.equals(existing.getDayOfWeek(), lesson.getDayOfWeek()) && Objects.equals(existing.getTimeSlot(), lesson.getTimeSlot()) && Objects.equals(existing.getRoom(), lesson.getRoom()));
    }

    private void registerStudentToCourse(Student student, Course course) {
        if (student == null || course == null) return;
        student.addRegisteredCourse(course);
        course.enrollStudent(student);
    }

    public boolean unregisterStudentFromCourse(Student student, Course course) {
        requireManager();
        if (student == null || course == null) {
            System.out.println("[Manager Service] : Student or course is null.");
            return false;
        }
        if (!student.getRegisteredCourses().contains(course)) {
            System.out.println("[Manager Service] : Student is not registered for this course.");
            return false;
        }
        student.removeRegisteredCourse(course);
        course.removeStudent(student);
        student.setCredits(Math.max(0, student.getCredits() - course.getCredits()));
        database.save();
        log("Unregistered student " + student.getFullName() + " from course " + course.getName());
        return true;
    }

    public Report createAcademicReport(List<Student> students) {
        requireManager();
        return reportService.createAcademicReport(students);
    }

    public void addNews(News news) {
        requireManager();

        if (news == null) {
            System.out.println("[Manager Service] : News is null.");
            return;
        }
        if (database.findNewsByTitle(news.getTitle()) != null) {
            System.out.println("[Manager Service] : News with title '" + news.getTitle() + "' already exists.");
            return;
        }

        database.addNews(news);
        log("Added news: " + news.getTitle());
        database.save();
        System.out.println("[Manager Service] : News '" + news.getTitle() + " added");
    }

    public boolean removeNews(String title) {
        requireManager();

        News news = database.findNewsByTitle(title);
        if (news == null) {
            System.out.println("[Manager Service] : News with title '" + title + "' not found.");
            return false;
        }

        database.getNews().remove(news);
        log("Removed news: " + title);
        database.save();
        System.out.println("[Manager Service] : News '" + title + "' removed.");
        return true;
    }

    public void pinNews(String title) {
        requireManager();

        News news = database.findNewsByTitle(title);
        if (news == null) {
            System.out.println("[Manager Service] : News with title '" + title + "' not found.");
            return;
        }

        news.pin();
        log("Pinned news: " + title);
        database.save();
        System.out.println("[Manager Service] : News '" + title + "' pinned.");
        return;
    }


    // Sorted views.
    public List<Student> viewStudentsSortedByGpa(List<Student> students) {
        requireManager();

        if (students == null) {
            return new ArrayList<>();
        }

        List<Student> sortedStudents = new ArrayList<>(students);
        sortedStudents.sort(new StudentGpaComparator());

        return sortedStudents;
    }

    public List<Teacher> viewTeachersAlphabetically(List<Teacher> teachers) {
        requireManager();

        if (teachers == null) {
            return new ArrayList<>();
        }

        List<Teacher> sortedTeachers = new ArrayList<>(teachers);
        sortedTeachers.sort(new TeacherNameComparator());

        return sortedTeachers;
    }


    // Helpers for methods.
    private void log(String action) {
        User actor = authService.getCurrentUser();
        if (actor != null) {
            database.addLog(new LogRecord(actor, action));
        }
    }
}
