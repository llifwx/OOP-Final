package model.users;

import enums.Language;

import java.util.Date;

public class TechSupportSpecialist extends Employee {
    private static final long serialVersionUID = 1L;

    public TechSupportSpecialist(String username, String password, String fullName, String email, Language language, String employeeId, String department, double salary, Date hierDate) {
        super(username, password, fullName, email, language, employeeId, department, salary, hierDate);
    }

    @Override
    public String toString() {
        return "TechSupportSpecialist: " + getUsername() + ". Full name: " + getFullName()
                + ". Email: " + getEmail() + ". ID: " + getId()
                + ". EmployeeID: " + getEmployeeId() + ". Department: " + getDepartment()
                + ". Salary: " + getSalary() + ". Hire date: " + getHierDate() + ".";
    }
}
