
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Verify verify = new Verify();
        Login login = new Login(verify, 3);
        Cancelation cancelationService = new Cancelation();
        StudentMenu studentMenu = new StudentMenu(cancelationService);

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                Student currentStudent = login.showMenu(scanner);
                if (currentStudent == null) {
                    break; // User chose Exit in LoginMenu
                }
                studentMenu.showMenu(scanner, currentStudent);
            }
        }
    }
}