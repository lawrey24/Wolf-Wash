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

    // Submit feedback or a suggestion.
    public void submit(User user, String category, String title, String message)
            throws SQLException {

        if (user == null || user.isAdmin()) {
            throw new IllegalArgumentException(
                    "A student account is required.");
        }

        if (!"FEEDBACK".equals(category)
                && !"SUGGESTION".equals(category)) {
            throw new IllegalArgumentException(
                    "Choose a valid category.");
        }

        String cleanedTitle = title == null ? "" : title.strip();
        if (cleanedTitle.isEmpty() || cleanedTitle.length() > 150) {
            throw new IllegalArgumentException("Enter a title of 1 to 150 characters.");
        }

        String cleaned = message == null ? "" : message.trim();

        if (cleaned.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "Please enter at least 10 characters.");
        }

        if (cleaned.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Please keep the message under 1000 characters.");
        }

        dao.save(user, category, cleanedTitle, cleaned);
    }

    // Retrieve feedback belonging to a student.
    public List<Feedback> myFeedback(User user)
            throws SQLException {

        if (user == null || user.isAdmin()) {
            throw new SecurityException(
                    "Student access required.");
        }

        return dao.findForUser(user.getId());
    }

    // Delete a student's own submission.
    public boolean delete(User user, int id)
            throws SQLException {

        if (user == null || user.isAdmin()) {
            return false;
        }

        return dao.deleteOwn(id, user.getId());
    }

    // Retrieve all submissions for administrators.
    public List<Feedback> allFeedback(User user)
            throws SQLException {

        requireAdmin(user);
        return dao.findAll();
    }

    // Update a submission's status.
    public void update(User admin, int id, String status, String note)
            throws SQLException {

        requireAdmin(admin);

        if (!"IN_REVIEW".equals(status)
                && !"RESOLVED".equals(status)
                && !"CLOSED".equals(status)) {
            throw new IllegalArgumentException(
                    "Choose a valid status.");
        }

        if (!dao.updateStatus(
                id, status, note == null ? "" : note.trim(),
                admin.getId())) {

            throw new IllegalArgumentException(
                    "Feedback ID was not found.");
        }
    }

    private void requireAdmin(User user) {

        if (user == null || !user.isAdmin()) {
            throw new SecurityException(
                    "Administrator access required.");
        }
    }
}
