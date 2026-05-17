package services;

import exceptions.NotResearcherEx;
import exceptions.InvalidSupervisorEx;
import interfaces.Researcher;
import model.research.ResearchProject;
import model.research.ResearchPaper;
import model.users.GraduateStudent;
import model.users.Teacher;
import storage.Database;
import java.util.List;

public class ResearchProjectService {
    private static ResearchProjectService instance;

    private ResearchProjectService() {
    }

    public static ResearchProjectService getInstance() {
        if (instance == null) instance = new ResearchProjectService();
        return instance;
    }

    public List<ResearchProject> getAllProjects() {
        return db().getResearchProjects();
    }

    public ResearchProject findProjectByTopic(String topic) {
        return db().findResearchProjectByTopic(topic);
    }

    public void addProject(ResearchProject project) {
        db().addResearchProject(project);
        db().save();
    }

    public void joinProject(ResearchProject project, Researcher researcher) throws NotResearcherEx {
        if (project == null || researcher == null) {
            throw new NotResearcherEx();
        }

        project.addParticipant(researcher);
        if (researcher instanceof GraduateStudent graduateStudent) {
            graduateStudent.addProject(project);
        } else if (researcher instanceof Teacher teacher) {
            teacher.addProject(project);
        }

        db().save();
    }

    public void addPaperToProject(ResearchProject project, ResearchPaper paper) {
        if (project == null || paper == null) {
            System.out.println("[ResearchProjectService] Project or paper is null.");
            return;
        }
        project.addPublishedPaper(paper);
        db().save();
    }

    public void assignSupervisor(GraduateStudent student, Researcher supervisor) throws InvalidSupervisorEx {
        if (student == null || supervisor == null) {
            throw new InvalidSupervisorEx("Student and supervisor are required");
        }
        if (supervisor.calculateHIndex() < 3) {
            throw new InvalidSupervisorEx();
        }
        student.setSupervisor(supervisor);
        db().save();
    }

    public void printProjectInfo(ResearchProject project) {
        System.out.println("Topic: " + project.getTopic());
        System.out.println("Participants: " + project.getParticipants().size());
        System.out.println("Published Papers: " + project.getPublishedPapers().size());
    }

    private Database db() {
        return Database.getInstance();
    }
}
