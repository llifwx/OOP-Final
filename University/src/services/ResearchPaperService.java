package services;

import enums.NewsTopic;
import enums.Format;
import interfaces.Researcher;
import model.research.ResearchPaper;
import model.social.Journal;
import model.social.News;
import model.users.GraduateStudent;
import model.users.Teacher;
import model.users.User;
import storage.Database;
import utils.LogRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ResearchPaperService {
    private final Database database;
    private final AuthService authService;
    private final JournalService journalService;

    public ResearchPaperService(Database database, AuthService authService, JournalService journalService) {
        this.database = database;
        this.authService = authService;
        this.journalService = journalService;
    }

    public List<ResearchPaper> getAllPapers() {
        return database.getResearchPapers();
    }

    public void printPaperCitation(ResearchPaper paper, Format format) {
        System.out.println(getCitation(paper, format));
    }

    public void printPaperInfo(ResearchPaper paper) {
        if (paper == null) {
            System.out.println("[ResearchPaperService] Paper is not available.");
            return;
        }
        System.out.println("Title: " + paper.getTitle());
        System.out.println("DOI: " + paper.getDoi());
        System.out.println("Journal: " + (paper.getJournal() == null ? "N/A" : paper.getJournal().getName()));
        System.out.println("Pages: " + paper.getPages());
        System.out.println("Citations: " + paper.getCitations());
        System.out.println("Date: " + paper.getPublishDate());
        System.out.println("Authors: " + paper.getAuthors());
    }

    public void printPapers(Researcher researcher, Comparator<ResearchPaper> comparator) {
        if (researcher == null) {
            System.out.println("[ResearchPaperService] Researcher is not available.");
            return;
        }
        List<ResearchPaper> papers = getPapersByResearcher(researcher);
        if (papers.isEmpty()) {
            System.out.println("No research papers yet.");
            return;
        }
        if (comparator != null) {
            papers.sort(comparator);
        }
        for (ResearchPaper paper : papers) {
            System.out.println("- " + paper.getTitle() + " | Citations: " + paper.getCitations());
        }
    }

    public String getCitation(ResearchPaper paper, Format format) {
        if (paper == null) {
            return "Unknown paper";
        }

        String authorsStr = paper.getAuthors()
                .stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        if (authorsStr.isBlank()) authorsStr = "Unknown author";

        String safeTitle = paper.getTitle() == null || paper.getTitle().isBlank() ? "Untitled" : paper.getTitle();
        String journalName = paper.getJournal() == null ? "Unknown journal" : paper.getJournal().getName();
        String safeDoi = paper.getDoi() == null || paper.getDoi().isBlank() ? "N/A" : paper.getDoi();
        Date publishDate = paper.getPublishDate();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(publishDate == null ? new Date() : publishDate);

        if (format == Format.PLAIN_TEXT) {
            return authorsStr + " (" + dateStr + "). " + safeTitle + ". " + journalName + ", pp. " + paper.getPages() + ". DOI: " + safeDoi + ". Citations: " + paper.getCitations();
        }

        String key = Integer.toString((safeDoi + safeTitle).hashCode());
        return "@article{" + key + ",\n  title={" + safeTitle + "},\n  author={" + authorsStr + "},\n  journal={" + journalName + "},\n  year={" + dateStr.substring(0, 4) + "},\n  pages={" + paper.getPages() + "},\n  doi={" + safeDoi + "}\n}";
    }

    private List<ResearchPaper> getPapersByResearcher(Researcher researcher) {
        return researcher == null ? new ArrayList<>() : new ArrayList<>(researcher.getPapers());
    }

    public void addPaperToDatabase(ResearchPaper paper) {
        if (paper == null || database.getResearchPapers().contains(paper)) return;
        database.addResearchPaper(paper);
        log("Added research paper to database: " + paper.getTitle());
        database.save();
    }

    public void publishPaper(Researcher researcher, ResearchPaper paper, Journal journal) {
        User current = requireResearcher();
        if (researcher == null || paper == null || journal == null) {
            System.out.println("[ResearchPaperService] Cannot publish paper with empty researcher, paper, or journal.");
            return;
        }
        if (researcher != current) {
            System.out.println("[ResearchPaperService] Cannot publish paper for another researcher.");
            return;
        }

        researcher.addPaper(paper);

        boolean newJournalPaper = !journal.getPapers().contains(paper);
        if (newJournalPaper) {
            journal.addPaper(paper);
            journalService.notifySubscribers(journal);
        }

        if (!database.getResearchPapers().contains(paper)) {
            database.addResearchPaper(paper);
        }

        News news = new News("New Paper Published: " + paper.getTitle(), getCitation(paper, Format.PLAIN_TEXT), NewsTopic.RESEARCH);
        news.pin();
        database.addNews(news);
        log("Published research paper: " + paper.getTitle());
        database.save();
    }

    public boolean addDiplomaPaper(GraduateStudent student, ResearchPaper paper) {
        User current = requireResearcher();
        if (student == null || paper == null) {
            System.out.println("[ResearchPaperService] Student and paper are required.");
            return false;
        }
        if (student != current) {
            System.out.println("[ResearchPaperService] Cannot add diploma paper for another student.");
            return false;
        }
        student.addDiplomaProject(paper);
        if (!database.getResearchPapers().contains(paper)) {
            database.addResearchPaper(paper);
        }
        log("Added diploma paper: " + paper.getTitle());
        database.save();
        return true;
    }

    public List<Journal> getAllJournals() {
        return database.getJournals();
    }

    public Journal findJournalByName(String name) {
        return database.findJournalByName(name);
    }

    private User requireResearcher() {
        User current = authService.getCurrentUser();
        if (!(current instanceof Researcher)) {
            throw new SecurityException("[ResearchPaperService] Access denied: current user is not a Researcher.");
        }
        return current;
    }

    private void log(String action) {
        User actor = authService.getCurrentUser();
        if (actor != null) {
            database.addLog(new LogRecord(actor, action));
        }
    }
}
