package model.users;

import enums.Language;

import java.util.List;
import java.util.Objects;

public abstract class User {
    private static int idCounter;
    private final int id;

    private String username;
    private String password;
    private String fullName;
    private String email;
    private Language language;

    public User(String username, String password, String fullName, String email, Language language) {
        this.id = ++idCounter;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.language = language;
    }

    public String getEmail() {return this.email;}
    public Language getLanguage() {return this.language;}
    public String getFullName() {return this.fullName;}
    public String getUsername() {return this.username;}
    public int getId() {return this.id;}

    public void setUsername(String newUsername) {this.username = newUsername;}

    public boolean login(String password) {
        return this.password.equals(password);
    };
    public void logout() {
        System.out.println(this.getUsername() + "logged out.");
    };
    public void viewNews(List<News> newsList) {
        for (News news : newsList) {
            System.out.println(news);
        }
    };
    public void subscribeToJournal(Journal journal) {
        journal.subscribe(this);
    };
    public void unsubscribeFromJournal(Journal journal) {
        journal.unsubscribe(this);
    };
    public void receiveNotification(String message) {
        System.out.println(username + " received notification: " + message);
    }

    @Override
    public String toString() {
        return "User: " + this.getUsername() + ". " +
                "Full name: " + this.getFullName() + ". " +
                "Email: " + this.getEmail() + ". " +
                "ID: " + this.getId() + ". ";
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
