
package testcases;

import controller.LoginController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

public class TestLogin {

    // TEST CASE 01.1A - Valid Student ID and password
    @Test
    @DisplayName("01.1A - Valid Student ID and password")
    public void testValidLogin() throws Exception {

        LoginController controller = new LoginController();

        boolean result = controller.login(
                "620012345", "Test123");

        assertTrue(result,
                "Valid student credentials should allow login.");

        assertEquals("620012345",
                controller.getAuthenticatedStudentId());

        System.out.println(
                "01.1A PASSED - Login successful. Access granted.");
    }


    // TEST CASE 01.1B - Invalid Student ID and password
    @Test
    @DisplayName("01.1B - Invalid Student ID and password")
    public void testInvalidLogin() throws Exception {

        LoginController controller = new LoginController();

        boolean result = controller.login(
                "999999999", "WrongPass987");

        assertFalse(result,
                "Invalid student credentials should be rejected.");

        assertNull(controller.getAuthenticatedStudentId(),
                "Student should not be authenticated.");

        System.out.println(
                "01.1B PASSED - Invalid login rejected.");
    }
}
