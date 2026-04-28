package model.academic;

import model.users.Student;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StudentOrganization {

    private static int idCounter = 0;
    private final int id;

    private String name;
    private String description;
    private Student head;
    private List<Student> members = new ArrayList<>();

    public StudentOrganization(String name, String description) {
        this.id = ++idCounter;
        this.name = name;
        this.description = description;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Student getHead() { return head; }
    public List<Student> getMembers() {
        return new ArrayList<>(members);
    }

    public void setHead(Student head) {
        this.head = head;
    }

    public void addMember(Student student) {
        if (student != null && !members.contains(student)) {
            members.add(student);
        }
    }

    public void removeMember(Student student) {
        members.remove(student);
    }

    public boolean isMember(Student student) {
        return members.contains(student);
    }

    @Override
    public String toString() {
        return "StudentOrganization{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", head=" + (head != null ? head.getFullName() : "No head yet") +
                ", membersCount=" + members.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudentOrganization org)) return false;
        return id == org.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}