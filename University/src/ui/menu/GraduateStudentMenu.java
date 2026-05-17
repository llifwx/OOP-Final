package ui.menu;

import comparator.ResearchPaperCitationComparator;
import comparator.ResearchPaperDateComparator;
import comparator.ResearchPaperLengthComparator;
import exceptions.NotResearcherEx;
import interfaces.Researcher;
import model.research.ResearchPaper;
import model.research.ResearchProject;
import model.social.Journal;
import model.users.GraduateStudent;
import services.AuthService;
import services.JournalService;
import services.ResearchPaperService;
import services.ResearchProjectService;
import services.StudentService;
import services.TranscriptService;
import services.UserService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class GraduateStudentMenu extends StudentMenu {
    private final AuthService authService;
    private final ResearchPaperService paperService;
    private final ResearchProjectService projectService;

    public GraduateStudentMenu(AuthService authService, StudentService studentService, UserService userService,
                               TranscriptService transcriptService, ResearchPaperService paperService,
                               ResearchProjectService projectService, JournalService journalService, Scanner sc) {
        super(authService, studentService, userService, transcriptService, journalService, sc);
        this.authService = authService;
        this.paperService = paperService;
        this.projectService = projectService;
    }

    @Override
    public void show() {
        boolean running = true;
        while (running) {
            GraduateStudent student = (GraduateStudent) authService.getCurrentUser();
            MenuPrinter.print("GRADUATE STUDENT", "Welcome, " + student.getFullName(), List.of(
                    "1. View available courses",
                    "2. Register for course",
                    "3. View registered courses",
                    "4. View teacher info",
                    "5. View marks",
                    "6. View transcript",
                    "7. Rate teacher",
                    "8. Student organizations",
                    "9. Journals",
                    "10. View notifications",
                    "11. Switch language",
                    "12. View supervisor",
                    "13. Publish research paper",
                    "14. View my papers sorted",
                    "15. View research projects",
                    "16. Join research project",
                    "17. Diploma papers",
                    "0. Logout"
            ));

            switch (sc.nextLine().trim()) {
                case "1" -> printCourses(studentService.getAvailableCourses(), "Available courses");
                case "2" -> registerForCourse();
                case "3" -> printCourses(student.getRegisteredCourses(), "Registered courses");
                case "4" -> viewTeacherInfo();
                case "5" -> printMarks(student.getTranscript().getMarks());
                case "6" -> transcriptService.printTranscript(student.getTranscript());
                case "7" -> rateTeacher();
                case "8" -> organizationsMenu(student);
                case "9" -> journalsMenu(student);
                case "10" -> printNotifications(student);
                case "11" -> switchLanguage(student);
                case "12" -> viewSupervisor(student);
                case "13" -> publishPaper(student, false);
                case "14" -> viewPapersSorted(student);
                case "15" -> viewProjects();
                case "16" -> joinProject(student);
                case "17" -> diplomaMenu(student);
                case "0" -> {
                    authService.logout();
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void viewSupervisor(GraduateStudent student) {
        Researcher supervisor = student.getSupervisor();
        if (supervisor == null) {
            System.out.println("No supervisor assigned.");
            return;
        }
        System.out.println(supervisor);
        System.out.println("H-index: " + supervisor.calculateHIndex());
    }

    private void publishPaper(GraduateStudent student, boolean diplomaOnly) {
        ResearchPaper paper = readPaper(student);
        if (paper == null) return;
        if (diplomaOnly) {
            paperService.addDiplomaPaper(student, paper);
            System.out.println("Diploma paper added.");
            return;
        }
        paperService.publishPaper(student, paper, paper.getJournal());
        System.out.println("Research paper published.");
    }

    private ResearchPaper readPaper(GraduateStudent student) {
        String title = promptRequired("Title");
        String journalName = promptRequired("Journal name");
        int pages = readInt("Pages");
        String doi = promptRequired("DOI");
        int citations = readInt("Citations");
        if (title == null || journalName == null || doi == null || pages < 0 || citations < 0) return null;
        Journal journal = paperService.findJournalByName(journalName);
        if (journal == null) {
            journal = new Journal(journalName);
            journalService.addJournal(journal);
        }
        List<Researcher> authors = new ArrayList<>();
        authors.add(student);
        return new ResearchPaper(title, authors, journal, citations, pages, new Date(), doi);
    }

    private void viewPapersSorted(GraduateStudent student) {
        Comparator<ResearchPaper> comparator = readPaperComparator();
        if (comparator != null) paperService.printPapers(student, comparator);
    }

    private Comparator<ResearchPaper> readPaperComparator() {
        MenuPrinter.print("SORT PAPERS", null, List.of(
                "1. Date",
                "2. Citations",
                "3. Pages",
                "0. Back"
        ));
        return switch (sc.nextLine().trim()) {
            case "1" -> new ResearchPaperDateComparator();
            case "2" -> new ResearchPaperCitationComparator();
            case "3" -> new ResearchPaperLengthComparator();
            case "0" -> null;
            default -> {
                System.out.println("Invalid choice.");
                yield null;
            }
        };
    }

    private void viewProjects() {
        List<ResearchProject> projects = projectService.getAllProjects();
        if (projects.isEmpty()) {
            System.out.println("No research projects available.");
            return;
        }
        projects.forEach(projectService::printProjectInfo);
    }

    private void joinProject(GraduateStudent student) {
        String topic = promptRequired("Project topic");
        if (topic == null) return;
        ResearchProject project = projectService.findProjectByTopic(topic);
        if (project == null) {
            System.out.println("Project not found.");
            return;
        }
        try {
            projectService.joinProject(project, student);
            System.out.println("Joined project.");
        } catch (NotResearcherEx e) {
            System.out.println(e.getMessage());
        }
    }

    private void diplomaMenu(GraduateStudent student) {
        MenuPrinter.print("DIPLOMA PAPERS", null, List.of(
                "1. View diploma papers",
                "2. Add diploma paper",
                "0. Back"
        ));
        switch (sc.nextLine().trim()) {
            case "1" -> {
                if (student.getDiplomaProjects().isEmpty()) System.out.println("No diploma papers.");
                else student.getDiplomaProjects().forEach(System.out::println);
            }
            case "2" -> publishPaper(student, true);
            case "0" -> { }
            default -> System.out.println("Invalid choice.");
        }
    }
}
