package utils;

import model.users.User;
import java.util.Date;

public class LogRecord {
    private User user;
    private String action;
    private Date date;

    public LogRecord(User user, String action) {
        this.user = user;
        this.action = action;
        this.date = new Date();
    }

    @Override
    public String toString() { return "[" + this.date + "] " + this.user.getUsername() + ": " + this.action; }
}