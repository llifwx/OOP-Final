package model.users;

import enums.Language;
import model.support.TechSupportReq;

import java.util.Date;
import java.util.List;

public class TechSupportSpecialist extends Employee {
    private static final long serialVersionUID = 1L;

    private List<TechSupportReq> requests;

    public TechSupportSpecialist(String username, String password, String fullName, String email, Language language, String employeeId, String department, double salary, Date hierDate, List<Message> inbox, List<TechSupportReq> requests) {
        super(username, password, fullName, email, language, employeeId, department, salary, hierDate, inbox);
        this.requests = requests;
    }

    public List<TechSupportReq> getRequests() {return requests;}

    public List<TechSupportReq> viewNewRequests() {}

    public void acceptRequest(TechSupportReq req) {}

    public void rejectRequest(TechSupportReq req) {}

    public void markAsDone(TechSupportReq req) {}

    @Override
    public String toString() {
        return "Requests: " + this.getRequests();
    }
}
