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
    private CourseType type;
    private String intendedMajor;
    private int intendedYear;

    private boolean openForRegistration;
    private Language language;
    private List<Lesson> lessons = new ArrayList<>();
    private List<Teacher> instructors = new ArrayList<>();
    private List<Student> enrolledStudents = new ArrayList<>();

    // Main constructor
    public Course(String courseCode, String name, int credits, CourseType type, Language language) {
        this.id = ++idCounter;
        this.courseCode = courseCode;
        this.name = name;
        this.credits = credits;
        this.type = type;
        this.language = language;
    }

    public Course(String courseCode, String name, int credits, CourseType type, Language language, String intendedMajor, int intendedYear, boolean openForRegistration) {
        this(courseCode, name, credits, type, language);
        this.intendedMajor = intendedMajor;
        this.intendedYear = intendedYear;
        this.openForRegistration = openForRegistration;
    }

    public static void synchronizeIdCounter(int maxId) {
        if (maxId > idCounter) {
            idCounter = maxId;
        }
    }

    // Getters
    public int getId() {return id;}

    public String getCourseCode() {return courseCode;}

    public String getName() {return name;}

    public int getCredits() {return credits;}

    public CourseType getType() {return type;}

    public boolean isOpenForRegistration() {return openForRegistration;}

    public int getIntendedYear() {return intendedYear;}

    public String getIntendedMajor() {return intendedMajor;}

    public Language getLanguage() {return language;}

    public List<Lesson> getLessons() {return new ArrayList<>(lessons);}

    public List<Teacher> getInstructors() {return new ArrayList<>(instructors);}

    public List<Student> getEnrolledStudents() {return new ArrayList<>(enrolledStudents);}

    public void setIntendedMajor(String intendedMajor) {
        if (intendedMajor == null || intendedMajor.isBlank()) {
            throw new IllegalArgumentException("Intended major cannot be empty.");
        }
        this.intendedMajor = intendedMajor.trim();
    }

    public void setIntendedYear(int intendedYear) {
        if (intendedYear < 1) {
            throw new IllegalArgumentException("Intended year must be at least 1.");
        }
        this.intendedYear = intendedYear;
    }

    public void setOpenForRegistration(boolean openForRegistration) {
        this.openForRegistration = openForRegistration;
    }

    // Methods
    public void addLesson(Lesson lesson) {
        if (lesson != null && !lessons.contains(lesson)) {
            lessons.add(lesson);
        }
    }

    public void addInstructor(Teacher teacher) {
        if (teacher != null && !instructors.contains(teacher)) {
            instructors.add(teacher);
        }
    }

    public void enrollStudent(Student student) {
        if (student != null && !enrolledStudents.contains(student)) {
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
