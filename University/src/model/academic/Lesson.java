package model.academic;

import enums.LessonType;
import model.users.Teacher;
import java.util.Objects;

public class Lesson {

    private static int idCounter = 0;
    private final int id;
    private LessonType type;
    private String dayOfWeek;
    private String timeSlot;
    private String room;
    private Course course;
    private Teacher teacher;

    public Lesson(LessonType type, String dayOfWeek, String timeSlot, String room, Course course, Teacher teacher) {
        this.id = ++idCounter;
        this.type = type;
        this.dayOfWeek = dayOfWeek;
        this.timeSlot = timeSlot;
        this.room = room;
        this.course = course;
        this.teacher = teacher;
    }

    public int getId() { return id; }
    public LessonType getType() { return type; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getTimeSlot() { return timeSlot; }
    public String getRoom() { return room; }
    public Course getCourse() { return course; }
    public Teacher getTeacher() { return teacher; }

    @Override
    public String toString() {
        return "Lesson{" +
                "id=" + id +
                ", type=" + type +
                ", day=" + dayOfWeek +
                ", time=" + timeSlot +
                ", room='" + room + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lesson lesson)) return false;
        return id == lesson.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}