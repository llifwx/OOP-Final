package model.social;

import enums.NewsTopic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class News implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int idCounter = 0;
    private int id;
    private String title;
    private String content;
    private NewsTopic topic;
    private Date date;
    private List<Comment> comments;
    private boolean pinned;

    public News(String title, String content, NewsTopic topic) {
        this.id = ++idCounter;
        this.title = title;
        this.content = content;
        this.topic = topic;
        this.date = new Date();
        this.comments = new ArrayList<>();
        this.pinned = false;
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

    public void addComment(Comment comment) {
        if (comment != null) {
            this.comments.add(comment);
        }
    }

    public void pin() {this.pinned = true;}

    public void unpin() {this.pinned = false;}

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public NewsTopic getTopic() {
        return topic;
    }

    public Date getDate() {
        return date == null ? null : new Date(date.getTime());
    }

    public List<Comment> getComments() {
        return new ArrayList<>(comments);
    }

    public boolean isPinned() {
        return pinned;
    }

    @Override
    public String toString() {
        return "News{" + "id=" + id + ", title='" + title + '\'' + ", topic=" + topic
                + ", date=" + date + ", pinned=" + pinned + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof News news)) return false;
        return id == news.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
