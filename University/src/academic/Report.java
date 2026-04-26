package academic;

import model.users.Student;
import java.util.List;

public class Report {
    private List<Student> studentData;
    private String reportContent;

    public Report(List<Student> studentData, String reportContent) {
        this.studentData = studentData;
        this.reportContent = reportContent;
    }
}