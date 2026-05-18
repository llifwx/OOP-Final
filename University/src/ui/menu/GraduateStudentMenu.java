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
import ui.MenuPrinter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import static i18n.I18n.t;

public class GraduateStudentMenu extends StudentMenu {
    private final AuthService authService;
    private final ResearchPaperService paperService;
    private final ResearchProjectService projectService;

    public GraduateStudentMenu(AuthService authService, StudentService studentService, UserService userService, TranscriptService transcriptService, ResearchPaperService paperService, ResearchProjectService projectService, JournalService journalService, Scanner sc) {
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
            MenuPrinter.print(t("grad.title"), t("grad.welcome", student.getFullName()), List.of("1.  " + t("student.courses_available"), "2.  " + t("student.courses_register"), "3.  " + t("student.courses_registered"), "4.  " + t("student.teacher_info"), "5.  " + t("student.marks"), "6.  " + t("student.transcript"), "7.  " + t("student.rate_teacher"), "8.  " + t("student.organizations"), "9.  " + t("student.journals"), "10. " + t("student.notifications"), "11. " + t("student.switch_lang"), "12. " + t("grad.supervisor"), "13. " + t("grad.publish_paper"), "14. " + t("grad.view_papers"), "15. " + t("grad.view_projects"), "16. " + t("grad.join_project"), "17. " + t("grad.diploma"), "0.  " + t("menu.logout")));

            switch (sc.nextLine().trim()) {
                case "1" -> printCourses(studentService.getAvailableCourses(), t("student.courses.available"));
                case "2" -> registerForCourse();
                case "3" -> printCourses(student.getRegisteredCourses(), t("student.courses.mine"));
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
                default -> System.out.println(t("app.invalid"));
            }
        }
    }

    private void viewSupervisor(GraduateStudent student) {
        Researcher supervisor = student.getSupervisor();
        if (supervisor == null) {
            System.out.println(t("grad.no_supervisor"));
            return;
        }
        System.out.println(supervisor);
        System.out.println(t("teacher.hindex_value", supervisor.calculateHIndex()));
    }

    private void publishPaper(GraduateStudent student, boolean diplomaOnly) {
        ResearchPaper paper = readPaper(student);
        if (paper == null) return;
        if (diplomaOnly) {
            paperService.addDiplomaPaper(student, paper);
            System.out.println(t("grad.diploma_added"));
            return;
        }
        paperService.publishPaper(student, paper, paper.getJournal());
        System.out.println(t("grad.paper_added"));
    }

    private ResearchPaper readPaper(GraduateStudent student) {
        String title = promptRequired(t("prompt.title"));
        String journalName = promptRequired(t("prompt.journal_name"));
        int pages = readInt(t("prompt.pages"));
        String doi = promptRequired(t("prompt.doi"));
        int citations = readInt(t("prompt.citations"));
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
        MenuPrinter.print(t("sort.title"), null, List.of("1. " + t("sort.date"), "2. " + t("sort.citations"), "3. " + t("sort.pages"), "0. " + t("menu.back")));
        return switch (sc.nextLine().trim()) {
            case "1" -> new ResearchPaperDateComparator();
            case "2" -> new ResearchPaperCitationComparator();
            case "3" -> new ResearchPaperLengthComparator();
            case "0" -> null;
            default -> {
                System.out.println(t("app.invalid"));
                yield null;
            }
        };
    }

    private void viewProjects() {
        List<ResearchProject> projects = projectService.getAllProjects();
        if (projects.isEmpty()) {
            System.out.println(t("grad.no_projects"));
            return;
        }
        projects.forEach(projectService::printProjectInfo);
    }

    private void joinProject(GraduateStudent student) {
        String topic = promptRequired(t("prompt.project_topic"));
        if (topic == null) return;
        ResearchProject project = projectService.findProjectByTopic(topic);
        if (project == null) {
            System.out.println(t("grad.project_not_found"));
            return;
        }
        try {
            projectService.joinProject(project, student);
            System.out.println(t("grad.project_joined"));
        } catch (NotResearcherEx e) {
            System.out.println(e.getMessage());
        }
    }

    private void diplomaMenu(GraduateStudent student) {
        MenuPrinter.print(t("diploma.title"), null, List.of("1. " + t("diploma.view"), "2. " + t("diploma.add"), "0. " + t("menu.back")));
        switch (sc.nextLine().trim()) {
            case "1" -> {
                if (student.getDiplomaProjects().isEmpty()) System.out.println(t("grad.no_diploma"));
                else student.getDiplomaProjects().forEach(System.out::println);
            }
            case "2" -> publishPaper(student, true);
            case "0" -> {}
            default -> System.out.println(t("app.invalid"));
        }
    }
}