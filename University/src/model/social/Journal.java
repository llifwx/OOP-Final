package model.social;

import model.users.User;
import model.research.ResearchPaper;

import java.util.ArrayList;
import java.util.List;

public class Journal {
    private String name;
    private List<ResearchPaper> papers;
    private List<User> subscribers;

    public Journal(String name) {
        this.name = name;
        this.papers = new ArrayList<>();
        this.subscribers = new ArrayList<>();
    }

    public void subscribe(User user) { this.subscribers.add(user); }
    public void unsubscribe(User user) { this.subscribers.remove(user); }
    public void addPaper(ResearchPaper paper) { this.papers.add(paper); }
    public void notifySubscribers() {
        for (User u : subscribers) u.receiveNotification("New paper in " + name);
    }
}