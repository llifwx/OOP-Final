package factory;

import enums.DegreeType;
import enums.Language;
import enums.ManagerType;
import enums.TeacherType;
import model.academic.Complaint;
import model.academic.Course;
import model.research.ResearchPaper;
import model.research.ResearchProject;
import model.social.Message;
import model.support.TechSupportReq;
import model.users.Admin;
import model.users.GraduateStudent;
import model.users.Manager;
import model.users.Student;
import model.users.Teacher;
import model.users.TechSupportSpecialist;
import model.users.User;
import utils.LogRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserFactory {

    public static User createUser(String role, String username, String password, String fullName, String email) {
        if (role == null) return null;

        switch (role.toLowerCase()) {
            case "admin":
                return createAdmin(username, password, fullName, email, Language.EN);
            case "student":
                return createStudent(username, password, fullName, email, Language.EN,
                        "S" + System.currentTimeMillis(), "SITE", "Computer Science", 1);
            case "graduate":
            case "graduatestudent":
                return createGraduateStudent(username, password, fullName, email, Language.EN,
                        "G" + System.currentTimeMillis(), "SITE", "Computer Science", 1, DegreeType.MASTER);
            case "teacher":
                return createTeacher(username, password, fullName, email, Language.EN,
                        "T" + System.currentTimeMillis(), "Academic Department", 0, TeacherType.TUTOR);
            case "manager":
                return createManager(username, password, fullName, email, Language.EN,
                        "M" + System.currentTimeMillis(), "Academic Department", 0, ManagerType.OR);
            case "techsupport":
            case "support":
                return createTechSupportSpecialist(username, password, fullName, email, Language.EN,
                        "TS" + System.currentTimeMillis(), "Tech Support", 0);
            default:
                return null;
        }
    }

    public static Admin createAdmin(String username, String password, String fullName, String email, Language language) {
        return new Admin(username, password, fullName, email, language, new ArrayList<LogRecord>());
    }

    public static Student createStudent(String username, String password, String fullName, String email,
                                        Language language, String studentId, String school, String major,
                                        int yearOfStudy) {
        return new Student(username, password, fullName, email, language, studentId, school, major, yearOfStudy);
    }

    public static GraduateStudent createGraduateStudent(String username, String password, String fullName,
                                                        String email, Language language, String studentId,
                                                        String school, String major, int yearOfStudy,
                                                        DegreeType degreeType) {
        return new GraduateStudent(username, password, fullName, email, language, studentId, school, major,
                yearOfStudy, degreeType);
    }

    public static Teacher createTeacher(String username, String password, String fullName, String email,
                                        Language language, String employeeId, String department, double salary,
                                        TeacherType teacherType) {
        return new Teacher(username, password, fullName, email, language, employeeId, department, salary, new Date(),
                new ArrayList<Message>(), teacherType, 0.0, new ArrayList<Course>(), new ArrayList<Complaint>(),
                new ArrayList<ResearchPaper>(), new ArrayList<ResearchProject>());
    }

    public static Manager createManager(String username, String password, String fullName, String email,
                                        Language language, String employeeId, String department, double salary,
                                        ManagerType managerType) {
        return new Manager(username, password, fullName, email, language, employeeId, department, salary, new Date(),
                new ArrayList<Message>(), managerType);
    }

    public static TechSupportSpecialist createTechSupportSpecialist(String username, String password,
                                                                    String fullName, String email, Language language,
                                                                    String employeeId, String department,
                                                                    double salary) {
        return new TechSupportSpecialist(username, password, fullName, email, language, employeeId, department,
                salary, new Date(), new ArrayList<Message>(), new ArrayList<TechSupportReq>());
    }

    public static List<String> getSupportedRoles() {
        List<String> roles = new ArrayList<>();
        roles.add("admin");
        roles.add("student");
        roles.add("graduate");
        roles.add("teacher");
        roles.add("manager");
        roles.add("techsupport");
        return roles;
    }
}
