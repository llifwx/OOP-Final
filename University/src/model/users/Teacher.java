package model.users;

import enums.Language;
import enums.TeacherType;
import interfaces.Researcher;
import model.academic.Complaint;
import model.academic.Course;
import model.research.ResearchPaper;
import model.research.ResearchProject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
        if (papers == null || papers.isEmpty()) return 0;

        List<Integer> citations = papers.stream()
                .map(ResearchPaper::getCitations)
                .sorted((a, b) -> b - a)
                .collect(Collectors.toList());
        int h = 0;
        for (int i = 0; i < citations.size(); i++) {
            if (citations.get(i) >= i + 1) h = i + 1;
            else break;
        }
        return h;
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
        return "Teacher's type: " + this.getTeacherType() + "\n" + "Rating: " + this.getRating() + "\n" + "Complaints: " + this.getComplaints() + "\n" + "Papers: " + this.getPapers() + "\n" + "Projects: " + this.getProjects() + "\n";
    }
}
