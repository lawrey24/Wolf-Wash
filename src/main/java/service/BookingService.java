package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import dao.BookingDAO;
import dao.DatabaseConnection;
import dao.TimeSlotDAO;
import model.Booking;
import model.TimeSlot;

public class BookingService {

    private final BookingDAO bookingDAO;
    private final TimeSlotDAO timeSlotDAO;

    public BookingService() {
        bookingDAO = new BookingDAO();
        timeSlotDAO = new TimeSlotDAO();
    }

    // Display available slots
    public List<TimeSlot> getAvailableSlots()
            throws SQLException {

        return timeSlotDAO.getAvailableSlots();
    }

    // Create a new booking
    public Booking createBooking(
            String studentId, int slotId)
            throws SQLException {

        // Validate student ID
        if (studentId == null ||
                !studentId.matches("\\d{9}")) {

            throw new IllegalArgumentException(
                    "Enter a valid 9-digit student ID.");
        }

        // Validate slot ID
        if (slotId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid time slot selected.");
        }

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                // 1. Validate student
                if (!bookingDAO.studentExists(
                        conn, studentId)) {

                    throw new IllegalArgumentException(
                            "Student does not exist.");
                }

                // 2. Check existing active booking
                if (bookingDAO.hasActiveBooking(
                        conn, studentId)) {

                    throw new IllegalArgumentException(
                            "You already have an active booking. "
                                    + "Please cancel or reschedule it "
                                    + "before making another booking.");
                }

                // 3. Lock selected time slot
                TimeSlot slot =
                        timeSlotDAO.findSlotForUpdate(
                                conn, slotId);

                if (slot == null) {

                    throw new IllegalArgumentException(
                            "Time slot does not exist.");
                }

                // 4. Check slot availability
                if (!"AVAILABLE".equals(
                        slot.getStatus())) {

                    throw new IllegalArgumentException(
                            "Selected time slot is unavailable.");
                }

                // 4b. Check washer/dryer status
                if (isMachineOutOfService(conn, slot)) {

                    throw new IllegalArgumentException(
                            "Selected machine or dryer is out of service.");
                }

                // 5. Reject slots that have already started
                LocalDateTime slotStart =
                        LocalDateTime.of(
                                slot.getSlotDate(),
                                slot.getStartTime());

                if (!slotStart.isAfter(
                        LocalDateTime.now())) {

                    throw new IllegalArgumentException(
                            "Cannot book a slot that has already started.");
                }

                // 6. Create booking object
                Booking booking =
                        new Booking(studentId, slotId);

                // 7. Insert booking record
                bookingDAO.createBooking(
                        conn, booking);

                // 8. Update time slot status
                boolean updated =
                        timeSlotDAO.markSlotAsBooked(
                                conn, slotId);

                if (!updated) {

                    throw new SQLException(
                            "Unable to update time slot.");
                }

                // 9. Commit transaction
                conn.commit();

                return booking;

            } catch (SQLException |
                     RuntimeException e) {

                conn.rollback();
                throw e;
            }
        }
    }

    private boolean isMachineOutOfService(Connection conn, TimeSlot slot)
            throws SQLException {

        if (slot.getMachineId() > 0) {
            String sql = "SELECT status FROM machines WHERE machine_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, slot.getMachineId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()
                            && "OUT_OF_SERVICE".equalsIgnoreCase(rs.getString("status"))) {
                        return true;
                    }
                }
            }
        }

        if (slot.getDryerId() > 0) {
            String sql = "SELECT status FROM dryers WHERE dryer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, slot.getDryerId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()
                            && "OUT_OF_SERVICE".equalsIgnoreCase(rs.getString("status"))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}