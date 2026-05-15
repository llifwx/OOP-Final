package model.research;

import enums.Format;
import interfaces.Researcher;
import model.social.Journal;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    public void addCitation() {this.citations++;}

    public String getCitation(Format format) {
        String authorsStr = authors.stream()
                .map(a -> a.toString())
                .collect(Collectors.joining(", "));
        String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(publishDate);

        if (format == Format.PLAIN_TEXT) {
            return authorsStr + " (" + dateStr + "). " + title + ". " + journal.getName() + ", pp. " + pages + ". DOI: " + doi + ". Citations: " + citations;
        } else {
            String key = doi.hashCode() + "";
            return "@article{" + key + ",\n  title={" + title + "},\n  author={" + authorsStr + "},\n  journal={" + journal.getName() + "},\n  year={" + (publishDate.getYear() + 1900) + "},\n  pages={" + pages + "},\n  doi={" + doi + "}\n}";
        }
    }
}
