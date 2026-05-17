package model.support;

import model.users.Employee;
import enums.RequestStatus;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class TechSupportReq implements Serializable {
    private static final long serialVersionUID = 1L;
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

    public void setStatus(RequestStatus status) {this.status = status;}

    public RequestStatus getStatus() {return this.status;}

    public int getId() {
        return id;
    }

    public Employee getSender() {
        return sender;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreatedDate() {
        return createdDate == null ? null : new Date(createdDate.getTime());
    }

    @Override
    public String toString() {
        return "TechSupportReq{" + "id=" + id + ", sender="
                + (sender != null ? sender.getUsername() : "N/A") + ", status="
                + status + ", createdDate=" + createdDate + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TechSupportReq request)) return false;
        return id == request.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
