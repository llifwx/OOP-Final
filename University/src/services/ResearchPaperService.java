package services;

import enums.Format;
import model.research.ResearchPaper;
import model.social.Journal;
import storage.Database;
import java.util.List;
import java.util.Scanner;

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
        System.out.println(paper.getCitation(format));
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

    public void addPaperToDatabase(ResearchPaper paper) {
        db().addResearchPaper(paper);
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
