package testcases;
import dao.TimeSlotDAO;
import model.TimeSlot;


import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestTimeSlotDAO {

    public static void main(String[] args) {

        TimeSlotDAO timeSlotDAO =
                new TimeSlotDAO();

        System.out.println("TIME SLOT DAO TEST");
        System.out.println("------------------");

        try {

            // Test retrieving available slots
            List<TimeSlot> slots =
                    timeSlotDAO.getAvailableSlots();

            System.out.println(
                    "Available slots found: "
                    + slots.size());

            for (TimeSlot slot : slots) {

                System.out.println(
                        "Slot ID: " + slot.getSlotId());

                System.out.println(
                        "Date: " + slot.getSlotDate());

                System.out.println(
                        "Start: " + slot.getStartTime());

                System.out.println(
                        "End: " + slot.getEndTime());

                System.out.println(
                        "Status: " + slot.getStatus());

                System.out.println("------------------");
            }

            System.out.println(
                    "TIME SLOT DAO TEST PASSED!");

        } catch (Exception e) {

            System.out.println(
                    "TIME SLOT DAO TEST FAILED!");

            System.out.println(
                    "Reason: " + e.getMessage());

            e.printStackTrace();
        }
    }
}