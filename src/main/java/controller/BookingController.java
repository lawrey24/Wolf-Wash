package controller;

import model.Booking;
import model.TimeSlot;
import service.BookingService;

import java.sql.SQLException;
import java.util.List;

public class BookingController {

    private final BookingService bookingService;

    public BookingController() {
        bookingService = new BookingService();
    }

    // Retrieve available slots
    public List<TimeSlot> showAvailableSlots()
            throws SQLException {

        return bookingService.getAvailableSlots();
    }

    // Process a student's booking request
    public Booking bookSlot(
            String studentId, int slotId)
            throws SQLException {

        return bookingService.createBooking(
                studentId, slotId);
    }
}

