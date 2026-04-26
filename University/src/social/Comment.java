package social;

import model.users.User;
import java.util.Date;

public class Comment {
    private User author;
    private String text;
    private Date date;

    public Comment(User author, String text) {
        this.author = author;
        this.text = text;
        this.date = new Date();
    }

    public void editText(String newText) { this.text = newText; }
}