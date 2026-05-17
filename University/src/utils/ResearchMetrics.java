package utils;

import model.research.ResearchPaper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class ResearchMetrics {
    private ResearchMetrics() {
    }

    public static int calculateHIndex(List<ResearchPaper> papers) {
        if (papers == null || papers.isEmpty()) return 0;

        List<Integer> citations = papers.stream()
                .map(ResearchPaper::getCitations)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        int h = 0;
        for (int i = 0; i < citations.size(); i++) {
            if (citations.get(i) >= i + 1) h = i + 1;
            else break;
        }
        return h;
    }
}
