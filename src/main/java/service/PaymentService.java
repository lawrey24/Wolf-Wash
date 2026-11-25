package service;

import dao.DatabaseConnection;
import dao.PaymentDAO;
import service.SimulatedPaymentGateway.Card;
import java.sql.SQLException;
import java.util.List;

public class PaymentService {
    private final String student;
    private final PaymentDAO payments;
    private final SimulatedPaymentGateway gateway = new SimulatedPaymentGateway();

    public PaymentService(String student) {
        this.student = student;
        payments = new PaymentDAO(student);
    }

    public String amountDue() throws SQLException { return payments.amountDue(); }
    public String balances() throws SQLException { return payments.balances(); }
    public List<String[]> unpaid() throws SQLException { return payments.unpaid(); }
    public boolean removeCard(int cardId) throws SQLException { return payments.removeCard(cardId); }

    public List<String[]> cards() throws SQLException { return payments.cards(); }
    public List<String[]> history() throws SQLException { return payments.history(); }
    public boolean paid(int slot) throws SQLException { return payments.paid(slot); }

    public void deliverNotifications() {
        try {
            dao.PaymentNotifications.deliver(student, System.out);
        } catch (SQLException e) {
            System.err.println("Could not update the notification log: " + e.getMessage());
        }
    }
    public List<String[]> notifications() throws SQLException { return payments.notifications(); }
    public void addCard(Card card) throws SQLException {
        gateway.authorize(card);
        try(var c=DatabaseConnection.getConnection()) { payments.saveCard(c,card); }
    }

    public void pay(int booking, int savedId, Card card, boolean save, boolean credit) throws SQLException {
        try (var conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                var fee = payments.lockUnpaidBooking(conn, booking);
                String method = "Account credit";
                if (credit) {
                    payments.spendCredit(conn, fee);
                } else {
                    if (savedId > 0) card = payments.savedCard(conn, savedId);
                    gateway.authorize(card);
                    if (save) payments.saveCard(conn, card);
                    method = "Card **** " + card.lastFour();
                }
                payments.recordPayment(conn, booking, fee, method);
                dao.PaymentNotifications.queue(conn,student,booking,"PAYMENT","Payment received: JMD " + fee + " via " + method + ".");
                if (credit) dao.PaymentNotifications.queue(conn,student,booking,"CREDIT_USED","Account credit used: JMD " + fee);
                conn.commit();
            } catch (SQLException | RuntimeException error) {
                conn.rollback();
                throw error;
            }
        }
    }
}
