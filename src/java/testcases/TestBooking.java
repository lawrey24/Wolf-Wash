
package testcases;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import model.Booking;

public class TestBooking {

    
    @Test
    
    public void testBookingObject() {

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

