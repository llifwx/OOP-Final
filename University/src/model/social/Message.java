package model.social;

import model.users.Employee;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int idCnt;
    private int id;
    private Employee sender;
    private Employee receiver;
    private String text;
    private Date sentDate;
    private boolean isRead;

    public Message(Employee sender, Employee receiver, String text) {
        this.id = idCnt++;
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.sentDate = new Date();
        this.isRead = false;
    }

    public static void synchronizeNextId(int nextId) {
        if (nextId > idCnt) {
            idCnt = nextId;
        }
    }

    public static int getIdCnt() {
        return idCnt;
    }

    public int getId() {
        return id;
    }

    public Employee getSender() {
        return sender;
    }

    public Employee getReceiver() {
        return receiver;
    }

    public String getText() {
        return text;
    }

    public Date getSentDate() {
        return sentDate == null ? null : new Date(sentDate.getTime());
    }

    public boolean isRead() {
        return isRead;
    }

    public void markAsRead() {this.isRead = true;}

    public String getPreview() {
        if (text == null) return "";
        return this.text.substring(0, Math.min(this.text.length(), 20));
    }
}
