package services;

import storage.Database;
import model.research.ResearchPaper;
import java.util.List;

public class ResearchService {
    private static ResearchService instance;

    private ResearchService() {
    }

    public static ResearchService getInstance() {
        if (instance == null) instance = new ResearchService();
        return instance;
    }

    public List<ResearchPaper> getAllPapers() {
        return db().getResearchPapers();
    }

    public ResearchPaper findPaperByTitle(String title) {
        return db().findResearchPaperByTitle(title);
    }

    public ResearchPaper findPaperByDoi(String doi) {
        return db().findResearchPaperByDoi(doi);
    }

    public void addPaper(ResearchPaper paper) {
        db().addResearchPaper(paper);
        db().save();
    }

    private Database db() {
        return Database.getInstance();
    }
}
