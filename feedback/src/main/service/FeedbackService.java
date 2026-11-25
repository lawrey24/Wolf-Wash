package service;

import dao.FeedbackDao;
import model.Feedback;
import model.User;

import java.sql.SQLException;
import java.util.List;

public class FeedbackService {
    public static final int MIN_LENGTH = 10;
    public static final int MAX_LENGTH = 1000;

    private final FeedbackDao dao;

    public FeedbackService(FeedbackDao dao) {
        this.dao = dao;
    }

    public User login(String id, String password) throws SQLException {
        if (id == null || id.trim().isEmpty() || password == null || password.isEmpty()) return null;
        return dao.login(id.trim(), password);
    }

    public void submit(User user, String category, String message) throws SQLException {
        if (user == null || user.isAdmin()) throw new IllegalArgumentException("A student account is required.");
        if (!"FEEDBACK".equals(category) && !"SUGGESTION".equals(category))
            throw new IllegalArgumentException("Choose a valid category.");
        String cleaned = message == null ? "" : message.trim();
        if (cleaned.length() < MIN_LENGTH)
            throw new IllegalArgumentException("Please enter at least " + MIN_LENGTH + " characters.");
        if (cleaned.length() > MAX_LENGTH)
            throw new IllegalArgumentException("Please keep the message under " + MAX_LENGTH + " characters.");
        dao.save(user, category, cleaned);
    }

    public List<Feedback> myFeedback(User user) throws SQLException {
        return dao.findForUser(user.getId());
    }

    public boolean delete(User user, int id) throws SQLException {
        if (user == null || user.isAdmin()) return false;
        return dao.deleteOwn(id, user.getId());
    }

    public List<Feedback> allFeedback(User user) throws SQLException {
        requireAdmin(user);
        return dao.findAll();
    }

    public void update(User admin, int id, String status, String note) throws SQLException {
        requireAdmin(admin);
        if (!"IN_REVIEW".equals(status) && !"RESOLVED".equals(status) && !"CLOSED".equals(status))
            throw new IllegalArgumentException("Choose IN_REVIEW, RESOLVED or CLOSED.");
        if (!dao.updateStatus(id, status, note == null ? "" : note.trim(), admin.getId()))
            throw new IllegalArgumentException("Feedback ID was not found.");
    }

    private void requireAdmin(User user) {
        if (user == null || !user.isAdmin()) throw new SecurityException("Administrator access required.");
    }
}
