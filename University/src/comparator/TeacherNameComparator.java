package comparator;

import model.users.Teacher;

import java.util.Comparator;

public class TeacherNameComparator implements Comparator<Teacher> {

    @Override
    public int compare(Teacher o1, Teacher o2) {
        return o1.getFullName().compareTo(o2.getFullName());
    }
}
