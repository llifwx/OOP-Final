package model.academic;

import model.users.Student;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Report implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int idCounter = 0;
    private int id;
    private String title;
    private List<Student> studentData;
    private String reportContent;

    public Report(String title, List<Student> studentData, String reportContent) {
        this.id = ++idCounter;
        this.title = title;
        this.studentData = studentData == null ? new ArrayList<>() : new ArrayList<>(studentData);
        this.reportContent = reportContent;
    }

    public static void synchronizeIdCounter(int maxId) {
        if (maxId > idCounter) {
            idCounter = maxId;
        }
    }

    public void ensureId() {
        if (id <= 0) {
            id = ++idCounter;
        }
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<Student> getStudentData() {
        return new ArrayList<>(studentData);
    }

    public String getReportContent() {
        return reportContent;
    }

    @Override
    public String toString() {
        return "Report{" + "id=" + id + ", title='" + title + '\'' + ", studentsCount=" + studentData.size() + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Report report)) return false;
        return id == report.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
