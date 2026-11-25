
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.TimeSlot;

public class TimeSlotDAO {

    // 1. Retrieve all available future time slots
    public List<TimeSlot> getAvailableSlots()
            throws SQLException {

        List<TimeSlot> slots = new ArrayList<>();

        String sql = """
                SELECT slot_id, machine_id, dryer_id,
                       slot_date, start_time, end_time, status
                FROM time_slots
                WHERE status = 'AVAILABLE'
                AND TIMESTAMP(slot_date, start_time) > NOW()
                ORDER BY slot_date, start_time
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                TimeSlot slot = new TimeSlot(
                        rs.getInt("slot_id"),
                        rs.getInt("machine_id"),
                        rs.getInt("dryer_id"),
                        rs.getDate("slot_date").toLocalDate(),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime(),
                        rs.getString("status")
                );

                slots.add(slot);
            }
        }

        return slots;
    }

    // 2. Find and lock a selected time slot
    public TimeSlot findSlotForUpdate(
            Connection conn, int slotId)
            throws SQLException {

        String sql = """
                SELECT slot_id, machine_id, dryer_id,
                       slot_date, start_time, end_time, status
                FROM time_slots
                WHERE slot_id = ?
                FOR UPDATE
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, slotId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    return new TimeSlot(
                            rs.getInt("slot_id"),
                            rs.getInt("machine_id"),
                            rs.getInt("dryer_id"),
                            rs.getDate("slot_date").toLocalDate(),
                            rs.getTime("start_time").toLocalTime(),
                            rs.getTime("end_time").toLocalTime(),
                            rs.getString("status")
                    );
                }
            }
        }

        return null;
    }

    // 3. Mark selected time slot as booked
    public boolean markSlotAsBooked(
            Connection conn, int slotId)
            throws SQLException {

        String sql = """
                UPDATE time_slots
                SET status = 'BOOKED'
                WHERE slot_id = ?
                AND status = 'AVAILABLE'
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, slotId);

            return stmt.executeUpdate() == 1;
        }
    }

    // 4. Release a time slot after cancellation
    public boolean releaseSlot(
            Connection conn, int slotId)
            throws SQLException {

        String sql = """
                UPDATE time_slots
                SET status = 'AVAILABLE'
                WHERE slot_id = ?
                AND status = 'BOOKED'
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, slotId);

            return stmt.executeUpdate() == 1;
        }
    }
}


   