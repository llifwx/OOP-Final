package services;

import enums.UrgencyLevel;
import model.academic.Complaint;
import model.academic.Course;
import model.academic.Mark;
import model.users.Student;
import model.users.Teacher;
import model.users.User;
import storage.Database;
import utils.LogRecord;

public class TeacherService {
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
        database.save();
        log("Teacher assigned self to course: " + course.getCourseCode());
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

        student.getTranscript().addMark(mark);
        student.setGpa(student.getTranscript().calculateGpa());
        database.save();
        log("Put mark for student " + student.getUsername() + " in course " + course.getCourseCode());
        return true;
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
        database.save();
        log("Sent complaint about student " + student.getUsername());
        return complaint;
    }

    private void log(String action) {
        User actor = authService.getCurrentUser();
        if (actor != null) {
            database.addLog(new LogRecord(actor, action));
            database.save();
        }
    }
}
