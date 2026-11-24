package controller;

import service.AuthenticationService;
import java.sql.SQLException;

public class LoginController {

    private final AuthenticationService authenticationService;
    private String authenticatedStudentId;

    public LoginController() {
        authenticationService = new AuthenticationService();
        authenticatedStudentId = null;
    }

    public boolean login(String studentId, String password)
            throws SQLException {

        // Clear any previous login
        authenticatedStudentId = null;

        boolean successful =
                authenticationService.login(studentId, password);

        if (successful) {
            authenticatedStudentId = studentId.trim();
        }

        return successful;
    }

    // Get the ID of the logged-in student
    public String getAuthenticatedStudentId() {
        return authenticatedStudentId;
    }

    // Log out the student
    public void logout() {
        authenticatedStudentId = null;
    }
}
