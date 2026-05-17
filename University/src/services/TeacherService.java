package services;

import enums.UrgencyLevel;
import exceptions.MarkException;
import model.academic.Complaint;
import model.academic.Course;
import model.academic.Mark;
import model.users.Student;
import model.users.Teacher;
import model.users.User;
import storage.Database;
import utils.LogRecord;

import java.util.ArrayList;
import java.util.List;

public class TeacherService {
    private static final double FAILING_SCORE = 50.0;
    private static final int MAX_FAILED_COURSES = 3;
    private final Database database;
    private final AuthService authService;

    public TeacherService(Database database, AuthService authService) {
        this.database = database;
        this.authService = authService;
    }

    private Teacher requireTeacher() {
        User current = authService.getCurrentUser();
        if (!(current instanceof Teacher teacher)) {
            throw new SecurityException("[TeacherService] Access denied: current user is not a Teacher.");
        }
        return teacher;
    }

    public void assignCourse(Course course) {
        Teacher teacher = requireTeacher();
        if (course == null) {
            System.out.println("[TeacherService] Course is null.");
            return;
        }

        teacher.addCourse(course);
        course.addInstructor(teacher);
        log("Teacher assigned self to course: " + course.getCourseCode());
        database.save();
    }

    public boolean putMark(Student student, Course course, Mark mark) {
        Teacher teacher = requireTeacher();
        if (student == null || course == null || mark == null) {
            System.out.println("[TeacherService] Student, course, or mark is null.");
            return false;
        }
        if (!teacher.getCourses().contains(course)) {
            System.out.println("[TeacherService] Teacher is not assigned to this course.");
            return false;
        }
        if (!course.getEnrolledStudents().contains(student)) {
            System.out.println("[TeacherService] Student is not enrolled in this course.");
            return false;
        }
        if (!student.equals(mark.getStudent()) || !course.equals(mark.getCourse())) {
            throw new MarkException("Mark must belong to the same student and course.");
        }
        boolean isNewMark = !student.getTranscript().getMarks().contains(mark);
        boolean isFailingMark = mark.getTotalScore() < FAILING_SCORE;
        if (isNewMark && isFailingMark && student.getFailedCoursesCount() >= MAX_FAILED_COURSES) {
            throw new MarkException("Student already has 3 failed courses.");
        }

        student.getTranscript().addMark(mark);
        if (isNewMark && isFailingMark) {
            student.setFailedCoursesCount(student.getFailedCoursesCount() + 1);
        }
        student.setGpa(student.getTranscript().calculateGpa());
        log("Put mark for student " + student.getUsername() + " in course " + course.getCourseCode());
        database.save();
        return true;
    }

    public List<Student> viewStudents(Course course) {
        requireTeacher();
        if (course == null) {
            return new ArrayList<>();
        }
        return course.getEnrolledStudents();
    }

    public Complaint sendComplaint(Student student, UrgencyLevel urgency, String text) {
        Teacher teacher = requireTeacher();
        if (student == null || urgency == null || text == null || text.isBlank()) {
            System.out.println("[TeacherService] Student, urgency, and text are required.");
            return null;
        }

        Complaint complaint = new Complaint(teacher, student, urgency, text);
        teacher.addComplaint(complaint);
        database.addComplaint(complaint);
        log("Sent complaint about student " + student.getUsername());
        database.save();
        return complaint;
    }

    private void log(String action) {
        User actor = authService.getCurrentUser();
        if (actor != null) {
            database.addLog(new LogRecord(actor, action));
        }
    }
}
