package controller;

import dao.FeedbackDao;
import model.User;
import service.FeedbackService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;

@Controller
public class FeedbackWebController {

    private final FeedbackService feedbackService =
            new FeedbackService(new FeedbackDao());

    @GetMapping("/student/feedback")
    public String feedbackPage(HttpSession session) {
        return session.getAttribute("studentId") == null ? "redirect:/login" : "feedback";
    }

    // Submit feedback or a suggestion.
    @PostMapping("/student/feedback")
    public String submitFeedback(
            @RequestParam("category") String category,
            @RequestParam(name = "title", defaultValue = "") String title,
            @RequestParam("message") String message,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String studentId =
                (String) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        User user = new User(studentId, "Student", false);

        try {
            feedbackService.submit(user, category, title, message);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your submission was received!");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());

        } catch (SQLException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to submit your feedback. Please try again.");
        }

        if (redirectAttributes.getFlashAttributes().containsKey("errorMessage")) {
            redirectAttributes.addFlashAttribute("submittedCategory", category);
            redirectAttributes.addFlashAttribute("submittedTitle", title.substring(0, Math.min(title.length(), 150)));
            redirectAttributes.addFlashAttribute("submittedMessage", message.substring(0, Math.min(message.length(), 1000)));
        }
        return "redirect:/student/feedback";
    }
}
