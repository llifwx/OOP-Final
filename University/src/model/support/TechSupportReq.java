package model.support;

import model.users.Employee;
import enums.RequestStatus;
import java.util.Date;

public class TechSupportReq {
    private int id;
    private Employee sender;
    private String description;
    private RequestStatus status;
    private Date createdDate;

    public TechSupportReq(int id, Employee sender, String description) {
        this.id = id;
        this.sender = sender;
        this.description = description;
        this.status = RequestStatus.NEW;
        this.createdDate = new Date();
    }

    public void setStatus(RequestStatus status) { this.status = status; }
    public RequestStatus getStatus() { return this.status; }
}