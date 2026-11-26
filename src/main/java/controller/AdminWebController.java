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
import service.MachineService;
import service.IssueManagementService;

import java.sql.SQLException;

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

        model.addAttribute("adminId", adminId);
        try {
            var equipment = new MachineService().getAllMachineStatuses();
            int total = equipment.size();
            long operational = equipment.stream()
                    .filter(item -> "AVAILABLE".equals(item.getStatus())).count();
            int percent = total == 0 ? 0 : (int) (operational * 100 / total);
            model.addAttribute("equipmentTotal", total);
            model.addAttribute("operational", operational);
            model.addAttribute("equipmentPercent", percent);
            model.addAttribute("equipmentTone", percent == 100 ? "green" : percent >= 75 ? "yellow" : "red");
        } catch (SQLException e) {
            model.addAttribute("equipmentUnavailable", true);
        }

        try {
            var submissions = feedbackService.allFeedback(new User(adminId, "Administrator", true));
            model.addAttribute("feedbackTotal", submissions.size());
            model.addAttribute("feedbackNew", submissions.stream()
                    .filter(item -> "NEW".equals(item.getStatus())).count());
            model.addAttribute("feedbackInReview", submissions.stream()
                    .filter(item -> "IN_REVIEW".equals(item.getStatus())).count());
            model.addAttribute("feedbackResolved", submissions.stream()
                    .filter(item -> "RESOLVED".equals(item.getStatus())).count());
        } catch (SQLException e) {
            model.addAttribute("feedbackUnavailable", true);
        }

        try {
            var tickets = new IssueManagementService().list("ALL");
            model.addAttribute("issueTotal", tickets.size());
            model.addAttribute("issuePending", tickets.stream()
                    .filter(ticket -> "PENDING".equals(ticket.getStatus())).count());
            model.addAttribute("issueInProgress", tickets.stream()
                    .filter(ticket -> "IN_PROGRESS".equals(ticket.getStatus())).count());
            model.addAttribute("issueResolved", tickets.stream()
                    .filter(ticket -> "RESOLVED".equals(ticket.getStatus())).count());
        } catch (SQLException e) {
            model.addAttribute("issuesUnavailable", true);
        }

        return "admin-dashboard";
    }

    @GetMapping("/admin/feedback")
    public String feedback(HttpSession session, Model model,
                           @RequestParam(name = "id", required = false) Integer id) {
        String adminId = (String) session.getAttribute("adminId");
        if (adminId == null) return "redirect:/admin-login";
        model.addAttribute("adminId", adminId);

        try {
            User admin = new User(adminId, "Administrator", true);

            var submissions = feedbackService.allFeedback(admin);
            model.addAttribute("feedbackList", submissions);
            if (id != null) {
                var selected = submissions.stream().filter(item -> item.getId() == id).findFirst();
                if (selected.isPresent()) {
                    model.addAttribute("feedback", selected.get());
                } else {
                    model.addAttribute("error", "Feedback submission not found.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute(
                    "error", "Unable to retrieve feedback.");
        }

        return "admin-feedback";
    }

    // UPDATE FEEDBACK
    @PostMapping("/admin/feedback/update")
    public String updateFeedback(
            @RequestParam("feedbackId") int feedbackId,
            @RequestParam("status") String status,
            @RequestParam(name = "note", required = false) String note,
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

        return "redirect:/admin/feedback?id=" + feedbackId;
    }

    // ADMIN LOGOUT
    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("adminId");
        return "redirect:/admin-login";
    }
}
