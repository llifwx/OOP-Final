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

import java.util.ArrayList;
import java.util.List;

public class ResearchProjectService {
    private static ResearchProjectService instance;
    private final Database database;
    private final AuthService authService;

    public ResearchProjectService(Database database, AuthService authService) {
        this.database = database;
        this.authService = authService;
    }

    public static ResearchProjectService getInstance() {
        if (instance == null) instance = new ResearchProjectService(Database.getInstance(), null);
        return instance;
    }

    public List<ResearchProject> getAllProjects() {
        return new ArrayList<>(db().getResearchProjects());
    }

    public ResearchProject findProjectByTopic(String topic) {
        if (topic == null || topic.isBlank()) return null;
        return db().findResearchProjectByTopic(topic);
    }

    public void addProject(ResearchProject project) {
        requireManager();
        if (project == null) {
            System.out.println("[ResearchProjectService] Project cannot be null.");
            return;
        }
        if (project.getTopic() == null || project.getTopic().isBlank()) {
            System.out.println("[ResearchProjectService] Project topic cannot be empty.");
            return;
        }
        if (db().findResearchProjectByTopic(project.getTopic()) != null) {
            System.out.println("[ResearchProjectService] Project with topic '" + project.getTopic() + "' already exists.");
            return;
        }
        db().addResearchProject(project);
        db().save();
        log("Added research project: " + project.getTopic());
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

        db().save();
        log("Joined research project: " + project.getTopic());
    }

    public void addPaperToProject(ResearchProject project, ResearchPaper paper) {
        requireResearcher();
        if (project == null || paper == null) {
            System.out.println("[ResearchProjectService] Project or paper is null.");
            return;
        }
        project.addPublishedPaper(paper);
        db().save();
        log("Added paper to project: " + project.getTopic());
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
        db().save();
        log("Assigned supervisor for graduate student: " + student.getUsername());
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

    private Database db() {
        return database;
    }

    private User requireResearcher() {
        User current = authService == null ? null : authService.getCurrentUser();
        if (!(current instanceof Researcher)) {
            throw new SecurityException("[ResearchProjectService] Access denied: current user is not a researcher.");
        }
        return current;
    }

    private void requireManager() {
        User current = authService == null ? null : authService.getCurrentUser();
        if (!(current instanceof Manager)) {
            throw new SecurityException("[ResearchProjectService] Access denied: current user is not a Manager.");
        }
    }

    private void log(String action) {
        User actor = authService == null ? null : authService.getCurrentUser();
        if (actor != null) {
            db().addLog(new LogRecord(actor, action));
            db().save();
        }
    }
}
