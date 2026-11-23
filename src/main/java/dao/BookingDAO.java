package dao;

import model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookingDAO {

    // Check whether student exists
    public boolean studentExists(
            Connection conn, String studentId)
            throws SQLException {

        String sql = """
                SELECT student_id
                FROM students
                WHERE student_id = ?
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Insert booking into database
    public void createBooking(
            Connection conn, Booking booking)
            throws SQLException {

        String sql = """
                INSERT INTO bookings
                (student_id, slot_id, status, payment_status)
                VALUES (?, ?, 'ACTIVE', 'UNPAID')
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setString(1, booking.getStudentId());
            stmt.setInt(2, booking.getSlotId());

            stmt.executeUpdate();
        }
    }
}

