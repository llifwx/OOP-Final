package services;

import comparator.StudentGpaComparator;
import comparator.TeacherNameComparator;
import enums.LessonType;
import model.academic.Course;
import model.academic.Report;
import model.social.News;
import model.users.Student;
import model.users.Teacher;
import model.users.User;
import storage.Database;

import java.util.ArrayList;
import java.util.List;

public class ManagerService {
    private final Database db;

    public ManagerService(Database db) {this.db = db;}

    public void assignCourseToTeacher(String courseCode, int TeacherId, LessonType lessonType) {
        Course course = db.findCourseByCode(courseCode);
        User user = db.findUserById(TeacherId);

        if (course == null) {
            System.out.println("Course not found!");
            return;
        }

        if (!(user instanceof Teacher teacher)) {
            System.out.println("Teacher not found!");
            return;
        }

        if (lessonType == LessonType.LECTURE) {
            course.addInstructor((Teacher) user);
        }

        if (!teacher.getCourses().contains(course)) {
            teacher.addCourse(course);
        }

        db.save();
        System.out.println("Teacher assigned to Course: " + course.getName());
    }

    public boolean approveRegistration(Student student, Course course) {
        if (student == null || course == null) {
            return false;
        }

        if (student.getCredits() + course.getCredits() > 21) {
            System.out.println("Cannot approve registration. Credit limit exceeded.");
            return false;
        }

        if (course.getEnrolledStudents().contains(student)) {
            System.out.println("Student is already enrolled in this course.");
            return false;
        }

        course.enrollStudent(student);
        student.registerForCourse(course);

        db.save();
        System.out.println("Registration approved for student: " + student.getFullName() + " in");
        return true;
    }

    public void addCourseForRegistration(Course course) {
        if (course == null) {
            System.out.println("Course is not found.");
            return;
        }

        if (db.findCourseByCode(course.getCourseCode()) != null) {
            System.out.println("Course with this code already exists.");
            return;
        }

        db.addCourse(course);
        db.save();

        System.out.println("Course added for registration: " + course.getName());
    }

    public Report createAcademicReport(List<Student> students) {
        if (students == null) {
            students = new ArrayList<>();
        }

        int studentCnt = students.size();
        double totalGpa = 0;
        double averageGpa = 0;

        for (Student student : students) {
            totalGpa += student.getGpa();
        }

        if (studentCnt > 0) {
            averageGpa = totalGpa / studentCnt;
        }

        String content = "Academic Report\n" + "Students count: " + studentCnt + "\n" + "Average GPA: " + averageGpa;

        Report report = new Report("Academic Performance Report", students, content);

        db.addReport(report);
        db.save();

        return report;
    }

    public void manageNews(News news) {
        if (news == null) {
            System.out.println("News are not found.");
            return;
        }

        if (db.findNewsByTitle(news.getTitle()) == null) {
            db.addNews(news);
            System.out.println("News added: " + news.getTitle());
        } else {
            System.out.println("News with this title already exists.");
        }

        db.save();
    }

    public List<Student> viewStudentsSortedByGpa(List<Student> students) {
        if (students == null) {
            return new ArrayList<>();
        }

        List<Student> sortedStudents = new ArrayList<>(students);
        sortedStudents.sort(new StudentGpaComparator());

        return sortedStudents;
    }

    public List<Teacher> viewTeachersAlphabetically(List<Teacher> teachers) {
        if (teachers == null) {
            return new ArrayList<>();
        }

        List<Teacher> sortedTeachers = new ArrayList<>(teachers);
        sortedTeachers.sort(new TeacherNameComparator());

        return sortedTeachers;
    }
}
