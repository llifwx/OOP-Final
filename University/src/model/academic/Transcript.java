package model.academic;

import model.users.Student;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import utils.GradeScale;

public class Transcript implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int MAX_FAILED_ATTEMPTS_PER_COURSE = 3;

    private Student student;
    private List<Mark> marks;
    private Map<Course, Integer> failedAttemptsByCourse;

    public Transcript(Student student) {
        this.student = student;
        this.marks = new ArrayList<>();
        this.failedAttemptsByCourse = new HashMap<>();
    }

    public void addMark(Mark mark) {
        if (mark != null && !hasMarkForStudentAndCourse(mark.getStudent(), mark.getCourse())) {
            marks.add(mark);
        }
    }

    public boolean hasMarkForCourse(Course course) {
        return hasMarkForStudentAndCourse(student, course);
    }

    public double calculateGpa() {
        if (marks.isEmpty()) return 0.0;

        double total = 0.0;
        for (Mark mark : marks) {
            total += GradeScale.scoreToGpa(mark.getTotalScore());
        }
        return total / marks.size();
    }

    public int getFailedAttempts(Course course) {
        if (course == null) return 0;
        return ensureFailedAttemptsByCourse().getOrDefault(course, 0);
    }

    public void incrementFailedAttempts(Course course) {
        if (course == null) return;
        Map<Course, Integer> attempts = ensureFailedAttemptsByCourse();
        attempts.put(course, attempts.getOrDefault(course, 0) + 1);
    }

    public boolean hasExceededFailedAttempts(Course course) {
        return getFailedAttempts(course) >= MAX_FAILED_ATTEMPTS_PER_COURSE;
    }

    public int countFailedCourses() {
        return ensureFailedAttemptsByCourse().size();
    }

    public Student getStudent() {return student;}

    public void setStudent(Student student) {this.student = student;}

    public List<Mark> getMarks() {return new ArrayList<>(marks);}

    private boolean hasMarkForStudentAndCourse(Student student, Course course) {
        return marks.stream().anyMatch(existing ->
                existing != null
                        && Objects.equals(existing.getStudent(), student)
                        && Objects.equals(existing.getCourse(), course)
        );
    }

    private Map<Course, Integer> ensureFailedAttemptsByCourse() {
        if (failedAttemptsByCourse == null) {
            failedAttemptsByCourse = new HashMap<>();
            for (Mark mark : marks) {
                if (mark != null && mark.getCourse() != null && mark.getTotalScore() < 50.0) {
                    Course course = mark.getCourse();
                    failedAttemptsByCourse.put(course, failedAttemptsByCourse.getOrDefault(course, 0) + 1);
                }
            }
        }
        return failedAttemptsByCourse;
    }

    public void setMarks(List<Mark> marks) {
        this.marks = new ArrayList<>();
        if (marks != null) {
            for (Mark mark : marks) {
                addMark(mark);
            }
        }
        this.failedAttemptsByCourse = null;
    }

    @Override
    public String toString() {
        String studentName = student == null ? "N/A" : student.getFullName();
        return "Transcript of: " + studentName + ". " + "Total marks: " + marks.size() + ". " + "GPA: " + calculateGpa() + ".";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Transcript t)) return false;
        return Objects.equals(student, t.student);
    }

    @Override
    public int hashCode() {
        return Objects.hash(student);
    }
}
