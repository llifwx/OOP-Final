package model.academic;

import enums.UrgencyLevel;
import model.users.Student;
import model.users.Teacher;

import java.util.Date;
import java.util.Objects;

public class Complaint {

    private static int idCnt = 1;
    private int id;
    private Teacher teacher;
    private Student student;
    private UrgencyLevel urgency;
    private String text;
    private Date date;

    public Complaint(Teacher teacher, Student student, UrgencyLevel urgency, String text) {
        this.id = idCnt++;
        this.teacher = teacher;
        this.student = student;
        this.urgency = urgency;
        this.text = text;
        this.date = new Date();
    }

    public int getId() {return this.id;}

    public String getSummary() {return "";}

    public Teacher getTeacher() {return teacher;}

    public void setTeacher(Teacher teacher) {this.teacher = teacher;}

    public Student getStudent() {return student;}

    public void setStudent(Student student) {this.student = student;}

    public UrgencyLevel getUrgency() {return urgency;}

    public void setUrgency(UrgencyLevel urgency) {this.urgency = urgency;}

    public String getText() {return text;}

    public void setText(String text) {this.text = text;}

    public Date getDate() {return date;}

    public void setDate(Date date) {this.date = date;}

    @Override
    public String toString() {
        return "Complaint. " + "Teacher: " + teacher.getFullName() + ". " + "Student: " + student.getFullName() + ". " + "Urgency: " + urgency + ". " + "Date: " + date + ".";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Complaint c)) return false;
        return Objects.equals(teacher, c.teacher) && Objects.equals(student, c.student) && Objects.equals(date, c.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacher, student, date);
    }
}