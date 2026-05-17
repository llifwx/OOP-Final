package services;

import enums.Language;
import model.academic.Course;
import model.users.Admin;
import model.users.*;
import storage.Database;
import utils.LogRecord;

import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final Database database;
    private final AuthService authService;

    public UserService(Database database, AuthService authService) {
        this.database = database;
        this.authService = authService;
    }

    public boolean registerUser(User user) {
        requireAdmin();
        if (user == null) return false;

        if (database.findUserByUsername(user.getUsername()) != null) {
            System.out.println("[UserService] Registration failed: username '"
                    + user.getUsername() + "' is already taken.");
            return false;
        }

        database.addUser(user);
        log("Registered new user: " + user.getUsername() + " [" + user.getClass().getSimpleName() + "]");
        database.save();
        System.out.println("[UserService] User '" + user.getUsername() + "' registered successfully.");
        return true;
    }

    public User findByUsername(String username) {
        return database.findUserByUsername(username);
    }

    public User findById(int id) {
        return database.findUserById(id);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(database.getUsers());
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        for (User user : database.getUsers()) {
            if (user instanceof Student) {
                students.add((Student) user);
            }
        }
        return students;
    }

    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        for (User user : database.getUsers()) {
            if (user instanceof Teacher) {
                teachers.add((Teacher) user);
            }
        }
        return teachers;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        for (User user : database.getUsers()) {
            if (user instanceof Employee) {
                employees.add((Employee) user);
            }
        }
        return employees;
    }

    public Teacher getTeacherInfoForStudent(Student student, Course course) {
        if (student == null || course == null) {
            return null;
        }
        if (!student.getRegisteredCourses().contains(course) || course.getInstructors().isEmpty()) {
            return null;
        }
        return course.getInstructors().get(0);
    }

    public boolean changeUsername(User user, String newUsername) {
        requireAdminOrSelf(user);
        if (user == null || newUsername == null || newUsername.isBlank()) return false;

        if (database.findUserByUsername(newUsername) != null) {
            System.out.println("[UserService] Username '" + newUsername + "' is already taken.");
            return false;
        }

        String old = user.getUsername();
        user.setUsername(newUsername);
        log("Changed username from '" + old + "' to '" + newUsername + "'");
        database.save();
        return true;
    }

    public void changeLanguage(User user, Language language) {
        requireAdminOrSelf(user);
        if (user == null || language == null) return;
        user.setLanguage(language);
        log("Changed language for: " + user.getUsername());
        database.save();
<<<<<<< HEAD
        System.out.println("[UserService] Language updated for '" + user.getUsername() + "' to " + language + ".");
=======
>>>>>>> fc28ef2 (review)
    }

    public boolean removeUser(String username) {
        requireAdmin();
        User user = database.findUserByUsername(username);
        if (user == null) {
            System.out.println("[UserService] Remove failed: user '" + username + "' not found.");
            return false;
        }

        database.removeUser(user);
        log("Removed user: " + username);
        database.save();
        System.out.println("[UserService] User '" + username + "' removed.");
        return true;
    }

    private void log(String action) {
        User actor = authService.getCurrentUser();
        if (actor != null) {
            database.addLog(new LogRecord(actor, action));
        }
    }

    private void requireAdmin() {
        if (!authService.hasRole(Admin.class)) {
            throw new SecurityException("[UserService] Access denied: current user is not an Admin.");
        }
    }

    private void requireAdminOrSelf(User user) {
        User current = authService.getCurrentUser();
        if (current == null || (user != null && !current.equals(user) && !(current instanceof Admin))) {
            throw new SecurityException("[UserService] Access denied.");
        }
    }
}
