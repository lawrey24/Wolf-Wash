package view;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import controller.AdminController;
import model.Machine;

public class AdminMenu {

    private final AdminController adminController;

    public AdminMenu(AdminController adminController) {
        this.adminController = adminController;
    }

    public void showMenu(Scanner scanner) {
        System.out.print("Enter Admin ID: ");
        String adminId = scanner.nextLine().trim();

        System.out.print("Enter Admin Password: ");
        String password = scanner.nextLine();

        try {
            if (!adminController.login(adminId, password)) {
                System.out.println("Invalid administrator credentials. Access denied.");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Admin login error: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\n=== WOLF WASH ADMIN MONITORING ===");
            System.out.println("Logged in as: " + adminController.getAuthenticatedAdminId());
            System.out.println("1. View All Washers & Dryers Status");
            System.out.println("2. Update Machine / Dryer Status");
            System.out.println("3. Logout");
            System.out.print("Enter choice: ");

            String input = scanner.nextLine().trim();

            try {
                switch (input) {
                    case "1" -> {
                        List<Machine> machines = adminController.viewAllMachines();
                        if (machines.isEmpty()) {
                            System.out.println("No machines or dryers found.");
                        } else {
                            for (Machine m : machines) {
                                System.out.println(m);
                            }
                        }
                    }
                    case "2" -> {
                        System.out.print("Type (WASHER or DRYER): ");
                        String type = scanner.nextLine().trim();

                        System.out.print("ID: ");
                        int id = Integer.parseInt(scanner.nextLine().trim());

                        System.out.print("New Status (AVAILABLE,OUT_OF_SERVICE): ");
                        String status = scanner.nextLine().trim().toUpperCase();

                        String reason = null;
                        if ("OUT_OF_SERVICE".equals(status)) {
                            System.out.print("Out-of-service reason: ");
                            reason = scanner.nextLine().trim();
                        }

                        System.out.println(
                                adminController.updateStatus(type, id, status, reason));
                    }
                    case "3" -> {
                        adminController.logout();
                        System.out.println("Admin logged out.");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid numeric ID.");
            } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
