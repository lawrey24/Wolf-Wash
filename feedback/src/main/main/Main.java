package main;

import dao.FeedbackDao;
import model.User;
import service.FeedbackService;
import view.FeedbackView;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
    	//Database Access object
        FeedbackService service = new FeedbackService(new FeedbackDao());
        Scanner input = new Scanner(System.in);
        FeedbackView view = new FeedbackView(service, input);

        while (true) {
            try {
                System.out.println("\\n=== Wolf Wash Feedback Module ===");
                System.out.print("User ID (or 0 to exit): ");
                String id = input.nextLine().trim();
                if ("0".equals(id)) break;

                System.out.print("Password: ");
                String password = input.nextLine();
                User user = service.login(id, password);

                if (user == null) {
                    System.out.println("Invalid ID or password.");
                } else {
                    view.show(user);
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
                System.out.println("Check the database settings and try again.");
            } catch (SecurityException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("Goodbye.");
    }
}
