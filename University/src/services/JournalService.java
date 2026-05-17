package services;

import model.social.Journal;
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
        if (journal.getPapers().contains(paper)) {
            return;
        }
        journal.addPaper(paper);
        notifySubscribers(journal);
        log("Published paper in journal: " + journal.getName());
        database.save();
    }

    // Observer Pattern: a Journal keeps subscribers, and this service notifies them when new papers appear.
    public void notifySubscribers(Journal journal) {
        if (journal == null) return;
        for (User user : journal.getSubscribers()) {
            if (user != null) {
                user.receiveNotification("New paper in " + journal.getName());
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
