package comparator;

import model.users.Student;

import java.util.Comparator;

public class StudentGpaComparator implements Comparator<Student> {

    @Override
    public int compare(Student o1, Student o2) {
        return 0;
    }

    @Override
    public Comparator<Student> reversed() {
        return Comparator.super.reversed();
    }
}
