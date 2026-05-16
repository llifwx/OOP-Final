package ui;

import enums.RequestStatus;
import model.support.TechSupportReq;
import model.users.TechSupportSpecialist;
import services.AuthService;
import services.TechSupportService;
import utils.UserNamePadding;

import java.util.List;
import java.util.Scanner;

public class TechSupportSpecialistMenu {
    private final TechSupportService techSupportService;
    private final AuthService authService;
    private final Scanner sc;
    private final UserNamePadding padding = new UserNamePadding();

    public TechSupportSpecialistMenu(TechSupportService techSupportService, AuthService authService, Scanner sc) {
        this.techSupportService = techSupportService;
        this.authService = authService;
        this.sc = sc;
    }

    public void show() {
        TechSupportSpecialist specialist = (TechSupportSpecialist) authService.getCurrentUser();
        boolean running = true;

        while (running) {
            int newCount = techSupportService.viewNewRequests().size();
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║         TECH SUPPORT SPECIALIST      ║");
            System.out.println("║         " + padding.padRight("Welcome, " + specialist.getFullName(), 29) + "║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.printf("║  1. View new requests  (%s)         ║%n", padding.padRight(String.valueOf(newCount), 3));
            System.out.println("║  2. View all requests                ║");
            System.out.println("║  3. Filter by status                 ║");
            System.out.println("║  4. Open a request                   ║");
            System.out.println("║  5. Accept request                   ║");
            System.out.println("║  6. Reject request                   ║");
            System.out.println("║  7. Mark as done                     ║");
            System.out.println("║  0. Logout                           ║");
            System.out.println("╚══════════════════════════════════════╝");

            System.out.print("Your choice: ");

            switch (sc.nextLine().trim()) {
                case "1" -> viewNewRequest();
                case "2" -> techSupportService.printAllRequests();
                case "3" -> filteredByStatus();
                case "4" -> openRequest();
                case "5" -> acceptRequest();
                case "6" -> rejectRequest();
                case "7" -> markAsDone();
                case "0" -> {
                    authService.logout();
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Menu actions
    private void viewNewRequest() {
        List<TechSupportReq> reqs = techSupportService.viewNewRequests();

        if (reqs.isEmpty()) return;
        printRequestList(reqs, "New requests");
    }

    private void filteredByStatus() {
        System.out.println("Available statuses: NEW, VIEWED, ACCEPTED, REJECTED, DONE");
        System.out.print("Enter status: ");
        String input = sc.nextLine().trim().toUpperCase();

        RequestStatus status;
        try {
            status = RequestStatus.valueOf(input);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status. Please try again.");
            return;
        }

        List<TechSupportReq> reqs = techSupportService.getRequestsByStatus(status);
        if (!reqs.isEmpty()) {
            printRequestList(reqs, "Requests with status: " + status);
        }
    }

    private void openRequest() {
        System.out.println("Enter request ID to open: ");
        int id = readInt();

        if (id < 0) return;
        TechSupportReq req = techSupportService.viewRequest(id);
        if (req != null) {
            printRequestDetails(req);
        }
    }

    private void acceptRequest() {
        System.out.println("Enter request ID to accept: ");
        int id = readInt();

        if (id < 0) return;
        techSupportService.acceptRequest(id);
    }

    private void rejectRequest() {
        System.out.println("Enter request ID to reject: ");
        int id = readInt();

        if (id < 0) return;
        System.out.println("Enter reason for rejection: ");
        String reason = sc.nextLine().trim();
        techSupportService.rejectRequest(id, reason);
    }

    private void markAsDone() {
        System.out.println("Enter request ID to mark as done: ");
        int id = readInt();

        if (id < 0) return;
        techSupportService.markAsDone(id);
    }

    // Utils
    private void printRequestList(List<TechSupportReq> reqs, String title) {
        System.out.println("\n─── " + title + " (" + reqs.size() + ") ─────────────────────────");
        for (TechSupportReq req : reqs) {
            System.out.printf("[#%-3d] %-10s | From: %-20s | %s%n", req.getId(), req.getStatus(), req.getSender()
                    .getUsername(), req.getDescription());
        }
        System.out.println("──────────────────────────────────────────────────");
    }

    private void printRequestDetails(TechSupportReq req) {
        System.out.println("\n─── Request Details ─────────────────────────");
        System.out.printf("ID: %d%n", req.getId());
        System.out.printf("Status: %s%n", req.getStatus());
        System.out.printf("From: %s%n", req.getSender().getUsername());
        System.out.printf("Description: %s%n", req.getDescription());
        System.out.printf("Created: %s%n", req.getCreatedDate());
        System.out.println("──────────────────────────────────────────────");
    }

    private int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return -1;
        }
    }
}
