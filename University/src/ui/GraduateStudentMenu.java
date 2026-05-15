package ui.menu;

import comparator.ResearchPaperCitationComparator;
import comparator.ResearchPaperDateComparator;
import comparator.ResearchPaperLengthComparator;
import enums.Format;
import exceptions.InvalidSupervisorEx;
import exceptions.NotResearcherEx;
import interfaces.Researcher;
import model.research.ResearchPaper;
import model.research.ResearchProject;
import model.social.Journal;
import model.users.GraduateStudent;
import services.*;
import utils.UserNamePadding;

import java.util.*;

public class GraduateStudentMenu {
    private final ResearchService researchService;
    private final ResearchPaperService paperService;
    private final ResearchProjectService projectService;
    private final AuthService authService;
    private final Scanner sc;
    private final UserNamePadding padding = new UserNamePadding();

    public GraduateStudentMenu(AuthService authService, Scanner sc) {
        this.authService = authService;
        this.researchService = ResearchService.getInstance();
        this.paperService = ResearchPaperService.getInstance();
        this.projectService = ResearchProjectService.getInstance();
        this.sc = sc;
    }

    public void show() {
        GraduateStudent student = (GraduateStudent) authService.getCurrentUser();
        boolean running = true;

        while (running) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║           GRADUATE STUDENT            ║");
            System.out.println("║  " + padding.padRight("Welcome, " + student.getFullName(), 32) + "║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║  1. View my research papers         ║");
            System.out.println("║  2. Add research paper              ║");
            System.out.println("║  3. View research projects           ║");
            System.out.println("║  4. Join research project           ║");
            System.out.println("║  5. View my supervisor              ║");
            System.out.println("║  0. Exit                            ║");
            System.out.println("╚══════════════════════════════════════╝");

            System.out.print("Your choice: ");

            switch (sc.nextLine().trim()) {
                case "1" -> viewPapers();
                case "2" -> addPaper();
                case "3" -> viewProjects();
                case "4" -> joinProject();
                case "5" -> viewSupervisor();
                case "0" -> running = false;
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void viewPapers() {
        GraduateStudent student = (GraduateStudent) authService.getCurrentUser();
        List<ResearchPaper> papers = student.getPapers();

        if (papers.isEmpty()) {
            System.out.println("You have no research papers yet.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║         YOUR RESEARCH PAPERS        ║");
        System.out.println("╠══════════════════════════════════════╣");

        System.out.println("║  Sort by:                           ║");
        System.out.println("║    1. By date (newest first)         ║");
        System.out.println("║    2. By citations (most first)      ║");
        System.out.println("║    3. By pages (longest first)       ║");
        System.out.println("║    0. Back                          ║");
        System.out.println("╚══════════════════════════════════════╝");

        System.out.print("Your choice: ");
        String choice = sc.nextLine().trim();

        Comparator<ResearchPaper> comparator;
        switch (choice) {
            case "1" -> comparator = new ResearchPaperDateComparator();
            case "2" -> comparator = new ResearchPaperCitationComparator();
            case "3" -> comparator = new ResearchPaperLengthComparator();
            case "0" -> { return; }
            default -> {
                System.out.println("Invalid choice.");
                return;
            }
        }

        student.printPapers(comparator);
    }

    private void addPaper() {
        GraduateStudent student = (GraduateStudent) authService.getCurrentUser();

        System.out.println("\n─── Add New Research Paper ───────────────────");
        System.out.print("Title: ");
        String title = sc.nextLine().trim();
        System.out.print("Journal name: ");
        String journalName = sc.nextLine().trim();
        System.out.print("Pages: ");
        int pages = readInt();
        if (pages < 0) return;
        System.out.print("DOI: ");
        String doi = sc.nextLine().trim();
        System.out.print("Citations (default 0): ");
        int citations = readInt();
        if (citations < 0) citations = 0;

        Journal journal = paperService.findJournalByName(journalName);
        if (journal == null) {
            journal = new Journal(journalName);
            JournalService.getInstance().addJournal(journal);
        }

        List<Researcher> authors = new ArrayList<>();
        authors.add(student);

        ResearchPaper paper = new ResearchPaper(title, authors, journal, citations, pages, new Date(), doi);

        student.publishPaper(paper, journal);
        System.out.println("Research paper added successfully!");
    }

    private void viewProjects() {
        List<ResearchProject> projects = projectService.getAllProjects();

        if (projects.isEmpty()) {
            System.out.println("No research projects available.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║           RESEARCH PROJECTS                 ║");
        System.out.println("╠══════════════════════════════════════════════╣");

        for (int i = 0; i < projects.size(); i++) {
            ResearchProject p = projects.get(i);
            System.out.printf("║  %d. %s%n", i + 1, padding.padRight(p.getTopic(), 38));
            System.out.println("║     Participants: " + p.getParticipants().size() +
                    " | Papers: " + p.getPublishedPapers().size());
        }
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    private void joinProject() {
        GraduateStudent student = (GraduateStudent) authService.getCurrentUser();
        List<ResearchProject> projects = projectService.getAllProjects();

        if (projects.isEmpty()) {
            System.out.println("No research projects available.");
            return;
        }

        System.out.println("\n─── Join Research Project ───────────────────────");
        viewProjects();
        System.out.print("Enter project number to join: ");
        int num = readInt();
        if (num < 1 || num > projects.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        ResearchProject project = projects.get(num - 1);
        try {
            student.joinProject(project);
            System.out.println("Successfully joined project: " + project.getTopic());
        } catch (NotResearcherEx e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewSupervisor() {
        GraduateStudent student = (GraduateStudent) authService.getCurrentUser();
        Researcher supervisor = student.viewSupervisor();

        if (supervisor == null) {
            System.out.println("You don't have a supervisor assigned yet.");
            return;
        }

        System.out.println("\n─── Your Supervisor ────────────────────────────");
        System.out.println("Supervisor: " + supervisor.toString());
        System.out.println("H-Index: " + supervisor.calculateHIndex());
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