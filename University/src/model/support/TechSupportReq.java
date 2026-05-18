package model.support;

import enums.RequestStatus;
import model.users.Employee;
import model.users.TechSupportSpecialist;

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

    private TechSupportSpecialist assignedSpecialist;
    private String rejectionReason;
    private Date resolvedDate;

    public TechSupportReq(int id, Employee sender, String description) {
        this.id = id;
        this.sender = sender;
        this.description = description;
        this.status = RequestStatus.NEW;
        this.createdDate = new Date();
    }

    public int getId() {
        return id;
    }

    public Employee getSender() {
        return sender;
    }

    public String getDescription() {
        return description;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public Date getCreatedDate() {
        return createdDate == null ? null : new Date(createdDate.getTime());
    }

    public TechSupportSpecialist getAssignedSpecialist() {
        return assignedSpecialist;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Date getResolvedDate() {
        return resolvedDate == null ? null : new Date(resolvedDate.getTime());
    }

    public void setStatus(RequestStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Request status cannot be null.");
        }
        this.status = status;
    }

    public void assignTo(TechSupportSpecialist specialist) {
        if (specialist == null) {
            throw new IllegalArgumentException("Assigned specialist cannot be null.");
        }

        this.assignedSpecialist = specialist;
        this.status = RequestStatus.ACCEPTED;
        this.rejectionReason = null;
        this.resolvedDate = null;
    }

    public void reject(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason cannot be empty.");
        }

        this.status = RequestStatus.REJECTED;
        this.rejectionReason = reason.trim();
        this.resolvedDate = new Date();
    }

    public void markDone() {
        if (status != RequestStatus.ACCEPTED) {
            throw new IllegalStateException("Only accepted request can be marked as done.");
        }

        this.status = RequestStatus.DONE;
        this.resolvedDate = new Date();
    }

    @Override
    public String toString() {
        return "TechSupportReq{" + "id=" + id + ", sender=" + (sender != null ? sender.getUsername() : "N/A") + ", status=" + status + ", createdDate=" + createdDate + ", assignedSpecialist=" + (assignedSpecialist != null ? assignedSpecialist.getUsername() : "none") + ", rejectionReason='" + (rejectionReason != null ? rejectionReason : "none") + '\'' + ", resolvedDate=" + resolvedDate + '}';
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