package model.academic;

import model.users.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Transcript {
    private Student student;
    private List<Mark> marks;

    public Transcript(Student student) {
        this.student = student;
        this.marks = new ArrayList<>();
    }

    public void addMark(Mark mark) {}
    public double calculateGpa() { return 0.0; }
    public void printTranscript() {}

    public Student getStudent() {return student;}

    public void setStudent(Student student) {this.student = student;}

    //TODO:refactor mark getter and setter
    public List<Mark> getMarks() {return marks;}

    public void setMarks(List<Mark> marks) {this.marks = marks;}

    @Override
    public String toString() {
        return "Transcript of: " + getStudent().getFullName() + ". " +
                "Total marks: " + marks.size() + ". " +
                "GPA: " + calculateGpa() + ".";
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