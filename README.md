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
├── service/
├── ui/
├── storage/
├── factory/
├── comparator/
├── enums/
├── exceptions/
└── util/
```

## Getting Started

### Requirements
- Java 17+
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

### Run

Open the project in IntelliJ IDEA and run the main class located in `University/src/`.

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