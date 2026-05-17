package utils;

public final class GradeScale {
    private GradeScale() {
    }

    public static double scoreToGpa(double score) {
        if (score >= 94.5) return 4.0;
        if (score >= 89.5) return 3.67;
        if (score >= 84.5) return 3.33;
        if (score >= 79.5) return 3.0;
        if (score >= 74.5) return 2.67;
        if (score >= 69.5) return 2.33;
        if (score >= 64.5) return 2.0;
        if (score >= 59.5) return 1.67;
        if (score >= 54.5) return 1.33;
        if (score >= 49.5) return 1.0;
        if (score >= 44.5) return 0.67;
        if (score >= 39.5) return 0.33;
        return 0.0;
    }
}
