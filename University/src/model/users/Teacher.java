package model.users;

import enums.Language;
import enums.TeacherType;
import interfaces.Researcher;
import model.academic.Complaint;
import model.academic.Course;
import model.research.ResearchPaper;
import model.research.ResearchProject;
import utils.ResearchMetrics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Teacher extends Employee implements Researcher {
    private static final long serialVersionUID = 1L;

    private TeacherType teacherType;
    private double rating;
    private List<Course> courses;
    private List<Complaint> complaints;
    private List<ResearchPaper> papers;
    private List<ResearchProject> projects;

    public Teacher(String username, String password, String fullName, String email, Language language, String employeeId, String department, double salary, Date hierDate, TeacherType teacherType, double rating, List<Course> courses, List<Complaint> complaints, List<ResearchPaper> papers, List<ResearchProject> projects) {
        super(username, password, fullName, email, language, employeeId, department, salary, hierDate);
        this.teacherType = teacherType;
        this.rating = rating;
        this.courses = courses == null ? new ArrayList<>() : new ArrayList<>(courses);
        this.complaints = complaints == null ? new ArrayList<>() : new ArrayList<>(complaints);
        this.papers = papers == null ? new ArrayList<>() : new ArrayList<>(papers);
        this.projects = projects == null ? new ArrayList<>() : new ArrayList<>(projects);
    }
    
    public TeacherType getTeacherType() {return this.teacherType;}

    public double getRating() {return this.rating;}

    public List<Complaint> getComplaints() {return new ArrayList<>(this.complaints);}

    public List<ResearchPaper> getPapers() {return new ArrayList<>(this.papers);}

    public List<ResearchProject> getProjects() {return new ArrayList<>(this.projects);}

    public List<Course> getCourses() {
        return new ArrayList<>(courses);
    }

    @Override
    public int calculateHIndex() {
        return ResearchMetrics.calculateHIndex(papers);
    }

    public void addCourse(Course course) {
        if (course != null && !courses.contains(course)) {
            courses.add(course);
        }
    }

    public void addComplaint(Complaint complaint) {
        if (complaint != null && !complaints.contains(complaint)) {
            complaints.add(complaint);
        }
    }

    public void addPaper(ResearchPaper paper) {
        if (paper != null && !papers.contains(paper)) {
            papers.add(paper);
        }
    }

    public void addProject(ResearchProject project) {
        if (project != null && !projects.contains(project)) {
            projects.add(project);
        }
    }

    @Override
    public String toString() {
        return "Teacher: " + getUsername() + ". Full name: " + getFullName()
                + ". ID: " + getId() + ". EmployeeID: " + getEmployeeId()
                + ". Department: " + getDepartment() + ". Type: " + teacherType
                + ". Rating: " + rating + ". Courses: " + courses.size()
                + ". Complaints: " + complaints.size() + ". Papers: " + papers.size()
                + ". Projects: " + projects.size() + ".";
    }
}
