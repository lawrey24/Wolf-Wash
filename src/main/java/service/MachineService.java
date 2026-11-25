package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import dao.AdminDAO;
import dao.DatabaseConnection;
import dao.MachineDAO;
import model.Machine;

public class MachineService {

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("AVAILABLE", "OUT_OF_SERVICE");

    private final AdminDAO adminDAO;
    private final MachineDAO machineDAO;

    public MachineService() {
        this.adminDAO = new AdminDAO();
        this.machineDAO = new MachineDAO();
    }

    public boolean authenticate(String adminId, String password)
            throws SQLException {

        if (adminId == null || adminId.isBlank()
                || password == null || password.isBlank()) {
            return false;
        }

        return adminDAO.authenticateAdmin(adminId.trim(), password);
    }

    public List<Machine> getAllMachineStatuses() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return machineDAO.getAllMachinesAndDryers(conn);
        }
    }

    public void updateMachineStatus(
            String machineType,
            int id,
            String newStatus,
            String reason,
            String adminId) throws SQLException {

        if (adminId == null || adminId.isBlank()) {
            throw new IllegalStateException("Administrator ID is required.");
        }

        if (machineType == null
                || (!"WASHER".equalsIgnoreCase(machineType)
                && !"DRYER".equalsIgnoreCase(machineType))) {
            throw new IllegalArgumentException("Type must be WASHER or DRYER.");
        }

        if (id <= 0) {
            throw new IllegalArgumentException("Invalid ID.");
        }

        if (newStatus == null
                || !ALLOWED_STATUSES.contains(newStatus.trim().toUpperCase())) {
            throw new IllegalArgumentException(
                    "Invalid status. Use AVAILABLE or OUT_OF_SERVICE.");
        }

        String normalizedType = machineType.trim().toUpperCase();
        String normalizedStatus = newStatus.trim().toUpperCase();
        String normalizedReason = null;

        if ("OUT_OF_SERVICE".equals(normalizedStatus)) {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException(
                        "Please provide an out-of-service reason.");
            }
            normalizedReason = reason.trim();
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean updated = machineDAO.updateMachineStatus(
                        conn,
                        normalizedType,
                        id,
                        normalizedStatus,
                        normalizedReason,
                        adminId.trim());

                if (!updated) {
                    throw new IllegalArgumentException("Machine/dryer record not found.");
                }

                syncTimeSlotStatuses(conn, normalizedType, id, normalizedStatus);

                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void syncTimeSlotStatuses(
            Connection conn,
            String machineType,
            int id,
            String newMachineStatus) throws SQLException {

        String idColumn = "DRYER".equals(machineType) ? "dryer_id" : "machine_id";

        if ("OUT_OF_SERVICE".equals(newMachineStatus)) {
            String sql = """
                    UPDATE time_slots
                    SET status = 'OUT_OF_SERVICE'
                    WHERE %s = ?
                      AND status = 'AVAILABLE'
                      AND TIMESTAMP(slot_date, start_time) > NOW()
                    """.formatted(idColumn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
        } else if ("AVAILABLE".equals(newMachineStatus)) {
            String sql = """
                    UPDATE time_slots ts
                    LEFT JOIN machines m ON ts.machine_id = m.machine_id
                    LEFT JOIN dryers d ON ts.dryer_id = d.dryer_id
                    SET ts.status = 'AVAILABLE'
                    WHERE ts.%s = ?
                      AND ts.status = 'OUT_OF_SERVICE'
                      AND (m.machine_id IS NULL OR m.status <> 'OUT_OF_SERVICE')
                      AND (d.dryer_id IS NULL OR d.status <> 'OUT_OF_SERVICE')
                    """.formatted(idColumn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
        }
    }
}