package model.research;

import interfaces.Researcher;
import model.social.Journal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResearchPaper implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title;
    private List<Researcher> authors;
    private Journal journal;
    private int citations;
    private int pages;
    private Date publishDate;
    private String doi;

    public ResearchPaper(String title, List<Researcher> authors, Journal journal, int citations, int pages, Date publishDate, String doi) {
        this.title = title;
        this.authors = authors == null ? new ArrayList<>() : new ArrayList<>(authors);
        this.journal = journal;
        this.citations = validateNonNegative(citations, "Citations");
        this.pages = validateNonNegative(pages, "Pages");
        this.publishDate = publishDate == null ? new Date() : new Date(publishDate.getTime());
        this.doi = doi;
    }

    public String getTitle() {return title;}

    public List<Researcher> getAuthors() {return new ArrayList<>(authors);}

    public Journal getJournal() {return journal;}

    public int getCitations() {return citations;}

    public int getPages() {return pages;}

    public Date getPublishDate() {return publishDate == null ? null : new Date(publishDate.getTime());}

    public String getDoi() {return doi;}

    public void addCitation() {this.citations++;}

    private int validateNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
        return value;
    }
}
