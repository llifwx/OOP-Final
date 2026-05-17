package services;

import exceptions.NotResearcherEx;
import exceptions.InvalidSupervisorEx;
import interfaces.Researcher;
import model.research.ResearchProject;
import model.research.ResearchPaper;
import model.users.GraduateStudent;
import model.users.Manager;
import model.users.Teacher;
import model.users.User;
import storage.Database;
import utils.LogRecord;

import java.util.List;

public class ResearchProjectService {
    private final Database database;
    private final AuthService authService;

    public ResearchProjectService(Database database, AuthService authService) {
        this.database = database;
        this.authService = authService;
    }

    public List<ResearchProject> getAllProjects() {
        return database.getResearchProjects();
    }

    public ResearchProject findProjectByTopic(String topic) {
        return database.findResearchProjectByTopic(topic);
    }

    public void addProject(ResearchProject project) {
        if (project == null) return;
        database.addResearchProject(project);
        log("Added research project: " + project.getTopic());
        database.save();
    }

    public void joinProject(ResearchProject project, Researcher researcher) throws NotResearcherEx {
        User current = requireResearcher();
        if (project == null || researcher == null) {
            throw new NotResearcherEx();
        }
        if (researcher != current) {
            throw new SecurityException("[ResearchProjectService] Access denied: cannot join project for another user.");
        }

        project.addParticipant(researcher);
        if (researcher instanceof GraduateStudent graduateStudent) {
            graduateStudent.addProject(project);
        } else if (researcher instanceof Teacher teacher) {
            teacher.addProject(project);
        }

        log("Researcher joined project: " + project.getTopic());
        database.save();
    }

    public void addPaperToProject(ResearchProject project, ResearchPaper paper) {
        if (project == null || paper == null) return;
        project.publishPaper(paper);
        log("Added paper to research project: " + project.getTopic());
        database.save();
    }

    public void assignSupervisor(GraduateStudent student, Researcher supervisor) throws InvalidSupervisorEx {
        requireManager();
        if (student == null || supervisor == null) {
            throw new InvalidSupervisorEx("Student and supervisor are required");
        }
        if (supervisor.calculateHIndex() < 3) {
            throw new InvalidSupervisorEx();
        }
        student.setSupervisor(supervisor);
        log("Assigned supervisor to graduate student: " + student.getUsername());
        database.save();
    }

    public void printProjectInfo(ResearchProject project) {
        if (project == null) {
            System.out.println("[ResearchProjectService] Project is not available.");
            return;
        }
        System.out.println("Topic: " + project.getTopic());
        System.out.println("Participants: " + project.getParticipants().size());
        System.out.println("Published Papers: " + project.getPublishedPapers().size());
    }

    private void log(String action) {
        User actor = authService.getCurrentUser();
        if (actor != null) {
            database.addLog(new LogRecord(actor, action));
        }
    }
}
