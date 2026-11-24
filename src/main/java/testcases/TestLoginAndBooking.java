package testcases;
import controller.LoginController;
import controller.BookingController;
import model.Booking;

public class TestLoginAndBooking {

    public static void main(String[] args) {

        // Create login controller
        LoginController loginController =
                new LoginController();

        // Create booking controller using the SAME login controller
        BookingController bookingController =
                new BookingController(loginController);

        try {

            // Step 1: Student logs in
            boolean loggedIn = loginController.login(
                    "620152234", "Test123");

            if (loggedIn) {

                System.out.println("Login successful!");

                // Step 2: Display available slots
                System.out.println("Available slots:");

                System.out.println(
                        bookingController.showAvailableSlots().size()
                        + " available slots found.");

                // Step 3: Try booking a slot
                int slotId = 5;

                Booking booking =
                        bookingController.bookSlot(slotId);

                // Step 4: Display confirmation
                System.out.println(
                        bookingController.getBookingConfirmation(booking)
                );

            } else {

                System.out.println(
                        "Invalid ID or password. Access denied.");
            }

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());

        } finally {

            // Log out after testing
            loginController.logout();
        }
    }
}