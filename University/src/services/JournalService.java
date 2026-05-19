package services;

import enums.NewsTopic;
import interfaces.Researcher;
import model.social.Journal;
import model.social.News;
import model.users.User;
import model.research.ResearchPaper;
import storage.Database;
import utils.LogRecord;

import java.util.List;

public class JournalService {
    private final Database database;
    private final AuthService authService;

    public JournalService(Database database, AuthService authService) {
        this.database = database;
        this.authService = authService;
    }

    public List<Journal> getAllJournals() {
        return database.getJournals();
    }

    public Journal findJournalByName(String name) {
        return database.findJournalByName(name);
    }

    public void addJournal(Journal journal) {
        if (journal == null) return;
        database.addJournal(journal);
        log("Added journal: " + journal.getName());
        database.save();
    }

    public void subscribe(User user, Journal journal) {
        if (user == null || journal == null) return;
        journal.subscribe(user);
        log("Subscribed " + user.getUsername() + " to journal: " + journal.getName());
        database.save();
    }

    public void unsubscribe(User user, Journal journal) {
        if (user == null || journal == null) return;
        journal.unsubscribe(user);
        log("Unsubscribed " + user.getUsername() + " from journal: " + journal.getName());
        database.save();
    }

    public void publishPaper(Journal journal, ResearchPaper paper) {
        if (journal == null || paper == null) return;

        for (Researcher author : paper.getAuthors()) {
            if (author != null) {
                author.addPaper(paper);
            }
        }

        boolean newJournalPaper = !journal.getPapers().contains(paper);
        if (newJournalPaper) {
            journal.addPaper(paper);
        }

        if (!database.getResearchPapers().contains(paper)) {
            database.addResearchPaper(paper);
        }

        String newsTitle = getResearchNewsTitle(paper);
        if (database.findNewsByTitle(newsTitle) == null) {
            News news = new News(newsTitle, getResearchNewsContent(paper, journal), NewsTopic.RESEARCH);
            news.pin();
            database.addNews(news);
        }

        if (newJournalPaper) {
            notifySubscribers(journal);
        }
        log("Published paper in journal: " + journal.getName());
        database.save();
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

    // Observer Pattern: a Journal keeps subscribers, and this service notifies them when new papers appear.
    public void notifySubscribers(Journal journal) {
        if (journal == null) return;
        for (User user : journal.getSubscribers()) {
            if (user != null) {
                String notification = "New paper in " + journal.getName();
                user.addNotification(notification);
                System.out.println(user.getUsername() + " received notification: " + notification);
            }
        }
    }

    public void printJournalInfo(Journal journal) {
        if (journal == null) return;
        System.out.println("Name: " + journal.getName());
        System.out.println("Papers: " + journal.getPapers().size());
        System.out.println("Subscribers: " + journal.getSubscribers().size());
    }

    private void log(String action) {
        User actor = authService.getCurrentUser();
        if (actor != null) {
            database.addLog(new LogRecord(actor, action));
        }
    }
}
