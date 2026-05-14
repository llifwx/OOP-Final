package services;

import enums.RequestStatus;
import model.support.TechSupportReq;
import model.users.Employee;
import model.users.TechSupportSpecialist;
import model.users.User;
import storage.Database;
import utils.LogRecord;

import java.util.ArrayList;
import java.util.List;

public class TechSupportService {
    private final Database database;
    private final AuthService authService;

    public TechSupportService(Database database, AuthService authService) {
        this.database = database;
        this.authService = authService;
    }

    // Helper for auth checker
    private Employee requireEmployee() {
        User current = authService.getCurrentUser();
        if (!(current instanceof Employee employee)) {
            throw new SecurityException("[TechSupportService] : Access denied. Current user is not an Employee.");
        }
        return employee;
    }

    private TechSupportSpecialist requireSpecialist() {
        User current = authService.getCurrentUser();
        if (!(current instanceof TechSupportSpecialist techSupportSpecialist)) {
            throw new SecurityException("[TechSupportService] : Access denied. Current user is not a Tech Support Specialist.");
        }
        return techSupportSpecialist;
    }

    public TechSupportReq submitRequest(String description) {
        Employee sender = requireEmployee();

        if (description == null || description.isBlank()) {
            System.out.println("[TechSupportService] : Description cannot be empty.");
            return null;
        }

        int id = database.getTechSupportReqs().size() + 1;
        TechSupportReq req = new TechSupportReq(id, sender, description);

        database.addTechSupportReq(req);
        database.save();

        log("Submitted tech support request: " + description);
        System.out.println("[TechSupportService] : Tech support request submitted.");
        return req;
    }

    public List<TechSupportReq> getMyRequests() {
        Employee current = requireEmployee();

        List<TechSupportReq> res = new ArrayList<>();
        for (TechSupportReq req : database.getTechSupportReqs()) {
            if (req.getSender().equals(current)) {
                res.add(req);
            }
        }

        if (res.isEmpty()) {
            System.out.println("[TechSupportService] : No tech support requests found for current user.");
        }

        return res;
    }

    public List<TechSupportReq> viewNewRequests() {
        requireSpecialist();

        List<TechSupportReq> reqs = database.findTechSupportReqsByStatus(RequestStatus.NEW);
        if (reqs.isEmpty()) {
            System.out.println("[TechSupportService] : No new tech support requests found.");
        } else {
            System.out.println("[TechSupportService] : Found " + reqs.size() + " new tech support request(s).");
        }

        return reqs;
    }

    public TechSupportReq viewRequest(int id) {
        User current = authService.getCurrentUser();
        if (!(current instanceof Employee)) {
            throw new SecurityException("[TechSupportService] : Access denied. Current user is not an Employee.");
        }

        TechSupportReq req = findById(id);
        if (req == null) return null;

        boolean isSender = req.getSender().equals(current);
        boolean isSpecialist = current instanceof TechSupportSpecialist;

        if (!isSender && !isSpecialist) {
            System.out.println("[TechSupportService] : Access denied. You can only view your own requests or, if you are a specialist, any request.");
            return null;
        }

        if (isSpecialist && req.getStatus() == RequestStatus.NEW) {
            req.setStatus(RequestStatus.VIEWED);
            database.save();
            log("Viewed tech support request ID " + id);
        }

        return req;
    }

    public boolean acceptRequest(int id) {
        requireSpecialist();

        TechSupportReq req = findById(id);
        if (req == null) return false;

        if (req.getStatus() != RequestStatus.NEW && req.getStatus() != RequestStatus.VIEWED) {
            System.out.println("[TechSupportService] : Cannot accept request. Current status is " + req.getStatus() + ".");
            return false;
        }

        req.setStatus(RequestStatus.ACCEPTED);
        database.save();

        log("Accepted tech support request ID " + id);
        System.out.println("[TechSupportService] : Tech support request ID " + id);

        return true;
    }

    public boolean rejectRequest(int id, String reason) {
        requireSpecialist();

        TechSupportReq req = findById(id);
        if (req == null) return false;

        if (req.getStatus() != RequestStatus.NEW && req.getStatus() != RequestStatus.VIEWED) {
            System.out.println("[TechSupportService] : Cannot reject request. Current status is " + req.getStatus() + ".");
            return false;
        }

        if (reason == null || reason.isBlank()) {
            System.out.println("[TechSupportService] : Rejection reason cannot be empty.");
        }

        req.setStatus(RequestStatus.REJECTED);
        database.save();

        log("Rejected tech support request ID " + id + " with reason: " + reason);
        System.out.println("[TechSupportService] : Tech support request ID " + id);
        return true;
    }

    public boolean markAsDone(int id) {
        requireSpecialist();

        TechSupportReq req = findById(id);
        if (req == null) return false;

        if (req.getStatus() != RequestStatus.ACCEPTED) {
            System.out.println("[TechSupportService] : Cannot mark request as done. Current status is " + req.getStatus() + ".");
            return false;
        }

        req.setStatus(RequestStatus.DONE);
        database.save();

        log("Marked tech support request ID " + id + " as done");
        System.out.println("[TechSupportService] : Tech support request ID " + id);
        return true;
    }

    public List<TechSupportReq> getRequestsByStatus(RequestStatus status) {
        requireSpecialist();

        if (status == null) {
            System.out.println("[TechSupportService] : Status cannot be null.");
            return new ArrayList<>();
        }

        List<TechSupportReq> res = database.findTechSupportReqsByStatus(status);

        System.out.println("[TechSupportService] : Found " + res.size() + " tech support request(s) with status " + status + ".");
        return res;
    }

    public void printAllRequests() {
        requireSpecialist();

        List<TechSupportReq> all = database.getTechSupportReqs();
        if (all.isEmpty()) {
            System.out.println("[TechSupportService] : No tech support requests found.");
            return;
        }

        System.out.println("─────Tech Support Requests (" + all.size() + ")─────");
        for (TechSupportReq req : all) {
            System.out.println("ID: " + req.getId() + " | Sender: " + req.getSender()
                    .getUsername() + " | Status: " + req.getStatus());
        }

        System.out.println("───────────────────────────────────────────────");
    }

    public void printMyRequests() {
        List<TechSupportReq> mine = getMyRequests();
        if (mine.isEmpty()) return;

        System.out.println("─────My Tech Support Requests (" + mine.size() + ")─────");
        for (TechSupportReq req : mine) {
            System.out.println("ID: " + req.getId() + " | Status: " + req.getStatus() + "\n    Description: " + req.getDescription());
        }

        System.out.println("───────────────────────────────────────────────");
    }

    // Helpers
    private void log(String action) {
        User actor = authService.getCurrentUser();
        if (actor != null) {
            database.addLog(new LogRecord(actor, action));
        }
    }

    private TechSupportReq findById(int id) {
        TechSupportReq req = database.findTechSupportReqById(id);
        if (req == null) {
            System.out.println("[TechSupportService] : Tech support request with ID " + id + " not found.");
        }

        return req;
    }
}
