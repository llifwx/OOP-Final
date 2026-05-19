package services;

import enums.CourseType;
import enums.RegistrationStatus;
import model.academic.Course;
import model.academic.CourseRegistration;
import model.academic.StudentOrganization;
import model.users.Student;
import model.users.Teacher;
import model.users.User;
import storage.Database;
import utils.LogRecord;

import java.util.ArrayList;
import java.util.List;

import static i18n.I18n.t;

public class StudentService {
    private static final int MAX_STUDENT_CREDITS = 21;

    private final Database database;
    private final AuthService authService;

    public StudentService(Database database, AuthService authService) {
        this.database = database;
        this.authService = authService;
    }

    public List<Course> getAvailableCourses() {
        Student student = requireStudent();

        List<Course> availableCourse = new ArrayList<>();

        for (Course c : database.getCourses()) {
            if (isCourseAvailableForStudent(c, student)) {
                availableCourse.add(c);
            }
        }

        return availableCourse;
    }

    public boolean registerForCourse(String courseCode) {
        Student student = requireStudent();
        Course course = database.findCourseByCode(courseCode);
        if (course == null) {
            System.out.println("[StudentService] Course not found.");
            return false;
        }
        if (!course.isOpenForRegistration()) {
            System.out.println("[StudentService] Course is not open for registration.");
            return false;
        }
        if (!isCourseAvailableForStudent(course, student)) {
            System.out.println("[StudentService] You do not meet the requirements for this course.");
            return false;
        }
        if (student.getRegisteredCourses().contains(course)) {
            System.out.println("[StudentService] You are already registered for this course.");
            return false;
        }
        if (hasPendingRegistration(student, course)) {
            System.out.println("[StudentService] You already have a pending request for this course.");
            return false;
        }
        if (student.getCredits() + getPendingCredits(student) + course.getCredits() > MAX_STUDENT_CREDITS) {
            System.out.println("[StudentService] Cannot request registration. Credit limit would be exceeded.");
            return false;
        }

        CourseRegistration registration = new CourseRegistration(student, course);
        database.addCourseRegistration(registration);
        log("Requested registration for course: " + course.getCourseCode());
        database.save();
        return true;
    }

    public Teacher getTeacherInfo(String courseCode) {
        Student student = requireStudent();
        Course course = database.findCourseByCode(courseCode);
        if (course == null || !student.getRegisteredCourses().contains(course) || course.getInstructors().isEmpty()) {
            return null;
        }
        return course.getInstructors().get(0);
    }

    public boolean rateTeacher(int teacherId, double rating) {
        Student student = requireStudent();
        User user = database.findUserById(teacherId);
        if (!(user instanceof Teacher teacher)) {
            System.out.println(t("student.teacher_not_found"));
            return false;
        }
        boolean studiedWithTeacher = student.getRegisteredCourses().stream()
                .anyMatch(course -> course.getInstructors().contains(teacher));
        if (!studiedWithTeacher) {
            System.out.println(t("student.teacher_not_registered_course"));
            return false;
        }
        teacher.addRating(rating);
        log("Rated teacher " + teacher.getUsername() + ": " + rating);
        database.save();
        return true;
    }

    public List<StudentOrganization> getAllOrganizations() {
        requireStudent();
        return new ArrayList<>(database.getStudentOrganizations());
    }

    public boolean joinOrganization(String name) {
        Student student = requireStudent();
        StudentOrganization organization = database.findStudentOrganizationByName(name);
        if (organization == null) {
            System.out.println("[StudentService] Organization not found.");
            return false;
        }
        organization.addMember(student);
        student.joinOrganization(organization);
        log("Joined organization: " + organization.getName());
        database.save();
        return true;
    }

    public boolean leaveOrganization(String name) {
        Student student = requireStudent();
        StudentOrganization organization = database.findStudentOrganizationByName(name);
        if (organization == null) {
            System.out.println("[StudentService] Organization not found.");
            return false;
        }
        organization.removeMember(student);
        student.leaveOrganization(organization);
        log("Left organization: " + organization.getName());
        database.save();
        return true;
    }

    private Student requireStudent() {
        User current = authService.getCurrentUser();
        if (!(current instanceof Student student)) {
            throw new SecurityException("[StudentService] Access denied: current user is not a Student.");
        }
        return student;
    }

    private boolean hasPendingRegistration(Student student, Course course) {
        return database.findRegistrationsByStudent(student)
                .stream()
                .anyMatch(registration -> registration.getStatus() == RegistrationStatus.PENDING && course.equals(registration.getCourse()));
    }

    private int getPendingCredits(Student student) {
        return database.findRegistrationsByStudent(student)
                .stream()
                .filter(registration -> registration.getStatus() == RegistrationStatus.PENDING)
                .map(CourseRegistration::getCourse)
                .filter(course -> course != null)
                .mapToInt(Course::getCredits)
                .sum();
    }

    private boolean isCourseAvailableForStudent(Course course, Student student) {
        if (course == null || student == null) return false;
        if (!course.isOpenForRegistration()) return false;

        if (course.getType() == CourseType.MAJOR) {
            return course.getIntendedMajor() != null && student.getMajor() != null && course.getIntendedMajor()
                    .equalsIgnoreCase(student.getMajor()) && course.getIntendedYear() == student.getYearOfStudy();
        }

        if (course.getType() == CourseType.FREE_ELECTIVE) {
            return true;
        }

        if (course.getType() == CourseType.MINOR) {
            return course.getIntendedMajor() != null && student.getMajor() != null && course.getIntendedMajor()
                    .equalsIgnoreCase(student.getMajor());
        }

        return false;
    }

    private void log(String action) {
        User actor = authService.getCurrentUser();
        if (actor != null) {
            database.addLog(new LogRecord(actor, action));
        }
    }
}
