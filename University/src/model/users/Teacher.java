package model.users;

import enums.Language;
import enums.TeacherType;
import enums.UrgencyLevel;
import interfaces.Researcher;
import model.academic.Complaint;
import model.academic.Course;
import model.academic.Mark;
import model.social.Journal;
import model.research.ResearchPaper;
import model.research.ResearchProject;
import storage.Database;

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

    public void manageCourse(Course course) {
        addCourse(course);
    }

    public void putMark(Student student, Course course, Mark mark) {
        if (student == null || course == null || mark == null) return;
        if (!course.getEnrolledStudents().contains(student)) return;

        student.getTranscript().addMark(mark);
        student.setGpa(student.getTranscript().calculateGpa());
        Database.getInstance().save();
    }

    public List<Student> viewStudents(Course course) {return course == null ? null : course.getEnrolledStudents();}

    public Complaint sendComplaint(Student student, UrgencyLevel urgency, String text) {
        if (student == null || urgency == null || text == null || text.isBlank()) return null;

        Complaint complaint = new Complaint(this, student, urgency, text);
        complaints.add(complaint);
        Database.getInstance().addComplaint(complaint);
        Database.getInstance().save();
        return complaint;
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

    @Override
    public void printPapers(Comparator<ResearchPaper> cmp) {
        List<ResearchPaper> sorted = new ArrayList<>(papers);
        sorted.sort(cmp);
        for (ResearchPaper paper : sorted) {
            System.out.println("- " + paper.getTitle() + " | Citations: " + paper.getCitations());
        }
    }

    @Override
    public void joinProject(ResearchProject project) {
        if (project == null || projects.contains(project)) return;
        try {
            project.addParticipant(this);
            projects.add(project);
            Database.getInstance().save();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void publishPaper(ResearchPaper paper, Journal journal) {
        if (paper == null || journal == null) return;
        if (!papers.contains(paper)) {
            papers.add(paper);
        }
        journal.addPaper(paper);
        journal.notifySubscribers();
        Database.getInstance().addResearchPaper(paper);
        Database.getInstance().save();
    }

    public void addCourse(Course course) {
        if (course != null && !courses.contains(course)) {
            courses.add(course);
        }
    }

    @Override
    public String toString() {
        return "Teacher's type: " + this.getTeacherType() + "\n" + "Rating: " + this.getRating() + "\n" + "Complaints: " + this.getComplaints() + "\n" + "Papers: " + this.getPapers() + "\n" + "Projects: " + this.getProjects() + "\n";
    }
}
