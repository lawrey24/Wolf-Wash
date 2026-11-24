package service;

import dao.StudentDAO;
import java.sql.SQLException;

public class AuthenticationService {

    private final StudentDAO studentDAO;

    public AuthenticationService() {
        studentDAO = new StudentDAO();
    }

    public boolean login(String studentId, String password)
            throws SQLException {

        if (studentId == null || studentId.isBlank()
                || password == null || password.isBlank()) {
            return false;
        }

        return studentDAO.authenticateStudent(
                studentId.trim(), password);
    }
}


