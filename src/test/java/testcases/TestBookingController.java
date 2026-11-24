
package testcases;

import controller.LoginController;
import controller.BookingController;
import model.Booking;
import model.TimeSlot;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestBookingController {

    @Test
    public void testBookingController() {

        // Create login controller
        LoginController loginController =
                new LoginController();

        // Create booking controller
        BookingController bookingController =
                new BookingController(loginController);

        try {

            // TEST 1: Login
            System.out.println("TEST 1: Student Login");

            boolean loginSuccessful = loginController.login(
                    "630014555",
                    "Test123"
            );

            if (loginSuccessful) {

                System.out.println("Login successful!");

                // TEST 2: Display available slots
                System.out.println("\nTEST 2: Available Slots");

                List<TimeSlot> slots =
                        bookingController.showAvailableSlots();

                System.out.println(
                        "Total available slots: " + slots.size()
                );

                // TEST 3: Make a booking
                System.out.println("\nTEST 3: Booking");

                if (!slots.isEmpty()) {

                    int slotId = slots.get(0).getSlotId();

                    Booking booking =
                            bookingController.bookSlot(slotId);

                    // TEST 4: Confirmation message
                    System.out.println("\nTEST 4: Confirmation");

                    System.out.println(
                            bookingController.getBookingConfirmation(
                                    booking)
                    );

                } else {
                    System.out.println("No available slots to book.");
                }

            } else {

                System.out.println(
                        "Login failed. Booking access denied."
                );
            }

        } catch (Exception e) {

            System.out.println("Test failed: " + e.getMessage());

            throw new RuntimeException(e);

        } finally {

            loginController.logout();
        }
    }
}
