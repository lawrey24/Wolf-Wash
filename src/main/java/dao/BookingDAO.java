
package dao;

import model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    // Check whether the student exists
    public boolean studentExists(Connection conn, String studentId)
            throws SQLException {

        String sql = "SELECT 1 FROM students WHERE student_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Check whether the student already has an active booking
    public boolean hasActiveBooking(Connection conn, String studentId)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM bookings
                WHERE student_id = ?
                AND status = 'ACTIVE'
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Create a new booking
    public void createBooking(Connection conn, Booking booking)
            throws SQLException {

        String sql = """
                INSERT INTO bookings
                (student_id, slot_id, status, payment_status)
                VALUES (?, ?, 'ACTIVE', 'UNPAID')
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, booking.getStudentId());
            stmt.setInt(2, booking.getSlotId());

            stmt.executeUpdate();
        }
    }

    // Retrieve all active bookings belonging to a student
    public List<Booking> getStudentBookings(Connection conn, String studentId)
            throws SQLException {

        List<Booking> bookings = new ArrayList<>();

        String sql = """
                SELECT b.student_id,
                       t.slot_id,
                       t.machine_id,
                       t.dryer_id,
                       t.slot_date,
                       t.start_time,
                       t.end_time
                FROM bookings b
                JOIN time_slots t ON b.slot_id = t.slot_id
                WHERE b.student_id = ?
                AND b.status = 'ACTIVE'
                ORDER BY t.slot_date, t.start_time
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    bookings.add(new Booking(
                            rs.getString("student_id"),
                            rs.getInt("slot_id"),
                            rs.getInt("machine_id"),
                            rs.getInt("dryer_id"),
                            rs.getDate("slot_date"),
                            rs.getTime("start_time"),
                            rs.getTime("end_time")
                    ));
                }
            }
        }

        return bookings;
    }
}

