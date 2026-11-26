package dao;

import service.SimulatedPaymentGateway.Card;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;


public class PaymentDAO {
    private final String student;
    public PaymentDAO(String student) { this.student = student; }

    private PreparedStatement prepare(Connection c, String sql, Object... values) throws SQLException {
        PreparedStatement p = c.prepareStatement(sql);
        for (int i=0; i<values.length; i++) p.setObject(i+1, values[i]);
        return p;
    }

    private List<String[]> rows(String sql, Object... values) throws SQLException {
        List<String[]> rows = new ArrayList<>();
        try (var c=DatabaseConnection.getConnection(); var p=prepare(c,sql,values); var r=p.executeQuery()) {
            while (r.next()) {
                String[] row=new String[r.getMetaData().getColumnCount()];
                for(int i=0;i<row.length;i++) row[i]=r.getString(i+1);
                rows.add(row);
            }
        }
        return rows;
    }

    public String amountDue() throws SQLException {
        return rows("SELECT COALESCE(SUM(fee),0.00) FROM bookings WHERE student_id=? AND status='ACTIVE' AND payment_status='UNPAID'",student).get(0)[0];
    }
    public String balances() throws SQLException {
        var totals=rows("SELECT balance,(SELECT COALESCE(SUM(fee),0.00) FROM bookings WHERE student_id=? AND status='ACTIVE' AND payment_status='UNPAID') FROM students WHERE student_id=?",student,student).get(0);
        return "Balance to be paid: JMD "+totals[1]+"    Available credit: JMD "+totals[0];
    }
    public List<String[]> unpaid() throws SQLException {
        return rows("SELECT booking_id,slot_id,fee FROM bookings WHERE student_id=? AND status='ACTIVE' AND payment_status='UNPAID'",student);
    }
    public boolean removeCard(int cardId) throws SQLException {
        try (var conn = DatabaseConnection.getConnection();
             var statement = prepare(conn, "DELETE FROM saved_cards WHERE student_id=? AND card_id=?", student, cardId)) {
            return statement.executeUpdate() == 1;
        }
    }

    public List<String[]> cards() throws SQLException {
        return rows("SELECT card_id,last_four,expiry FROM saved_cards WHERE student_id=? ORDER BY card_id",student);
    }
    public List<String[]> history() throws SQLException {
        return rows("SELECT p.payment_id,b.booking_id,p.paid_at,p.amount,p.method,b.payment_status FROM payments p JOIN bookings b USING(booking_id) WHERE b.student_id=? ORDER BY p.payment_id DESC",student);
    }

    public List<String[]> notifications() throws SQLException {
        return rows("SELECT event_type,message,status,COALESCE(CAST(sent_at AS CHAR),'—') FROM terminal_notifications WHERE student_id=? ORDER BY notification_id DESC",student);
    }
    public boolean paid(int slot) throws SQLException {
        return !rows("SELECT booking_id FROM bookings WHERE slot_id=? AND student_id=? AND status='ACTIVE' AND payment_status='PAID'",slot,student).isEmpty();
    }

    public BigDecimal lockUnpaidBooking(Connection conn, int booking) throws SQLException {
        String sql = "SELECT fee FROM bookings WHERE booking_id=? AND student_id=? AND status='ACTIVE' AND payment_status='UNPAID' FOR UPDATE";
        try (var stmt = prepare(conn, sql, booking, student); var result = stmt.executeQuery()) {
            if (!result.next()) throw new IllegalArgumentException("You have no unpaid bookings.");
            return result.getBigDecimal(1);
        }
    }

    public void spendCredit(Connection conn, BigDecimal fee) throws SQLException {
        try (var stmt = prepare(conn, "UPDATE students SET balance=balance-? WHERE student_id=? AND balance>=?", fee, student, fee)) {
            if (stmt.executeUpdate() != 1) throw new IllegalArgumentException("Insufficient account credit.");
        }
    }

    public Card savedCard(Connection conn, int id) throws SQLException {
        try (var stmt = prepare(conn, "SELECT token,last_four,expiry FROM saved_cards WHERE card_id=? AND student_id=?", id, student);
             var row = stmt.executeQuery()) {
            if (!row.next()) throw new IllegalArgumentException("Unknown card.");
            return new Card(row.getString(1), row.getString(2), row.getString(3));
        }
    }

    public void saveCard(Connection conn, Card card) throws SQLException {
        String sql = "INSERT INTO saved_cards(student_id,token,last_four,expiry) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE last_four=VALUES(last_four)";
        try (var stmt = prepare(conn, sql, student, card.token(), card.lastFour(), card.expiry())) {
            stmt.executeUpdate();
        }
    }

    public void recordPayment(Connection conn, int booking, BigDecimal fee, String method) throws SQLException {
        try (var stmt = prepare(conn, "INSERT INTO payments(booking_id,amount,method) VALUES(?,?,?)", booking, fee, method)) {
            stmt.executeUpdate();
        }
        try (var stmt = prepare(conn, "UPDATE bookings SET payment_status='PAID' WHERE booking_id=?", booking)) {
            stmt.executeUpdate();
        }
    }
}
