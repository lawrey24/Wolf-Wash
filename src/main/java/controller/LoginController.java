package controller;

import service.AuthenticationService;
import java.sql.SQLException;

public class LoginController {

    private final AuthenticationService authenticationService;

    public LoginController() {
        authenticationService = new AuthenticationService();
    }

    public boolean login(String studentId, String password)
            throws SQLException {

        return authenticationService.login(studentId, password);
    }
}