package model.users;

import enums.Language;
import enums.TeacherType;
import interfaces.Researcher;
import model.academic.Complaint;
import model.academic.Course;
import model.research.ResearchPaper;
import model.research.ResearchProject;

import java.util.ArrayList;
import java.util.Comparator;
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
        this.courses = courses;
        this.complaints = complaints;
        this.papers = papers;
        this.projects = projects;
    }
    
    public TeacherType getTeacherType() {return this.teacherType;}

    public double getRating() {return this.rating;}

    public List<Complaint> getComplaints() {return this.complaints;}

    public List<ResearchPaper> getPapers() {return this.papers;}

    public List<ResearchProject> getProjects() {return this.projects;}

    public List<Course> getCourses() {
        return courses;
    }

    public List<Student> viewStudents(Course course) {return course == null ? null : course.getEnrolledStudents();}

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

    @Override
    public void printPapers(Comparator<ResearchPaper> cmp) {
        List<ResearchPaper> sorted = new ArrayList<>(papers);
        sorted.sort(cmp);
        for (ResearchPaper paper : sorted) {
            System.out.println("- " + paper.getTitle() + " | Citations: " + paper.getCitations());
        }
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
