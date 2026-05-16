package services;

import enums.NewsTopic;
import model.social.News;
import model.social.Comment;
import storage.Database;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class NewsService {
    private static NewsService instance;

    private NewsService() {
    }

    public static NewsService getInstance() {
        if (instance == null) instance = new NewsService();
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
        return db().findNewsByTopic(topic);
    }

    public News findNewsByTitle(String title) {
        return db().findNewsByTitle(title);
    }

    public void addNews(News news) {
        db().addNews(news);
        db().save();
    }

    public void addComment(News news, Comment comment) {
        news.addComment(comment);
        db().save();
    }

    public void pinNews(News news) {
        news.pin();
        db().save();
    }

    public void createResearchNews(String title, String content) {
        News news = new News(title, content, NewsTopic.RESEARCH);
        news.pin();
        db().addNews(news);
        db().save();
    }

    private Database db() {
        return Database.getInstance();
    }
}
