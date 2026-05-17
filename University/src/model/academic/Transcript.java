package model.academic;

import model.users.Student;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import utils.GradeScale;

public class Transcript implements Serializable {
    private static final long serialVersionUID = 1L;

    private Student student;
    private List<Mark> marks;

    public Transcript(Student student) {
        this.student = student;
        this.marks = new ArrayList<>();
    }

    public void addMark(Mark mark) {
        if (mark != null && !marks.contains(mark)) {
            marks.add(mark);
        }
    }

    public double calculateGpa() {
        if (marks.isEmpty()) return 0.0;

        double total = 0.0;
        for (Mark mark : marks) {
            total += GradeScale.scoreToGpa(mark.getTotalScore());
        }
        return total / marks.size();
    }

    public Student getStudent() {return student;}

    public void setStudent(Student student) {this.student = student;}

    public List<Mark> getMarks() {return new ArrayList<>(marks);}

    public void setMarks(List<Mark> marks) {
        this.marks = marks == null ? new ArrayList<>() : new ArrayList<>(marks);
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
