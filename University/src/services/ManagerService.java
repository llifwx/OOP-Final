package services;

import comparator.StudentGpaComparator;
import comparator.TeacherNameComparator;
import enums.LessonType;
import model.academic.Course;
import model.academic.Mark;
import model.academic.Report;
import model.academic.Transcript;
import model.social.News;
import model.users.Manager;
import model.users.Student;
import model.users.Teacher;
import model.users.User;
import storage.Database;
import utils.LogRecord;

import java.util.ArrayList;
import java.util.List;

public class ManagerService {
    private final Database database;
    private final AuthService authService;

    public ManagerService(Database database, AuthService authService) {
        this.database = database;
        this.authService = authService;
    }

    // Helper for auth checking.
    private void requireManager() {
        User current = authService.getCurrentUser();
        if (!(current instanceof Manager)) {
            throw new SecurityException("[Manager Service] : Access denied.");
        }
    }

    // Academic management.
    public void assignCourseToTeacher(String courseCode, int teacherId, LessonType lessonType) {
        requireManager();

        Course course = database.findCourseByCode(courseCode);
        if (course == null) {
            System.out.println("Course with code '" + courseCode + "' not found.");
            return;
        }

        User user = database.findUserById(teacherId);
        if (!(user instanceof Teacher teacher)) {
            System.out.println("Teacher with ID '" + teacherId + "' not found.");
            return;
        }

        if (lessonType == LessonType.LECTURE) {
            course.addInstructor(teacher);
        }

        teacher.addCourse(course);
        database.save();

        log("Assigned teacher" + teacher.getFullName() + " to course " + course.getName() + " as " + lessonType);
        System.out.println("[Manager Service] : Teacher " + teacher.getFullName() + " assigned to course " + course.getName() + " as " + lessonType + ".");
    }

    public boolean approveRegistration(Student student, Course course) {
        requireManager();

        if (student == null || course == null) {
            System.out.println("[Manager Service] : Approve failed. Student or course is null.");
            return false;
        }

        if (student.getCredits() + course.getCredits() > 21) {
            System.out.println("[Manager Service] : Cannot approve registration. Credit limit exceeded for student " + student.getFullName() + ". Limit: 21, Current: " + student.getCredits() + ", Course: " + course.getCredits());
            return false;
        }
        if (course.getEnrolledStudents().contains(student)) {
            System.out.println("[Manager Service] : Student " + student.getFullName() + " is already enrolled in course " + course.getName() + ".");
            return false;
        }

        student.getRegisteredCourses().add(course);
        course.enrollStudent(student);

        student.setCredits(student.getCredits() + course.getCredits());

        database.save();
        log("Approved registration for student " + student.getFullName() + " in course " + course.getName());
        System.out.println("[Manager Service] : Registration approved for student " + student.getFullName() + " in course " + course.getName() + ".");

        return true;
    }


    public void addCourseForRegistration(Course course) {
        requireManager();

        if (course == null) {
            System.out.println("[Manager Service] : Course is null.");
            return;
        }

        if (database.findCourseByCode(course.getCourseCode()) != null) {
            System.out.println("[Manager Service] : Course with code '" + course.getCourseCode() + "' already exists.");
            return;
        }

        database.addCourse(course);
        database.save();
        log("Added course for registration: " + course.getName());
        System.out.println("[Manager Service] : Course '" + course.getName() + "' added for registration.");
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
            student.setGpa(gpa);
            totalGpa += gpa;
        }

        double averageGpa = studentCnt > 0 ? totalGpa / studentCnt : 0;

        String content = "Academic Report\n" + "Students count: " + studentCnt + "\n" + "Average GPA: " + averageGpa;

        Report report = new Report("Academic Performance Report", students, content);

        database.addReport(report);
        database.save();
        log("Created academic report with " + studentCnt + " students, average GPA: " + averageGpa);
        System.out.println("[Manager Service] : Academic report created with " + studentCnt + " students, average GPA: " + averageGpa);

        return report;
    }

    public void addNews(News news) {
        requireManager();

        if (news == null) {
            System.out.println("[Manager Service] : News is null.");
            return;
        }
        if (database.findNewsByTitle(news.getTitle()) != null) {
            System.out.println("[Manager Service] : News with title '" + news.getTitle() + "' already exists.");
            return;
        }

        database.addNews(news);
        database.save();
        log("Added news: " + news.getTitle());
        System.out.println("[Manager Service] : News '" + news.getTitle() + " added");
    }

    public boolean removeNews(String title) {
        requireManager();

        News news = database.findNewsByTitle(title);
        if (news == null) {
            System.out.println("[Manager Service] : News with title '" + title + "' not found.");
            return false;
        }

        database.getNews().remove(news);
        database.save();
        log("Removed news: " + title);
        System.out.println("[Manager Service] : News '" + title + "' removed.");
        return true;
    }

    public void pinNews(String title) {
        requireManager();

        News news = database.findNewsByTitle(title);
        if (news == null) {
            System.out.println("[Manager Service] : News with title '" + title + "' not found.");
            return;
        }

        news.pin();
        database.save();
        log("Pinned news: " + title);
        System.out.println("[Manager Service] : News '" + title + "' pinned.");
        return;
    }


    // Sorted views.
    public List<Student> viewStudentsSortedByGpa(List<Student> students) {
        requireManager();

        if (students == null) {
            return new ArrayList<>();
        }

        List<Student> sortedStudents = new ArrayList<>(students);
        sortedStudents.sort(new StudentGpaComparator());

        return sortedStudents;
    }

    public List<Teacher> viewTeachersAlphabetically(List<Teacher> teachers) {
        requireManager();

        if (teachers == null) {
            return new ArrayList<>();
        }

        List<Teacher> sortedTeachers = new ArrayList<>(teachers);
        sortedTeachers.sort(new TeacherNameComparator());

        return sortedTeachers;
    }


    // Helpers for methods.
    private double calculateGpaFromTranscript(Transcript transcript) {
        if (transcript == null || transcript.getMarks().isEmpty()) return 0.0;

        double totalGpa = 0;
        for (Mark mark : transcript.getMarks()) {
            totalGpa += scoreToGpa(mark.getTotalScore());
        }

        return totalGpa / transcript.getMarks().size();
    }

    private double scoreToGpa(double score) {
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

    private void log(String action) {
        User actor = authService.getCurrentUser();
        if (actor != null) {
            database.addLog(new LogRecord(actor, action));
        }
    }
}
