package model.users;


import enums.Language;

public class Admin extends User{
    private List<LogRecord> logs;

    public Admin(String username, String password, String fullName, String email, Language language, List<LogRecord> logs) {
        super(username, password, fullName, email, language);
        this.logs = logs;
    }

    public List<LogRecord> getLogs() {return logs;}

    public void addUser(User user) {}
    public void removeUser(User user) {}
    public void updateUser(User user) {}
    public List<LogRecord> viewLogFiles() {}

    @Override
    public String toString() {
        return "Logs: " + this.getLogs();
    }
}
