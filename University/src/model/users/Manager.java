package model.users;

import academic.Report;
import enums.Language;
import enums.LessonType;
import enums.ManagerType;
import model.academic.Course;
import social.News;

import java.util.Date;
import java.util.List;

public class Manager extends Employee{
    private ManagerType managerType;

    public Manager(String username, String password, String fullName, String email, Language language, String employeeId, String department, double salary, Date hierDate, List<Message> inbox, ManagerType managerType) {
        super(username, password, fullName, email, language, employeeId, department, salary, hierDate, inbox);
        this.managerType = managerType;
    }

    public void assignCourseToTeacher(Course course, Teacher teacher, LessonType lessonType) {}
    public boolean approveRegistration(Student student, Course Course) {return true}
    public void addCourseForRegistration(Course course) {}
    public Report createAcademicReport(List<Student> students) {}
    public void manageNews(News news) {}
    public List<Student> viewStudentsSortedByGpa(List<Student> students) {}
    public List<Teacher> viewTeachersAlphabetically(List<Teacher> teachers) {}
}
