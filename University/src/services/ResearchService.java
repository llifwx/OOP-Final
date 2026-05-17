package services;

import storage.Database;
import interfaces.Researcher;
import model.research.ResearchPaper;
import model.users.Admin;
import model.users.Manager;
import model.users.User;
import utils.LogRecord;

import java.util.ArrayList;
import java.util.List;

public class ResearchService {
    private static ResearchService instance;
    private final Database database;
    private final AuthService authService;

    public ResearchService(Database database, AuthService authService) {
        this.database = database;
        this.authService = authService;
    }

    public static ResearchService getInstance() {
        if (instance == null) instance = new ResearchService(Database.getInstance(), null);
        return instance;
    }

    public List<ResearchPaper> getAllPapers() {
        return new ArrayList<>(db().getResearchPapers());
    }

    public ResearchPaper findPaperByTitle(String title) {
        if (title == null || title.isBlank()) return null;
        return db().findResearchPaperByTitle(title);
    }

    public ResearchPaper findPaperByDoi(String doi) {
        if (doi == null || doi.isBlank()) return null;
        return db().findResearchPaperByDoi(doi);
    }

    public void addPaper(ResearchPaper paper) {
        requireResearchMutationAccess();
        if (paper == null) {
            System.out.println("[ResearchService] Paper cannot be null.");
            return;
        }
        if (db().getResearchPapers().contains(paper)) {
            System.out.println("[ResearchService] Paper already exists.");
            return;
        }
        db().addResearchPaper(paper);
        db().save();
        log("Added research paper: " + paper.getTitle());
    }

    private Database db() {
        return database;
    }

    private User requireLoggedIn() {
        User current = authService == null ? null : authService.getCurrentUser();
        if (current == null) {
            throw new SecurityException("[ResearchService] Access denied: no user is logged in.");
        }
        return current;
    }

    private void requireResearchMutationAccess() {
        User current = requireLoggedIn();
        if (!(current instanceof Researcher) && !(current instanceof Manager) && !(current instanceof Admin)) {
            throw new SecurityException("[ResearchService] Access denied: research changes require Researcher, Manager, or Admin.");
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
