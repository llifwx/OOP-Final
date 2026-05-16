package services;

import exceptions.NotResearcherEx;
import interfaces.Researcher;
import model.research.ResearchProject;
import model.research.ResearchPaper;
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
        project.addParticipant(researcher);
        db().save();
    }

    public void addPaperToProject(ResearchProject project, ResearchPaper paper) {
        project.publishPaper(paper);
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
