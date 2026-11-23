import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Cancelation{

    public boolean authenticate(String studentID, String password){
        String sql = "Select 1 FROM students WHERE student_id = ? AND password_hash = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){
            
            stmt.setString(1, studentID);
            stmt.setString(2,password);
            
            try (ResultSet rs = stmt.executeQuery()){
                return rs.next();
            }

        }catch (SQLException e){
            System.err.println("Authentication Error:" + e.getMessage());
            return false;
        }
    }

    public List<Booking> getStudentBookings(String studentID){
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT t.slot_id, t.machine_id, t.dryer_id, t.slot_date, t.start_time, t.end_time " +
             "FROM bookings b " +
             "JOIN time_slots t ON b.slot_id = t.slot_id " +
             "WHERE b.student_id = ? AND b.status = 'ACTIVE' " +
             "ORDER BY t.slot_date, t.start_time";
                    
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){
            
            stmt.setString(1,studentID);
            try (ResultSet rs = stmt.executeQuery()){
                while(rs.next()){
                    bookings.add(new Booking(
                        rs.getInt("slot_id"),
                        rs.getInt("machine_id"),
                        rs.getInt("dryer_id"),
                        rs.getDate("slot_date"),
                        rs.getTime("start_time"),
                        rs.getTime("end_time")

                    ));
                }

            }
        }catch(SQLException e){
            System.err.println("Error receiving bookings" + e.getMessage());
        } 
        return bookings;
    }

    public List<TimeSlot> getAvailableSlots() {
         List<TimeSlot> slots = new ArrayList<>();
         String sql = "SELECT slot_id, machine_id, dryer_id, slot_date, start_time, end_time " +
                      "FROM time_slots " +
                      "WHERE status = 'AVAILABLE' " +
                      "AND (slot_date > CURDATE() OR (slot_date = CURDATE() AND start_time > CURTIME())) " +
                      "ORDER BY slot_date, start_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                slots.add(new TimeSlot(
                    rs.getInt("slot_id"),
                    rs.getInt("machine_id"),
                    rs.getInt("dryer_id"),
                    rs.getDate("slot_date"),
                    rs.getTime("start_time"),
                    rs.getTime("end_time")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving available slots: " + e.getMessage());
        }
        return slots;
    }

    

    public String cancelBooking(String studentId, int slotId) {
        String cancelBookingSql = "UPDATE bookings SET status = 'CANCELLED' WHERE student_id = ? AND slot_id = ? AND status = 'ACTIVE'";
        String freeSlotSql = "UPDATE time_slots SET status = 'AVAILABLE' WHERE slot_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement cancelStmt = conn.prepareStatement(cancelBookingSql);
                 PreparedStatement freeStmt = conn.prepareStatement(freeSlotSql)) {

                cancelStmt.setString(1, studentId);
                cancelStmt.setInt(2, slotId);
                if (cancelStmt.executeUpdate() == 0) {
                    conn.rollback();
                    return "Cancellation failed: Booking not found or already cancelled.";
                }

                freeStmt.setInt(1, slotId);
                freeStmt.executeUpdate();

                conn.commit();
                return "Confirmation: Your booking for Slot #" + slotId + " has been cancelled and the slot is now available.";
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            return "Error processing cancellation. Your original booking remains unchanged.";
        }
    }

    // SR-2.3 & SR-2.4: Reschedule Booking
    public String rescheduleBooking(String studentId, int oldSlotId, int newSlotId) {
        String bookNewSlotSql = "UPDATE time_slots SET status = 'BOOKED' WHERE slot_id = ? AND status = 'AVAILABLE'";
        String freeOldSlotSql = "UPDATE time_slots SET status = 'AVAILABLE' WHERE slot_id = ?";
        String updateBookingSql = "UPDATE bookings SET slot_id = ? WHERE student_id = ? AND slot_id = ? AND status = 'ACTIVE'";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement bookNewStmt = conn.prepareStatement(bookNewSlotSql);
                 PreparedStatement freeOldStmt = conn.prepareStatement(freeOldSlotSql);
                 PreparedStatement updateBookingStmt = conn.prepareStatement(updateBookingSql)) {

                // 1. Reserve the new slot only if it is still available
                bookNewStmt.setInt(1, newSlotId);
                if (bookNewStmt.executeUpdate() == 0) {
                    conn.rollback();
                    return "Rescheduling failed: The selected time slot is no longer available.";
                }

                // 2. Release the old slot
                freeOldStmt.setInt(1, oldSlotId);
                freeOldStmt.executeUpdate();

                // 3. Update the booking to the new slot
                updateBookingStmt.setInt(1, newSlotId);
                updateBookingStmt.setString(2, studentId);
                updateBookingStmt.setInt(3, oldSlotId);
                if (updateBookingStmt.executeUpdate() == 0) {
                    conn.rollback();
                    return "Rescheduling failed: Original booking could not be updated.";
                }

                conn.commit();
                return "Confirmation: Your booking has been rescheduled from Slot #" + oldSlotId + " to Slot #" + newSlotId + ".";
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            return "Error processing reschedule. Your original booking remains unchanged.";
        }
    }
}