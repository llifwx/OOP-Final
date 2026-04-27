package interfaces;

import research.Journal;
import research.ResearchPaper;
import research.ResearchProject;

import java.util.Comparator;

public interface Researcher {
    int calculateHIndex();
    void printPapers(Comparator<ResearchPaper> cmp);
    void joinProject(ResearchProject project);
    void publishPaper(ResearchPaper paper, Journal journal);
}
