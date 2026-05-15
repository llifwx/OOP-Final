package services;

import model.social.Journal;
import model.social.News;
import model.users.User;
import model.research.ResearchPaper;
import storage.Database;
import java.util.List;

public class JournalService {
    private static JournalService instance;
    private Database db;

    private JournalService() {
        this.db = Database.getInstance();
    }

    public static JournalService getInstance() {
        if (instance == null) instance = new JournalService();
        return instance;
    }

    public List<Journal> getAllJournals() {
        return db.getJournals();
    }

    public Journal findJournalByName(String name) {
        return db.findJournalByName(name);
    }

    public void addJournal(Journal journal) {
        db.addJournal(journal);
    }

    public void subscribe(User user, Journal journal) {
        journal.subscribe(user);
    }

    public void unsubscribe(User user, Journal journal) {
        journal.unsubscribe(user);
    }

    public void publishPaper(Journal journal, ResearchPaper paper) {
        journal.addPaper(paper);
        journal.notifySubscribers();
    }

    public void printJournalInfo(Journal journal) {
        System.out.println("Name: " + journal.getName());
        System.out.println("Papers: " + journal.getPapers().size());
        System.out.println("Subscribers: " + journal.getSubscribers().size());
    }
}