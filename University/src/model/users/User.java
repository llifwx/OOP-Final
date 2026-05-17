package model.users;

import enums.Language;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int idCounter;
    private final int id;

    private String username;
    private String password;
    private String fullName;
    private String email;
    private Language language;
    private List<String> notifications;

    public User(String username, String password, String fullName, String email, Language language) {
        this.id = ++idCounter;
        this.username = username;
        this.password = hashPassword(password);
        this.fullName = fullName;
        this.email = email;
        this.language = language;
        this.notifications = new ArrayList<>();
    }

    public static void synchronizeIdCounter(int maxId) {
        if (maxId > idCounter) {
            idCounter = maxId;
        }
    }

    public String getEmail() {return this.email;}

    public Language getLanguage() {return this.language;}

    public List<String> getNotifications() {
        if (notifications == null) notifications = new ArrayList<>();
        return new ArrayList<>(notifications);
    }

    public void addNotification(String notification) {
        if (notification == null || notification.isBlank()) return;
        if (notifications == null) notifications = new ArrayList<>();
        notifications.add(notification);
    }

    public String getFullName() {return this.fullName;}

    public String getUsername() {return this.username;}

    public int getId() {return this.id;}

    public void setUsername(String newUsername) {this.username = newUsername;}

    public void setFullName(String fullName) {this.fullName = fullName;}

    public void setEmail(String email) {this.email = email;}

    public void setLanguage(Language language) {this.language = language;}

    public boolean login(String password) {
        return Objects.equals(this.password, hashPassword(password));
    }

    private static String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    @Override
    public String toString() {
        return "User: " + this.getUsername() + ". " + "Full name: " + this.getFullName() + ". " + "Email: " + this.getEmail() + ". " + "ID: " + this.getId() + ". ";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User user)) return false;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
