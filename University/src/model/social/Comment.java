package model.social;

import model.users.User;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;
    private User author;
    private String text;
    private Date date;

    public Comment(User author, String text) {
        this.author = author;
        this.text = text;
        this.date = new Date();
    }

    public User getAuthor() {return author;}

    public String getText() {return text;}

    public Date getDate() {return date == null ? null : new Date(date.getTime());}

    public void editText(String newText) {this.text = newText;}

    @Override
    public String toString() {
        return "Comment{" + "author=" + (author != null ? author.getUsername() : "N/A")
                + ", text='" + text + '\'' + ", date=" + date + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Comment comment)) return false;
        return Objects.equals(author, comment.author)
                && Objects.equals(text, comment.text)
                && Objects.equals(date, comment.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, text, date);
    }
}
