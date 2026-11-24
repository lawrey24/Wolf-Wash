
package view;

import controller.BookingController;
import model.Booking;
import model.TimeSlot;
import service.Cancelation;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class StudentMenu {

    private final BookingController bookingController;
    private final Cancelation cancelationService;

    public StudentMenu(
            BookingController bookingController,
            Cancelation cancelationService) {

        this.bookingController = bookingController;
        this.cancelationService = cancelationService;
    }

    public void showMenu(Scanner scanner, String studentId) {

        while (true) {

            System.out.println("\n================================");
            System.out.println("       WOLF WASH STUDENT MENU");
            System.out.println("================================");
            System.out.println("Student ID: " + studentId);

            System.out.println("\n1. Book a Time Slot");
            System.out.println("2. View My Bookings");
            System.out.println("3. View Available Time Slots");
            System.out.println("4. Reschedule Booking");
            System.out.println("5. Cancel Booking");
            System.out.println("6. Logout");

            System.out.print("\nEnter your choice: ");

            int choice = promptForInt(scanner);

            switch (choice) {

                case 1:
                    handleBooking(scanner);
                    break;

                case 2:
                    displayBookings(studentId);
                    break;

                case 3:
                    displayAvailableSlots();
                    break;

                case 4:
                    handleReschedule(scanner, studentId);
                    break;

                case 5:
                    handleCancel(scanner, studentId);
                    break;

                case 6:
                    System.out.println("Logging out...");
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Book a time slot
    private void handleBooking(Scanner scanner) {

        try {

            List<TimeSlot> availableSlots =
                    bookingController.showAvailableSlots();

            if (availableSlots == null || availableSlots.isEmpty()) {
                System.out.println("No available time slots.");
                return;
            }

            System.out.println("\n=== AVAILABLE TIME SLOTS ===");

            for (TimeSlot slot : availableSlots) {
                System.out.println(slot);
            }

            System.out.print("\nEnter the slot ID you want to book: ");

            int slotId = promptForInt(scanner);

            if (slotId <= 0) {
                System.out.println("Invalid slot ID.");
                return;
            }

            Booking booking =
                    bookingController.bookSlot(slotId);

            System.out.println(
                    bookingController.getBookingConfirmation(booking));

        } catch (SQLException | IllegalArgumentException e) {

            System.out.println("Booking error: " + e.getMessage());

        } catch (IllegalStateException e) {

            System.out.println(e.getMessage());
        }
    }

    // Display student's bookings
    private void displayBookings(String studentId) {

        List<Booking> bookings =
                cancelationService.getStudentBookings(studentId);

        if (bookings == null || bookings.isEmpty()) {
            System.out.println("You have no active bookings.");
            return;
        }

        System.out.println("\n=== MY BOOKINGS ===");

        for (Booking booking : bookings) {
            System.out.println(booking);
        }
    }

    // Display available slots
    private void displayAvailableSlots() {

        try {

            List<TimeSlot> availableSlots =
                    bookingController.showAvailableSlots();

            if (availableSlots == null || availableSlots.isEmpty()) {
                System.out.println("No available time slots.");
                return;
            }

            System.out.println("\n=== AVAILABLE TIME SLOTS ===");

            for (TimeSlot slot : availableSlots) {
                System.out.println(slot);
            }

        } catch (SQLException | IllegalStateException e) {

            System.out.println("Unable to retrieve slots: " + e.getMessage());
        }
    }

    // Reschedule booking
    private void handleReschedule(
            Scanner scanner, String studentId) {

        List<Booking> bookings =
                cancelationService.getStudentBookings(studentId);

        if (bookings == null || bookings.isEmpty()) {
            System.out.println("You have no bookings to reschedule.");
            return;
        }

        System.out.println("\n=== YOUR CURRENT BOOKINGS ===");

        for (Booking booking : bookings) {
            System.out.println(booking);
        }

        System.out.print("\nEnter the old slot ID: ");

        int oldSlotId = promptForInt(scanner);

        if (oldSlotId <= 0) {
            System.out.println("Invalid slot ID.");
            return;
        }

        try {

            List<TimeSlot> availableSlots =
                    bookingController.showAvailableSlots();

            if (availableSlots == null || availableSlots.isEmpty()) {
                System.out.println("No available slots for rescheduling.");
                return;
            }

            System.out.println("\n=== AVAILABLE TIME SLOTS ===");

            for (TimeSlot slot : availableSlots) {
                System.out.println(slot);
            }
            


            System.out.print("\nEnter the new slot ID: ");

            int newSlotId = promptForInt(scanner);

            if (newSlotId <= 0) {
                System.out.println("Invalid slot ID.");
                return;
            }

            String result = cancelationService.rescheduleBooking(
                    studentId, oldSlotId, newSlotId);

            System.out.println(result);

        } catch (SQLException | IllegalStateException e) {

            System.out.println("Rescheduling error: " + e.getMessage());
        }
    }

    // Cancel booking
    private void handleCancel(
            Scanner scanner, String studentId) {

        List<Booking> bookings =
                cancelationService.getStudentBookings(studentId);

        if (bookings == null || bookings.isEmpty()) {
            System.out.println("You have no bookings to cancel.");
            return;
        }

        System.out.println("\n=== YOUR CURRENT BOOKINGS ===");

        for (Booking booking : bookings) {
            System.out.println(booking);
        }

        System.out.print("\nEnter the slot ID you want to cancel: ");

        int slotId = promptForInt(scanner);

        if (slotId <= 0) {
            System.out.println("Invalid slot ID.");
            return;
        }

        String result = cancelationService.cancelBooking(
                studentId, slotId);

        System.out.println(result);
    }

    // Read integer input
    private int promptForInt(Scanner scanner) {

        try {
            return Integer.parseInt(scanner.nextLine().trim());

        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

