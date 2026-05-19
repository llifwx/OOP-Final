package services;

import enums.UrgencyLevel;
import exceptions.MarkException;
import model.academic.Complaint;
import model.academic.Course;
import model.academic.Mark;
import model.academic.Transcript;
import model.users.Student;
import model.users.Teacher;
import model.users.User;
import storage.Database;
import utils.LogRecord;

import java.util.ArrayList;
import java.util.List;

public class TeacherService {
    private static final double FAILING_SCORE = 50.0;
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
        Transcript transcript = student.getTranscript();
        boolean isExistingRecord = transcript.getMarks().contains(mark);
        if (transcript.hasMarkForCourse(course) && !isExistingRecord) {
            System.out.println("[TeacherService] Mark for this student and course already exists.");
            return false;
        }

        boolean isNewMark = !isExistingRecord;
        boolean isFailingMark = mark.getTotalScore() < FAILING_SCORE;
        if (isNewMark && isFailingMark && transcript.hasExceededFailedAttempts(course)) {
            throw new MarkException("Student cannot fail this course more than 3 times.");
        }

        transcript.addMark(mark);
        if (isNewMark && isFailingMark) {
            transcript.incrementFailedAttempts(course);
        }
        student.setGpa(transcript.calculateGpa());
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
