

package service;

import dao.DatabaseConnection;
import model.Booking;
import model.TimeSlot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Cancelation {

    // 1. Authenticate student
    public boolean authenticate(String studentID, String password) {

        String sql = """
                SELECT 1
                FROM students
                WHERE student_id = ?
                AND password_hash = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentID);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Authentication Error: " + e.getMessage());
            return false;
        }
    }

    // 2. Retrieve all active bookings belonging to a student
    public List<Booking> getStudentBookings(String studentID) {

        List<Booking> bookings = new ArrayList<>();

        String sql = """
                SELECT b.payment_status, t.slot_id,
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

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentID);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                   Booking booking = new Booking(
                      studentID,
                      rs.getInt("slot_id"),
                      rs.getInt("machine_id"),
                      rs.getInt("dryer_id"),
                      rs.getDate("slot_date"),
                      rs.getTime("start_time"),
                      rs.getTime("end_time")
                    
                );
                    booking.setPaymentStatus(rs.getString("payment_status"));
                    bookings.add(booking);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error receiving bookings: " + e.getMessage());
        }

        return bookings;
    }

    // 3. Retrieve all available future time slots
    public List<TimeSlot> getAvailableSlots() {

        List<TimeSlot> slots = new ArrayList<>();

        String sql = """
                SELECT slot_id,
                       machine_id,
                       dryer_id,
                       slot_date,
                       start_time,
                       end_time,
                       status
                FROM time_slots
                WHERE status = 'AVAILABLE'
                AND TIMESTAMP(slot_date, start_time) > NOW()
                ORDER BY slot_date, start_time
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                TimeSlot slot = new TimeSlot(
                        rs.getInt("slot_id"),
                        rs.getInt("machine_id"),
                        rs.getInt("dryer_id"),
                        rs.getDate("slot_date").toLocalDate(),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime(),
                        rs.getString("status")
                );

                slots.add(slot);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving available slots: " + e.getMessage());
        }

        return slots;
    }

    // 4. Cancel an existing booking
    public String cancelBooking(String studentId, int slotId) {

        return cancelBooking(studentId, slotId, "");
    }

    public String cancelBooking(String studentId, int slotId, String settlement) {
        boolean credit = "CREDITED".equals(settlement);
        boolean refund = "REFUND_PENDING".equals(settlement);
        String cancelBookingSql = """
                UPDATE bookings
                SET status = 'CANCELLED', payment_status = ?, refund_due = IF(? = 'REFUND_PENDING', NOW() + INTERVAL 3 DAY, NULL)
                WHERE student_id = ?
                AND slot_id = ?
                AND status = 'ACTIVE' AND payment_status = ?
                """;

        String releaseSlotSql = """
                UPDATE time_slots
                SET status = 'AVAILABLE'
                WHERE slot_id = ?
                AND status = 'BOOKED'
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                int bookingId;
                try (var lock=conn.prepareStatement("SELECT booking_id FROM bookings WHERE student_id=? AND slot_id=? AND status='ACTIVE' FOR UPDATE")) {
                    lock.setString(1,studentId);
                    lock.setInt(2,slotId);
                    try(var row=lock.executeQuery()) {
                        if(!row.next()) { conn.rollback(); return "No active booking for that slot."; }
                        bookingId=row.getInt(1);
                    }
                }
                // Cancel the student's active booking
                try (PreparedStatement cancelStmt =
                             conn.prepareStatement(cancelBookingSql)) {

                    String status = credit || refund ? settlement : "UNPAID";
                    cancelStmt.setString(1, status);
                    cancelStmt.setString(2, status);
                    cancelStmt.setString(3, studentId);
                    cancelStmt.setInt(4, slotId);
                    cancelStmt.setString(5, credit || refund ? "PAID" : "UNPAID");

                    if (cancelStmt.executeUpdate() != 1) {
                        conn.rollback();

                        return "Cancellation failed: Booking not found or already cancelled.";
                    }
                }

                if (credit) {
                    String sql = """
                        UPDATE students s JOIN bookings b ON b.student_id=s.student_id
                        JOIN payments p ON p.booking_id=b.booking_id
                        SET s.balance=s.balance+p.amount
                        WHERE s.student_id=? AND b.slot_id=? AND b.booking_id=?
                        """;
                    try (var stmt=conn.prepareStatement(sql)) {
                        stmt.setString(1,studentId);
                        stmt.setInt(2,slotId);
                        stmt.setInt(3,bookingId);
                        if(stmt.executeUpdate()!=1) throw new SQLException("Credit could not be applied.");
                    }
                }

                // Release the booked time slot
                try (PreparedStatement releaseStmt =
                             conn.prepareStatement(releaseSlotSql)) {

                    releaseStmt.setInt(1, slotId);

                    if (releaseStmt.executeUpdate() != 1) {
                        conn.rollback();

                        return "Cancellation failed: Time slot could not be released.";
                    }
                }

                String type = credit ? "CREDIT_ADDED" : refund ? "REFUND_REQUESTED" : "AMOUNT_DUE_CLEARED";
                String message = credit ? "Booking cancelled. The payment amount was added to your available credit."
                        : refund ? "Booking cancelled. Refund requested. Allow up to three days."
                        : "Booking cancelled. This reservation has no remaining amount due.";
                dao.PaymentNotifications.queue(conn, studentId, bookingId, type, message);
                conn.commit();

                return "Confirmation: Your booking for Slot #"
                        + slotId
                        + " has been cancelled and the slot is now available."
                        + (credit ? " Credit applied." : refund ? " Refund requested. Allow up to 3 days for processing." : "");

            } catch (SQLException e) {

                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {

            System.err.println("Cancellation Error: " + e.getMessage());

            return "Error processing cancellation. Your original booking remains unchanged.";
        }
    }

    // 5. SR-2.3 & SR-2.4: Reschedule Booking
    public String rescheduleBooking(
            String studentId, int oldSlotId, int newSlotId) {

        if (oldSlotId == newSlotId) {
            return "Rescheduling failed: You selected your existing time slot.";
        }

        String bookNewSlotSql = """
                UPDATE time_slots
                SET status = 'BOOKED'
                WHERE slot_id = ?
                AND status = 'AVAILABLE'
                AND TIMESTAMP(slot_date, start_time) > NOW()
                """;

        String updateBookingSql = """
                UPDATE bookings
                SET slot_id = ?
                WHERE student_id = ?
                AND slot_id = ?
                AND status = 'ACTIVE'
                """;

        String releaseOldSlotSql = """
                UPDATE time_slots
                SET status = 'AVAILABLE'
                WHERE slot_id = ?
                AND status = 'BOOKED'
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                // 1. Reserve the new slot if it is available
                try (PreparedStatement bookNewStmt =
                             conn.prepareStatement(bookNewSlotSql)) {

                    bookNewStmt.setInt(1, newSlotId);

                    if (bookNewStmt.executeUpdate() != 1) {
                        conn.rollback();

                        return "Rescheduling failed: The selected time slot is no longer available.";
                    }
                }

                // 2. Update the student's active booking
                try (PreparedStatement updateBookingStmt =
                             conn.prepareStatement(updateBookingSql)) {

                    updateBookingStmt.setInt(1, newSlotId);
                    updateBookingStmt.setString(2, studentId);
                    updateBookingStmt.setInt(3, oldSlotId);

                    if (updateBookingStmt.executeUpdate() != 1) {
                        conn.rollback();

                        return "Rescheduling failed: Original booking could not be updated.";
                    }
                }

                // 3. Release the student's old time slot
                try (PreparedStatement releaseOldStmt =
                             conn.prepareStatement(releaseOldSlotSql)) {

                    releaseOldStmt.setInt(1, oldSlotId);

                    if (releaseOldStmt.executeUpdate() != 1) {
                        conn.rollback();

                        return "Rescheduling failed: Original time slot could not be released.";
                    }
                }

                // 4. Save all changes
                conn.commit();

                return "Confirmation: Your booking has been rescheduled from Slot #"
                        + oldSlotId + " to Slot #" + newSlotId + ".";

            } catch (SQLException e) {

                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {

            System.err.println("Rescheduling Error: " + e.getMessage());

            return "Error processing reschedule. Your original booking remains unchanged.";
        }
    }
}
