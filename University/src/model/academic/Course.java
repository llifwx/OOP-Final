package model.academic;

import enums.Language;
import model.users.Student;
import model.users.Teacher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import enums.CourseType;

public class Course implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int idCounter = 0;
    private final int id;
    private String courseCode;
    private String name;
    private int credits;
    private CourseType type;        // Major, Minor, FreeElective (enum from Amina)
    private Language language;
    private List<Lesson> lessons = new ArrayList<>();
    private List<Teacher> instructors = new ArrayList<>();
    private List<Student> enrolledStudents = new ArrayList<>();

    // Основной конструктор
    public Course(String courseCode, String name, int credits, CourseType type, Language language) {
        this.id = ++idCounter;
        this.courseCode = courseCode;
        this.name = name;
        this.credits = credits;
        this.type = type;
        this.language = language;
    }

    // Геттеры
    public int getId() {return id;}

    public String getCourseCode() {return courseCode;}

    public String getName() {return name;}

    public int getCredits() {return credits;}

    public CourseType getType() {return type;}

    public Language getLanguage() {return language;}

    public List<Lesson> getLessons() {return new ArrayList<>(lessons);}

    public List<Teacher> getInstructors() {return new ArrayList<>(instructors);}

    public List<Student> getEnrolledStudents() {return new ArrayList<>(enrolledStudents);}

    // Методы
    public void addLesson(Lesson lesson) {
        if (lesson == null) return;
        boolean alreadyExists = lessons.stream().anyMatch(existing -> existing.getDayOfWeek().equals(lesson.getDayOfWeek()) && existing.getTimeSlot().equals(lesson.getTimeSlot()) && existing.getRoom().equals(lesson.getRoom()));

        if (!alreadyExists) {
            lessons.add(lesson);
        }
    }

    public void addInstructor(Teacher teacher) {
        if (!instructors.contains(teacher)) {
            instructors.add(teacher);
        }
    }

    public void enrollStudent(Student student) {
        if (!enrolledStudents.contains(student)) {
            enrolledStudents.add(student);
        }
    }

    public void removeStudent(Student student) {
        enrolledStudents.remove(student);
    }

    @Override
    public String toString() {
        return "Course{" + "id=" + id + ", courseCode='" + courseCode + '\'' + ", name='" + name + '\'' + ", credits=" + credits + ", type=" + type + ", language=" + language + ", instructorsCount=" + instructors.size() + ", studentsCount=" + enrolledStudents.size() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course course)) return false;
        return id == course.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}