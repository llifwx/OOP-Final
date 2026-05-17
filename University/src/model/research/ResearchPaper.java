package model.research;

import enums.Format;
import interfaces.Researcher;
import model.social.Journal;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
        this.authors = authors == null ? new ArrayList<>() : new ArrayList<>(authors);
        this.journal = journal;
        this.citations = citations;
        this.pages = pages;
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

    public String getCitation(Format format) {
        String authorsStr = authors.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        if (authorsStr.isBlank()) authorsStr = "Unknown author";

        String safeTitle = title == null || title.isBlank() ? "Untitled" : title;
        String journalName = journal == null ? "Unknown journal" : journal.getName();
        String safeDoi = doi == null || doi.isBlank() ? "N/A" : doi;
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(publishDate == null ? new Date() : publishDate);

        if (format == Format.PLAIN_TEXT) {
            return authorsStr + " (" + dateStr + "). " + safeTitle + ". " + journalName
                    + ", pp. " + pages + ". DOI: " + safeDoi + ". Citations: " + citations;
        } else {
            String key = Integer.toString((safeDoi + safeTitle).hashCode());
            return "@article{" + key + ",\n  title={" + safeTitle + "},\n  author={" + authorsStr
                    + "},\n  journal={" + journalName + "},\n  year={" + dateStr.substring(0, 4)
                    + "},\n  pages={" + pages + "},\n  doi={" + safeDoi + "}\n}";
        }
    }
}
