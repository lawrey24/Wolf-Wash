
package controller;

import dao.BookingDAO;
import dao.DatabaseConnection;
import jakarta.servlet.http.HttpSession;
import model.Booking;
import model.TimeSlot;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import service.AuthenticationService;
import service.BookingService;
import service.Cancelation;

import java.sql.Connection;
import java.util.List;

@Controller
public class StudentWebController {

    private final AuthenticationService authenticationService;
    private final BookingDAO bookingDAO;
    private final BookingService bookingService;
    private final Cancelation cancelationService;

    public StudentWebController() {
        authenticationService = new AuthenticationService();
        bookingDAO = new BookingDAO();
        bookingService = new BookingService();
        cancelationService = new Cancelation();
    }

    // STUDENT LOGIN
    @PostMapping("/login")
    public String login(
            @RequestParam String studentId,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            boolean successful =
                    authenticationService.login(studentId, password);

            if (successful) {
                session.setAttribute("studentId", studentId.trim());
                return "redirect:/student/dashboard";
            }

            redirectAttributes.addFlashAttribute(
                    "error", "Invalid Student ID or password.");

            return "redirect:/login";

        } catch (Exception e) {
            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error", "Unable to log in. Please try again.");

            return "redirect:/login";
        }
    }

    // STUDENT DASHBOARD
    @GetMapping("/student/dashboard")
    public String dashboard(HttpSession session, Model model) {

        String studentId =
                (String) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            List<Booking> bookings =
                    bookingDAO.getStudentBookings(conn, studentId);

            model.addAttribute("recentBookings", bookings);
            model.addAttribute("activeBookings", bookings.size());

            if (!bookings.isEmpty()) {
                Booking nextBooking = bookings.get(0);

                model.addAttribute(
                        "upcomingAppointment",
                        nextBooking.getSlotDate() + " at "
                                + nextBooking.getStartTime());
            } else {
                model.addAttribute("upcomingAppointment", "None");
            }

            model.addAttribute("studentName", "Student");
            model.addAttribute("amountDue", "0.00");

        } catch (Exception e) {
            e.printStackTrace();

            model.addAttribute(
                    "dashboardError",
                    "Unable to retrieve your bookings.");

            model.addAttribute("recentBookings", List.of());
            model.addAttribute("activeBookings", 0);
            model.addAttribute("upcomingAppointment", "Unavailable");
            model.addAttribute("studentName", "Student");
            model.addAttribute("amountDue", "0.00");
        }

        return "student-dashboard";
    }

    // DISPLAY AVAILABLE TIME SLOTS
    @GetMapping("/student/book")
    public String showAvailableSlots(
            HttpSession session,
            Model model) {

        String studentId =
                (String) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        try {
            List<TimeSlot> availableSlots =
                    bookingService.getAvailableSlots();

            model.addAttribute("availableSlots", availableSlots);

        } catch (Exception e) {
            e.printStackTrace();

            model.addAttribute(
                    "bookingError",
                    "Unable to retrieve available time slots.");

            model.addAttribute("availableSlots", List.of());
        }

        return "book-time-slot";
    }

    // BOOK SELECTED TIME SLOT
    @PostMapping("/student/book")
    public String bookTimeSlot(
            @RequestParam int slotId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String studentId =
                (String) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        try {
            bookingService.createBooking(studentId, slotId);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Your time slot has been booked successfully!");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "bookingError", e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "bookingError",
                    "Unable to complete your booking. Please try again.");
        }

        return "redirect:/student/book";
    }

    // DISPLAY STUDENT BOOKINGS
    @GetMapping("/student/bookings")
    public String myBookings(
            HttpSession session,
            Model model) {

        String studentId =
                (String) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        List<Booking> bookings =
                cancelationService.getStudentBookings(studentId);

        model.addAttribute("bookings", bookings);

        return "my-bookings";
    }

    // CANCEL BOOKING
    @PostMapping("/student/cancel-booking")
    public String cancelBooking(
            @RequestParam int slotId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String studentId =
                (String) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        String result =
                cancelationService.cancelBooking(studentId, slotId);

        if (result.startsWith("Confirmation:")) {
            redirectAttributes.addFlashAttribute("success", result);
        } else {
            redirectAttributes.addFlashAttribute("bookingError", result);
        }

        return "redirect:/student/bookings";
    }

    // DISPLAY RESCHEDULE OPTIONS
    @GetMapping("/student/reschedule")
    public String showRescheduleOptions(
            @RequestParam int slotId,
            HttpSession session,
            Model model) {

        String studentId =
                (String) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        List<Booking> bookings =
                cancelationService.getStudentBookings(studentId);

        boolean ownsBooking = bookings.stream()
                .anyMatch(b -> b.getSlotID() == slotId);

        if (!ownsBooking) {
            return "redirect:/student/bookings";
        }

        List<TimeSlot> availableSlots =
                cancelationService.getAvailableSlots();

        model.addAttribute("oldSlotId", slotId);
        model.addAttribute("availableSlots", availableSlots);

        return "reschedule-booking";
    }

    // SUBMIT RESCHEDULE REQUEST
    @PostMapping("/student/reschedule")
    public String rescheduleBooking(
            @RequestParam int oldSlotId,
            @RequestParam int newSlotId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String studentId =
                (String) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        String result =
                cancelationService.rescheduleBooking(
                        studentId, oldSlotId, newSlotId);

        if (result.startsWith("Confirmation:")) {
            redirectAttributes.addFlashAttribute("success", result);
        } else {
            redirectAttributes.addFlashAttribute("bookingError", result);
        }

        return "redirect:/student/bookings";
    }

    // STUDENT LOGOUT
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
