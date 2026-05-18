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
import ui.MenuPrinter;

import java.util.List;
import java.util.Scanner;

import static i18n.I18n.t;

public class TechSupportSpecialistMenu {
    private final TechSupportService techSupportService;
    private final AuthService authService;
    private final MessageService messageService;
    private final UserService userService;
    private final Scanner sc;

    public TechSupportSpecialistMenu(TechSupportService techSupportService, AuthService authService, MessageService messageService, UserService userService, Scanner sc) {
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
            MenuPrinter.print(t("tech.title"), t("teacher.welcome", specialist.getFullName()), List.of("1.  " + t("tech.view_new"), "2.  " + t("tech.view_by_id"), "3.  " + t("tech.accept"), "4.  " + t("tech.reject"), "5.  " + t("tech.mark_done"), "6.  " + t("tech.by_status"), "7.  " + t("tech.view_all"), "8.  " + t("tech.my_requests"), "9.  " + t("tech.submit"), "10. " + t("msg.title"), "11. " + t("teacher.switch_lang"), "0.  " + t("menu.logout")));

            switch (sc.nextLine().trim()) {
                case "1" -> printRequests(techSupportService.viewNewRequests(), t("tech.view_new"));
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
                default -> System.out.println(t("app.invalid"));
            }
        }
    }

    private void openRequest() {
        int id = readInt(t("prompt.request_id"));
        if (id < 0) return;
        TechSupportReq req = techSupportService.viewRequest(id);
        if (req != null) printRequestDetails(req);
    }

    private void acceptRequest() {
        int id = readInt(t("prompt.request_id"));
        if (id >= 0) techSupportService.acceptRequest(id);
    }

    private void rejectRequest() {
        int id = readInt(t("prompt.request_id"));
        String reason = promptRequired(t("prompt.reason"));
        if (id >= 0 && reason != null) techSupportService.rejectRequest(id, reason);
    }

    private void markAsDone() {
        int id = readInt(t("prompt.request_id"));
        if (id >= 0) techSupportService.markAsDone(id);
    }

    private void filterByStatus() {
        RequestStatus status = readStatus();
        if (status != null) printRequests(techSupportService.getRequestsByStatus(status), "Status: " + status);
    }

    private void submitRequest() {
        String description = promptRequired(t("prompt.description"));
        if (description != null) techSupportService.submitRequest(description);
    }

    private void messagesMenu() {
        MenuPrinter.print(t("msg.title"), null, List.of("1. " + t("msg.send"), "2. " + t("msg.inbox"), "3. " + t("msg.sent"), "4. " + t("msg.open"), "5. " + t("msg.mark_read"), "0. " + t("menu.back")));
        switch (sc.nextLine().trim()) {
            case "1" -> sendMessage();
            case "2" -> messageService.printInbox();
            case "3" -> messageService.printSentMessages();
            case "4" -> {
                int id = readInt(t("prompt.message_id"));
                if (id >= 0) System.out.println(messageService.openMessage(id));
            }
            case "5" -> messageService.markAllRead();
            case "0" -> {}
            default -> System.out.println(t("app.invalid"));
        }
    }

    private void sendMessage() {
        User user = userService.findById(readInt(t("prompt.receiver_id")));
        if (!(user instanceof Employee employee)) {
            System.out.println(t("msg.receiver_not_found"));
            return;
        }
        String text = promptRequired(t("prompt.message"));
        if (text != null) messageService.sendMessage(employee, text);
    }

    private void printRequests(List<TechSupportReq> requests, String title) {
        if (requests.isEmpty()) {
            System.out.println(t("tech.no_requests"));
            return;
        }
        System.out.println("--- " + title + " ---");
        requests.forEach(req -> System.out.println("#" + req.getId() + " | " + req.getStatus() + " | " + req.getSender()
                .getUsername() + " | " + req.getDescription()));
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
        System.out.print(t("prompt.status") + ": ");
        try {
            return RequestStatus.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println(t("invalid.status"));
            return null;
        }
    }

    private Language readLanguage() {
        System.out.print(t("prompt.language") + ": ");
        try {
            return Language.valueOf(sc.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println(t("lang.invalid"));
            return null;
        }
    }

    private int readInt(String label) {
        System.out.print(label + ": ");
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println(t("invalid.number"));
            return -1;
        }
    }

    private String promptRequired(String label) {
        System.out.print(label + ": ");
        String value = sc.nextLine().trim();
        if (value.isEmpty()) {
            System.out.println(t("prompt.cannot_empty", label));
            return null;
        }
        return value;
    }
}