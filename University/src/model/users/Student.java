package model.users;

import enums.Language;
import model.academic.Course;
import model.academic.Mark;
import model.academic.Transcript;
import model.academic.StudentOrganization;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Student extends User {
    private static final long serialVersionUID = 1L;

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

    public Student(String username, String password, String fullName, String email, Language language, String studentId, String school, String major, int yearOfStudy) {
        super(username, password, fullName, email, language);
        this.studentId = studentId;
        this.school = school;
        this.major = major;
        setYearOfStudy(yearOfStudy);
        this.gpa = 0.0;
        this.credits = 0;
        this.failedCoursesCount = 0;
        this.registeredCourses = new ArrayList<>();
        this.organizations = new ArrayList<>();
        this.transcript = new Transcript(this);
    }

    public List<Course> viewCourses() {return new ArrayList<>(registeredCourses);}

    public List<Mark> viewMarks() {return transcript.getMarks();}

    public Transcript viewTranscript() {return transcript;}

    public Transcript getTranscript() {return transcript;}

    public Teacher viewTeacherInfo(Course course) {
        if (course == null || !registeredCourses.contains(course) || course.getInstructors().isEmpty()) {
            return null;
        }
        return course.getInstructors().get(0);
    }

    public String getStudentId() {return this.studentId;}

    public String getSchool() {return this.school;}

    public String getMajor() {return this.major;}

    public int getYearOfStudy() {return this.yearOfStudy;}

    public String getMinor() {return this.minor;}

    public double getGpa() {return this.gpa;}

    public int getCredits() {return this.credits;}

    public int getFailedCoursesCount() {return this.failedCoursesCount;}

    public List<StudentOrganization> getOrganizations() {return new ArrayList<>(this.organizations);}

    public List<Course> getRegisteredCourses() {return new ArrayList<>(registeredCourses);}

    public void addRegisteredCourse(Course course) {
        if (course != null && !registeredCourses.contains(course)) {
            registeredCourses.add(course);
        }
    }

    public void removeRegisteredCourse(Course course) {
        registeredCourses.remove(course);
    }

    public void setStudentId(String studentId) {this.studentId = studentId;}

    public void setGpa(double gpa) {
        if (gpa < 0 || gpa > 4.0) {
            throw new IllegalArgumentException("GPA must be between 0 and 4.0");
        }
        this.gpa = gpa;
    }

    public void setCredits(int credits) {
        if (credits < 0) {
            throw new IllegalArgumentException("Credits cannot be negative");
        }
        this.credits = credits;
    }

    public void setYearOfStudy(int yearOfStudy) {
        if (yearOfStudy < 1) {
            throw new IllegalArgumentException("Year of study must be positive");
        }
        this.yearOfStudy = yearOfStudy;
    }

    public void setMinor(String minor) {this.minor = minor;}

    public void setMajor(String major) {this.major = major;}

    public void setSchool(String school) {this.school = school;}

    public void setFailedCoursesCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Failed courses count cannot be negative");
        }
        this.failedCoursesCount = count;
    }

    @Override
    public String toString() {
        return "Student: " + getUsername() + ". " + "Full name: " + getFullName() + ". " + "ID: " + getId() + ". " + "School: " + school + ". " + "Major: " + major + ". " + "GPA: " + gpa + ". " + "Credits: " + credits + ".";
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
