package controller;

import model.Booking;
import model.TimeSlot;
import service.BookingService;

import java.sql.SQLException;
import java.util.List;

public class BookingController {

    private final BookingService bookingService;
    private final LoginController loginController;

    public BookingController(LoginController loginController) {
        this.bookingService = new BookingService();
        this.loginController = loginController;
    }

    // Check that the student is logged in
    private String checkLogin() {

        String studentId =
                loginController.getAuthenticatedStudentId();

        if (studentId == null) {
            throw new IllegalStateException(
                    "Please log in before accessing bookings.");
        }

        return studentId;
    }

    // Retrieve available slots
    public List<TimeSlot> showAvailableSlots()
            throws SQLException {

        checkLogin();

        return bookingService.getAvailableSlots();
    }

    // Book a slot using the authenticated student's ID
    public Booking bookSlot(int slotId)
            throws SQLException {

        String studentId = checkLogin();

        return bookingService.createBooking(
                studentId, slotId);
    }

    // Return booking confirmation message
    public String getBookingConfirmation(Booking booking) {

        if (booking != null) {
            return "Booking successful! Your time slot has been reserved.";
        }

        return "Booking unsuccessful. Please try again.";
    }
}

