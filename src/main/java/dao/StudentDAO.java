package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDAO {

    // Check if a student exists in the database
    public boolean studentExists(int studentId) throws SQLException {

        String sql = "SELECT student_id FROM students WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {

                // Returns true if student exists
                // Returns false if student does not exist
                return rs.next();
            }
        }
    }

    // Test the StudentDAO class
    public static void main(String[] args) {

        StudentDAO studentDAO = new StudentDAO();

        // Replace these IDs with IDs from your students table
        int validStudentId = 620012345;
        int invalidStudentId = 999999;

        try {

            // Test valid student
            if (studentDAO.studentExists(validStudentId)) {
                System.out.println("Valid student. Access granted.");
            } else {
                System.out.println("Invalid student. Access denied.");
            }

            // Test invalid student
            if (studentDAO.studentExists(invalidStudentId)) {
                System.out.println("Valid student. Access granted.");
            } else {
                System.out.println("Invalid student. Access denied.");
            }

        } catch (SQLException e) {
            System.out.println("Database error while checking student.");
            e.printStackTrace();
        }
    }
}
