package services;

import enums.NewsTopic;
import interfaces.Researcher;
import model.social.Comment;
import model.social.News;
import model.users.Admin;
import model.users.Manager;
import model.users.User;
import storage.Database;
import utils.LogRecord;

import java.util.ArrayList;
import java.util.List;

public class NewsService {
    private static NewsService instance;
    private final Database database;
    private final AuthService authService;

    public NewsService(Database database, AuthService authService) {
        this.database = database;
        this.authService = authService;
    }

    public static NewsService getInstance() {
        if (instance == null) instance = new NewsService(Database.getInstance(), null);
        return instance;
    }

    public List<News> getAllNews() {
        List<News> all = new ArrayList<>(db().getNews());
        all.sort((a, b) -> {
            if (a.isPinned() && !b.isPinned()) return -1;
            if (!a.isPinned() && b.isPinned()) return 1;
            return b.getDate().compareTo(a.getDate());
        });
        return all;
    }

    public List<News> getNewsByTopic(NewsTopic topic) {
        if (topic == null) return new ArrayList<>();
        return db().findNewsByTopic(topic);
    }

    public News findNewsByTitle(String title) {
        if (title == null || title.isBlank()) return null;
        return db().findNewsByTitle(title);
    }

    public void addNews(News news) {
        requireNewsManager();
        if (news == null) {
            System.out.println("[NewsService] News cannot be null.");
            return;
        }
        if (news.getTitle() == null || news.getTitle().isBlank()) {
            System.out.println("[NewsService] News title cannot be empty.");
            return;
        }
        if (db().findNewsByTitle(news.getTitle()) != null) {
            System.out.println("[NewsService] News with title '" + news.getTitle() + "' already exists.");
            return;
        }
        db().addNews(news);
        db().save();
        log("Added news: " + news.getTitle());
    }

    public void addComment(News news, Comment comment) {
        requireLoggedIn();
        if (news == null || comment == null) {
            System.out.println("[NewsService] News and comment are required.");
            return;
        }
        news.addComment(comment);
        db().save();
        log("Added comment to news: " + news.getTitle());
    }

    public void pinNews(News news) {
        requireNewsManager();
        if (news == null) {
            System.out.println("[NewsService] News cannot be null.");
            return;
        }
        news.pin();
        db().save();
        log("Pinned news: " + news.getTitle());
    }

    public void createResearchNews(String title, String content) {
        requireResearchNewsAuthor();
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            System.out.println("[NewsService] Research news title and content are required.");
            return;
        }
        if (db().findNewsByTitle(title) != null) {
            System.out.println("[NewsService] Research news with title '" + title + "' already exists.");
            return;
        }
        News news = new News(title, content, NewsTopic.RESEARCH);
        news.pin();
        db().addNews(news);
        db().save();
        log("Created research news: " + title);
    }

    private Database db() {
        return database;
    }

    private User requireLoggedIn() {
        User current = authService == null ? null : authService.getCurrentUser();
        if (current == null) {
            throw new SecurityException("[NewsService] Access denied: no user is logged in.");
        }
        return current;
    }

    private void requireNewsManager() {
        User current = requireLoggedIn();
        if (!(current instanceof Manager) && !(current instanceof Admin)) {
            throw new SecurityException("[NewsService] Access denied: news management requires Manager or Admin.");
        }
    }

    private void requireResearchNewsAuthor() {
        User current = requireLoggedIn();
        if (!(current instanceof Manager) && !(current instanceof Admin) && !(current instanceof Researcher)) {
            throw new SecurityException("[NewsService] Access denied: research news requires Manager, Admin, or Researcher.");
        }
    }

    private void log(String action) {
        User actor = authService == null ? null : authService.getCurrentUser();
        if (actor != null) {
            db().addLog(new LogRecord(actor, action));
            db().save();
        }
    }
}
