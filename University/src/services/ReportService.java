package services;

import enums.Format;
import model.academic.Mark;
import model.academic.Report;
import model.academic.Transcript;
import model.research.ResearchPaper;
import model.users.Manager;
import model.users.Student;
import model.users.Teacher;
import model.users.User;
import storage.Database;
import utils.GradeScale;
import utils.LogRecord;
import utils.ResearchMetrics;

import java.util.ArrayList;
import java.util.List;

public class ReportService {
    private final Database database;
    private final AuthService authService;

    public ReportService(Database database, AuthService authService) {
        this.database = database;
        this.authService = authService;
    }

    private void requireManager() {
        User current = authService.getCurrentUser();
        if (!(current instanceof Manager)) {
            throw new SecurityException("[Report Service] : Access denied.");
        }
    }

    public Report createAcademicReport(List<Student> students) {
        requireManager();

        if (students == null) {
            students = new ArrayList<>();
        }

        int studentCnt = students.size();
        double totalGpa = 0;

        for (Student student : students) {
            double gpa = calculateGpaFromTranscript(student.getTranscript());
            totalGpa += gpa;
        }

        double averageGpa = studentCnt > 0 ? totalGpa / studentCnt : 0;

        StringBuilder content = new StringBuilder();
        content.append("---------- Academic Performance Report ----------\n");
        content.append("Total Students: ").append(studentCnt).append("\n");
        content.append(String.format("Average GPA: %.2f\n", averageGpa));
        content.append("---------------------------------------------\n\n");
        content.append("Student Details:\n");
        for (Student s : students) {
            content.append("  -")
                    .append(s.getFullName())
                    .append(" | GPA: ")
                    .append(String.format("%.2f", calculateGpaFromTranscript(s.getTranscript())))
                    .append("\n")
                    .append(" | Credits: ")
                    .append(s.getCredits())
                    .append(" | Year: ")
                    .append(s.getYearOfStudy())
                    .append("\n");
        }

        Report report = new Report("Academic Performance Report", students, content.toString());

        database.addReport(report);
        log("Created academic report with " + studentCnt + " students, average GPA: " + averageGpa);
        database.save();
        System.out.println("[Report Service] : Academic report created with " + studentCnt + " students, average GPA: " + averageGpa);

        return report;
    }

    public Report createResearchReport(List<Teacher> teachers) {
        requireManager();

        if (teachers == null) teachers = new ArrayList<>();

        int totalPapers = 0;
        int totalCitations = 0;

        StringBuilder content = new StringBuilder();
        content.append("---------- Research Performance Report ----------\n");
        content.append("Total Teachers: ").append(teachers.size()).append("\n\n");

        for (Teacher t : teachers) {
            List<ResearchPaper> papers = t.getPapers();
            int citations = papers.stream().mapToInt(ResearchPaper::getCitations).sum();
            int hIndex = ResearchMetrics.calculateHIndex(papers);

            totalPapers += papers.size();
            totalCitations += citations;

            content.append(" - Teacher: ")
                    .append(t.getFullName())
                    .append("\n")
                    .append(" | Papers: ")
                    .append(papers.size())
                    .append("\n")
                    .append(" | Citations: ")
                    .append(citations)
                    .append("\n")
                    .append(" | H-Index: ")
                    .append(hIndex)
                    .append("\n\n");
        }

        content.append("Total Research Papers: ").append(totalPapers).append("\n");
        content.append("Total Citations: ").append(totalCitations).append("\n");

        Report report = new Report("Research Performance Report", new ArrayList<>(), content.toString());

        database.addReport(report);
        log("Created research report with " + teachers.size() + " teachers, total papers: " + totalPapers + ", total citations: " + totalCitations);
        database.save();
        System.out.println("[Report Service] : Research report created with " + teachers.size() + " teachers, total papers: " + totalPapers + ", total citations: " + totalCitations);
        return report;
    }

    // Retrieve reports.
    public Report getReportByTitle(String title) {
        requireManager();

        Report report = database.findReportByTitle(title);
        if (report == null) {
            System.out.println("[Report Service] : Report with title '" + title + "' not found.");
            return null;
        }

        return report;
    }

    public List<Report> getAllReports() {
        requireManager();

        return new ArrayList<>(database.getReports());
    }

    // Print reports.
    public void printReport(Report report, Format format) {
        requireManager();

        if (report == null) {
            System.out.println("[Report Service] : Cannot print null report.");
            return;
        }

        if (format == Format.PLAIN_TEXT) {
            printPlainText(report);
        } else {
            printBibtex(report);
        }
    }

    //Delete report
    public boolean deleteReport(String title) {
        requireManager();

        Report report = database.findReportByTitle(title);
        if (report == null) {
            System.out.println("[Report Service] : Report with title '" + title + "' not found.");
            return false;
        }

<<<<<<< HEAD
        database.removeReport(report);
        database.save();
=======
        database.getReports().remove(report);
>>>>>>> fc28ef2 (review)
        log("Deleted report: " + title);
        database.save();
        System.out.println("[Report Service] : Report '" + title + "' deleted.");

        return true;
    }

    // Helpers for methods.
    private double calculateGpaFromTranscript(Transcript transcript) {
        if (transcript == null || transcript.getMarks().isEmpty()) return 0.0;

        double totalGpa = 0;
        for (Mark mark : transcript.getMarks()) {
            totalGpa += GradeScale.scoreToGpa(mark.getTotalScore());
        }

        return totalGpa / transcript.getMarks().size();
    }

    private void printPlainText(Report report) {
        System.out.println("──────────────" + report.getTitle() + "──────────────");
        System.out.println(report.getReportContent());
    }

    private void printBibtex(Report report) {
        System.out.println("@report{");
        System.out.println("  title = {" + report.getTitle() + "},");
        System.out.println("  content = {" + report.getReportContent().replace("\n", "\\n") + "}");
        System.out.println("}");
    }

    private void log(String action) {
        User actor = authService.getCurrentUser();
        if (actor != null) {
            database.addLog(new LogRecord(actor, action));
        }
    }
}
