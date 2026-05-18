package interfaces;

import model.research.ResearchPaper;
import model.research.ResearchProject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface Researcher {
    int calculateHIndex();

    List<ResearchPaper> getPapers();

    List<ResearchProject> getProjects();

    void addPaper(ResearchPaper paper);

    void addProject(ResearchProject project);

    default void printPapers(Comparator<ResearchPaper> comparator) {
        List<ResearchPaper> sortedPapers = new ArrayList<>(getPapers());

        if (comparator != null) {
            sortedPapers.sort(comparator);
        }

        if (sortedPapers.isEmpty()) {
            System.out.println("No research papers yet.");
            return;
        }

        for (ResearchPaper paper : sortedPapers) {
            System.out.println("- " + paper.getTitle() + " | Citations: " + paper.getCitations());
        }
    }
}