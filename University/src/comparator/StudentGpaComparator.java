package comparator;

import model.users.Student;

import java.util.Comparator;

public class StudentGpaComparator implements Comparator<Student> {

    @Override
    public int compare(Student o1, Student o2) {
        return Double.compare(o2.getGpa(), o1.getGpa());
    }
}
