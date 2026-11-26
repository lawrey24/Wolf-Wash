package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import dao.DatabaseConnection;
import dao.MachineDAO;
import model.Machine;

public class MachineService {

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("AVAILABLE", "OUT_OF_SERVICE");

    private final MachineDAO machineDAO = new MachineDAO();

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
            if (normalizedReason.length() > 255) {
                throw new IllegalArgumentException("Use at most 255 characters for the reason.");
            }
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean updated = machineDAO.updateMachineStatus(
                    conn, normalizedType, id, normalizedStatus, normalizedReason, adminId.trim());
            if (!updated) {
                throw new IllegalArgumentException("Machine/dryer record not found.");
            }
        }
    }
}
