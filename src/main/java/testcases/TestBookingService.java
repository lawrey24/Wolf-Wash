package testcases;
import service.BookingService;
import model.Booking;

public class TestBookingService {

    public static void main(String[] args) {

        BookingService bookingService =
                new BookingService();

        // Replace with your actual student ID
        String studentId = "620012345";

        // Available slot
        int slotId = 4;

        System.out.println("BOOKING SERVICE TEST");
        System.out.println("--------------------");

        try {

            Booking booking =
                    bookingService.createBooking(
                            studentId, slotId);

            System.out.println(
                    "BOOKING SUCCESSFUL!");

            System.out.println(
                    "Student ID: "
                    + booking.getStudentId());

            System.out.println(
                    "Slot ID: "
                    + booking.getSlotId());

        } catch (Exception e) {

            System.out.println(
                    "BOOKING FAILED!");

            System.out.println(
                    "Reason: " + e.getMessage());
        }
    }
}