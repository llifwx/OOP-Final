package model.users;

import enums.Language;

import java.util.Date;
import java.util.List;

public class Employee extends User {
    private String employed;
    private String department;
    private double salary;
    private Date hierDate;
    private List<Message> inbox;

    public Employee(String username, String password, String fullName, String email, Language language) {
        super(username, password, fullName, email, language);
    }

    public Message sendMessage(Employee receiver, String text) {};

}
