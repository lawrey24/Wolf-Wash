package view;

import service.PaymentService;
import service.SimulatedPaymentGateway;
import java.sql.SQLException;
import java.util.Scanner;

public class PaymentMenu {
    private final Scanner input;
    private final PaymentService checkout;
    public PaymentMenu(Scanner input, PaymentService checkout) {this.input=input; this.checkout=checkout;}

    private String ask(String prompt) {
        System.out.print(prompt+" ");
        return input.nextLine().trim();
    }
    private boolean yes(String prompt) {
        while(true) {
            String answer=ask(prompt+" (y/n)");
            if(answer.equalsIgnoreCase("y")) return true;
            if(answer.equalsIgnoreCase("n")) return false;
            System.out.println("Enter y or n.");
        }
    }
    public void show() {
        while(true) {
            checkout.deliverNotifications();
            System.out.println("\nPAYMENTS\n1. Pay for current booking\n2. View payment history\n3. Print a receipt of payment\n4. Notification log\n5. Add a saved card\n0. Back");
            try {
                switch(ask("Choice:")) {
                    case "1" -> pay();
                    case "2" -> history(false);
                    case "3" -> history(true);
                    case "4" -> {
                        var events=checkout.notifications();
                        if(events.isEmpty()) System.out.println("No notifications yet.");
                        else System.out.println("Type | Message | Status | Sent at");
                        for(var event:events) System.out.println(String.join(" | ",event));
                    }
                    case "5" -> { checkout.addCard(enterCard()); System.out.println("Card saved for future bookings."); }
                    case "0" -> {return;}
                    default -> System.out.println("Invalid choice.");
                }
            } catch(SQLException|IllegalArgumentException e) {System.out.println("Payment: "+e.getMessage());}
        }
    }
    private SimulatedPaymentGateway.Card enterCard() {
                System.out.println("Use fictional details. Enter any 16-digit card number. Spaces and hyphens are allowed.");
                return SimulatedPaymentGateway.demoCard(ask("Billing address:"),ask("Card number:"),ask("Security code (3 digits):"),
                        ask("Expiry date (YYYY-MM):"),ask("Telephone number:"));
    }

    private void pay() throws SQLException {
        var bookings=checkout.unpaid();
        if(bookings.isEmpty()){System.out.println("You have no unpaid bookings.");return;}
        for(var row:bookings) System.out.printf("Booking #%s | Slot %s | JMD %s%n",row[0],row[1],row[2]);
        String selected=ask("Booking ID to pay (0 to return):");
        if(selected.equals("0")) return;
        var booking=bookings.stream().filter(row -> row[0].equals(selected)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Choose a listed booking ID."));
        System.out.printf("Booking #%s | Slot %s | Amount due: JMD %s%n",booking[0],booking[1],booking[2]);
        String method=ask("1. Card   2. Account credit   0. Back");
        if(method.equals("0")) return;
        if(!method.equals("1") && !method.equals("2")) throw new IllegalArgumentException("Invalid method.");
        int id=0;
        SimulatedPaymentGateway.Card card=null;
        boolean save=false;
        if(method.equals("1")){
            var cards=checkout.cards();
            if(!cards.isEmpty()){
                for(var row:cards) System.out.printf("%s. Card ending in %s (expires %s)%n",row[0],row[1],row[2]);
                id=Integer.parseInt(ask("Select a card ID or enter 0 for a different card:"));
                if(id<0) throw new IllegalArgumentException("Invalid card ID.");
            }
            if(id==0){
                card=enterCard();
                save=yes("Save this card for future bookings?");
            }
        }
        if(!yes("Confirm payment of JMD "+booking[2]+"?")) return;
        checkout.pay(Integer.parseInt(booking[0]),id,card,save,method.equals("2"));
        System.out.println("Payment successful." + (save ? " Card saved for future bookings." : ""));
        System.out.println(checkout.balances());
    }
    private void history(boolean print) throws SQLException {
        var rows=checkout.history();
        if(rows.isEmpty()){System.out.println("You have no payment history.");return;}
        System.out.println("Payment | Booking | Date | JMD | Method | Status");
        for(var row:rows) System.out.println(String.join(" | ",row));
        if(!print) return;
        String id=ask("Enter payment ID to print:");
        for(var row:rows) if(id.equals(row[0])) {
            System.out.printf("""
                    ========================================
                             WOLF WASH LAUNDRY
                              PAYMENT RECEIPT
                    ----------------------------------------
                    Receipt number   #%s
                    Booking number   #%s
                    Date paid        %s
                    Payment method   %s
                    Amount paid      JMD %s
                    Current status   %s
                    ----------------------------------------
                           Simulated transaction
                    ========================================
                    """,row[0],row[1],row[2],row[4],row[3],row[5]);
            return;
        }
        System.out.println("Receipt not found.");
    }
}
