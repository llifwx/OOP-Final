package model.users;

import model.academic.Report;
import enums.Language;
import enums.LessonType;
import enums.ManagerType;
import model.academic.Course;
import model.social.Message;
import model.social.News;
import storage.Database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Manager extends Employee {
    private static final long serialVersionUID = 1L;
    private ManagerType managerType;

    public Manager(String username, String password, String fullName, String email, Language language, String employeeId, String department, double salary, Date hierDate, ManagerType managerType) {
        super(username, password, fullName, email, language, employeeId, department, salary, hierDate);
        this.managerType = managerType;
    }

    public void assignCourseToTeacher(Course course, Teacher teacher, LessonType lessonType) {
        if (course == null || teacher == null || lessonType == null) return;
        if (lessonType == LessonType.LECTURE) {
            course.addInstructor(teacher);
        }
        teacher.addCourse(course);
        Database.getInstance().save();
    }

    public boolean approveRegistration(Student student, Course course) {
        if (student == null || course == null) return false;
        if (student.getCredits() + course.getCredits() > 21) return false;
        if (course.getEnrolledStudents().contains(student)) return false;

        student.getRegisteredCourses().add(course);
        course.enrollStudent(student);
        student.setCredits(student.getCredits() + course.getCredits());
        Database.getInstance().save();
        return true;
    }

    public void addCourseForRegistration(Course course) {
        if (course != null && Database.getInstance().findCourseByCode(course.getCourseCode()) == null) {
            Database.getInstance().addCourse(course);
            Database.getInstance().save();
        }
    }

    public Report createAcademicReport(List<Student> students) {
        if (students == null) students = new ArrayList<>();
        String content = "Total Students: " + students.size();
        Report report = new Report("Academic Performance Report", students, content);
        Database.getInstance().addReport(report);
        Database.getInstance().save();
        return report;
    }

    public void manageNews(News news) {
        if (news != null && Database.getInstance().findNewsByTitle(news.getTitle()) == null) {
            Database.getInstance().addNews(news);
            Database.getInstance().save();
        }
    }

    public List<Student> viewStudentsSortedByGpa(List<Student> students) {
        if (students == null) return new ArrayList<>();
        List<Student> sorted = new ArrayList<>(students);
        sorted.sort((a, b) -> Double.compare(b.getGpa(), a.getGpa()));
        return sorted;
    }

    public List<Teacher> viewTeachersAlphabetically(List<Teacher> teachers) {
        if (teachers == null) return new ArrayList<>();
        List<Teacher> sorted = new ArrayList<>(teachers);
        sorted.sort((a, b) -> a.getFullName().compareToIgnoreCase(b.getFullName()));
        return sorted;
    }
    public ManagerType getManagerType() {
        return managerType;
    }

    public void setManagerType(ManagerType managerType) {
        this.managerType = managerType;
    }
}
