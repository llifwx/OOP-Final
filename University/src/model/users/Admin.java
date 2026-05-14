package model.users;


import enums.Language;
import utils.LogRecord;

import java.util.List;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin(String username, String password, String fullName, String email, Language language) {
        super(username, password, fullName, email, language);
    }

    @Override
    public String toString() {
        return "Admin: " + this.getUsername() + ". " + "Full name: " + this.getFullName() + ". " + "Email: " + this.getEmail() + ". " + "ID: " + this.getId();
    }
}
