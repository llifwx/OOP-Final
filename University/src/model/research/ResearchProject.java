package model.research;

import interfaces.Researcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResearchProject implements Serializable {
    private static final long serialVersionUID = 1L;
    private String topic;
    private List<Researcher> participants;
    private List<ResearchPaper> publishedPapers;

    public ResearchProject(String topic) {
        this.topic = topic;
        this.participants = new ArrayList<>();
        this.publishedPapers = new ArrayList<>();
    }

    public String getTopic() {
        return topic;
    }

    public List<Researcher> getParticipants() {
        return participants;
    }

    public List<ResearchPaper> getPublishedPapers() {
        return publishedPapers;
    }

    public void addParticipant(Researcher researcher) {this.participants.add(researcher);}

    public void removeParticipant(Researcher researcher) {this.participants.remove(researcher);}

    public void publishPaper(ResearchPaper paper) {this.publishedPapers.add(paper);}
}