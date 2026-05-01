package model.social;

import model.users.User;

import java.io.Serializable;
import java.util.Date;

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

    public void editText(String newText) {this.text = newText;}
}