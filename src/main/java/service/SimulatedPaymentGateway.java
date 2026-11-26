package service;

import java.time.YearMonth;
import java.util.UUID;

public class SimulatedPaymentGateway {
    public record Card(String token, String lastFour, String expiry) {}

    public static Card demoCard(String address, String number, String code, String expiry, String phone) {
        String digits = number.replace(" ", "").replace("-", "");
        if (!digits.matches("[0-9]{16}")) {
            throw new IllegalArgumentException("Card number must contain exactly 16 digits.");
        }
        if (address.isBlank()) {
            throw new IllegalArgumentException("Billing address cannot be blank.");
        }

        if (!code.matches("[0-9]{3}")) {
            throw new IllegalArgumentException("Security code must be exactly 3 digits.");
        }

        if (!phone.replaceAll("[ ()+-]", "").matches("[0-9]{7,15}")) {
            throw new IllegalArgumentException("Telephone number must contain 7 to 15 digits.");
        }

        if(!expiry.matches("[0-9]{4}-(0[1-9]|1[0-2])") || YearMonth.parse(expiry).isBefore(YearMonth.now()))
            throw new IllegalArgumentException("Enter an unexpired date in YYYY-MM format.");
        return new Card(UUID.randomUUID().toString(), digits.substring(12), expiry);
    }

    public void authorize(Card card) {
        if (card == null || card.token() == null || card.token().isBlank()
                || YearMonth.parse(card.expiry()).isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("Card declined. Booking remains unpaid.");
        }
    }
}
