package services;

import model.social.Message;
import model.users.Employee;
import model.users.User;
import storage.Database;
import utils.LogRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MessageService {
    private final Database database;
    private final AuthService authService;

    public MessageService(Database database, AuthService authService) {
        this.database = database;
        this.authService = authService;
    }

    // Helper for User role checking
    private Employee requireEmployee() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("[Message Service] : No user is logged in.");
        }

        if (!(currentUser instanceof Employee employee)) {
            throw new SecurityException("[Message service] : Access denied. Only Employee users can write messages.");
        }

        return employee;
    }

    public Message sendMessage(Employee receiver, String text) {
        Employee sender = requireEmployee();

        if (receiver == null) {
            System.out.println("[Message Service] : Receiver cannot be null.");
            return null;
        }
        if (sender.equals(receiver)) {
            System.out.println("[Message Service] : Cannot send message to yourself.");
            return null;
        }
        if (text == null || text.isBlank()) {
            System.out.println("[Message Service] : Message text cannot be empty.");
            return null;
        }

        Message message = new Message(sender, receiver, text);
        database.addMessage(message);

        log("Sent message to " + receiver.getUsername() + ": " + text);
        database.save();
        System.out.println("[Message Service] : Message sent to " + receiver.getUsername() + ".");
        return message;
    }

    public List<Message> getInbox() {
        Employee current = requireEmployee();

        return database.findMessagesByReceiver(current).stream().sorted(Comparator.comparing(Message::getSentDate).reversed()).collect(Collectors.toList());
    }

    public List<Message> getSentMessages() {
        Employee employee = requireEmployee();

        return database.findMessagesBySender(employee).stream().sorted(Comparator.comparing(Message::getSentDate).reversed()).collect(Collectors.toList());
    }

    public List<Message> getUnreadMessages() {
        return getInbox().stream().filter(message -> !message.isRead()).collect(Collectors.toList());
    }

    public int getUnreadMessagesCount() {
        return getUnreadMessages().size();
    }

    public Message openMessage(int messageId) {
        Employee current = requireEmployee();
        Message message = database.findMessageById(messageId);

        if (message == null) {
            System.out.println("[Message Service] : Message with ID " + messageId + " not found.");
            return null;
        }
        if (!message.getReceiver().equals(current) && !message.getSender().equals(current)) {
            System.out.println("[Message Service] : Access denied. You can only open messages sent to or from you.");
            return null;
        }
        if (!message.isRead() && message.getReceiver().equals(current)) {
            message.markAsRead();
            database.save();
        }

        return message;
    }

    public void markAllRead() {
        Employee current = requireEmployee();
        List<Message> unread = getUnreadMessages();
        unread.forEach(Message::markAsRead);
        if (!unread.isEmpty()) {
            log("Marked " + unread.size() + " messages as read.");
            database.save();
        }

        System.out.println("[Message Service] : Marked " + unread.size() + " messages as read.");
    }

    public List<Message> getInboxFrom(Employee sender) {
        if (sender == null) return new ArrayList<>();
        return getInbox().stream().filter(message -> message.getSender().equals(sender)).collect(Collectors.toList());
    }

    public List<Message> searchMessages(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            System.out.println("[Message Service] : Search keyword cannot be empty.");
            return new ArrayList<>();
        }

        String lower = keyword.toLowerCase();
        List<Message> all = new ArrayList<>(getInbox());
        all.addAll(getSentMessages());

        return all.stream().filter(message -> message.getText().toLowerCase().contains(lower)).collect(Collectors.toList());
    }

    public String getPreview(Message message) {
        if (message == null || message.getText() == null) {
            return "";
        }
        String text = message.getText();
        return text.substring(0, Math.min(text.length(), 20));
    }

    public boolean deleteMessage(int messageId) {
        Employee current = requireEmployee();
        Message message = database.findMessageById(messageId);

        if (message == null) {
            System.out.println("[Message Service] : Message with ID " + messageId + " not found.");
            return false;
        }
        if (!message.getSender().equals(current) && !message.getReceiver().equals(current)) {
            System.out.println("[Message Service] : Access denied. You can only delete messages sent to or from you.");
            return false;
        }

        boolean removed = database.removeMessage(message);
        if (removed) {
            log("Deleted message with ID " + messageId);
            database.save();
            System.out.println("[Message Service] : Message with ID " + messageId + " deleted.");
        }

        return removed;
    }

    public void printInbox() {
        List<Message> inbox = getInbox();
        if (inbox.isEmpty()) {
            System.out.println("[Message Service] : Your inbox is empty.");
            return;
        }

        System.out.println("─── Inbox for " + authService.getCurrentUser().getUsername() + ". You have: " + getUnreadMessagesCount() + " unread messages. " + " ───────────────────────────────");
        for (Message m : inbox) {
            String status = m.isRead() ? " " : "*";
            System.out.println("[" + m.getId() + "] " + status + " From: " + m.getSender().getUsername() + " | " + m.getSentDate() + "\n    " + m.getText());
            System.out.println("──────────────────────────────────────────────────────────────────────────────");
        }
    }

    public void printSentMessages() {
        List<Message> sent = getSentMessages();
        if (sent.isEmpty()) {
            System.out.println("[Message Service] : You have not sent any messages.");
            return;
        }

        System.out.println("─── Sent Messages for " + authService.getCurrentUser().getUsername() + ". You have: " + sent.size() + " sent messages. " + " ───────────────────────────────");
        for (Message m : sent) {
            System.out.println("[" + m.getId() + "] To: " + m.getReceiver().getUsername() + " | " + m.getSentDate() + "\n    " + m.getText());
            System.out.println("──────────────────────────────────────────────────────────────────────────────");
        }
    }

    private void log(String action) {
        User actor = authService.getCurrentUser();
        if (actor != null) {
            database.addLog(new LogRecord(actor, action));
        }
    }
}
