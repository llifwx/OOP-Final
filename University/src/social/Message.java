package social;

import model.users.Employee;
import java.util.Date;

public class Message {
    private Employee sender;
    private Employee receiver;
    private String text;
    private Date sentDate;
    private boolean isRead;

    public Message(Employee sender, Employee receiver, String text) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.sentDate = new Date();
        this.isRead = false;
    }

    public void markAsRead() { this.isRead = true; }
    public String getPreview() { return this.text.substring(0, Math.min(this.text.length(), 20)); }
}