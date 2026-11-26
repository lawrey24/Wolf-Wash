package dao;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;

public class PaymentNotifications {
    public static void queue(Connection c, String student, int booking, String type, String message) throws SQLException {
        String sql = "INSERT INTO terminal_notifications(student_id,booking_id,event_type,message) VALUES(?,?,?,?)";
        try (var p = c.prepareStatement(sql)) {
            p.setString(1, student);
            p.setInt(2, booking);
            p.setString(3, type);
            p.setString(4, "Booking #" + booking + ": " + message);
            p.executeUpdate();
        }
    }

    private static void completeRefunds(Connection c, String student) throws SQLException {
        String due = "SELECT booking_id FROM bookings WHERE student_id=? AND payment_status='REFUND_PENDING' AND refund_due<=NOW() FOR UPDATE";
        try (var p = c.prepareStatement(due);
             var finish = c.prepareStatement("UPDATE bookings SET payment_status='REFUNDED' WHERE booking_id=?")) {
            p.setString(1, student);
            try (var rows = p.executeQuery()) {
                while (rows.next()) {
                    int booking = rows.getInt(1);
                    finish.setInt(1, booking);
                    finish.executeUpdate();
                    queue(c, student, booking, "REFUND_COMPLETED", "Your simulated refund has been processed.");
                }
            }
        }
    }

    static boolean printNotification(PrintStream terminal, String message) {
        terminal.println("[Notification] " + message);
        return !terminal.checkError(); // Flush and check the terminal write before marking it sent.
    }

    public static void deliver(String student, PrintStream terminal) throws SQLException {
        String next = "SELECT notification_id,message FROM terminal_notifications WHERE student_id=? AND status='PENDING' ORDER BY notification_id LIMIT 1 FOR UPDATE";
        String record = "UPDATE terminal_notifications SET status=?, attempted_at=NOW(6), sent_at=IF(?,NOW(6),NULL) WHERE notification_id=?";
        try (var c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try (var read = c.prepareStatement(next); var write = c.prepareStatement(record)) {
                completeRefunds(c, student);
                c.commit();
                read.setString(1, student);
                while (true) {
                    long id;
                    String message;
                    try (var row = read.executeQuery()) {
                        if (!row.next()) { c.commit(); return; }
                        id = row.getLong(1);
                        message = row.getString(2);
                    }
                    boolean sent = printNotification(terminal, message);
                    write.setString(1, sent ? "SENT" : "FAILED");
                    write.setBoolean(2, sent);
                    write.setLong(3, id);
                    write.executeUpdate();
                    c.commit();
                    if (!sent) return;
                }
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
