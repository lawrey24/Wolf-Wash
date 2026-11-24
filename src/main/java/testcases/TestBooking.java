
package testcases;
import model.Booking;

public class TestBooking {

    public static void main(String[] args) {

        String studentId = "123456789";
        int slotId = 1;

        Booking booking =
                new Booking(studentId, slotId);

        System.out.println("BOOKING OBJECT TEST");
        System.out.println("-------------------");

        System.out.println(
                "Student ID: " + booking.getStudentId());

        System.out.println(
                "Slot ID: " + booking.getSlotId());

        System.out.println(
                "Booking object created successfully!");
    }
}

