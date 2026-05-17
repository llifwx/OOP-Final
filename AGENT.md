# AGENT.md — University Management System

Полные инструкции для AI-агентов, работающих с этой кодовой базой. Прочитай это целиком перед тем как что-либо трогать.

---

## Обзор проекта

Java-приложение с консольным интерфейсом, симулирующее работу исследовательского университета. Финальный проект по курсу ООП и дизайна (преподаватель: Izbassar Assylzhan). Система поддерживает аутентификацию, управление курсами, исследовательские процессы, мессенджер, техподдержку и администрирование. Данные хранятся через Java-сериализацию.


---

## ГЛАВНОЕ АРХИТЕКТУРНОЕ ПРАВИЛО

```
Модели — только данные.
Сервисы — вся логика.
```

### Что должно быть в модели (model/)

Модельный класс — это контейнер данных. В нём разрешено только:

- **Приватные поля** (`private`)
- **Геттеры** — возвращают копии коллекций (`new ArrayList<>(list)`), копии дат (`new Date(date.getTime())`), примитивы и строки напрямую
- **Сеттеры** — только простая валидация (не null, диапазон числа). Никакой бизнес-логики
- **`addXxx()` / `removeXxx()`** — только добавление/удаление элемента в список без побочных эффектов
- **`toString()`, `equals()`, `hashCode()`** — обязательны для каждого класса
- **`synchronizeIdCounter(int)`** — статический метод для восстановления ID после десериализации
- **Конструкторы** — инициализация полей, базовая валидация

### Что ЗАПРЕЩЕНО в модели

- Любые обращения к `Database`
- Вызовы других сервисов
- `System.out.println(...)` с бизнес-сообщениями
- Логика принятия решений (if credits > 21, if h-index < 3, etc.)
- Каскадные операции (добавить бумагу → создать новость → уведомить подписчиков)
- `database.save()` — никогда в модели

### Что должно быть в сервисе (services/)

Вся бизнес-логика живёт только в сервисах:

1. **Auth guard** — `requireXxx()` проверяет роль через `authService.hasRole()`; бросает `SecurityException` если не та роль
2. **Null-проверки** на все входные параметры
3. **Бизнес-правила** — проверка кредитов, h-index, статусов и т.д.
4. **Изменение состояния** — вызов методов модели (`addXxx`, `setXxx`)
5. **`database.save()`** — после каждой мутации
6. **`log(action)`** — приватный хелпер, добавляет `LogRecord` и сохраняет
7. **Вывод** — `System.out.println("[ServiceName] ...")` с префиксом сервиса

**Пример правильного разделения:**

```java
// ПРАВИЛЬНО — в TeacherService:
public boolean putMark(Student student, Course course, Mark mark) {
    Teacher teacher = requireTeacher();                    // auth guard
    if (student == null || course == null || mark == null) return false; // null check
    if (!teacher.getCourses().contains(course)) return false; // business rule
    student.getTranscript().addMark(mark);                // delegate to model
    student.setGpa(student.getTranscript().calculateGpa());
    database.save();                                      // persist
    log("Put mark for " + student.getUsername());         // log
    return true;
}

// НЕПРАВИЛЬНО — бизнес-логика в модели:
// Student.registerForCourse() { if (credits > 21) throw ... } — НЕЛЬЗЯ
```

---

## Правила репозитория

### Ветки — никогда не пушить напрямую в `main`

| Префикс | Назначение | Пример |
|---------|-----------|---------|
| `feat/` | Новая функциональность | `feat/course-registration` |
| `fix/` | Баг-фикс | `fix/mark-calculation` |
| `refactor/` | Рефакторинг без новых фич | `refactor/user-hierarchy` |
| `docs/` | Только документация | `docs/update-readme` |
| `test/` | Тесты | `test/research-paper` |

Только строчные буквы и дефисы — никаких пробелов, никаких camelCase.

```bash
git checkout -b feat/your-feature-name
git push origin feat/your-feature-name
```

---

## Структура проекта

```
University/src/
├── Main.java                        — точка входа; загружает DB, создаёт admin, запускает ConsoleApplication
├── app/
│   └── ConsoleApplication.java      — цикл логина, инициализация сервисов, маршрутизация по ролям
├── model/
│   ├── users/                       — User(abstract), Employee(abstract), Teacher, Manager, Student, GraduateStudent, Admin, TechSupportSpecialist
│   ├── academic/                    — Course, Lesson, Mark, Transcript, Complaint, Report, StudentOrganization
│   ├── research/                    — ResearchPaper, ResearchProject
│   ├── social/                      — News, Comment, Message, Journal
│   └── support/                     — TechSupportReq
├── interfaces/
│   └── Researcher.java              — интерфейс; реализуют Teacher и GraduateStudent
├── services/
│   ├── AuthService.java             — логин/логаут, сессия, проверка ролей
│   ├── AdminService.java            — управление пользователями, логи
│   ├── ManagerService.java          — курсы, регистрация студентов, новости, отчёты
│   ├── TeacherService.java          — оценки, жалобы
│   ├── TechSupportService.java      — цикл жизни заявок
│   ├── MessageService.java          — отправка, инбокс, поиск
│   ├── ReportService.java           — академические и исследовательские отчёты
│   ├── ResearchPaperService.java    — публикация, цитирование, поиск бумаг
│   ├── ResearchProjectService.java  — проекты, участие, назначение супервайзера
│   ├── TranscriptService.java       — печать транскрипта
│   ├── JournalService.java          — подписки, уведомления
│   ├── NewsService.java             — фид новостей, закреп
│   ├── ResearchService.java         — поиск бумаг в DB
│   └── UserService.java             — поиск, список, регистрация пользователей
├── ui/menu/                         — AdminMenu, StudentMenu, GraduateStudentMenu, TeacherMenu, ManagerMenu, TechSupportSpecialistMenu
├── storage/
│   ├── Database.java                — singleton; все списки сущностей + методы поиска
│   └── FileStorage.java             — сериализация в University/database.ser
├── factory/
│   └── UserFactory.java             — создание всех типов пользователей по строке роли
├── comparator/                      — ResearchPaperCitationComparator, ResearchPaperDateComparator, ResearchPaperLengthComparator, StudentGpaComparator, TeacherNameComparator
├── enums/                           — AttendanceStatus, CourseType, DegreeType, Format, Language, LessonType, ManagerType, NewsTopic, RequestStatus, TeacherType, UrgencyLevel
├── exceptions/
│   ├── InvalidSupervisorEx.java     — supervisor h-index < 3
│   └── NotResearcherEx.java         — не-исследователь пытается вступить в проект
└── utils/
    ├── GradeScale.java              — статический scoreToGpa(double); используется в Transcript и ReportService
    ├── LogRecord.java               — запись лога
    └── UserNamePadding.java         — padRight() для выравнивания в консоли
```

---

## Иерархия классов

### Дерево наследования

```
User (abstract)
├── Employee (abstract)
│   ├── Teacher          — implements Researcher
│   ├── Manager
│   └── TechSupportSpecialist
├── Student
│   └── GraduateStudent  — implements Researcher
└── Admin
```

### Ключевые решения по дизайну

- `User` и `Employee` — **абстрактные**, никогда не инстанцировать напрямую
- `Researcher` — **интерфейс** (`interfaces/Researcher.java`), не абстрактный класс — потому что `Teacher` и `GraduateStudent` находятся в разных ветках иерархии
- `Researcher` содержит только `calculateHIndex()`. Методы `printPapers`, `joinProject`, `publishPaper` — в `ResearchPaperService` и `ResearchProjectService`, что соответствует архитектурному правилу
- `Transcript` создаётся **внутри конструктора `Student`**, снаружи не создавать
- Коллекции в геттерах всегда возвращаются как **копии** (`new ArrayList<>(list)`)
- Даты всегда возвращаются как **копии** (`new Date(date.getTime())`)

---

## Бизнес-правила (применять только в сервисах)

| Правило | Где |
|---------|-----|
| Максимум 21 кредит у студента | `ManagerService.approveRegistration()` |
| Максимум 3 проваленных курса | сервис выставления оценок при fail-статусе |
| Супервайзер — h-index ≥ 3 | `ResearchProjectService.assignSupervisor()` → `InvalidSupervisorEx` |
| Только `Researcher` вступает в `ResearchProject` | `ResearchProjectService.joinProject()` → `NotResearcherEx` |
| Формула оценки: 30% + 30% + 40% | `Mark` пересчитывает через приватный `calculateTotal()` — допустимо, т.к. чистое вычисление без побочных эффектов |
| Research-новости закрепляются автоматически | `ResearchPaperService.publishPaper()` → `news.pin()` |
| Публикация бумаги уведомляет подписчиков | `ResearchPaperService.publishPaper()` → `JournalService.notifySubscribers()` |
| Статусы заявок: NEW → VIEWED → ACCEPTED/REJECTED → DONE | `TechSupportService` — только валидные переходы |
| score → GPA | `GradeScale.scoreToGpa()` — используется в `Transcript.calculateGpa()` и `ReportService` |

---

## Конвенции сервисов

Каждый сервис следует этому шаблону:

```java
public ReturnType doSomething(Param param) {
    requireXxx();                           // 1. auth guard
    if (param == null) { ... return; }      // 2. null check
    // бизнес-правила                       // 3. business rules
    model.addSomething(param);              // 4. mutate model
    database.save();                        // 5. persist
    log("Did something with " + param);    // 6. log
    System.out.println("[ServiceName] ..."); // 7. feedback
}
```

**Singleton vs injection.** `JournalService`, `NewsService`, `ResearchService`, `ResearchPaperService`, `ResearchProjectService`, `TranscriptService` — синглтоны (устаревший подход в кодовой базе). Новые сервисы создавать через **constructor injection** и прокидывать из `ConsoleApplication`.

---

## Database и хранилище

`Database` — singleton, хранит все списки. Прямой доступ к спискам (`getUsers()` и т.д.) — **только из сервисов**, никогда из UI или моделей.

`FileStorage` сериализует в `University/database.ser`. Если директория `University/` существует — туда, иначе в рабочую директорию.

### Требования к каждому новому классу-сущности

- Реализовать `Serializable`
- Добавить `private static final long serialVersionUID = 1L;`
- Если есть статический счётчик ID — добавить `synchronizeXxx(int)` и зарегистрировать в `Database.synchronizeGeneratedIds()`
- `equals()`, `hashCode()`, `toString()` — обязательны

---

## Аутентификация

`AuthService` управляет сессией. Один залогиненный пользователь за раз.

- `login(username, password)` — хэширует SHA-256, сравнивает, логирует
- `logout()` — логирует, обнуляет `currentUser`
- `hasRole(Class<?>)` — использовать для проверок ролей в сервисах

Пароли хранятся как SHA-256 hex. `User.login()` только сравнивает хэши.

---

## Интерфейс Researcher

```java
public interface Researcher {
    int calculateHIndex();
}
```

Алгоритм h-index (реализован в `Teacher` и `GraduateStudent`):
- собрать citations всех бумаг, отсортировать по убыванию
- h = наибольшее i такое, что papers[i].citations ≥ i+1

Методы `printPapers`, `publishPaper`, `joinProject` — не в интерфейсе намеренно. Они в `ResearchPaperService` и `ResearchProjectService`.

---

## Компараторы

Все сортируют **по убыванию** (наибольшее / новейшее сначала):

| Компаратор | Поле | Порядок |
|------------|------|---------|
| `ResearchPaperCitationComparator` | `citations` | больше цитат — первее |
| `ResearchPaperDateComparator` | `publishDate` | новее — первее |
| `ResearchPaperLengthComparator` | `pages` | длиннее — первее |
| `StudentGpaComparator` | `gpa` | выше GPA — первее |
| `TeacherNameComparator` | `fullName` | алфавитный по возрастанию |

---

## Конвенции UI / меню

- Сервисы передаются через конструктор
- Опция `"0"` — всегда `authService.logout()` + `running = false`
- `UserNamePadding.padRight(text, length)` — для выравнивания
- Приватный `readInt()` — ловит `NumberFormatException`, возвращает `-1`
- **Меню не содержат бизнес-логики** — только ввод/вывод и вызовы сервисов

**Важно:** порядок `case` в `routeToMenu()` имеет значение — `GraduateStudent` должен быть **раньше** `Student`:

```java
case GraduateStudent i -> graduateStudentMenu.show();  // СНАЧАЛА (subclass)
case Student i -> studentMenu.show();                  // ПОТОМ
```

---

## Статус меню

| Меню | Статус | Что есть |
|------|--------|---------|
| `AdminMenu` | ✅ Готово | добавить/удалить пользователя, список, логи |
| `TechSupportSpecialistMenu` | ✅ Готово | полный цикл заявок |
| `GraduateStudentMenu` | ✅ Готово | бумаги, проекты, супервайзер |
| `StudentMenu` | ⚠️ Минимум | курсы, оценки, транскрипт |
| `TeacherMenu` | ⚠️ Минимум | курсы, бумаги, h-index |
| `ManagerMenu` | ⚠️ Минимум | GPA-сортировка, алфавитный список, отчёт |

---

## Что реализовано и что нужно доделать

### Полностью готово
- `AuthService`, `AdminService`, `AdminMenu`
- `TechSupportService`, `TechSupportSpecialistMenu`
- `ManagerService` (логика), `MessageService`, `ReportService`
- `ResearchPaperService` (публикация, цитирование PLAIN_TEXT + BIBTEX)
- `ResearchProjectService` (проекты, вступление, назначение супервайзера)
- `TeacherService` (оценки, жалобы)
- `TranscriptService`, `GradeScale`
- `Teacher.calculateHIndex()`, `GraduateStudent.calculateHIndex()`
- `Transcript.calculateGpa()`, `Transcript.addMark()`
- Все enum, компараторы, исключения, `Database`

### Нужно доделать

**StudentMenu — расширить:**
- регистрация на курс / отписка
- рейтинг преподавателя
- вступление в студенческую организацию
- подписка/отписка от журналов

**TeacherMenu — расширить:**
- выставление оценок (сервис есть: `TeacherService.putMark()`)
- отправка жалоб (сервис есть: `TeacherService.sendComplaint()`)
- публикация бумаг (сервис есть: `ResearchPaperService.publishPaper()`)
- вступление в проекты, сообщения

**ManagerMenu — расширить:**
- назначение курса преподавателю (сервис есть: `ManagerService.assignCourseToTeacher()`)
- одобрение регистрации студента (сервис есть: `ManagerService.approveRegistration()`)
- добавление курса для регистрации (сервис есть: `ManagerService.addCourseForRegistration()`)
- управление новостями (сервис есть: `ManagerService.addNews/removeNews/pinNews()`)

**Бизнес-логика:**
- Переключение языка — в `User` нет `setLanguage()`; нужно добавить поле-сеттер
- Автоматическая новость о топ-цитируемом исследователе — не реализована (требование задания)
- Проверка лимита проваленных курсов (3) — не реализована

**Известные баги:**
- `MessageService.printInbox()` — опечатка `"From);:"` вместо `"From:"` — исправить

---

## Паттерны проектирования

Задание требует 4+. Текущие:

| Паттерн | Где |
|---------|-----|
| **Singleton** | `Database`, `JournalService`, `NewsService`, `ResearchService`, `ResearchPaperService`, `ResearchProjectService`, `TranscriptService` |
| **Factory** | `UserFactory` |
| **Observer** | `Journal` уведомляет `User`-подписчиков через `JournalService.notifySubscribers()` |
| **Strategy** | `Comparator<ResearchPaper>` передаётся в `ResearchPaperService.printPapers()` |

---

## Чеклист задания

- [x] Аутентификация для всех пользователей
- [x] Сериализация / персистентность данных
- [x] Enum (TeacherType, ManagerType, CourseType, DegreeType, и др.)
- [x] Компараторы и Comparable
- [x] equals / hashCode / toString на всех сущностях
- [x] Кастомные исключения (InvalidSupervisorEx, NotResearcherEx)
- [x] Типы уроков: LECTURE / PRACTICE
- [x] Курсы MAJOR / MINOR / FREE_ELECTIVE
- [x] Формула оценки: 1-я + 2-я аттестация + финал
- [x] Расчёт h-index (Teacher и GraduateStudent)
- [x] Валидация супервайзера (h-index < 3 → исключение)
- [x] getCitation(Format) — PLAIN_TEXT и BIBTEX
- [x] printPapers с 3 компараторами
- [x] ResearchProject → исключение для не-исследователей
- [x] Подписка на журнал + Observer-уведомление
- [x] Research-новости закреплены автоматически
- [x] Цикл заявок техподдержки (NEW→VIEWED→ACCEPTED/REJECTED→DONE)
- [x] Admin: пользователи + логи
- [x] Manager: курсы, регистрация, отчёты, новости
- [x] Teacher: оценки, жалобы (сервис готов)
- [x] GraduateStudent: супервайзер, дипломные бумаги
- [ ] Переключение языка KZ/EN/RU
- [ ] Автоматическая новость о топ-цитируемом исследователе
- [ ] Student: регистрация на курс, рейтинг, организации (UI)
- [ ] Teacher: оценки, жалобы, бумаги (UI)
- [ ] Manager: полное управление курсами (UI)
- [ ] Посещаемость (бонус)

---

## Чеклист добавления новой фичи

1. Создать/обновить класс модели → `Serializable`, `serialVersionUID`, только геттеры/сеттеры/add/remove, `equals`, `hashCode`, `toString`
2. Если статический счётчик ID → добавить `synchronizeXxx()` + зарегистрировать в `Database.synchronizeGeneratedIds()`
3. Добавить список + геттер + `addXxx()` + finder(ы) в `Database`
4. Всю бизнес-логику — в сервис: auth guard → null check → правила → мутация → save → log
5. Подключить сервис в `ConsoleApplication` если новый (constructor injection)
6. Добавить/расширить класс меню для нужной роли — только UI, без логики
7. При новой роли добавить route в `ConsoleApplication.routeToMenu()`
8. Создать feature-ветку, закоммитить, запушить — прямые коммиты в `main` запрещены