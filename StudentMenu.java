import java.util.List;
import java.util.Scanner;

public class StudentMenu {
    private final Cancelation cancelationService;

    public StudentMenu(Cancelation cancelationService) {
        this.cancelationService = cancelationService;
    }

    public void showMenu(Scanner scanner, Student student) {
        while (true) {
            System.out.println("\n=== Student Menu (" + student.getFullName() + ") ===");
            System.out.println("1. View Active Bookings");
            System.out.println("2. View Available Time Slots");
            System.out.println("3. Reschedule a Booking");
            System.out.println("4. Cancel a Booking");
            System.out.println("5. Log Out");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    displayBookings(student.getStudentId());
                    break;
                case "2":
                    displayAvailableSlots();
                    break;
                case "3":
                    handleReschedule(scanner, student.getStudentId());
                    break;
                case "4":
                    handleCancel(scanner, student.getStudentId());
                    break;
                case "5":
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option. Please enter 1, 2, 3, 4, or 5.");
            }
        }
    }

    private void displayBookings(String studentId) {
        List<Booking> bookings = cancelationService.getStudentBookings(studentId);
        System.out.println("\n--- Your Active Bookings ---");
        if (bookings.isEmpty()) {
            System.out.println("You have no active bookings.");
        } else {
            for (Booking booking : bookings) {
                System.out.println(booking);
            }
        }
    }

    private void displayAvailableSlots() {
        List<TimeSlot> availableSlots = cancelationService.getAvailableSlots();
        System.out.println("\n--- Available Time Slots ---");
        if (availableSlots.isEmpty()) {
            System.out.println("No available time slots found.");
        } else {
            for (TimeSlot slot : availableSlots) {
                System.out.println(slot);
            }
        }
    }

    private void handleReschedule(Scanner scanner, String studentId) {
        List<Booking> bookings = cancelationService.getStudentBookings(studentId);
        if (bookings.isEmpty()) {
            System.out.println("You have no active bookings to reschedule.");
            return;
        }

        List<TimeSlot> availableSlots = cancelationService.getAvailableSlots();
        if (availableSlots.isEmpty()) {
            System.out.println("There are currently no available slots to reschedule into.");
            return;
        }

        // 1. Show both current bookings AND available times up front
        System.out.println("\n--- Your Active Bookings ---");
        for (Booking booking : bookings) {
            System.out.println(booking);
        }

        System.out.println("\n--- Available Time Slots ---");
        for (TimeSlot slot : availableSlots) {
            System.out.println(slot);
        }

        // 2. Let the user pick which booking to move (or 0 to go back)
        int oldSlotId = promptForInt(scanner, "\nEnter your current Slot ID to reschedule (or 0 to go back): ");
        if (oldSlotId <= 0) return;

        // 3. Let the user pick the new available slot (or 0 to go back)
        int newSlotId = promptForInt(scanner, "Enter the new Available Slot ID (or 0 to go back): ");
        if (newSlotId <= 0) return;

        String result = cancelationService.rescheduleBooking(studentId, oldSlotId, newSlotId);
        System.out.println(result);
    }

    private void handleCancel(Scanner scanner, String studentId) {
        List<Booking> bookings = cancelationService.getStudentBookings(studentId);
        if (bookings.isEmpty()) {
            System.out.println("You have no active bookings to cancel.");
            return;
        }

        System.out.println("\n--- Your Active Bookings ---");
        for (Booking booking : bookings) {
            System.out.println(booking);
        }

        int slotId = promptForInt(scanner, "\nEnter the Slot ID you want to cancel (or 0 to go back): ");
        if (slotId <= 0) return;

        String result = cancelationService.cancelBooking(studentId, slotId);
        System.out.println(result);
    }

    private int promptForInt(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered. Action cancelled.");
            return -1;
        }
    }
}