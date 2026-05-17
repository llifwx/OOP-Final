package ui.menu;

import enums.Language;
import enums.RequestStatus;
import model.support.TechSupportReq;
import model.users.Employee;
import model.users.TechSupportSpecialist;
import model.users.User;
import services.AuthService;
import services.MessageService;
import services.TechSupportService;
import services.UserService;

import java.util.List;
import java.util.Scanner;

public class TechSupportSpecialistMenu {
    private final TechSupportService techSupportService;
    private final AuthService authService;
    private final MessageService messageService;
    private final UserService userService;
    private final Scanner sc;

    public TechSupportSpecialistMenu(TechSupportService techSupportService, AuthService authService,
                                     MessageService messageService, UserService userService, Scanner sc) {
        this.techSupportService = techSupportService;
        this.authService = authService;
        this.messageService = messageService;
        this.userService = userService;
        this.sc = sc;
    }

    public void show() {
        boolean running = true;
        while (running) {
            TechSupportSpecialist specialist = (TechSupportSpecialist) authService.getCurrentUser();
            MenuPrinter.print("TECH SUPPORT", "Welcome, " + specialist.getFullName(), List.of(
                    "1. View new requests",
                    "2. View request by ID",
                    "3. Accept request",
                    "4. Reject request",
                    "5. Mark request as done",
                    "6. View by status",
                    "7. View all requests",
                    "8. My requests",
                    "9. Submit my request",
                    "10. Messages",
                    "11. Switch language",
                    "0. Logout"
            ));

            switch (sc.nextLine().trim()) {
                case "1" -> printRequests(techSupportService.viewNewRequests(), "New requests");
                case "2" -> openRequest();
                case "3" -> acceptRequest();
                case "4" -> rejectRequest();
                case "5" -> markAsDone();
                case "6" -> filterByStatus();
                case "7" -> techSupportService.printAllRequests();
                case "8" -> techSupportService.printMyRequests();
                case "9" -> submitRequest();
                case "10" -> messagesMenu();
                case "11" -> switchLanguage(specialist);
                case "0" -> {
                    authService.logout();
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void openRequest() {
        int id = readInt("Request ID");
        if (id < 0) return;
        TechSupportReq req = techSupportService.viewRequest(id);
        if (req != null) printRequestDetails(req);
    }

    private void acceptRequest() {
        int id = readInt("Request ID");
        if (id >= 0) techSupportService.acceptRequest(id);
    }

    private void rejectRequest() {
        int id = readInt("Request ID");
        String reason = promptRequired("Reason");
        if (id >= 0 && reason != null) techSupportService.rejectRequest(id, reason);
    }

    private void markAsDone() {
        int id = readInt("Request ID");
        if (id >= 0) techSupportService.markAsDone(id);
    }

    private void filterByStatus() {
        RequestStatus status = readStatus();
        if (status != null) printRequests(techSupportService.getRequestsByStatus(status), "Requests: " + status);
    }

    private void submitRequest() {
        String description = promptRequired("Description");
        if (description != null) techSupportService.submitRequest(description);
    }

    private void messagesMenu() {
        MenuPrinter.print("MESSAGES", null, List.of(
                "1. Send message",
                "2. View inbox",
                "3. View sent messages",
                "4. Open message",
                "5. Mark all read",
                "0. Back"
        ));
        switch (sc.nextLine().trim()) {
            case "1" -> sendMessage();
            case "2" -> messageService.printInbox();
            case "3" -> messageService.printSentMessages();
            case "4" -> {
                int id = readInt("Message ID");
                if (id >= 0) System.out.println(messageService.openMessage(id));
            }
            case "5" -> messageService.markAllRead();
            case "0" -> { }
            default -> System.out.println("Invalid choice.");
        }
    }

    private void sendMessage() {
        User user = userService.findById(readInt("Receiver employee ID"));
        if (!(user instanceof Employee employee)) {
            System.out.println("Employee not found.");
            return;
        }
        String text = promptRequired("Message");
        if (text != null) messageService.sendMessage(employee, text);
    }

    private void printRequests(List<TechSupportReq> requests, String title) {
        if (requests.isEmpty()) {
            System.out.println("No requests found.");
            return;
        }
        System.out.println("--- " + title + " ---");
        requests.forEach(req -> System.out.println("#" + req.getId() + " | " + req.getStatus() + " | " + req.getSender().getUsername() + " | " + req.getDescription()));
    }

    private void printRequestDetails(TechSupportReq req) {
        System.out.println("ID: " + req.getId());
        System.out.println("Status: " + req.getStatus());
        System.out.println("From: " + req.getSender().getUsername());
        System.out.println("Description: " + req.getDescription());
        System.out.println("Created: " + req.getCreatedDate());
    }

    private void switchLanguage(User user) {
        Language language = readLanguage();
        if (language != null) userService.changeLanguage(user, language);
    }

    private RequestStatus readStatus() {
        System.out.print("Status (NEW, VIEWED, ACCEPTED, REJECTED, DONE): ");
        try {
            return RequestStatus.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status.");
            return null;
        }
    }

    private Language readLanguage() {
        System.out.print("Language (KZ, EN, RU): ");
        try {
            return Language.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid language.");
            return null;
        }
    }

    private int readInt(String label) {
        System.out.print(label + ": ");
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return -1;
        }
    }

    private String promptRequired(String label) {
        System.out.print(label + ": ");
        String value = sc.nextLine().trim();
        if (value.isEmpty()) {
            System.out.println(label + " cannot be empty.");
            return null;
        }
        return value;
    }
}
