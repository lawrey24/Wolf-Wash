package main;

import java.sql.SQLException;
import java.util.Scanner;

import controller.AdminController;
import controller.BookingController;
import controller.LoginController;
import service.Cancelation;
import view.AdminMenu;
import view.StudentMenu;

public class Main {

    public static void main(String[] args) {

        // Create the application components
        LoginController loginController = new LoginController();

        BookingController bookingController =
                new BookingController(loginController);

        Cancelation cancelationService = new Cancelation();

        StudentMenu studentMenu =
                new StudentMenu(bookingController, cancelationService);

        AdminController adminController = new AdminController();
        AdminMenu adminMenu = new AdminMenu(adminController);

        // Start the application
        try (Scanner scanner = new Scanner(System.in)) {

            boolean running = true;

            System.out.println("================================");
            System.out.println("       WELCOME TO WOLF WASH");
            System.out.println("================================");

            while (running) {

                System.out.println("\n========== MAIN MENU ==========");
                System.out.println("1. Student Login");
                System.out.println("2. Admin Login");
                System.out.println("3. Exit");

                System.out.print("\nChoose an option: ");

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> {
                        System.out.print("Enter Student ID: ");
                        String studentId = scanner.nextLine().trim();

                        System.out.print("Enter Password: ");
                        String password = scanner.nextLine();

                        try {

                            boolean successful =
                                    loginController.login(studentId, password);

                            if (successful) {

                                System.out.println("\nLogin successful!");
                                System.out.println("Welcome to Wolf Wash!");

                                // Open the student menu
                                studentMenu.showMenu(
                                        scanner,
                                        loginController.getAuthenticatedStudentId()
                                );

                                // Log out after leaving the student menu
                                loginController.logout();

                                System.out.println("\nYou have been logged out.");

                            } else {

                                System.out.println(
                                        "\nInvalid Student ID or password."
                                );
                            }

                        } catch (SQLException e) {

                            System.out.println(
                                    "Login error: " + e.getMessage()
                            );
                        }
                    }
                    case "2" -> adminMenu.showMenu(scanner);
                    case "3" -> {
                        System.out.println(
                                "\nThank you for using Wolf Wash!"
                        );

                        running = false;
                    }
                    default -> {
                        System.out.println(
                                "Invalid option. Please choose 1, 2, or 3."
                        );
                    }
                }
            }

            System.out.println("\nWolf Wash application closed.");

        }
    }
}