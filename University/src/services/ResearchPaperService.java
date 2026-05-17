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
    private static ResearchPaperService instance;
    private final Database database;
    private final AuthService authService;
    private final JournalService journalService;

    public ResearchPaperService(Database database, AuthService authService, JournalService journalService) {
        this.database = database;
        this.authService = authService;
        this.journalService = journalService;
    }

    public static ResearchPaperService getInstance() {
        if (instance == null) instance = new ResearchPaperService(Database.getInstance(), null, JournalService.getInstance());
        return instance;
    }

    public List<ResearchPaper> getAllPapers() {
        return new ArrayList<>(db().getResearchPapers());
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

        String authorsStr = paper.getAuthors().stream()
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
            return authorsStr + " (" + dateStr + "). " + safeTitle + ". " + journalName
                    + ", pp. " + paper.getPages() + ". DOI: " + safeDoi + ". Citations: " + paper.getCitations();
        }

        String key = Integer.toString((safeDoi + safeTitle).hashCode());
        return "@article{" + key + ",\n  title={" + safeTitle + "},\n  author={" + authorsStr
                + "},\n  journal={" + journalName + "},\n  year={" + dateStr.substring(0, 4)
                + "},\n  pages={" + paper.getPages() + "},\n  doi={" + safeDoi + "}\n}";
    }

    private List<ResearchPaper> getPapersByResearcher(Researcher researcher) {
        if (researcher instanceof GraduateStudent graduateStudent) {
            return new ArrayList<>(graduateStudent.getPapers());
        }
        if (researcher instanceof Teacher teacher) {
            return new ArrayList<>(teacher.getPapers());
        }
        return new ArrayList<>();
    }

    public void addPaperToDatabase(ResearchPaper paper) {
        requireResearcher();
        if (paper == null) {
            System.out.println("[ResearchPaperService] Paper cannot be null.");
            return;
        }
        if (isPaperInDatabase(paper)) {
            System.out.println("[ResearchPaperService] Paper already exists in database.");
            return;
        }
        db().addResearchPaper(paper);
        db().save();
        log("Added research paper to database: " + paper.getTitle());
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

        if (researcher instanceof GraduateStudent graduateStudent) {
            graduateStudent.addPaper(paper);
        } else if (researcher instanceof Teacher teacher) {
            teacher.addPaper(paper);
        }

        journal.addPaper(paper);
        journalService.notifySubscribers(journal);

        if (!isPaperInDatabase(paper)) {
            db().addResearchPaper(paper);
        }

        String newsTitle = "New Paper Published: " + paper.getTitle();
        if (db().findNewsByTitle(newsTitle) == null) {
            News news = new News(newsTitle, getCitation(paper, Format.PLAIN_TEXT), NewsTopic.RESEARCH);
            news.pin();
            db().addNews(news);
        }
        createTopCitedResearcherNews();

        db().save();
        log("Published research paper: " + paper.getTitle());
        System.out.println("[ResearchPaperService] Research paper '" + paper.getTitle() + "' published.");
    }

    public List<Journal> getAllJournals() {
        return new ArrayList<>(db().getJournals());
    }

    public Journal findJournalByName(String name) {
        if (name == null || name.isBlank()) return null;
        return db().findJournalByName(name);
    }

    private Database db() {
        return database;
    }

    private User requireResearcher() {
        User current = authService == null ? null : authService.getCurrentUser();
        if (!(current instanceof Researcher)) {
            throw new SecurityException("[ResearchPaperService] Access denied: current user is not a researcher.");
        }
        return current;
    }

    private boolean isPaperInDatabase(ResearchPaper paper) {
        if (paper == null) return false;
        if (db().getResearchPapers().contains(paper)) return true;
        for (ResearchPaper existing : db().getResearchPapers()) {
            if (existing == null) continue;
            if (paper.getDoi() != null && existing.getDoi() != null
                    && existing.getDoi().equalsIgnoreCase(paper.getDoi())) {
                return true;
            }
            if (paper.getTitle() != null && existing.getTitle() != null
                    && existing.getTitle().equalsIgnoreCase(paper.getTitle())) {
                return true;
            }
        }
        return false;
    }

    private void createTopCitedResearcherNews() {
        Researcher topResearcher = null;
        int maxCitations = 0;

        for (User user : db().getUsers()) {
            if (!(user instanceof Researcher researcher)) continue;
            int citations = getPapersByResearcher(researcher).stream()
                    .mapToInt(ResearchPaper::getCitations)
                    .sum();
            if (citations > maxCitations) {
                maxCitations = citations;
                topResearcher = researcher;
            }
        }

        if (topResearcher == null || maxCitations <= 0) return;

        String researcherName = topResearcher instanceof User user ? user.getFullName() : topResearcher.toString();
        String title = "Top Cited Researcher: " + researcherName;
        if (db().findNewsByTitle(title) != null) return;

        News news = new News(title,
                researcherName + " leads research citations with " + maxCitations + " total citation(s).",
                NewsTopic.RESEARCH);
        news.pin();
        db().addNews(news);
    }

    private void log(String action) {
        User actor = authService == null ? null : authService.getCurrentUser();
        if (actor != null) {
            db().addLog(new LogRecord(actor, action));
            db().save();
        }
    }
}
