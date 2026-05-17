package services;

import comparator.StudentGpaComparator;
import comparator.TeacherNameComparator;
import enums.LessonType;
import model.academic.Course;
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
    private final Database database;
    private final AuthService authService;
    private final ReportService reportService;

    public ManagerService(Database database, AuthService authService, ReportService reportService) {
        this.database = database;
        this.authService = authService;
        this.reportService = reportService;
    }

    // Helper for auth checking.
    private void requireManager() {
        User current = authService.getCurrentUser();
        if (!(current instanceof Manager)) {
            throw new SecurityException("[Manager Service] : Access denied.");
        }
    }

    // Academic management.
    public void assignCourseToTeacher(String courseCode, int teacherId, LessonType lessonType) {
        requireManager();

        Course course = database.findCourseByCode(courseCode);
        if (course == null) {
            System.out.println("Course with code '" + courseCode + "' not found.");
            return;
        }

        User user = database.findUserById(teacherId);
        if (!(user instanceof Teacher teacher)) {
            System.out.println("Teacher with ID '" + teacherId + "' not found.");
            return;
        }

        if (lessonType == LessonType.LECTURE) {
            course.addInstructor(teacher);
        }

        teacher.addCourse(course);
        database.save();

        log("Assigned teacher" + teacher.getFullName() + " to course " + course.getName() + " as " + lessonType);
        System.out.println("[Manager Service] : Teacher " + teacher.getFullName() + " assigned to course " + course.getName() + " as " + lessonType + ".");
    }

    public boolean approveRegistration(Student student, Course course) {
        requireManager();

        if (student == null || course == null) {
            System.out.println("[Manager Service] : Approve failed. Student or course is null.");
            return false;
        }

        if (student.getCredits() + course.getCredits() > 21) {
            System.out.println("[Manager Service] : Cannot approve registration. Credit limit exceeded for student " + student.getFullName() + ". Limit: 21, Current: " + student.getCredits() + ", Course: " + course.getCredits());
            return false;
        }
        if (course.getEnrolledStudents().contains(student)) {
            System.out.println("[Manager Service] : Student " + student.getFullName() + " is already enrolled in course " + course.getName() + ".");
            return false;
        }

        registerStudentToCourse(student, course);

        student.setCredits(student.getCredits() + course.getCredits());

        database.save();
        log("Approved registration for student " + student.getFullName() + " in course " + course.getName());
        System.out.println("[Manager Service] : Registration approved for student " + student.getFullName() + " in course " + course.getName() + ".");

        return true;
    }


    public void addCourseForRegistration(Course course) {
        requireManager();

        if (course == null) {
            System.out.println("[Manager Service] : Course is null.");
            return;
        }

        if (database.findCourseByCode(course.getCourseCode()) != null) {
            System.out.println("[Manager Service] : Course with code '" + course.getCourseCode() + "' already exists.");
            return;
        }

        database.addCourse(course);
        database.save();
        log("Added course for registration: " + course.getName());
        System.out.println("[Manager Service] : Course '" + course.getName() + "' added for registration.");
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
        return course.getLessons().stream().anyMatch(existing ->
                Objects.equals(existing.getDayOfWeek(), lesson.getDayOfWeek())
                        && Objects.equals(existing.getTimeSlot(), lesson.getTimeSlot())
                        && Objects.equals(existing.getRoom(), lesson.getRoom()));
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
        database.save();
        log("Added news: " + news.getTitle());
        System.out.println("[Manager Service] : News '" + news.getTitle() + " added");
    }

    public boolean removeNews(String title) {
        requireManager();

        News news = database.findNewsByTitle(title);
        if (news == null) {
            System.out.println("[Manager Service] : News with title '" + title + "' not found.");
            return false;
        }

        database.removeNews(news);
        database.save();
        log("Removed news: " + title);
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
        database.save();
        log("Pinned news: " + title);
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
            database.save();
        }
    }
}
