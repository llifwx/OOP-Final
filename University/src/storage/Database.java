package storage;


import enums.NewsTopic;
import enums.RequestStatus;
import model.academic.*;
import model.research.ResearchPaper;
import model.research.ResearchProject;
import model.social.Journal;
import model.social.Message;
import model.social.News;
import model.support.TechSupportReq;
import model.users.Employee;
import model.users.Student;
import model.users.Teacher;
import model.users.User;
import utils.LogRecord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Database implements Serializable {
    private static final long serialVersionUID = 1L;

    private static Database instance;

    private List<User> users;

    private List<Course> courses;
    private List<Lesson> lessons;
    private List<Complaint> complaints;
    private List<StudentOrganization> studentOrganizations;

    private List<News> news;
    private List<Journal> journals;
    private List<Message> messages;

    private List<ResearchProject> researchProjects;
    private List<ResearchPaper> researchPapers;

    private List<TechSupportReq> techSupportReqs;

    private List<LogRecord> logs;
    private List<Report> reports;
    private int nextTechSupportReqId;

    private Database() {
        this.users = new ArrayList<>();

        this.courses = new ArrayList<>();
        this.lessons = new ArrayList<>();
        this.complaints = new ArrayList<>();
        this.studentOrganizations = new ArrayList<>();

        this.news = new ArrayList<>();
        this.journals = new ArrayList<>();
        this.messages = new ArrayList<>();

        this.researchProjects = new ArrayList<>();
        this.researchPapers = new ArrayList<>();

        this.techSupportReqs = new ArrayList<>();

        this.logs = new ArrayList<>();
        this.reports = new ArrayList<>();
        this.nextTechSupportReqId = 1;
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }

        return instance;
    }

    public static void setInstance(Database instance) {
        Database.instance = instance;
        if (Database.instance != null) {
            Database.instance.normalizeAfterLoad();
        }
    }

    public static void load() {
        Database loadedDb = FileStorage.load();
        if (loadedDb != null) {
            setInstance(loadedDb);
        }
    }

    public void save() {
        FileStorage.save(this);
    }

    public int nextTechSupportReqId() {
        return nextTechSupportReqId++;
    }

    private void normalizeAfterLoad() {
        ensureLists();
        synchronizeGeneratedIds();
    }

    private void ensureLists() {
        if (users == null) users = new ArrayList<>();
        if (courses == null) courses = new ArrayList<>();
        if (lessons == null) lessons = new ArrayList<>();
        if (complaints == null) complaints = new ArrayList<>();
        if (studentOrganizations == null) studentOrganizations = new ArrayList<>();
        if (news == null) news = new ArrayList<>();
        if (journals == null) journals = new ArrayList<>();
        if (messages == null) messages = new ArrayList<>();
        if (researchProjects == null) researchProjects = new ArrayList<>();
        if (researchPapers == null) researchPapers = new ArrayList<>();
        if (techSupportReqs == null) techSupportReqs = new ArrayList<>();
        if (logs == null) logs = new ArrayList<>();
        if (reports == null) reports = new ArrayList<>();
        if (nextTechSupportReqId <= 0) nextTechSupportReqId = 1;
    }

    private void synchronizeGeneratedIds() {
        int maxUserId = 0;
        for (User user : users) {
            if (user != null && user.getId() > maxUserId) maxUserId = user.getId();
        }
        User.synchronizeIdCounter(maxUserId);

        int maxCourseId = 0;
        for (Course course : courses) {
            if (course != null && course.getId() > maxCourseId) maxCourseId = course.getId();
        }
        Course.synchronizeIdCounter(maxCourseId);

        int maxLessonId = 0;
        for (Lesson lesson : lessons) {
            if (lesson != null && lesson.getId() > maxLessonId) maxLessonId = lesson.getId();
        }
        Lesson.synchronizeIdCounter(maxLessonId);

        int maxComplaintId = 0;
        for (Complaint complaint : complaints) {
            if (complaint != null && complaint.getId() > maxComplaintId) maxComplaintId = complaint.getId();
        }
        Complaint.synchronizeNextId(maxComplaintId + 1);

        int maxOrganizationId = 0;
        for (StudentOrganization organization : studentOrganizations) {
            if (organization != null && organization.getId() > maxOrganizationId) maxOrganizationId = organization.getId();
        }
        StudentOrganization.synchronizeIdCounter(maxOrganizationId);

        int maxMessageId = -1;
        for (Message message : messages) {
            if (message != null && message.getId() > maxMessageId) maxMessageId = message.getId();
        }
        Message.synchronizeNextId(maxMessageId + 1);

        int maxSupportReqId = 0;
        for (TechSupportReq request : techSupportReqs) {
            if (request != null && request.getId() > maxSupportReqId) maxSupportReqId = request.getId();
        }
        if (nextTechSupportReqId <= maxSupportReqId) nextTechSupportReqId = maxSupportReqId + 1;

        int maxLogId = -1;
        for (LogRecord log : logs) {
            if (log != null && log.getId() > maxLogId) maxLogId = log.getId();
        }
        LogRecord.synchronizeNextId(maxLogId + 1);
    }

    public List<User> getUsers() {return users;}

    public List<Course> getCourses() {return courses;}

    public List<Lesson> getLessons() {return lessons;}

    public List<Complaint> getComplaints() {return complaints;}

    public List<StudentOrganization> getStudentOrganizations() {return studentOrganizations;}

    public List<News> getNews() {return news;}

    public List<Journal> getJournals() {return journals;}

    public List<Message> getMessages() {return messages;}

    public List<ResearchProject> getResearchProjects() {return researchProjects;}

    public List<ResearchPaper> getResearchPapers() {return researchPapers;}

    public List<TechSupportReq> getTechSupportReqs() {return techSupportReqs;}

    public List<LogRecord> getLogs() {return logs;}

    public List<Report> getReports() {return reports;}

    public void addUser(User user) {users.add(user);}

    public void addCourse(Course course) {courses.add(course);}

    public void addLesson(Lesson lesson) {lessons.add(lesson);}

    public void addComplaint(Complaint complaint) {complaints.add(complaint);}

    public void addStudentOrganization(StudentOrganization studentOrganization) {studentOrganizations.add(studentOrganization);}

    public void addNews(News newNews) {news.add(newNews);}

    public void addJournal(Journal journal) {journals.add(journal);}

    public void addMessage(Message message) {messages.add(message);}

    public void addResearchProject(ResearchProject researchProject) {researchProjects.add(researchProject);}

    public void addResearchPaper(ResearchPaper researchPaper) {researchPapers.add(researchPaper);}

    public void addTechSupportReq(TechSupportReq techSupportReq) {techSupportReqs.add(techSupportReq);}

    public void addLog(LogRecord log) {logs.add(log);}

    public void addReport(Report report) {reports.add(report);}

    public User findUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public User findUserById(int id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    public Course findCourseByCode(String code) {
        for (Course course : courses) {
            if (course.getCourseCode().equalsIgnoreCase(code)) {
                return course;
            }
        }
        return null;
    }

    public Course findCourseByName(String name) {
        for (Course course : courses) {
            if (course.getName().equalsIgnoreCase(name)) {
                return course;
            }
        }
        return null;
    }

    public Lesson findLessonById(int id) {
        for (Lesson lesson : lessons) {
            if (lesson.getId() == id) {
                return lesson;
            }
        }
        return null;
    }

    public List<Lesson> findLessonsByCourse(Course course) {
        List<Lesson> result = new ArrayList<>();

        for (Lesson lesson : lessons) {
            if (lesson.getCourse().equals(course)) {
                result.add(lesson);
            }
        }

        return result;
    }

    public Complaint findComplaintById(int id) {
        for (Complaint complaint : complaints) {
            if (complaint.getId() == id) {
                return complaint;
            }
        }
        return null;
    }

    public List<Complaint> findComplaintsByStudent(Student student) {
        List<Complaint> result = new ArrayList<>();

        for (Complaint complaint : complaints) {
            if (complaint.getStudent().equals(student)) {
                result.add(complaint);
            }
        }

        return result;
    }

    public List<Complaint> findComplaintsByTeacher(Teacher teacher) {
        List<Complaint> result = new ArrayList<>();

        for (Complaint complaint : complaints) {
            if (complaint.getTeacher().equals(teacher)) {
                result.add(complaint);
            }
        }

        return result;
    }

    public StudentOrganization findStudentOrganizationByName(String name) {
        for (StudentOrganization organization : studentOrganizations) {
            if (organization.getName().equalsIgnoreCase(name)) {
                return organization;
            }
        }
        return null;
    }

    public News findNewsByTitle(String title) {
        for (News newsItem : news) {
            if (newsItem.getTitle().equalsIgnoreCase(title)) {
                return newsItem;
            }
        }
        return null;
    }

    public List<News> findNewsByTopic(NewsTopic topic) {
        List<News> result = new ArrayList<>();

        for (News newsItem : news) {
            if (newsItem.getTopic() == topic) {
                result.add(newsItem);
            }
        }

        return result;
    }

    public Journal findJournalByName(String name) {
        for (Journal journal : journals) {
            if (journal.getName().equalsIgnoreCase(name)) {
                return journal;
            }
        }
        return null;
    }

    public Message findMessageById(int id) {
        for (Message message : messages) {
            if (message.getId() == id) {
                return message;
            }
        }
        return null;
    }

    public List<Message> findMessagesBySender(Employee sender) {
        List<Message> result = new ArrayList<>();

        for (Message message : messages) {
            if (message.getSender().equals(sender)) {
                result.add(message);
            }
        }

        return result;
    }

    public List<Message> findMessagesByReceiver(Employee receiver) {
        List<Message> result = new ArrayList<>();

        for (Message message : messages) {
            if (message.getReceiver().equals(receiver)) {
                result.add(message);
            }
        }

        return result;
    }

    public ResearchProject findResearchProjectByTopic(String topic) {
        for (ResearchProject project : researchProjects) {
            if (project.getTopic().equalsIgnoreCase(topic)) {
                return project;
            }
        }
        return null;
    }

    public ResearchPaper findResearchPaperByTitle(String title) {
        for (ResearchPaper paper : researchPapers) {
            if (paper.getTitle().equalsIgnoreCase(title)) {
                return paper;
            }
        }
        return null;
    }

    public ResearchPaper findResearchPaperByDoi(String doi) {
        for (ResearchPaper paper : researchPapers) {
            if (paper.getDoi().equalsIgnoreCase(doi)) {
                return paper;
            }
        }
        return null;
    }

    public TechSupportReq findTechSupportReqById(int id) {
        for (TechSupportReq request : techSupportReqs) {
            if (request.getId() == id) {
                return request;
            }
        }
        return null;
    }

    public List<TechSupportReq> findTechSupportReqsByStatus(RequestStatus status) {
        List<TechSupportReq> result = new ArrayList<>();

        for (TechSupportReq request : techSupportReqs) {
            if (request.getStatus() == status) {
                result.add(request);
            }
        }

        return result;
    }

    public LogRecord findLogById(int id) {
        for (LogRecord log : logs) {
            if (log.getId() == id) {
                return log;
            }
        }
        return null;
    }

    public List<LogRecord> findLogsByUser(User user) {
        List<LogRecord> result = new ArrayList<>();

        for (LogRecord log : logs) {
            if (log.getUser().equals(user)) {
                result.add(log);
            }
        }

        return result;
    }

    public Report findReportByTitle(String title) {
        for (Report report : reports) {
            if (report.getTitle().equalsIgnoreCase(title)) {
                return report;
            }
        }
        return null;
    }
}
