package model.social;

import model.users.User;
import model.research.ResearchPaper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Journal implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int idCounter = 0;
    private int id;
    private String name;
    private List<ResearchPaper> papers;
    private List<User> subscribers;

    public Journal(String name) {
        this.id = ++idCounter;
        this.name = name;
        this.papers = new ArrayList<>();
        this.subscribers = new ArrayList<>();
    }

    public static void synchronizeIdCounter(int maxId) {
        if (maxId > idCounter) {
            idCounter = maxId;
        }
    }

    public void ensureId() {
        if (id <= 0) {
            id = ++idCounter;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<ResearchPaper> getPapers() {
        return new ArrayList<>(papers);
    }

    public List<User> getSubscribers() {
        return new ArrayList<>(subscribers);
    }

    public void subscribe(User user) {
        if (user != null && !this.subscribers.contains(user)) {
            this.subscribers.add(user);
        }
    }

    public void unsubscribe(User user) {this.subscribers.remove(user);}

    public void addPaper(ResearchPaper paper) {
        if (paper != null && !this.papers.contains(paper)) {
            this.papers.add(paper);
        }
    }

    @Override
    public String toString() {
        return "Journal{" + "id=" + id + ", name='" + name + '\'' + ", papersCount=" + papers.size()
                + ", subscribersCount=" + subscribers.size() + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Journal journal)) return false;
        return id == journal.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
