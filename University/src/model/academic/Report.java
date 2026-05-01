package model.academic;

import model.users.Student;

import java.util.List;

public class Report {
    private String title;
    private List<Student> studentData;
    private String reportContent;

    public Report(String title, List<Student> studentData, String reportContent) {
        this.title = title;
        this.studentData = studentData;
        this.reportContent = reportContent;
    }

    public String getTitle() {
        return title;
    }

    public List<Student> getStudentData() {
        return studentData;
    }

    public String getReportContent() {
        return reportContent;
    }
}