package model.users;

import enums.Language;
import model.academic.Course;
import model.academic.Mark;
import model.academic.Transcript;
import model.academic.Complaint;
import model.social.StudentOrganization;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Student extends User {

    private String studentId;
    private String school;
    private String major;
    private String minor;
    private int yearOfStudy;
    private double gpa;
    private int credits;
    private int failedCoursesCount;
    private List<Course> registeredCourses;
    private Transcript transcript;
    private List<StudentOrganization> organizations;

    public Student(String username, String password, String fullName, String email,
                   Language language, String studentId, String school,
                   String major, int yearOfStudy) {
        super(username, password, fullName, email, language);
        this.studentId = studentId;
        this.school = school;
        this.major = major;
        this.yearOfStudy = yearOfStudy;
        this.gpa = 0.0;
        this.credits = 0;
        this.failedCoursesCount = 0;
        this.registeredCourses = new ArrayList<>();
        this.organizations = new ArrayList<>();
        this.transcript = new Transcript(this);
    }

    public void registerForCourse(Course course) {}
    public void dropCourse(Course course) {}
    public List<Course> viewCourses() { return registeredCourses; }
    public List<Mark> viewMarks() { return transcript.getMarks(); }
    public Transcript viewTranscript() { return transcript; }
    public Transcript getTranscript() { return transcript; }
    public Teacher viewTeacherInfo(Course course) { return null; }
    public void rateTeacher(Teacher teacher, int rate) {}
    public void joinOrganization(StudentOrganization org) {}

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getMinor() { return minor; }
    public void setMinor(String minor) { this.minor = minor; }

    public int getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(int yearOfStudy) { this.yearOfStudy = yearOfStudy; }

    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public int getFailedCoursesCount() { return failedCoursesCount; }
    public void setFailedCoursesCount(int count) { this.failedCoursesCount = count; }

    public List<StudentOrganization> getOrganizations() { return organizations; }

    @Override
    public String toString() {
        return "Student: " + getUsername() + ". " +
                "Full name: " + getFullName() + ". " +
                "ID: " + getId() + ". " +
                "School: " + school + ". " +
                "Major: " + major + ". " +
                "GPA: " + gpa + ". " +
                "Credits: " + credits + ".";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Student student)) return false;
        return Objects.equals(studentId, student.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }
}