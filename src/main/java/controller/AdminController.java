package controller;


import model.Machine;
import service.MachineService;

import java.sql.SQLException;
import java.util.List;

public class AdminController {

    private final MachineService service;
    private String authenticatedAdminId;

    public AdminController() {
        this.service = new MachineService();
        this.authenticatedAdminId = null;
    }

    public boolean login(String adminId, String password) throws SQLException {
        authenticatedAdminId = null;
        boolean ok = service.authenticate(adminId, password);
        if (ok) {
            authenticatedAdminId = adminId.trim();
        }
        return ok;
    }

    public void logout() {
        authenticatedAdminId = null;
    }

    public String getAuthenticatedAdminId() {
        return authenticatedAdminId;
    }

    private void requireAdmin() {
        if (authenticatedAdminId == null) {
            throw new IllegalStateException("Please log in as an administrator first.");
        }
    }

    public List<Machine> viewAllMachines() throws SQLException {
        requireAdmin();
        return service.getAllMachineStatuses();
    }

    public String updateStatus(
            String machineType, int id, String newStatus, String reason)
            throws SQLException {

        requireAdmin();
        service.updateMachineStatus(
                machineType, id, newStatus, reason, authenticatedAdminId);

        return "Updated " + machineType.toUpperCase() + " #" + id
                + " to " + newStatus.toUpperCase() + ".";
    }
}
