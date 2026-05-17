package services;

import model.social.Journal;
import model.social.News;
import model.users.User;
import model.research.ResearchPaper;
import storage.Database;
import java.util.List;

public class JournalService {
    private static JournalService instance;

    private JournalService() {
    }

    public static JournalService getInstance() {
        if (instance == null) instance = new JournalService();
        return instance;
    }

    public List<Journal> getAllJournals() {
        return db().getJournals();
    }

    public Journal findJournalByName(String name) {
        return db().findJournalByName(name);
    }

    public void addJournal(Journal journal) {
        db().addJournal(journal);
        db().save();
    }

    public void subscribe(User user, Journal journal) {
        journal.subscribe(user);
        db().save();
    }

    public void unsubscribe(User user, Journal journal) {
        journal.unsubscribe(user);
        db().save();
    }

    public void publishPaper(Journal journal, ResearchPaper paper) {
        journal.addPaper(paper);
        notifySubscribers(journal);
        db().save();
    }

    public void notifySubscribers(Journal journal) {
        if (journal == null) return;
        for (User user : journal.getSubscribers()) {
            if (user != null) {
                System.out.println(user.getUsername() + " received notification: New paper in " + journal.getName());
            }
        }
    }

    public void printJournalInfo(Journal journal) {
        System.out.println("Name: " + journal.getName());
        System.out.println("Papers: " + journal.getPapers().size());
        System.out.println("Subscribers: " + journal.getSubscribers().size());
    }

    private Database db() {
        return Database.getInstance();
    }
}
