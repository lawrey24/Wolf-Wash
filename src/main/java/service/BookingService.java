package service;

import dao.BookingDAO;
import dao.DatabaseConnection;
import dao.TimeSlotDAO;
import model.Booking;
import model.TimeSlot;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

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

        if (studentId == null ||
                !studentId.matches("\\d{9}")) {

            throw new IllegalArgumentException(
                    "Enter a valid 9-digit student ID.");
        }

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

                // 2. Lock selected time slot
                TimeSlot slot =
                        timeSlotDAO.findSlotForUpdate(
                                conn, slotId);

                if (slot == null) {
                    throw new IllegalArgumentException(
                            "Time slot does not exist.");
                }

                // 3. Check slot availability
                if (!"AVAILABLE".equals(
                        slot.getStatus())) {

                    throw new IllegalArgumentException(
                            "Selected time slot is unavailable.");
                }

                // 4. Reject past time slots
                LocalDateTime slotEnd =
                        LocalDateTime.of(
                                slot.getSlotDate(),
                                slot.getEndTime());

                if (!slotEnd.isAfter(
                        LocalDateTime.now())) {

                    throw new IllegalArgumentException(
                            "Cannot book a slot that has already started/booked.");
                }

                // 5. Create booking object
                Booking booking =
                        new Booking(studentId, slotId);

                // 6. Insert booking record
                bookingDAO.createBooking(
                        conn, booking);

                // 7. Update time slot status
                boolean updated =
                        timeSlotDAO.markSlotAsBooked(
                                conn, slotId);

                if (!updated) {
                    throw new SQLException(
                            "Unable to update time slot.");
                }

                // 8. Commit transaction
                conn.commit();

                return booking;

            } catch (SQLException |
                     RuntimeException e) {

                conn.rollback();
                throw e;
            }
        }
    }
}
