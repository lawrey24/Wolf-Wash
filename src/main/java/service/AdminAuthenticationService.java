package service;

import dao.AdminDAO;
import java.sql.SQLException;

public class AdminAuthenticationService {

    private final AdminDAO adminDAO;

    public AdminAuthenticationService() {
        adminDAO = new AdminDAO();
    }

    public boolean login(String adminId, String password)
            throws SQLException {

        if (adminId == null || adminId.isBlank()
                || password == null || password.isBlank()) {
            return false;
        }

        return adminDAO.authenticateAdmin(
                adminId.trim(), password);
    }
}
