package utils;

import model.users.User;

import java.io.Serializable;
import java.util.Date;

public class LogRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int idCnt;
    private int id;
    private User user;
    private String action;
    private Date date;

    public LogRecord(User user, String action) {
        this.id = idCnt++;
        this.user = user;
        this.action = action;
        this.date = new Date();
    }

    public static void synchronizeNextId(int nextId) {
        if (nextId > idCnt) {
            idCnt = nextId;
        }
    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getAction() {
        return action;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {return "[" + this.date + "] " + this.user.getUsername() + ": " + this.action;}
}
