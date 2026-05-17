package services;

import model.social.Journal;
import interfaces.Researcher;
import model.users.Admin;
import model.users.Manager;
import model.users.User;
import model.research.ResearchPaper;
import storage.Database;
import utils.LogRecord;

import java.util.ArrayList;
import java.util.List;

public class JournalService {
    private static JournalService instance;
    private final Database database;
    private final AuthService authService;

    public JournalService(Database database, AuthService authService) {
        this.database = database;
        this.authService = authService;
    }

    public static JournalService getInstance() {
        if (instance == null) instance = new JournalService(Database.getInstance(), null);
        return instance;
    }

    public List<Journal> getAllJournals() {
        return new ArrayList<>(db().getJournals());
    }

    public Journal findJournalByName(String name) {
        if (name == null || name.isBlank()) return null;
        return db().findJournalByName(name);
    }

    public void addJournal(Journal journal) {
        requireJournalAuthor();
        if (journal == null) {
            System.out.println("[JournalService] Journal cannot be null.");
            return;
        }
        if (journal.getName() == null || journal.getName().isBlank()) {
            System.out.println("[JournalService] Journal name cannot be empty.");
            return;
        }
        if (db().findJournalByName(journal.getName()) != null) {
            System.out.println("[JournalService] Journal with name '" + journal.getName() + "' already exists.");
            return;
        }
        db().addJournal(journal);
        db().save();
        log("Added journal: " + journal.getName());
    }

    public void subscribe(User user, Journal journal) {
        User current = requireLoggedIn();
        if (user == null || journal == null) {
            System.out.println("[JournalService] User and journal are required.");
            return;
        }
        if (!current.equals(user) && !(current instanceof Admin)) {
            throw new SecurityException("[JournalService] Access denied: users can only subscribe themselves.");
        }
        journal.subscribe(user);
        db().save();
        log("Subscribed to journal: " + journal.getName());
    }

    public void unsubscribe(User user, Journal journal) {
        User current = requireLoggedIn();
        if (user == null || journal == null) {
            System.out.println("[JournalService] User and journal are required.");
            return;
        }
        if (!current.equals(user) && !(current instanceof Admin)) {
            throw new SecurityException("[JournalService] Access denied: users can only unsubscribe themselves.");
        }
        journal.unsubscribe(user);
        db().save();
        log("Unsubscribed from journal: " + journal.getName());
    }

    public void publishPaper(Journal journal, ResearchPaper paper) {
        requireJournalAuthor();
        if (journal == null || paper == null) {
            System.out.println("[JournalService] Journal and paper are required.");
            return;
        }
        journal.addPaper(paper);
        notifySubscribers(journal);
        db().save();
        log("Published paper in journal: " + journal.getName());
    }

    public void notifySubscribers(Journal journal) {
        if (journal == null) return;
        for (User user : journal.getSubscribers()) {
            if (user != null) {
                user.receiveNotification("New paper in " + journal.getName());
            }
        }
    }

    public void printJournalInfo(Journal journal) {
        if (journal == null) {
            System.out.println("[JournalService] Journal is not available.");
            return;
        }
        System.out.println("Name: " + journal.getName());
        System.out.println("Papers: " + journal.getPapers().size());
        System.out.println("Subscribers: " + journal.getSubscribers().size());
    }

    private Database db() {
        return database;
    }

    private User requireLoggedIn() {
        User current = authService == null ? null : authService.getCurrentUser();
        if (current == null) {
            throw new SecurityException("[JournalService] Access denied: no user is logged in.");
        }
        return current;
    }

    private void requireJournalAuthor() {
        User current = requireLoggedIn();
        if (!(current instanceof Manager) && !(current instanceof Admin) && !(current instanceof Researcher)) {
            throw new SecurityException("[JournalService] Access denied: journal changes require Manager, Admin, or Researcher.");
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
