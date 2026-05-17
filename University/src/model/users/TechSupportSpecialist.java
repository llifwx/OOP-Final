package model.users;

import enums.Language;
import enums.RequestStatus;
import model.support.TechSupportReq;
import storage.Database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TechSupportSpecialist extends Employee {
    private static final long serialVersionUID = 1L;

    public TechSupportSpecialist(String username, String password, String fullName, String email, Language language, String employeeId, String department, double salary, Date hierDate) {
        super(username, password, fullName, email, language, employeeId, department, salary, hierDate);
    }

    public List<TechSupportReq> getRequests() {
        return new ArrayList<>(Database.getInstance().getTechSupportReqs());
    }

    public List<TechSupportReq> viewNewRequests() {
        List<TechSupportReq> requests = new ArrayList<>();
        for (TechSupportReq req : Database.getInstance().getTechSupportReqs()) {
            if (req.getStatus() == RequestStatus.NEW) {
                requests.add(req);
            }
        }
        return requests;
    }

    public void acceptRequest(TechSupportReq req) {
        if (req != null) {
            req.setStatus(RequestStatus.ACCEPTED);
            Database.getInstance().save();
        }
    }

    public void rejectRequest(TechSupportReq req) {
        if (req != null) {
            req.setStatus(RequestStatus.REJECTED);
            Database.getInstance().save();
        }
    }

    public void markAsDone(TechSupportReq req) {
        if (req != null) {
            req.setStatus(RequestStatus.DONE);
            Database.getInstance().save();
        }
    }

    @Override
    public String toString() {
        return "TechSupportSpecialist{" + "username='" + getUsername() + '\'' + ", fullName='" + getFullName() + '\'' + ", email='" + getEmail() + '\'' + ", language=" + getLanguage() + ", employeeId='" + getEmployeeId() + '\'' + ", department='" + getDepartment();
    }
}
