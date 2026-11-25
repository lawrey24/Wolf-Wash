package view;

import model.Feedback;
import model.User;
import service.FeedbackService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class FeedbackView {
    private final FeedbackService service;
    private final Scanner input;

    public FeedbackView(FeedbackService service, Scanner input) {
        this.service = service;
        this.input = input;
    }

    public void show(User user) throws SQLException {
        if (user.isAdmin()) adminMenu(user);
        else studentMenu(user);
    }

    private void studentMenu(User user) throws SQLException {
        while (true) {
            System.out.println("\\nWelcome, " + user.getName());
            System.out.println("1. Submit feedback/suggestion");
            System.out.println("2. View my submissions and updates");
            System.out.println("3. Delete a new submission");
            System.out.println("0. Log out");
            System.out.print("> ");
            String choice = input.nextLine().trim();

            if ("1".equals(choice)) submit(user);
            else if ("2".equals(choice)) showList(service.myFeedback(user));
            else if ("3".equals(choice)) delete(user);
            else if ("0".equals(choice)) return;
            else System.out.println("Invalid option.");
        }
    }

    private void submit(User user) throws SQLException {
        System.out.println("Category: 1=Feedback, 2=Suggestion");
        String choice = input.nextLine().trim();
        String category = "1".equals(choice) ? "FEEDBACK" :
                          "2".equals(choice) ? "SUGGESTION" : "";
        System.out.println("Enter your message (minimum " + FeedbackService.MIN_LENGTH + " characters):");
        String message = input.nextLine();
        try {
            service.submit(user, category, message);
            System.out.println("Your submission has been saved. Thank you!");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void delete(User user) throws SQLException {
        System.out.print("Enter the ID of the submission to delete: ");
        try {
            int id = Integer.parseInt(input.nextLine().trim());
            if (service.delete(user, id)) System.out.println("Submission deleted.");
            else System.out.println("It could not be deleted. Only your NEW submissions can be deleted.");
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }
    }

    private void adminMenu(User admin) throws SQLException {
        while (true) {
            System.out.println("\\nAdministrator: " + admin.getName());
            System.out.println("1. View all feedback");
            System.out.println("2. Update a submission / notify its owner");
            System.out.println("0. Log out");
            System.out.print("> ");
            String choice = input.nextLine().trim();
            if ("1".equals(choice)) showList(service.allFeedback(admin));
            else if ("2".equals(choice)) update(admin);
            else if ("0".equals(choice)) return;
            else System.out.println("Invalid option.");
        }
    }

    private void update(User admin) throws SQLException {
        try {
            System.out.print("Feedback ID: ");
            int id = Integer.parseInt(input.nextLine().trim());
            System.out.print("New status (IN_REVIEW, RESOLVED, CLOSED): ");
            String status = input.nextLine().trim().toUpperCase();
            System.out.print("Message to user (shown as an update): ");
            String note = input.nextLine();
            service.update(admin, id, status, note);
            System.out.println("Status and update saved. The user will see it when viewing their submissions.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage() == null ? "Invalid feedback ID." : e.getMessage());
        }
    }

    private void showList(List<Feedback> items) {
        if (items.isEmpty()) {
            System.out.println("No submissions found.");
            return;
        }
        for (Feedback f : items) {
            System.out.println("\\n#" + f.getId() + " | " + f.getCategory() + " | " + f.getStatus()
                    + " | " + f.getCreatedAt());
            if (f.getUserName() != null) System.out.println("From: " + f.getUserName());
            System.out.println(f.getMessage());
            if (f.getAdminNote() != null && !f.getAdminNote().trim().isEmpty())
                System.out.println("Administrator update: " + f.getAdminNote());
        }
    }
}
