package dao;

import model.TimeSlot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotDAO {

    // Slots are entered in Jamaica local time (UTC-05:00 year-round).
    private static final String FUTURE_SLOT =
            " AND TIMESTAMP(slot_date, start_time) > (UTC_TIMESTAMP() - INTERVAL 5 HOUR) ";

    private static final String SERVICEABLE_EQUIPMENT = """
            AND NOT EXISTS (SELECT 1 FROM machines m
                            WHERE m.machine_id = time_slots.machine_id AND m.status = 'OUT_OF_SERVICE')
            AND NOT EXISTS (SELECT 1 FROM dryers d
                            WHERE d.dryer_id = time_slots.dryer_id AND d.status = 'OUT_OF_SERVICE')
            """;

    // 1. Retrieve all available future time slots
    public List<TimeSlot> getAvailableSlots()
            throws SQLException {

        List<TimeSlot> slots = new ArrayList<>();

        String sql = """
                SELECT slot_id, machine_id, dryer_id,
                       slot_date, start_time, end_time, status
                FROM time_slots
                WHERE status = 'AVAILABLE'
                """ + FUTURE_SLOT + SERVICEABLE_EQUIPMENT + " ORDER BY slot_date, start_time";

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
                """ + FUTURE_SLOT + SERVICEABLE_EQUIPMENT;

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
