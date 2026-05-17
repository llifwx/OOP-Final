package interfaces;

import model.research.ResearchPaper;

import java.util.Comparator;

public interface Researcher {
    int calculateHIndex();
    void printPapers(Comparator<ResearchPaper> cmp);
}
