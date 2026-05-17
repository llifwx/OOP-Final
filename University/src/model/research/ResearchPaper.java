package model.research;

import interfaces.Researcher;
import model.social.Journal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ResearchPaper implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int idCounter = 0;
    private int id;
    private String title;
    private List<Researcher> authors;
    private Journal journal;
    private int citations;
    private int pages;
    private Date publishDate;
    private String doi;

    public ResearchPaper(String title, List<Researcher> authors, Journal journal, int citations, int pages, Date publishDate, String doi) {
        this.id = ++idCounter;
        this.title = title;
        this.authors = authors == null ? new ArrayList<>() : new ArrayList<>(authors);
        this.journal = journal;
        this.citations = validateNonNegative(citations, "Citations");
        this.pages = validateNonNegative(pages, "Pages");
        this.publishDate = publishDate == null ? new Date() : new Date(publishDate.getTime());
        this.doi = doi;
    }

    public static void synchronizeIdCounter(int maxId) {
        if (maxId > idCounter) {
            idCounter = maxId;
        }
    }

    public void ensureId() {
        if (id <= 0) {
            id = ++idCounter;
        }
    }

    public int getId() {return id;}

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

    @Override
    public String toString() {
        return "ResearchPaper{" + "id=" + id + ", title='" + title + '\'' + ", doi='" + doi + '\''
                + ", citations=" + citations + ", pages=" + pages + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ResearchPaper paper)) return false;
        return id == paper.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
