package model.research;

import interfaces.Researcher;
import model.social.Journal;

import java.util.Date;
import java.util.List;

public class ResearchPaper {
    private String title;
    private List<Researcher> authors;
    private Journal journal;
    private int citations;
    private int pages;
    private Date publishDate;
    private String doi;

    public ResearchPaper(String title, List<Researcher> authors, Journal journal, int citations, int pages, Date publishDate, String doi) {
        this.title = title;
        this.authors = authors;
        this.journal = journal;
        this.citations = citations;
        this.pages = pages;
        this.publishDate = publishDate;
        this.doi = doi;
    }

    public String getTitle() {return title;}
    public List<Researcher> getAuthors() {return authors;}
    public Journal getJournal() {return journal;}
    public int getCitations() {return citations;}
    public int getPages() {return pages;}
    public Date getPublishDate() {return publishDate;}
    public String getDoi() {return doi;}

    public void addCitation() {}
}
