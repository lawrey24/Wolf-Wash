
package testcases;

import dao.DatabaseConnection;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

public class TestConnection {

    @Test
    public void testConnection() {

        try {
            Connection conn =
                    DatabaseConnection.getConnection();

            System.out.println("Database connected successfully!");

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }
}
