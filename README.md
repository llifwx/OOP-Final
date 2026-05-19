# University Research System

A research-oriented university management system built in Java as a final project for the Object-Oriented Programming & Design course.

The system simulates a real university environment where different types of users (students, teachers, managers, admins, researchers) can interact with each other and perform role-specific actions.

## Features

- **Authentication** — every user accesses the system through login credentials.
- **Role-based access** — Admin, Student, Graduate Student, Teacher, Manager, and Tech Support Specialist have different permissions.
- **Course management** — course registration, approval, teacher assignment, lesson types, and mark management.
- **Research module** — research papers, research projects, h-index calculation, journals, and researcher-specific behavior.
- **Multi-language support** — KZ / EN / RU.
- **Tech support** — users can submit requests, while tech support specialists can review and update request statuses.
- **Messaging and news** — employees can exchange messages; research news can be prioritized.
- **Reports and logs** — academic reports, transcripts, and user action logs.
- **Serialization** — system data is saved and restored through file-based storage.

## Project Structure

```
University/
│
├── Main.java
├── app/
├── model/
│   ├── academic/
│   ├── research/
│   ├── social/
│   ├── users/
│   └── support/
├── interfaces/
├── services/
├── ui/
│   └── menu/
├── storage/
├── i18n/
├── factory/
├── comparator/
├── enums/
├── exceptions/
└── utils/
```

## Getting Started

### Requirements
- Java 21+
- IntelliJ IDEA (recommended)

### Clone the repository

```bash
git clone <repository-url>
cd OOP-Final
```

> If someone has already cloned it and you need the latest changes:
> ```bash
> git pull
> ```

### Run from terminal

First, check that Java and the Java compiler are available:

```powershell
java -version
javac -version
```

If `javac` is not recognized, install JDK 21+ and add it to `PATH`.

From the project root folder, run:

```powershell
cd University
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java -Path src).FullName
java -cp out Main
```

Default admin account:

```text
login: admin
password: admin123
```

### Run from IntelliJ IDEA

Open the project in IntelliJ IDEA and run the `Main` class located in `University/src/Main.java`.

## Design Patterns

- **Factory Pattern**: `factory.UserFactory` centralizes creation of role-specific users.
- **Singleton Pattern**: `storage.Database` is the single central storage instance used by services.
- **Strategy Pattern**: classes in `comparator/` provide interchangeable sorting strategies for students, teachers, and research papers.
- **Observer Pattern**: `model.social.Journal` stores subscribers, and `services.JournalService.notifySubscribers()` notifies them when a new paper is published. Duplicate paper publications are ignored so subscribers are not notified twice for the same journal paper.

## Main OOP Concepts Used

### Encapsulation

Domain classes keep their data inside fields and expose behavior through methods.

Examples:

- `Course` stores course code, credits, instructors, lessons, enrolled students, intended major/year, and registration status.
- `User` hides user-related data and exposes it through controlled getters, setters, and methods.

### Inheritance

The user hierarchy is based on inheritance.

```text
User
├── Employee
│   ├── Teacher
│   ├── Manager
│   └── TechSupportSpecialist
└── Admin
```

This allows common user data to stay in `User`, while specific behavior is implemented in subclasses.

Examples:

- `Student` inherits common user fields from `User`.
- `Teacher`, `Manager`, and `TechSupportSpecialist` extend `Employee`.

### Polymorphism

Different user types can be treated through a common parent type or interface, while still keeping their own role-specific behavior.

Examples:

- Authentication can return a general `User`, but the system can handle it as `Student`, `Teacher`, `Manager`, `Admin`, or `TechSupportSpecialist`.
- `Researcher` can be implemented by different user types.
- Comparators allow different sorting behavior for the same type of object.

### Abstraction

The project separates high-level behavior from implementation details.

Examples:

- `Researcher` defines research-related behavior without forcing every user to be a researcher.
- Service classes hide business logic from UI classes.
- Menu classes call services instead of directly changing model data.
- Storage logic is hidden inside `Database` and `FileStorage`.

## SOLID Principles

### S — Single Responsibility Principle

Each class has a focused responsibility.

Examples:

- `AuthService` handles authentication.
- `UserService` handles user-related operations.
- `CourseService` works with courses and course registration.
- UI menu classes only interact with the user and call services.

This makes the system easier to maintain because one class does not try to do everything at once.

### O — Open/Closed Principle

The system can be extended without rewriting existing logic.

Examples:

- New user roles can be added by creating new subclasses of `User`.
- New sorting rules can be added by creating another `Comparator`.

### L — Liskov Substitution Principle

Subclasses can be used where their parent class is expected.

Examples:

- `Student` and `GraduateStudent` can be handled as `User`.
- `Teacher`, `Manager`, and `TechSupportSpecialist` can be handled as `Employee`.
- Any class implementing `Researcher` can participate in research-related operations.


### I — Interface Segregation Principle

The system uses focused interfaces instead of forcing all classes to implement unnecessary methods.

Example:

- `Researcher` is separate from `User`, because not every user must have research papers, research projects, or h-index calculation.
- Only users that actually perform research need researcher-specific behavior.
- `Notifiable` can be used for users or entities that should receive notifications.

This avoids the problem where every user class would be forced to implement research methods even if that role does not need them.

### D — Dependency Inversion Principle

Higher-level logic depends on services and abstractions instead of direct console or storage details.

Examples:

- UI classes call service classes instead of directly modifying the database.
- Services work with models and storage access methods.
- Business rules are kept outside the menu layer.
- The console UI can be changed in the future without rewriting the whole business logic.

Example:

```java
StudentMenu -> StudentService -> Database
```

The menu does not directly change all collections manually. It asks the service to perform the operation.



## Team

**Li Nikita** - Captain \
**Toktarova Amina** \
**Zeinetula Ayan** \
**Nurmakhanbetov Arman**

---

*OOP & Design Final Project — Instructor: Izbassar Assylzhan*
