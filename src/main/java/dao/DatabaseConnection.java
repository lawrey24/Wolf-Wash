package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            System.getenv().getOrDefault(
                    "DB_URL",
                    "jdbc:mysql://localhost:3306/wolf_wash");

    private static final String USER =
            System.getenv().getOrDefault("DB_USERNAME", "root");

    private static final String PASSWORD =
            System.getenv("DB_PASSWORD");

    public static Connection getConnection()
            throws SQLException {

        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException(
                    "Database password is not configured. Please set DB_PASSWORD.");
        }

        return DriverManager.getConnection(
                URL, USER, PASSWORD);
    }
}