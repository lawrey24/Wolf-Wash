
package testcases;

import controller.LoginController;
import controller.BookingController;
import model.Booking;
import model.TimeSlot;
import dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

public class TestBookingService {

    private final String studentId = "230100080";
    private final String password = "Test123";

    private final int firstSlot = 3;
    private final int secondSlot = 4;


    // Login student before booking
    private BookingController loginStudent() throws Exception {

        LoginController loginController =
                new LoginController();

        boolean result =
                loginController.login(studentId, password);

        assertTrue(result,
                "Student login should be successful.");

        return new BookingController(loginController);
    }


    // Check time slot status in database
    private String getSlotStatus(int slotId)
            throws SQLException {

        String sql =
                "SELECT status FROM time_slots WHERE slot_id = ?";

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, slotId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        }

        return null;
    }


    // Check whether a student has an active booking
    private boolean hasActiveBooking()
            throws SQLException {

        String sql = """
                SELECT 1 FROM bookings
                WHERE student_id = ?
                AND status = 'ACTIVE'
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }


    // Check whether a student has booked a particular slot
    private boolean hasBookingForSlot(int slotId)
            throws SQLException {

        String sql = """
                SELECT 1 FROM bookings
                WHERE student_id = ?
                AND slot_id = ?
                AND status = 'ACTIVE'
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setInt(2, slotId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }


    // Remove temporary test bookings and restore unused slots
    private void cleanupTestData() throws SQLException {

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                String deleteSql = """
                        DELETE FROM bookings
                        WHERE student_id = ?
                        AND slot_id IN (?, ?)
                        AND status = 'ACTIVE'
                        """;

                try (PreparedStatement stmt =
                             conn.prepareStatement(deleteSql)) {

                    stmt.setString(1, studentId);
                    stmt.setInt(2, firstSlot);
                    stmt.setInt(3, secondSlot);

                    stmt.executeUpdate();
                }


                String updateSql = """
                        UPDATE time_slots ts
                        SET status = 'AVAILABLE'
                        WHERE ts.slot_id IN (?, ?)
                        AND NOT EXISTS (
                            SELECT 1
                            FROM bookings b
                            WHERE b.slot_id = ts.slot_id
                            AND b.status = 'ACTIVE'
                        )
                        """;

                try (PreparedStatement stmt =
                             conn.prepareStatement(updateSql)) {

                    stmt.setInt(1, firstSlot);
                    stmt.setInt(2, secondSlot);

                    stmt.executeUpdate();
                }

                conn.commit();

            } catch (SQLException e) {

                conn.rollback();
                throw e;
            }
        }
    }


    // TEST CASE 02.1A - Successful booking
    @Test
    @DisplayName("02.1A - Successful booking")
    public void testSuccessfulBooking() throws Exception {

        System.out.println("TEST CASE 02.1A");
        System.out.println("-------------------------");

        boolean bookingCreated = false;

        try {

            assertFalse(hasActiveBooking(),
                    "Student should not have an existing active booking.");

            BookingController controller = loginStudent();

            List<TimeSlot> availableSlots =
                    controller.showAvailableSlots();

            assertTrue(
                    availableSlots.stream()
                            .anyMatch(slot ->
                                    slot.getSlotId() == firstSlot),
                    "Slot 3 should be displayed as available.");


            // Make booking
            Booking booking =
                    controller.bookSlot(firstSlot);

            bookingCreated = true;

            assertNotNull(booking,
                    "Booking should be created.");

            assertEquals(firstSlot, booking.getSlotId());


            // Check confirmation message
            String confirmation =
                    controller.getBookingConfirmation(booking);

            assertEquals(
                    "Booking successful! Your time slot has been reserved.",
                    confirmation);


            // Verify slot status
            assertEquals("BOOKED",
                    getSlotStatus(firstSlot));


            // Verify slot is removed from available display
            List<TimeSlot> updatedSlots =
                    controller.showAvailableSlots();

            assertFalse(
                    updatedSlots.stream()
                            .anyMatch(slot ->
                                    slot.getSlotId() == firstSlot),
                    "Booked slot should no longer appear as available.");

            System.out.println("02.1A PASSED");
            System.out.println(confirmation);

        } finally {

            if (bookingCreated) {
                cleanupTestData();
            }
        }
    }


    // TEST CASE 02.2B - Reject second active booking
    @Test
    @DisplayName("02.2B - Reject second active booking")
    public void testUnsuccessfulDuplicateBooking()
            throws Exception {

        System.out.println("TEST CASE 02.2B");
        System.out.println("-------------------------");

        boolean bookingCreated = false;

        try {

            assertFalse(hasActiveBooking(),
                    "Student should not have an existing active booking.");

            BookingController controller = loginStudent();

            List<TimeSlot> availableSlots =
                    controller.showAvailableSlots();

            assertTrue(
                    availableSlots.stream()
                            .anyMatch(slot ->
                                    slot.getSlotId() == firstSlot),
                    "First slot should be available.");

            assertTrue(
                    availableSlots.stream()
                            .anyMatch(slot ->
                                    slot.getSlotId() == secondSlot),
                    "Second slot should be available.");


            // Make first booking
            controller.bookSlot(firstSlot);

            bookingCreated = true;

            assertTrue(hasActiveBooking(),
                    "Student should now have an active booking.");


            // Attempt second booking
            IllegalArgumentException exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> controller.bookSlot(secondSlot));


            assertTrue(
                    exception.getMessage().contains(
                            "already have an active booking"),
                    "Correct error message should be displayed.");


            // Verify second slot remains available
            assertEquals("AVAILABLE",
                    getSlotStatus(secondSlot));

            assertFalse(
                    hasBookingForSlot(secondSlot),
                    "Second booking should not be saved.");


            // Verify second slot remains in available display
            List<TimeSlot> updatedSlots =
                    controller.showAvailableSlots();

            assertTrue(
                    updatedSlots.stream()
                            .anyMatch(slot ->
                                    slot.getSlotId() == secondSlot),
                    "Second slot should remain available.");

            System.out.println("02.2B PASSED");
            System.out.println(exception.getMessage());

        } finally {

            if (bookingCreated) {
                cleanupTestData();
            }
        }
    }
}

