# University Research System

A research-oriented university management system built in Java as a final project for the Object-Oriented Programming & Design course.

The system simulates a real university environment where different types of users (students, teachers, managers, admins, researchers) can interact with each other and perform role-specific actions.

## Features

- **Authentication** — all users log in with credentials
- **Course management** — registration, approval, mark assignment
- **Research** — publish papers, manage projects, calculate h-index, subscribe to journals
- **Multi-language support** — KZ / EN / RU
- **Tech support** — submit and handle requests with status tracking
- **Messaging & News** — internal messages between employees, pinned research news
- **Reports** — academic statistics, transcripts, log files

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
│   └── support/
├── interfaces/
├── services/
├── ui/
│   └── menu/
├── storage/
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

### Manual verification: manager-only course assignment

1. Log in as a teacher.
2. Open the teacher menu and confirm there is no action for assigning yourself to a course.
3. Use teacher actions only for assigned courses: view courses, view enrolled students, put marks, send complaints, and send messages.
4. Log out, then log in as a manager.
5. Choose the manager course assignment action and assign a teacher with lesson type `LECTURE` or `PRACTICE`.
6. Confirm the course lists the teacher for that lesson type and the teacher's course list includes the course.
7. Repeat the same assignment for the same lesson type and confirm no duplicate instructor is added.

### Run from IntelliJ IDEA

Open the project in IntelliJ IDEA and run the `Main` class located in `University/src/Main.java`.

## Design Patterns

- **Factory Pattern**: `factory.UserFactory` centralizes creation of role-specific users.
- **Singleton Pattern**: `storage.Database` is the single central storage instance used by services.
- **Strategy Pattern**: classes in `comparator/` provide interchangeable sorting strategies for students, teachers, and research papers.
- **Observer Pattern**: `model.social.Journal` stores subscribers, and `services.JournalService.notifySubscribers()` notifies them when a new paper is published. Duplicate paper publications are ignored so subscribers are not notified twice for the same journal paper.

## Git Branch Naming

Always create a new branch before working on something. Never push directly to `main`.

| Prefix | When to use | Example |
|--------|-------------|---------|
| `feat/` | Adding new functionality | `feat/course-registration` |
| `fix/` | Fixing a bug | `fix/mark-calculation` |
| `refactor/` | Restructuring code, no new features | `refactor/user-hierarchy` |
| `docs/` | Documentation only | `docs/update-readme` |
| `test/` | Adding or fixing tests | `test/research-paper` |

```bash
# Create and switch to a new branch
git checkout -b feat/your-feature-name
 
# Push the branch
git push origin feat/your-feature-name
```

> Use lowercase and hyphens only — no spaces, no camelCase.

## Team

**Li Nikita** - Captain \
**Toktarova Amina** \
**Zeinetula Ayan** \
**Nurmakhanbetov Arman**

---

*OOP & Design Final Project — Instructor: Izbassar Assylzhan*
