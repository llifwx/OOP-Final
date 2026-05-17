package model.users;

import enums.Language;
import enums.ManagerType;
import java.util.Date;

public class Manager extends Employee {
    private static final long serialVersionUID = 1L;
    private ManagerType managerType;

    public Manager(String username, String password, String fullName, String email, Language language, String employeeId, String department, double salary, Date hierDate, ManagerType managerType) {
        super(username, password, fullName, email, language, employeeId, department, salary, hierDate);
        this.managerType = managerType;
    }

    public ManagerType getManagerType() {
        return managerType;
    }

    public void setManagerType(ManagerType managerType) {
        this.managerType = managerType;
    }
}
