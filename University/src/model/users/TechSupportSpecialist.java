package model.users;

import enums.Language;
import model.support.TechSupportReq;

import java.util.Date;
import java.util.List;

public class TechSupportSpecialist extends Employee {
    private static final long serialVersionUID = 1L;

    public TechSupportSpecialist(String username, String password, String fullName, String email, Language language, String employeeId, String department, double salary, Date hierDate) {
        super(username, password, fullName, email, language, employeeId, department, salary, hierDate);
    }

    @Override
    public String toString() {
        return "TechSupportSpecialist{" + "username='" + getUsername() + '\'' + ", fullName='" + getFullName() + '\'' + ", email='" + getEmail() + '\'' + ", language=" + getLanguage() + ", employeeId='" + getEmployeeId() + '\'' + ", department='" + getDepartment();
    }
}
