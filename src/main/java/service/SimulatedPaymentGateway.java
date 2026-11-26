package service;

import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class SimulatedPaymentGateway {
    public record Card(String token, String lastFour, String expiry) {}

    public static final class InvalidCardException extends IllegalArgumentException {
        private final Map<String, String> fieldErrors;

        private InvalidCardException(Map<String, String> fieldErrors) {
            super(String.join(" ", fieldErrors.values()));
            this.fieldErrors = Map.copyOf(fieldErrors);
        }

        public Map<String, String> fieldErrors() {
            return fieldErrors;
        }
    }

    public static Card demoCard(String address, String number, String code, String expiry, String phone) {
        Map<String, String> errors = new LinkedHashMap<>();
        String digits = number.replace(" ", "").replace("-", "");
        String telephone = phone.strip();
        String telephoneDigits = telephone.replaceAll("[ ()+-]", "");

        if (address.length() > 200 || !address.matches("(?s).*\\p{L}.*")
                || address.split("[\\s,]+").length < 2) {
            errors.put("address", "Enter a billing address with at least two parts, such as 12 Main Street.");
        }
        if (!digits.matches("[0-9]{16}")) {
            errors.put("number", "Enter exactly 16 digits for the card number.");
        }
        if (!code.matches("[0-9]{3}")) {
            errors.put("code", "Enter a 3-digit security code.");
        }
        if (!expiry.matches("[0-9]{4}-(0[1-9]|1[0-2])")) {
            errors.put("expiry", "Enter a valid expiry month and year.");
        } else if (YearMonth.parse(expiry).isBefore(YearMonth.now())) {
            errors.put("expiry", "This card has expired. Enter the current month or a future month.");
        }
        if (!telephone.matches("\\+?[0-9 ()-]+") || !telephoneDigits.matches("[0-9]{7,15}")) {
            errors.put("phone", "Enter a telephone number with 7 to 15 digits.");
        }
        if (!errors.isEmpty()) {
            throw new InvalidCardException(errors);
        }

        return new Card(UUID.randomUUID().toString(), digits.substring(12), expiry);
    }

    public void authorize(Card card) {
        if (card == null || card.token() == null || card.token().isBlank()
                || YearMonth.parse(card.expiry()).isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("Card declined. Booking remains unpaid.");
        }
    }
}
