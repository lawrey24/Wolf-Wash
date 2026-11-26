package controller;

import dao.FeedbackDao;
import jakarta.servlet.http.HttpSession;
import model.User;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import service.AdminAuthenticationService;
import service.FeedbackService;

@Controller
public class AdminWebController {

    private final AdminAuthenticationService authenticationService;
    private final FeedbackService feedbackService;

    public AdminWebController() {
        authenticationService = new AdminAuthenticationService();
        feedbackService = new FeedbackService(new FeedbackDao());
    }

    // ADMIN LOGIN
    @PostMapping("/admin-login")
    public String login(
            @RequestParam String adminId,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            boolean successful =
                    authenticationService.login(adminId, password);

            if (successful) {
                session.setAttribute("adminId", adminId.trim());
                return "redirect:/admin/dashboard";
            }

            redirectAttributes.addFlashAttribute(
                    "error", "Invalid Admin ID or password.");

            return "redirect:/admin-login";

        } catch (Exception e) {
            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error", "Unable to log in. Please try again.");

            return "redirect:/admin-login";
        }
    }

    // ADMIN DASHBOARD
    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model) {

        String adminId = (String) session.getAttribute("adminId");

        if (adminId == null) {
            return "redirect:/admin-login";
        }

        try {
            User admin = new User(adminId, "Administrator", true);

            model.addAttribute(
                    "feedbackList",
                    feedbackService.allFeedback(admin));

            model.addAttribute("adminId", adminId);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute(
                    "error", "Unable to retrieve feedback.");
        }

        return "admin-dashboard";
    }

    // UPDATE FEEDBACK
    @PostMapping("/admin/feedback/update")
    public String updateFeedback(
            @RequestParam int feedbackId,
            @RequestParam String status,
            @RequestParam(required = false) String note,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String adminId = (String) session.getAttribute("adminId");

        if (adminId == null) {
            return "redirect:/admin-login";
        }

        try {
            User admin = new User(adminId, "Administrator", true);

            feedbackService.update(admin, feedbackId, status, note);

            redirectAttributes.addFlashAttribute(
                    "success", "Feedback updated successfully.");

        } catch (Exception e) {
            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error", "Unable to update feedback.");
        }

        return "redirect:/admin/dashboard";
    }

    // ADMIN LOGOUT
    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("adminId");
        return "redirect:/admin-login";
    }
}
