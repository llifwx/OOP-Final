package services;

import enums.NewsTopic;
import enums.Format;
import interfaces.Researcher;
import model.research.ResearchPaper;
import model.social.Journal;
import model.social.News;
import model.users.GraduateStudent;
import model.users.Teacher;
import storage.Database;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ResearchPaperService {
    private static ResearchPaperService instance;

    private ResearchPaperService() {
    }

    public static ResearchPaperService getInstance() {
        if (instance == null) instance = new ResearchPaperService();
        return instance;
    }

    public List<ResearchPaper> getAllPapers() {
        return db().getResearchPapers();
    }

    public void printPaperCitation(ResearchPaper paper, Format format) {
        System.out.println(getCitation(paper, format));
    }

    public void printPaperInfo(ResearchPaper paper) {
        System.out.println("Title: " + paper.getTitle());
        System.out.println("DOI: " + paper.getDoi());
        System.out.println("Journal: " + paper.getJournal().getName());
        System.out.println("Pages: " + paper.getPages());
        System.out.println("Citations: " + paper.getCitations());
        System.out.println("Date: " + paper.getPublishDate());
        System.out.println("Authors: " + paper.getAuthors());
    }

    public void printPapers(Researcher researcher, Comparator<ResearchPaper> comparator) {
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
        db().addResearchPaper(paper);
        db().save();
    }

    public void publishPaper(Researcher researcher, ResearchPaper paper, Journal journal) {
        if (researcher == null || paper == null || journal == null) {
            System.out.println("[ResearchPaperService] Cannot publish paper with empty researcher, paper, or journal.");
            return;
        }

        if (researcher instanceof GraduateStudent graduateStudent) {
            graduateStudent.addPaper(paper);
        } else if (researcher instanceof Teacher teacher) {
            teacher.addPaper(paper);
        }

        journal.addPaper(paper);
        JournalService.getInstance().notifySubscribers(journal);

        if (!db().getResearchPapers().contains(paper)) {
            db().addResearchPaper(paper);
        }

        News news = new News("New Paper Published: " + paper.getTitle(),
                getCitation(paper, Format.PLAIN_TEXT), NewsTopic.RESEARCH);
        news.pin();
        db().addNews(news);
        db().save();
    }

    public List<Journal> getAllJournals() {
        return db().getJournals();
    }

    public Journal findJournalByName(String name) {
        return db().findJournalByName(name);
    }

    private Database db() {
        return Database.getInstance();
    }
}
