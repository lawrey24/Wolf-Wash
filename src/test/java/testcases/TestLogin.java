
package testcases;

import controller.LoginController;
import org.junit.jupiter.api.Test;

public class TestLogin {

    @Test
    public void testLogin() {

        LoginController controller = new LoginController();

        try {
            boolean result = controller.login(
                    "620012345", "Test123");

            if (result) {
                System.out.println("Login successful! Access granted.");
            } else {
                System.out.println("Invalid student ID or password.");

                throw new AssertionError(
                        "Login failed. Invalid student ID or password.");
            }

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());

            throw new RuntimeException(e);
        }
    }
}
