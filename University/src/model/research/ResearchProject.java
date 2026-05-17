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
        return new ArrayList<>(participants);
    }

    public List<ResearchPaper> getPublishedPapers() {
        return new ArrayList<>(publishedPapers);
    }

    public void addParticipant(Researcher researcher) {
        if (researcher == null) {
            throw new IllegalArgumentException("Researcher cannot be null");
        }
        if (!this.participants.contains(researcher)) {
            this.participants.add(researcher);
        }
    }

    public void removeParticipant(Researcher researcher) {this.participants.remove(researcher);}

    public void publishPaper(ResearchPaper paper) {
        if (paper != null && !this.publishedPapers.contains(paper)) {
            this.publishedPapers.add(paper);
        }
    }
}
