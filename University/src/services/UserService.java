package services;

import enums.Language;
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
        if (user == null) return false;

        if (database.findUserByUsername(user.getUsername()) != null) {
            System.out.println("[UserService] Registration failed: username '"
                    + user.getUsername() + "' is already taken.");
            return false;
        }

        database.addUser(user);
        log("Registered new user: " + user.getUsername() + " [" + user.getClass().getSimpleName() + "]");
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

    public boolean changeUsername(User user, String newUsername) {
        if (user == null || newUsername == null || newUsername.isBlank()) return false;

        if (database.findUserByUsername(newUsername) != null) {
            System.out.println("[UserService] Username '" + newUsername + "' is already taken.");
            return false;
        }

        String old = user.getUsername();
        user.setUsername(newUsername);
        log("Changed username from '" + old + "' to '" + newUsername + "'");
        return true;
    }

    public void changeLanguage(User user, Language language) {
        if (user == null || language == null) return;
        // Language is stored in User but there's no setter in the base class,
        // so we rely on the field being set; extend User if needed.
        System.out.println("[UserService] Language update for '" + user.getUsername()
                + "' to " + language + " (extend User with setLanguage if needed).");
        log("Language change requested for: " + user.getUsername());
    }

    public boolean removeUser(String username) {
        User user = database.findUserByUsername(username);
        if (user == null) {
            System.out.println("[UserService] Remove failed: user '" + username + "' not found.");
            return false;
        }

        database.getUsers().remove(user);
        log("Removed user: " + username);
        System.out.println("[UserService] User '" + username + "' removed.");
        return true;
    }

    private void log(String action) {
        User actor = authService.getCurrentUser();
        if (actor != null) {
            database.addLog(new LogRecord(actor, action));
        }
    }
}
