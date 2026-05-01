package comparator;

import model.users.Teacher;

import java.util.Comparator;

public class TeacherNameComparator implements Comparator<Teacher> {

    @Override
    public int compare(Teacher o1, Teacher o2) {
        return 0;
    }

    @Override
    public Comparator<Teacher> reversed() {
        return Comparator.super.reversed();
    }
}
