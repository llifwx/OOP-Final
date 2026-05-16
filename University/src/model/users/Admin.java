package model.users;


import enums.Language;
import utils.LogRecord;

import java.util.List;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin(String username, String password, String fullName, String email, Language language) {
        super(username, password, fullName, email, language);
    }

    public List<LogRecord> getLogs() {return logs;}

    public void addUser(User user) {}

    public void removeUser(User user) {}

    public void updateUser(User user) {}

    public List<LogRecord> viewLogFiles() {return logs;}

    @Override
    public String toString() {
        return "Admin: " + getUsername() + ". Full name: " + getFullName() + ". ID: " + getId() + ".";
    }
}
