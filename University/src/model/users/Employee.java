package model.users;

import enums.Language;
import model.social.Message;

import java.util.Date;
import java.util.List;

public class Employee extends User {
    private static final long serialVersionUID = 1L;
    private String employeeId;
    private String department;
    private double salary;
    private Date hierDate;
    private List<Message> inbox;

    public Employee(String username, String password, String fullName, String email, Language language, String employeeId, String department, double salary, Date hierDate, List<Message> inbox) {
        super(username, password, fullName, email, language);
        this.employeeId = employeeId;
        this.department = department;
        this.salary = salary;
        this.hierDate = hierDate;
        this.inbox = inbox;
    }

    public String getEmployeeId() {return this.employeeId;}

    public String getDepartment() {return this.department;}

    public Message sendMessage(Employee receiver, String text) {}

    ;

    public List<Message> viewMessages() {}

    public String getRoleName() {}

    @Override
    public String toString() {
        return "User: " + this.getUsername() + ". " + "Full name: " + this.getFullName() + ". " + "Email: " + this.getEmail() + ". " + "ID: " + this.getId() + ". " + "EmployeeID: " + this.getEmployeeId() + "Department: " + this.getDepartment();
    }
}
