package model.users;

import enums.DegreeType;
import enums.Language;
import interfaces.Researcher;
import model.research.ResearchPaper;
import model.research.ResearchProject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GraduateStudent extends Student implements Researcher {
    private static final long serialVersionUID = 1L;

    private DegreeType degreeType;
    private Researcher supervisor;
    private List<ResearchPaper> diplomaProjects;
    private List<ResearchPaper> papers;
    private List<ResearchProject> projects;

    public GraduateStudent(String username, String password, String fullName, String email, Language language, String studentId, String school, String major, int yearOfStudy, DegreeType degreeType) {
        super(username, password, fullName, email, language, studentId, school, major, yearOfStudy);
        this.degreeType = degreeType;
        this.diplomaProjects = new ArrayList<>();
        this.papers = new ArrayList<>();
        this.projects = new ArrayList<>();
    }

    public void setSupervisor(Researcher supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public int calculateHIndex() {
        if (papers.isEmpty()) return 0;
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

    public DegreeType getDegreeType() {return degreeType;}

    public void setDegreeType(DegreeType degreeType) {this.degreeType = degreeType;}

    public Researcher getSupervisor() {return supervisor;}

    public List<ResearchPaper> getPapers() {return new ArrayList<>(papers);}

    public List<ResearchPaper> getDiplomaProjects() {return new ArrayList<>(diplomaProjects);}

    public List<ResearchProject> getProjects() {return new ArrayList<>(projects);}

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
        return "GraduateStudent: " + getUsername() + ". " + "Full name: " + getFullName() + ". " + "ID: " + getId() + ". " + "Degree: " + degreeType + ". " + "Supervisor: " + (supervisor != null ? supervisor.toString() : "none") + ".";
    }

}
