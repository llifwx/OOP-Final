# OOP Final Project Code Review

Repository reviewed: `llifwx/OOP-Final`  
Project: University Research System  
Review focus: architecture, OOP requirements, business logic, code correctness, defense readiness.

---

## 1. Overall conclusion

The project is already strong enough to be defended as an OOP final project, but it still needs cleanup before submission.
The code has a good base: user hierarchy, services, authentication, serialization, research module, tech support module, reports, journals, messages, comparators, enums, and custom exceptions.

However, several issues should be fixed before the final defense:

3. Dangerous singleton methods in services that create objects with `authService = null`.
4. Repeated `database.save()` and repeated logging inside one operation.
5. Some business requirements from the assignment are only partially implemented.
6. Some role checks and business rules are implemented, but not always consistently.
7. Some service methods are too UI-like because they print directly instead of returning results.
8. Some required design patterns are present, but they should be documented clearly.

---

## 2. Requirements from the assignment that matter most

According to the project specification, the implementation should include:

- authentication for all users;
- properly working serialization;
- UML-consistent class structure;
- object-oriented style with low coupling and high cohesion;
- usage of `Comparable`, `Comparator`, `equals`, `hashCode`, `toString`;
- enums;
- custom exceptions;
- at least 4 design patterns;
- documentation;
- course registration;
- putting marks;
- research functionality;
- h-index calculation;
- graduate student supervisor validation;
- research paper publication news;
- top cited researcher news;
- journal subscription notifications;
- tech support request workflow;
- admin user management and logs;
- manager registration approval and reports;
- teacher marks and complaints;
- student courses, marks and transcript.

The current project covers many of these, but not all are fully polished.

---

## 3. High-priority issues


```md
Default admin account:
username: admin
password: admin123
```

---

## 4. Dangerous singleton service methods

### 4.1 Problem

Several services have `getInstance()` methods that create service objects with `authService = null`.

Examples:

```java
public static ResearchPaperService getInstance() {
    if (instance == null) {
        instance = new ResearchPaperService(Database.getInstance(), null, JournalService.getInstance());
    }
    return instance;
}
```

```java
public static ResearchProjectService getInstance() {
    if (instance == null) {
        instance = new ResearchProjectService(Database.getInstance(), null);
    }
    return instance;
}
```

```java
public static JournalService getInstance() {
    if (instance == null) {
        instance = new JournalService(Database.getInstance(), null);
    }
    return instance;
}
```

```java
public static TranscriptService getInstance() {
    if (instance == null) {
        instance = new TranscriptService(null);
    }
    return instance;
}
```

### 4.2 Why this is bad

These services require the current logged-in user for access control.

For example:

```java
private User requireResearcher() {
    User current = authService == null ? null : authService.getCurrentUser();
    if (!(current instanceof Researcher)) {
        throw new SecurityException("[ResearchPaperService] Access denied: current user is not a researcher.");
    }
    return current;
}
```

If `authService` is `null`, then `current` becomes `null`, and the method always throws `SecurityException`.

So this call may fail even if a user is actually logged in:

```java
ResearchPaperService.getInstance().publishPaper(...);
```

The singleton-created service does not know about the real `AuthService`.

### 4.3 Correct approach

The project already uses constructor injection inside `ConsoleApplication`:

```java
this.authService = new AuthService(db);
this.journalService = new JournalService(db, authService);
this.researchPaperService = new ResearchPaperService(db, authService, journalService);
this.researchProjectService = new ResearchProjectService(db, authService);
this.transcriptService = new TranscriptService(authService);
```

This is better.

### 4.4 What to fix

Remove singleton fields and `getInstance()` methods from these services:

- `ResearchPaperService`
- `ResearchProjectService`
- `JournalService`
- `TranscriptService`

Use only constructor injection.

**Before**

```java
private static ResearchPaperService instance;

public static ResearchPaperService getInstance() {
    if (instance == null) {
        instance = new ResearchPaperService(Database.getInstance(), null, JournalService.getInstance());
    }
    return instance;
}
```

**After**

```java
public ResearchPaperService(Database database, AuthService authService, JournalService journalService) {
    this.database = database;
    this.authService = authService;
    this.journalService = journalService;
}
```

### 4.5 Defense explanation

Use this explanation:

> We removed singleton access from services that depend on the authenticated user. These services require `AuthService`, so they must be created through constructor injection. This makes dependencies explicit and prevents accidental creation of services without authentication context.

---

## 5. Repeated `save()` and repeated `log()`

### 5.1 Problem

Many services call `database.save()` directly, and then their private `log()` method also calls `database.save()`.

Example pattern:

```java
database.save();
log("Some action");
```

And inside `log()`:

```java
private void log(String action) {
    User actor = authService.getCurrentUser();
    if (actor != null) {
        database.addLog(new LogRecord(actor, action));
        database.save();
    }
}
```

So one business action may save the full serialized database multiple times.

This happens in:

- `AdminService`
- `UserService`
- `ManagerService`
- `TeacherService`
- `TechSupportService`
- `JournalService`
- `ResearchPaperService`
- `ResearchProjectService`

### 5.2 Why this is bad

One operation should follow this order:

```text
validate input
check permission
change data
add log
save once
```

Current behavior is closer to:

```text
change data
save
log
save
possibly another service call
save
another log
save
```

This creates unnecessary file serialization and makes the behavior harder to reason about.

### 5.3 Fix

Change every private `log()` method so it only adds the log record and does not save:

```java
private void log(String action) {
    User actor = authService.getCurrentUser();
    if (actor != null) {
        database.addLog(new LogRecord(actor, action));
    }
}
```

Then each public method that changes data should call `database.save()` once at the end.

**Correct pattern**

```java
public boolean someBusinessOperation(...) {
    requireRole();

    if (invalidInput) {
        return false;
    }

    // change domain objects
    log("Action description");

    database.save();
    return true;
}
```

### 5.4 Example: fix `UserService.registerUser()`

**Recommended version**

```java
public boolean registerUser(User user) {
    requireAdmin();

    if (user == null) {
        System.out.println("[UserService] Registration failed: user is null.");
        return false;
    }

    if (user.getUsername() == null || user.getUsername().isBlank()) {
        System.out.println("[UserService] Registration failed: username is empty.");
        return false;
    }

    if (database.findUserByUsername(user.getUsername()) != null) {
        System.out.println("[UserService] Registration failed: username '"
                + user.getUsername() + "' is already taken.");
        return false;
    }

    database.addUser(user);
    log("Registered new user: " + user.getUsername()
            + " [" + user.getClass().getSimpleName() + "]");

    database.save();

    System.out.println("[UserService] User '" + user.getUsername()
            + "' registered successfully.");

    return true;
}
```

### 5.5 Example: fix `AdminService.addUser()`

Current `AdminService.addUser()` delegates to `UserService.registerUser()` and then logs/saves again.

It should be simplified:

```java
public boolean addUser(User user) {
    requireAdmin();
    return userService.registerUser(user);
}
```

Same for `removeUser()`:

```java
public boolean removeUser(String username) {
    requireAdmin();
    return userService.removeUser(username);
}
```

---

## 6. Admin update user logic is incomplete

### 6.1 Problem

`AdminService.updateUser(User user)` does not really update fields. It only logs and saves an already modified object.

Current behavior:

```java
public void updateUser(User user) {
    requireAdmin();
    if (user == null) return;
    log("Admin updated user: " + user.getUsername());
    database.save();
    System.out.println("[AdminService] User '" + user.getUsername() + "' updated.");
}
```

### 6.2 Why it matters

The checklist requires Admin to manage users: add, remove, update.  
The current method exists, but the actual update operation is unclear.

### 6.3 Fix

Replace it with explicit update methods:

```java
public boolean updateUserEmail(String username, String newEmail) {
    requireAdmin();

    User user = userService.findByUsername(username);
    if (user == null || newEmail == null || newEmail.isBlank()) {
        return false;
    }

    user.setEmail(newEmail);
    log("Updated email for user: " + username);
    database.save();
    return true;
}
```

```java
public boolean updateUserFullName(String username, String newFullName) {
    requireAdmin();

    User user = userService.findByUsername(username);
    if (user == null || newFullName == null || newFullName.isBlank()) {
        return false;
    }

    user.setFullName(newFullName);
    log("Updated full name for user: " + username);
    database.save();
    return true;
}
```

Optional:

```java
public boolean updateUserLanguage(String username, Language language) {
    requireAdmin();

    User user = userService.findByUsername(username);
    if (user == null || language == null) {
        return false;
    }

    user.setLanguage(language);
    log("Updated language for user: " + username);
    database.save();
    return true;
}
```

---

## 7. Research supervisor business rule

### 7.1 Requirement

The assignment requires:

> All graduate students have a research supervisor who is a Researcher. If a person whose h-index < 3 is assigned as a supervisor, a custom exception must be thrown.

### 7.2 Current state

Good parts:

- `Researcher` interface exists.
- `calculateHIndex()` exists.
- `GraduateStudent` has `supervisor`.
- `InvalidSupervisorEx` exists.
- `ResearchProjectService.assignSupervisor()` checks h-index.

Current logic:

```java
public void assignSupervisor(GraduateStudent student, Researcher supervisor) throws InvalidSupervisorEx {
    requireManager();
    if (student == null || supervisor == null) {
        throw new InvalidSupervisorEx("Student and supervisor are required");
    }
    if (supervisor.calculateHIndex() < 3) {
        throw new InvalidSupervisorEx();
    }
    student.setSupervisor(supervisor);
    db().save();
    log("Assigned supervisor for graduate student: " + student.getUsername());
}
```

### 7.3 Remaining issue

`GraduateStudent.setSupervisor()` itself does not validate anything:

```java
public void setSupervisor(Researcher supervisor) {
    this.supervisor = supervisor;
}
```

This means any code can bypass the service and set an invalid supervisor directly.

### 7.4 Fix

Move the validation into the model or make `setSupervisor()` package-private/private and force usage through service.

Recommended model-level validation:

```java
public void setSupervisor(Researcher supervisor) throws InvalidSupervisorEx {
    if (supervisor == null) {
        throw new InvalidSupervisorEx("Supervisor is required");
    }

    if (supervisor.calculateHIndex() < 3) {
        throw new InvalidSupervisorEx();
    }

    this.supervisor = supervisor;
}
```

Then update service:

```java
public void assignSupervisor(GraduateStudent student, Researcher supervisor) throws InvalidSupervisorEx {
    requireManager();

    if (student == null) {
        throw new InvalidSupervisorEx("Student is required");
    }

    student.setSupervisor(supervisor);

    log("Assigned supervisor for graduate student: " + student.getUsername());
    db().save();
}
```

---

## 8. Course registration and credit limit

### 8.1 Good part

`ManagerService.approveRegistration()` checks the 21-credit limit:

```java
if (student.getCredits() + course.getCredits() > 21) {
    return false;
}
```

This matches the requirement that students cannot have more than 21 credits.

### 8.2 Issue

The number `21` is hardcoded.

### 8.3 Fix

Add a constant:

```java
private static final int MAX_STUDENT_CREDITS = 21;
```

Use it:

```java
if (student.getCredits() + course.getCredits() > MAX_STUDENT_CREDITS) {
    System.out.println("[ManagerService] Cannot approve registration. Credit limit exceeded.");
    return false;
}
```

### 8.4 Bigger business logic issue

The assignment says students should be able to view courses and register for courses.  
Currently registration approval is controlled by `ManagerService`, but the student-side registration request flow is not clearly visible.

Recommended design:

```text
Student requests registration
Manager approves/rejects registration
If approved:
    student.registeredCourses.add(course)
    course.enrolledStudents.add(student)
    student.credits += course.credits
```

Possible new class:

```java
public class CourseRegistrationRequest implements Serializable {
    private Student student;
    private Course course;
    private RequestStatus status;
}
```

Or simpler for current project: add a pending registrations list to `Database`.

---

## 9. Failed courses business rule

### 9.1 Requirement

The assignment says:

> Students cannot fail more than 3 times.

### 9.2 Current state

`Student` has:

```java
private int failedCoursesCount;
```

`TeacherService.putMark()` checks:

```java
if (mark.getTotalScore() < FAILING_SCORE && student.getFailedCoursesCount() >= MAX_FAILED_COURSES) {
    return false;
}
```

### 9.3 Issue

This prevents putting the 4th failing mark, but the business meaning is not perfect.  
In a real academic system, the mark should probably still be recorded, but the student should be flagged, blocked, or reported.

### 9.4 Recommended fix

Keep the current check for simplicity, but rename the message and document the rule.

Better:

```java
if (mark.getTotalScore() < FAILING_SCORE
        && student.getFailedCoursesCount() >= MAX_FAILED_COURSES) {
    throw new MarkException("Student cannot fail more than 3 courses.");
}
```

Also add a custom exception:

```java
public class MarkException extends Exception {
    public MarkException(String message) {
        super(message);
    }
}
```

---

## 10. Teacher mark logic needs one more validation

### 10.1 Problem

`TeacherService.putMark(Student student, Course course, Mark mark)` receives `student`, `course`, and `mark`, but does not check whether the mark belongs to the same student and course.

Possible bug:

```java
Mark mark = new Mark(studentA, courseA);
teacherService.putMark(studentB, courseB, mark);
```

The method could put a mark object with mismatched internal data into another student's transcript.

### 10.2 Fix

Add validation:

```java
if (!mark.getStudent().equals(student) || !mark.getCourse().equals(course)) {
    System.out.println("[TeacherService] Mark does not belong to this student/course.");
    return false;
}
```

Recommended:

```java
public boolean putMark(Student student, Course course, Mark mark) {
    Teacher teacher = requireTeacher();

    if (student == null || course == null || mark == null) {
        System.out.println("[TeacherService] Student, course, or mark is null.");
        return false;
    }

    if (!mark.getStudent().equals(student) || !mark.getCourse().equals(course)) {
        System.out.println("[TeacherService] Mark does not match the given student and course.");
        return false;
    }

    if (!teacher.getCourses().contains(course)) {
        System.out.println("[TeacherService] Teacher is not assigned to this course.");
        return false;
    }

    if (!course.getEnrolledStudents().contains(student)) {
        System.out.println("[TeacherService] Student is not enrolled in this course.");
        return false;
    }

    if (mark.getTotalScore() < FAILING_SCORE
            && student.getFailedCoursesCount() >= MAX_FAILED_COURSES) {
        System.out.println("[TeacherService] Cannot put failing mark. Student already has 3 failed courses.");
        return false;
    }

    student.getTranscript().addMark(mark);
    student.setGpa(student.getTranscript().calculateGpa());

    if (mark.getTotalScore() < FAILING_SCORE) {
        student.setFailedCoursesCount(student.getFailedCoursesCount() + 1);
    }

    log("Put mark for student " + student.getUsername()
            + " in course " + course.getCourseCode());

    database.save();
    return true;
}
```

---

## 11. Research paper and research project services have the same singleton problem

### 11.1 Problem

Both services depend on authentication but have singleton fallback with `authService = null`.

Affected services:

- `ResearchPaperService`
- `ResearchProjectService`

### 11.2 Fix

Remove `getInstance()` from both.

Also check all calls to:

```java
ResearchPaperService.getInstance()
ResearchProjectService.getInstance()
```

Replace them with constructor-injected service objects.

---

## 12. Journal subscription is a good Observer Pattern candidate

### 12.1 Good part

`Journal` has subscribers:

```java
private List<User> subscribers;
```

`JournalService.notifySubscribers()` notifies users:

```java
for (User user : journal.getSubscribers()) {
    user.receiveNotification("New paper in " + journal.getName());
}
```

This is a good implementation of the Observer pattern.

### 12.2 What to improve

Document this clearly in README/report:

```text
Observer Pattern:
Journal is the subject/publisher.
Users are observers/subscribers.
When a new paper is published in a journal, JournalService notifies all subscribed users.
```

### 12.3 Code improvement

Avoid duplicate notifications if the same paper is republished or already exists in a journal.

Add a check:

```java
if (journal.getPapers().contains(paper)) {
    System.out.println("[JournalService] Paper already exists in journal.");
    return;
}
```

---

## 13. Design patterns status

The assignment requires 4 or more design patterns.

Current likely patterns:

### 13.1 Factory Pattern

`UserFactory` creates users based on role and hides constructor complexity.

Status: implemented.

### 13.2 Singleton-like Pattern

`Database.getInstance()` works as a central storage singleton.

Status: implemented, but be careful with service singletons.  
Only `Database` should remain singleton-like.

### 13.3 Strategy Pattern

Comparators such as:

- `StudentGpaComparator`
- `TeacherNameComparator`
- `ResearchPaperDateComparator`
- `ResearchPaperCitationComparator`
- `ResearchPaperLengthComparator`

can be explained as Strategy Pattern because sorting behavior changes depending on comparator.

Status: implemented.

### 13.4 Observer Pattern

`Journal` subscribers and `JournalService.notifySubscribers()` represent Observer Pattern.

Status: implemented.

### 13.5 Recommended documentation

Add a `DESIGN_PATTERNS.md` or README section:

```md
## Design Patterns Used

1. Factory Pattern — `UserFactory`
2. Singleton Pattern — `Database`
3. Strategy Pattern — custom comparators for sorting students, teachers, research papers
4. Observer Pattern — journal subscriptions and notifications
```

---


## 15. Service classes print too much

### 15.1 Problem

Many service methods directly print messages with `System.out.println()`.

Example:

```java
System.out.println("[UserService] User registered successfully.");
```

### 15.2 Why this matters

Services should ideally contain business logic, while menu/UI classes should handle printing.  
Current services are mixed with UI output.

### 15.3 Recommended approach

For this console project, direct printing is acceptable, but do not overdo it.

Better design:

```java
public boolean registerUser(User user) {
    // return true/false
}
```

Then menu prints:

```java
if (userService.registerUser(user)) {
    System.out.println("User registered successfully.");
}
```

### 15.4 Defense explanation

Use this if asked:

> Since this is a console application, some services print short status messages for usability. In a larger application, we would move all presentation logic to UI/menu classes.

---

## 16. Authentication and password security

### 16.1 Good part

`User` hashes passwords using SHA-256 instead of storing plain text.

### 16.2 What to say carefully

Do not claim this is production-level security.

Use this:

> Passwords are hashed with SHA-256 for a simplified educational console project. In a production system, we would use salted password hashing such as BCrypt or Argon2.

---

## 17. Documentation required before submission

The assignment explicitly requires documentation with project submission.

Recommended files:

```text
README.md
REVIEW.md
DESIGN_PATTERNS.md
USER_GUIDE.md
```

At minimum update README with:

- Java version;
- correct project structure;
- how to run;
- default admin credentials;
- feature list;
- design patterns used;
- serialization explanation;
- known limitations.

Also the final report should include:

- detailed description of classes;
- interfaces;
- enums;
- custom exceptions;
- UML diagrams;
- code fragments;
- problems faced;
- project management screenshots.

---

## 18. Priority fix list

### Critical fixes

1. Fix Java version mismatch.
2. Update README structure.
3. Remove service singletons with `authService = null`.
4. Refactor `log()` methods so they do not call `database.save()`.
5. Save database once per business operation.

### High priority business logic fixes

6. Make `AdminService.updateUser()` actually update specific fields.
7. Add mark/student/course consistency validation in `TeacherService.putMark()`.
8. Ensure supervisor validation cannot be bypassed.
9. Document and verify 4 design patterns.
10. Clarify student course registration request flow.

### Medium priority fixes

11. Replace hardcoded credit limit `21` with constant.
12. Add custom `MarkException` or use existing exceptions consistently.
13. Add rejection reason to tech support requests.
14. Prevent duplicate journal paper notifications.
15. Reduce direct printing inside services where possible.

### Polish fixes

16. Add `DESIGN_PATTERNS.md`.
17. Add `USER_GUIDE.md`.
18. Add screenshots or sample console flow for report.
19. Add sample users/courses for demo.
20. Add short known limitations section.

---

## 19. Prompt for fixing the project

Use this prompt for an AI coding assistant or teammate.

```text
You are reviewing and refactoring a Java OOP final project called "University Research System".

Repository structure:
- University/src/Main.java
- University/src/app/ConsoleApplication.java
- University/src/model/users/*
- University/src/model/academic/*
- University/src/model/research/*
- University/src/model/social/*
- University/src/model/support/*
- University/src/services/*
- University/src/storage/*
- University/src/factory/*
- University/src/comparator/*
- University/src/enums/*
- University/src/exceptions/*
- University/src/utils/*

Main project requirements:
- Console university system with authentication.
- Roles: User, Employee, Teacher, Manager, Student, GraduateStudent, Admin, TechSupportSpecialist.
- Researcher interface.
- Course registration, putting marks, research functionality.
- Proper serialization.
- Enums, custom exceptions, comparators, equals/hashCode/toString.
- At least 4 design patterns.
- Graduate student supervisor must be a Researcher with h-index >= 3, otherwise throw custom exception.
- Students cannot take more than 21 credits.
- Students cannot fail more than 3 times.
- Research paper publication must create research news.
- Top cited researcher news should be generated.
- Journal subscriptions should notify users.
- Tech support workflow: NEW, VIEWED, ACCEPTED, REJECTED, DONE.
- Admin can manage users and see logs.
- Manager can approve registration, assign courses, create reports, manage news.
- Teacher can view courses, put marks, send complaints.
- Student can view courses, transcript, marks.

Your task:
Refactor the project without changing its overall architecture and without breaking existing package names.

Fix the following issues:


3. Remove dangerous service singletons:
   - Remove static instance and getInstance() from:
     ResearchPaperService,
     ResearchProjectService,
     JournalService,
     TranscriptService.
   - These services require AuthService and must be created through constructors.
   - Keep Database.getInstance() because Database is central storage.

4. Fix repeated save/log:
   - In every service, private log(String action) must only add LogRecord.
   - log() must NOT call database.save().
   - Each public method that changes data should call database.save() once at the end.
   - Apply this to AdminService, UserService, ManagerService, TeacherService, TechSupportService, JournalService, ResearchPaperService, ResearchProjectService.

5. AdminService:
   - Simplify addUser() and removeUser() so they do not duplicate UserService logs/saves.
   - Replace vague updateUser(User user) with explicit methods:
     updateUserEmail(String username, String newEmail),
     updateUserFullName(String username, String newFullName),
     optionally updateUserLanguage(String username, Language language).

6. TeacherService.putMark():
   - Add validation that Mark belongs to the same Student and Course passed into the method.
   - If mark.getStudent() does not equal student or mark.getCourse() does not equal course, return false or throw MarkException.
   - Keep validation that teacher is assigned to course and student is enrolled.

7. Supervisor validation:
   - Make sure invalid supervisor assignment cannot bypass ResearchProjectService.
   - Either validate in GraduateStudent.setSupervisor() or make setter package-private/private and only assign through ResearchProjectService.
   - If supervisor.calculateHIndex() < 3, throw InvalidSupervisorEx.

8. Credit limit:
   - Replace hardcoded 21 in ManagerService with:
     private static final int MAX_STUDENT_CREDITS = 21;

9. Failed courses:
   - Consider adding MarkException.
   - If student already has 3 failed courses, prevent another failing mark or throw MarkException.
   - Keep behavior consistent with project requirements.

10. Journal observer pattern:
   - Keep Journal subscribers and notifySubscribers().
   - Avoid duplicate paper notifications if the paper already exists in the journal.
   - Document this as Observer Pattern.

11. Design patterns documentation:
   - Document at least:
     Factory Pattern: UserFactory
     Singleton Pattern: Database
     Strategy Pattern: Comparators
     Observer Pattern: Journal subscriptions


Important rules:
- Do not rename packages unless necessary.
- Do not remove existing working functionality.
- Preserve serialization compatibility where possible.
- Keep the project console-based.
- Keep business logic in services, not in menus.
- Keep models responsible for entity state and simple invariants.
- After refactoring, the project must compile and run.
- Provide a short summary of changed files and why each change was made.
```

---

## 20. Defense-ready summary

Use this in the final report or oral defense:

> The system follows a layered OOP structure. Model classes represent university entities, service classes contain business logic and access control, menu classes handle console interaction, and storage classes handle serialization. The project uses inheritance for user roles, interfaces for research behavior, enums for fixed domain values, comparators for sorting strategies, a factory for user creation, observer-like journal subscriptions, and singleton-like centralized storage. The main improvement areas are reducing duplicated save/log calls, removing unsafe service singletons, synchronizing documentation with code, and strengthening business rules such as supervisor validation and mark consistency.
