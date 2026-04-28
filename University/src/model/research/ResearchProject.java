package model.research;

import interfaces.Researcher;
import java.util.ArrayList;
import java.util.List;

public class ResearchProject {
    private String topic;
    private List<Researcher> participants;
    private List<ResearchPaper> publishedPapers;

    public ResearchProject(String topic) {
        this.topic = topic;
        this.participants = new ArrayList<>();
        this.publishedPapers = new ArrayList<>();
    }

    public void addParticipant(Researcher researcher) { this.participants.add(researcher); }
    public void removeParticipant(Researcher researcher) { this.participants.remove(researcher); }
    public void publishPaper(ResearchPaper paper) { this.publishedPapers.add(paper); }
}