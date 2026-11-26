package controller;

import dao.FeedbackDao;
import model.User;
import service.FeedbackService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;

@Controller
public class FeedbackWebController {

    private final FeedbackService feedbackService =
            new FeedbackService(new FeedbackDao());

    // Display the feedback page.
    @GetMapping("/student/feedback")
    public String feedbackPage(HttpSession session, Model model) {

        String studentId =
                (String) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        User user = new User(studentId, "Student", false);

        try {
            model.addAttribute(
                    "feedbackList",
                    feedbackService.myFeedback(user));
        } catch (SQLException e) {
            model.addAttribute(
                    "errorMessage",
                    "Unable to load your feedback.");
        }

        return "feedback";
    }

    // Submit feedback or a suggestion.
    @PostMapping("/student/feedback")
    public String submitFeedback(
            @RequestParam String category,
            @RequestParam String message,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String studentId =
                (String) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        User user = new User(studentId, "Student", false);

        try {
            feedbackService.submit(user, category, message);

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

        return "redirect:/student/feedback";
    }

    // Delete an eligible submission.
    @PostMapping("/student/feedback/delete")
    public String deleteFeedback(
            @RequestParam int feedbackId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String studentId =
                (String) session.getAttribute("studentId");

        if (studentId == null) {
            return "redirect:/login";
        }

        User user = new User(studentId, "Student", false);

        try {
            boolean deleted =
                    feedbackService.delete(user, feedbackId);

            if (deleted) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Feedback deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "This submission cannot be deleted.");
            }

        } catch (SQLException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to delete this submission.");
        }

        return "redirect:/student/feedback";
    }
}
