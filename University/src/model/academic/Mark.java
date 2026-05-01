package model.academic;

import model.users.Student;

import java.io.Serializable;
import java.util.Objects;

public class Mark implements Serializable {
    private static final long serialVersionUID = 1L;

    private static int idCounter = 0;
    private final int id;

    private Student student;
    private Course course;

    private double firstAttestation;
    private double secondAttestation;
    private double finalExam;
    private double totalScore;

    public Mark(Student student, Course course) {
        this.id = ++idCounter;
        this.student = student;
        this.course = course;
    }

    public int getId() {return id;}

    public Student getStudent() {return student;}

    public Course getCourse() {return course;}

    public double getFirstAttestation() {return firstAttestation;}

    public double getSecondAttestation() {return secondAttestation;}

    public double getFinalExam() {return finalExam;}

    public double getTotalScore() {return totalScore;}

    public void setFirstAttestation(double score) {
        this.firstAttestation = score;
        calculateTotal();
    }

    public void setSecondAttestation(double score) {
        this.secondAttestation = score;
        calculateTotal();
    }

    public void setFinalExam(double score) {
        this.finalExam = score;
        calculateTotal();
    }

    private void calculateTotal() {
        this.totalScore = (firstAttestation * 0.3) + (secondAttestation * 0.3) + (finalExam * 0.4);
    }

    @Override
    public String toString() {
        return "Mark{" + "id=" + id + ", student=" + (student != null ? student.getFullName() : "N/A") + ", course=" + (course != null ? course.getCourseCode() : "N/A") + ", totalScore=" + String.format("%.2f", totalScore) + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mark mark)) return false;
        return id == mark.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}