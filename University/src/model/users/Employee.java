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


    public Employee(String username, String password, String fullName, String email, Language language, String employeeId, String department, double salary, Date hierDate) {
        super(username, password, fullName, email, language);
        this.employeeId = employeeId;
        this.department = department;
        this.salary = salary;
        this.hierDate = hierDate;
    }

    public String getEmployeeId() {return this.employeeId;}

    public String getDepartment() {return this.department;}

    @Override
    public String toString() {
        return "User: " + this.getUsername() + ". " + "Full name: " + this.getFullName() + ". " + "Email: " + this.getEmail() + ". " + "ID: " + this.getId() + ". " + "EmployeeID: " + this.getEmployeeId() + "Department: " + this.getDepartment();
    }
}
