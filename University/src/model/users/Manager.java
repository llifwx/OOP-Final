package model.users;

import model.academic.Report;
import enums.Language;
import enums.LessonType;
import enums.ManagerType;
import model.academic.Course;
import model.social.Message;
import model.social.News;

import java.util.Date;
import java.util.List;

public class Manager extends Employee {
    private static final long serialVersionUID = 1L;
    private ManagerType managerType;

    public Manager(String username, String password, String fullName, String email, Language language, String employeeId, String department, double salary, Date hierDate, List<Message> inbox, ManagerType managerType) {
        super(username, password, fullName, email, language, employeeId, department, salary, hierDate, inbox);
        this.managerType = managerType;
    }

    public ManagerType getManagerType() {
        return managerType;
    }

    public void setManagerType(ManagerType managerType) {
        this.managerType = managerType;
    }
}
