package model.academic;

import model.users.Student;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            total += scoreToGpa(mark.getTotalScore());
        }
        return total / marks.size();
    }

    public void printTranscript() {
        System.out.println(toString());
        for (Mark mark : marks) {
            System.out.println(mark);
        }
    }

    public Student getStudent() {return student;}

    public void setStudent(Student student) {this.student = student;}

    //TODO:refactor mark getter and setter
    public List<Mark> getMarks() {return marks;}

    public void setMarks(List<Mark> marks) {this.marks = marks;}

    private double scoreToGpa(double score) {
        if (score >= 94.5) return 4.0;
        if (score >= 89.5) return 3.67;
        if (score >= 84.5) return 3.33;
        if (score >= 79.5) return 3.0;
        if (score >= 74.5) return 2.67;
        if (score >= 69.5) return 2.33;
        if (score >= 64.5) return 2.0;
        if (score >= 59.5) return 1.67;
        if (score >= 54.5) return 1.33;
        if (score >= 49.5) return 1.0;
        if (score >= 44.5) return 0.67;
        if (score >= 39.5) return 0.33;
        return 0.0;
    }

    @Override
    public String toString() {
        return "Transcript of: " + getStudent().getFullName() + ". " + "Total marks: " + marks.size() + ". " + "GPA: " + calculateGpa() + ".";
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
