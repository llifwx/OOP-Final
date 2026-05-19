package services;

import enums.NewsTopic;
import storage.Database;
import interfaces.Researcher;
import model.research.ResearchPaper;
import model.social.Journal;
import model.social.News;
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

    public void publishPaper(ResearchPaper paper) {
        requireResearchMutationAccess();
        if (paper == null) {
            System.out.println("[ResearchService] Paper cannot be null.");
            return;
        }

        for (Researcher author : paper.getAuthors()) {
            if (author != null) {
                author.addPaper(paper);
            }
        }

        Journal journal = paper.getJournal();
        boolean newJournalPaper = journal != null && !journal.getPapers().contains(paper);
        if (newJournalPaper) {
            journal.addPaper(paper);
        }

        if (!db().getResearchPapers().contains(paper)) {
            db().addResearchPaper(paper);
        }

        String newsTitle = getResearchNewsTitle(paper);
        if (db().findNewsByTitle(newsTitle) == null) {
            News news = new News(newsTitle, getResearchNewsContent(paper, journal), NewsTopic.RESEARCH);
            news.pin();
            db().addNews(news);
        }

        if (newJournalPaper) {
            notifyJournalSubscribers(journal);
        }

        log("Published research paper: " + paper.getTitle());
        db().save();
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

    private void notifyJournalSubscribers(Journal journal) {
        if (journal == null) return;
        for (User user : journal.getSubscribers()) {
            if (user != null) {
                user.addNotification("New paper in " + journal.getName());
            }
        }
    }

    private String getResearchNewsTitle(ResearchPaper paper) {
        String title = paper.getTitle() == null || paper.getTitle().isBlank() ? "Untitled" : paper.getTitle();
        return "New Paper Published: " + title;
    }

    private String getResearchNewsContent(ResearchPaper paper, Journal journal) {
        String journalName = journal == null ? "Unknown journal" : journal.getName();
        String doi = paper.getDoi() == null || paper.getDoi().isBlank() ? "N/A" : paper.getDoi();
        return paper.getTitle() + " was published in " + journalName + ". DOI: " + doi + ".";
    }

    private void log(String action) {
        User actor = authService == null ? null : authService.getCurrentUser();
        if (actor != null) {
            db().addLog(new LogRecord(actor, action));
            db().save();
        }
    }
}
