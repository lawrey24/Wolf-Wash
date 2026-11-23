import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Verify {

    public Student login(String studentId, String password) {
        if (studentId == null || studentId.isBlank() || password == null || password.isEmpty()) {
            return null;
        }

        String sql = "SELECT student_id, full_name, balance " +
                     "FROM students " +
                     "WHERE student_id = ? AND password_hash = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId.trim());
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                        rs.getString("student_id"),
                        rs.getString("full_name"),
                        rs.getBigDecimal("balance")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }

        return null;
    }
}