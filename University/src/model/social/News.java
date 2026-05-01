package model.social;

import enums.NewsTopic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class News implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title;
    private String content;
    private NewsTopic topic;
    private Date date;
    private List<Comment> comments;
    private boolean pinned;

    public News(String title, String content, NewsTopic topic) {
        this.title = title;
        this.content = content;
        this.topic = topic;
        this.date = new Date();
        this.comments = new ArrayList<>();
        this.pinned = false;
    }

    public void addComment(Comment comment) {this.comments.add(comment);}

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
        return date;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public boolean isPinned() {
        return pinned;
    }
}