package testcases;
import dao.BookingDAO;
import dao.DatabaseConnection;
import model.Booking;

import java.sql.Connection;
import java.sql.SQLException;

public class TestBookingDAO {

    public static void main(String[] args) {

        System.out.println("BOOKING DAO TEST");
        System.out.println("----------------");

        String studentId = "620012345";
        int slotId = 9;

        BookingDAO bookingDAO = new BookingDAO();

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                Booking booking =
                        new Booking(studentId, slotId);

                bookingDAO.createBooking(conn, booking);

                conn.commit();

                System.out.println(
                        "Booking inserted successfully!");

            } catch (SQLException |
                     RuntimeException e) {

                conn.rollback();
                throw e;
            }

        } catch (Exception e) {

            System.out.println(
                    "Booking DAO test failed!");

            System.out.println(
                    "Reason: " + e.getMessage());

            e.printStackTrace();
        }
    }
}
