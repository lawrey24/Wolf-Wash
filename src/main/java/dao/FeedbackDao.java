package dao;

import model.Feedback;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDao {

    // Save a student's feedback or suggestion.
    public void save(User user, String category, String message)
            throws SQLException {

        String sql = "INSERT INTO feedback (student_id, type, message) VALUES (?, ?, ?)";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, user.getId());
            p.setString(2, category);
            p.setString(3, message);

            p.executeUpdate();
        }
    }

    // Retrieve feedback belonging to one student.
    public List<Feedback> findForUser(String userId)
            throws SQLException {

        String sql = "SELECT f.*, s.full_name FROM feedback f " +
                "JOIN students s ON s.student_id = f.student_id " +
                "WHERE f.student_id = ? ORDER BY f.created_at DESC";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, userId);

            try (ResultSet r = p.executeQuery()) {
                return readRows(r);
            }
        }
    }

    // Retrieve all feedback submissions.
    public List<Feedback> findAll() throws SQLException {

        String sql = "SELECT f.*, s.full_name FROM feedback f " +
                "JOIN students s ON s.student_id = f.student_id " +
                "ORDER BY f.created_at DESC";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {

            return readRows(r);
        }
    }

    // Allow students to delete their own NEW submissions.
    public boolean deleteOwn(int feedbackId, String userId)
            throws SQLException {

        String sql = "DELETE FROM feedback " +
                "WHERE feedback_id = ? AND student_id = ? AND status = 'NEW'";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, feedbackId);
            p.setString(2, userId);

            return p.executeUpdate() == 1;
        }
    }

    // Allow administrators to update feedback status.
    public boolean updateStatus(int feedbackId, String status,
                                String note, String adminId)
            throws SQLException {

        String sql = "UPDATE feedback SET status = ?, admin_note = ?, " +
                "updated_by = ? WHERE feedback_id = ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, status);
            p.setString(2, note);
            p.setString(3, adminId);
            p.setInt(4, feedbackId);

            return p.executeUpdate() == 1;
        }
    }

    // Convert database records into Feedback objects.
    private List<Feedback> readRows(ResultSet r)
            throws SQLException {

        List<Feedback> result = new ArrayList<>();

        while (r.next()) {

            Timestamp created = r.getTimestamp("created_at");

            result.add(new Feedback(
                    r.getInt("feedback_id"),
                    r.getString("student_id"),
                    r.getString("full_name"),
                    r.getString("type"),
                    r.getString("message"),
                    r.getString("status"),
                    r.getString("admin_note"),
                    created == null ? null : created.toLocalDateTime()
            ));
        }

        return result;
    }
}
