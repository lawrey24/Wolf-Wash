
package testcases;

import dao.TimeSlotDAO;
import model.TimeSlot;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestTimeSlot {

    @Test
    public void testTimeSlot() {

        try {
            TimeSlotDAO dao = new TimeSlotDAO();

            List<TimeSlot> slots = dao.getAvailableSlots();

            System.out.println("Available Time Slots:");

            for (TimeSlot slot : slots) {
                System.out.println(slot);
            }

            System.out.println("Total slots found: " + slots.size());

        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }
}
