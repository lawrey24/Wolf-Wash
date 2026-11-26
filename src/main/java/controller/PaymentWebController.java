package controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.PaymentService;
import service.SimulatedPaymentGateway;
import java.sql.SQLException;
import java.util.UUID;

@Controller
@RequestMapping("/student/payments")
public class PaymentWebController {
    private static final String BACK = "redirect:/student/payments";

    static String formToken(HttpSession session) {
        synchronized (session) {
            String token = (String) session.getAttribute("paymentFormToken");
            if (token == null) {
                token = UUID.randomUUID().toString();
                session.setAttribute("paymentFormToken", token);
            }
            return token;
        }
    }

    static boolean validToken(HttpSession session, String token) {
        return token != null && token.equals(session.getAttribute("paymentFormToken"));
    }

    @GetMapping
    public String payments(HttpSession session, Model model) {
        String student = (String) session.getAttribute("studentId");
        if (student == null) return "redirect:/login";
        var service = new PaymentService(student);
        service.deliverNotifications();
        try {
            model.addAttribute("balances", service.balances());
            model.addAttribute("bookings", service.unpaid());
            model.addAttribute("cards", service.cards());
            model.addAttribute("history", service.history());
            model.addAttribute("notifications", service.notifications());
            model.addAttribute("formToken", formToken(session));
            model.addAttribute("ready", true);
        } catch (SQLException e) {
            model.addAttribute("error", "Unable to load payments. Check the database setup.");
        }
        return "student-payments";
    }

    @PostMapping("/cards/remove")
    public String removeCard(HttpSession session, RedirectAttributes flash,
                             @RequestParam int cardId, @RequestParam String formToken) {
        String student = (String) session.getAttribute("studentId");
        if (student == null) return "redirect:/login";
        if (!validToken(session, formToken)) {
            flash.addFlashAttribute("error", "Reload Payments and try again.");
            return BACK;
        }
        try {
            boolean removed = new PaymentService(student).removeCard(cardId);
            flash.addFlashAttribute(removed ? "success" : "error",
                    removed ? "Saved card removed." : "Card is no longer available.");
        } catch (SQLException e) {
            flash.addFlashAttribute("error", "Unable to remove this card. Please try again.");
        }
        return BACK;
    }

    @PostMapping
    public String submit(HttpSession session, RedirectAttributes flash,
            @RequestParam String formToken, @RequestParam String method,
            @RequestParam(defaultValue="0") int bookingId, @RequestParam(defaultValue="0") int cardId,
            @RequestParam(defaultValue="") String address, @RequestParam(defaultValue="") String number,
            @RequestParam(defaultValue="") String code, @RequestParam(defaultValue="") String expiry,
            @RequestParam(defaultValue="") String phone, @RequestParam(defaultValue="false") boolean save) {
        String student = (String) session.getAttribute("studentId");
        if (student == null) return "redirect:/login";
        if (!validToken(session, formToken)) {
            flash.addFlashAttribute("error", "Reload Payments and try again.");
            return BACK;
        }
        var service = new PaymentService(student);
        try {
            switch (method) {
                case "new", "add" -> {
                    var card = SimulatedPaymentGateway.demoCard(address.trim(), number, code, expiry, phone);
                    if (method.equals("add")) service.addCard(card);
                    else service.pay(bookingId, 0, card, save, false);
                }
                case "saved" -> {
                    if (cardId <= 0) throw new IllegalArgumentException("Select a saved card.");
                    service.pay(bookingId, cardId, null, false, false);
                }
                case "credit" -> service.pay(bookingId, 0, null, false, true);
                default -> throw new IllegalArgumentException("Invalid payment method.");
            }
            flash.addFlashAttribute("success", method.equals("add") ? "Card saved." : "Payment successful.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        } catch (SQLException e) {
            flash.addFlashAttribute("error", "Unable to confirm the transaction. Check payment history before retrying.");
        }
        return BACK;
    }

    @GetMapping("/receipt/{id}")
    public String receipt(@PathVariable int id, HttpSession session, Model model, RedirectAttributes flash) {
        String student = (String) session.getAttribute("studentId");
        if (student == null) return "redirect:/login";
        try {
            for (var payment : new PaymentService(student).history()) {
                if (Integer.toString(id).equals(payment[0])) {
                    model.addAttribute("payment", payment);
                    return "payment-receipt";
                }
            }
            flash.addFlashAttribute("error", "Receipt unavailable.");
        } catch (SQLException e) {
            flash.addFlashAttribute("error", "Unable to load receipt.");
        }
        return BACK;
    }
}
