package model.users;


import enums.Language;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin(String username, String password, String fullName, String email, Language language) {
        super(username, password, fullName, email, language);
    }

    @Override
    public String toString() {
        return "Admin: " + getUsername() + ". Full name: " + getFullName() + ". ID: " + getId() + ".";
    }
}
