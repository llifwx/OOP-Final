package model.social;

import model.users.Employee;

import java.util.Date;

public class Message {
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
        return sentDate;
    }

    public boolean isRead() {
        return isRead;
    }

    public void markAsRead() {this.isRead = true;}

    public String getPreview() {return this.text.substring(0, Math.min(this.text.length(), 20));}
}