package model.users;

import enums.Language;
import enums.TeacherType;
import enums.UrgencyLevel;
import model.academic.Complaint;
import model.academic.Course;
import model.academic.Mark;
import model.social.Journal;
import model.research.ResearchPaper;
import model.research.ResearchProject;
import model.social.Message;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Teacher extends Employee {
    private static final long serialVersionUID = 1L;

    /*TODO : import missing classes*/
    private TeacherType teacherType;
    private double rating;
    private List<Course> courses;
    private List<Complaint> complaints;
    private List<ResearchPaper> papers;
    private List<ResearchProject> projects;

    public Teacher(String username, String password, String fullName, String email, Language language, String employeeId, String department, double salary, Date hierDate, List<Message> inbox, TeacherType teacherType, double rating, List<Course> courses, List<Complaint> complaints, List<ResearchPaper> papers, List<ResearchProject> projects) {
        super(username, password, fullName, email, language, employeeId, department, salary, hierDate, inbox);
        this.teacherType = teacherType;
        this.rating = rating;
        this.courses = courses;
        this.complaints = complaints;
        this.papers = papers;
        this.projects = projects;
    }

    /*TODO: make each list getter with for loop*/
    public TeacherType getTeacherType() {return this.teacherType;}

    public double getRating() {return this.rating;}

    public List<Complaint> getComplaints() {return this.complaints;}

    public List<ResearchPaper> getPapers() {return this.papers;}

    public List<ResearchProject> getProjects() {return this.projects;}

    public List<Course> viewCourses() {
        for (Course course : courses) {
            /*TODO: allocate this to toString Course method*/
            System.out.println("Code: " + course.getCourseCode() + "\n" + "Name: " + course.getName() + "\n" + "Credits: " + course.getCredits() + "\n" + "Course: " + course.getType() + "\n");
        }
        return courses;
    }

    public void manageCourse(Course course) {}

    public void putMark(Student student, Course course, Mark mark) {}

    public List<Student> viewStudents(Course course) {}

    public Complaint sendComplaint(Student student, UrgencyLevel urgency, String text) {}

    public int calculateHIndex() {}

    public void printPapers(Comparator<ResearchPaper> cmp) {}

    public void joinProject(ResearchProject project) {}

    public void publishPaper(ResearchPaper paper, Journal journal) {}

    @Override
    public String toString() {
        return "Teacher's type: " + this.getTeacherType() + "\n" + "Rating: " + this.getRating() + "\n" + "Complaints: " + this.getComplaints() + "\n" + "Papers: " + this.getPapers() + "\n" + "Projects: " + this.getProjects() + "\n";
    }
}