import java.util.Scanner;

public class Login {
    private final Verify verify;
    private final int maxAttempts;

    public Login(Verify verify, int maxAttempts) {
        this.verify = verify;
        this.maxAttempts = maxAttempts;
    }

    public Student showMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n=== Wolf Wash Login Menu ===");
            System.out.println("1. Log In");
            System.out.println("2. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            if ("2".equals(choice)) {
                System.out.println("Goodbye!");
                return null;
            }

            if (!"1".equals(choice)) {
                System.out.println("Invalid option. Please enter 1 or 2.");
                continue;
            }

            int attempts = 0;
            while (attempts < maxAttempts) {
                System.out.print("Enter Student ID: ");
                String studentId = scanner.nextLine();

                System.out.print("Enter Password: ");
                String password = scanner.nextLine();

                Student loggedInStudent = verify.login(studentId, password);
                if (loggedInStudent != null) {
                    System.out.println("\nLogin successful! Welcome, " + loggedInStudent.getFullName() + ".");
                    System.out.println(loggedInStudent);
                    return loggedInStudent;
                }

                attempts++;
                int remaining = maxAttempts - attempts;
                if (remaining > 0) {
                    System.out.println("Invalid Student ID or password. " + remaining + " attempt(s) left.\n");
                }
            }

            System.out.println("Maximum login attempts reached. Returning to menu.");
        }
    }
}
