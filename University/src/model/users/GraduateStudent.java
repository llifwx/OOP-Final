package model.users;

import enums.DegreeType;
import enums.Language;
import interfaces.Researcher;
import research.Journal;
import research.ResearchPaper;
import research.ResearchProject;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GraduateStudent extends Student implements Researcher {

    private DegreeType degreeType;
    private Researcher supervisor;
    private List<ResearchPaper> diplomaProjects;
    private List<ResearchPaper> papers;
    private List<ResearchProject> projects;

    public GraduateStudent(String username, String password, String fullName, String email,
                           Language language, String studentId, String school,
                           String major, int yearOfStudy, DegreeType degreeType) {
        super(username, password, fullName, email, language, studentId, school, major, yearOfStudy);
        this.degreeType = degreeType;
        this.diplomaProjects = new ArrayList<>();
        this.papers = new ArrayList<>();
        this.projects = new ArrayList<>();
    }

    public void setSupervisor(Researcher supervisor) { this.supervisor = supervisor; }
    public Researcher viewSupervisor() { return supervisor; }
    public void addDiplomaProject(ResearchPaper paper) {}

    @Override
    public int calculateHIndex() { return 0; }

    @Override
    public void printPapers(Comparator<ResearchPaper> comparator) {}

    @Override
    public void joinProject(ResearchProject project) {}

    @Override
    public void publishPaper(ResearchPaper paper, Journal journal) {}

    public DegreeType getDegreeType() { return degreeType; }
    public void setDegreeType(DegreeType degreeType) { this.degreeType = degreeType; }

    public Researcher getSupervisor() { return supervisor; }

    //TODO:refactor lists getters and setters
    public List<ResearchPaper> getPapers() { return papers; }
    public void addPaper(ResearchPaper paper) {}

    public List<ResearchPaper> getDiplomaProjects() { return diplomaProjects; }
    public List<ResearchProject> getProjects() { return projects; }

    @Override
    public String toString() {
        return "GraduateStudent: " + getUsername() + ". " +
                "Full name: " + getFullName() + ". " +
                "ID: " + getId() + ". " +
                "Degree: " + degreeType + ". " +
                "Supervisor: " + (supervisor != null ? supervisor.toString() : "none") + ".";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GraduateStudent gs)) return false;
        return Objects.equals(getStudentId(), gs.getStudentId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStudentId());
    }
}