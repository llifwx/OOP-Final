package model.users;


import enums.Language;
import storage.Database;
import utils.LogRecord;

import java.util.ArrayList;
import java.util.List;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin(String username, String password, String fullName, String email, Language language) {
        super(username, password, fullName, email, language);
    }

    public List<LogRecord> getLogs() {return new ArrayList<>(Database.getInstance().getLogs());}

    public void addUser(User user) {
        if (user != null && Database.getInstance().findUserByUsername(user.getUsername()) == null) {
            Database.getInstance().addUser(user);
            Database.getInstance().save();
        }
    }

    public void removeUser(User user) {
        if (user != null) {
            Database.getInstance().getUsers().remove(user);
            Database.getInstance().save();
        }
    }

    public void updateUser(User user) {
        if (user != null) {
            Database.getInstance().save();
        }
    }

    public List<LogRecord> viewLogFiles() {return getLogs();}

    @Override
    public String toString() {
        return "Admin: " + getUsername() + ". Full name: " + getFullName() + ". ID: " + getId() + ".";
    }
}
