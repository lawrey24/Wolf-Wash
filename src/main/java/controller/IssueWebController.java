package controller;

import dao.ReportedIssueDAO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.IssueManagementService;

import java.sql.SQLException;

@Controller
public class IssueWebController {
    private final ReportedIssueDAO issues = new ReportedIssueDAO();
    private final IssueManagementService management = new IssueManagementService();

    @GetMapping("/student/support")
    public String support(HttpSession session, Model model) {
        String student = (String) session.getAttribute("studentId");
        if (student == null) return "redirect:/login";

        model.addAttribute("formToken", PaymentWebController.formToken(session));
        try {
            model.addAttribute("tickets", issues.forStudent(student));
            model.addAttribute("alerts", issues.alertsForStudent(student));
        } catch (SQLException e) {
            model.addAttribute("error", "Unable to load your tickets. Check the reported issues database setup.");
        }
        return "student-support";
    }

    @PostMapping("/student/support")
    public String report(HttpSession session, RedirectAttributes flash,
                         @RequestParam("formToken") String token,
                         @RequestParam("title") String title,
                         @RequestParam("description") String description) {
        String student = (String) session.getAttribute("studentId");
        if (student == null) return "redirect:/login";
        title = title.strip();
        description = description.strip();

        if (!PaymentWebController.validToken(session, token)) {
            flash.addFlashAttribute("error", "Please reload Help & Support and try again.");
        } else if (title.isEmpty() || title.length() > 150 || description.isEmpty() || description.length() > 2000) {
            flash.addFlashAttribute("error", "Enter a title of up to 150 characters and a description of up to 2000 characters.");
        } else {
            try {
                issues.create(student, title, description);
                flash.addFlashAttribute("success", "Issue reported.");
            } catch (SQLException e) {
                flash.addFlashAttribute("error", "Unable to save your report. Please try again.");
            }
        }
        if (flash.getFlashAttributes().containsKey("error")) {
            flash.addFlashAttribute("submittedTitle", title.substring(0, Math.min(title.length(), 150)));
            flash.addFlashAttribute("submittedDescription", description.substring(0, Math.min(description.length(), 2000)));
        }
        return "redirect:/student/support";
    }

    @GetMapping("/admin/issues")
    public String manage(HttpSession session, Model model,
                         @RequestParam(name = "id", required = false) Integer id) {
        if (session.getAttribute("adminId") == null) return "redirect:/admin-login";

        model.addAttribute("formToken", PaymentWebController.formToken(session));
        try {
            model.addAttribute("tickets", management.list("ALL"));
            if (id != null) {
                var ticket = management.get(id);
                if (ticket == null) {
                    model.addAttribute("error", "Issue not found.");
                } else {
                    model.addAttribute("ticket", ticket);
                    model.addAttribute("history", management.history(id));
                }
            }
        } catch (SQLException e) {
            model.addAttribute("error", "Unable to load issues. Check the reported issues database setup.");
        }
        return "admin-issues";
    }

    @PostMapping("/admin/issues")
    public String update(HttpSession session, RedirectAttributes flash,
                         @RequestParam("formToken") String token, @RequestParam("id") int id,
                         @RequestParam("status") String status, @RequestParam("note") String note) {
        String admin = (String) session.getAttribute("adminId");
        if (admin == null) return "redirect:/admin-login";
        if (!PaymentWebController.validToken(session, token)) {
            flash.addFlashAttribute("error", "Please reload the issue and try again.");
        } else {
            try {
                management.changeStatus(id, admin, status, note);
                flash.addFlashAttribute("success", "Issue updated. The student can see the alert in Help & Support.");
            } catch (IllegalArgumentException e) {
                flash.addFlashAttribute("error", e.getMessage());
            } catch (SQLException e) {
                flash.addFlashAttribute("error", "Unable to update this issue. Please try again.");
            }
        }
        return "redirect:/admin/issues?id=" + id;
    }
}
