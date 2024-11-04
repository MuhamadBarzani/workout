package user;

import auth.LoginManager;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserView {
    private final UserController userController;
    private final LoginManager loginManager;
    private final Scanner scanner;

    public UserView(UserController userController, LoginManager loginManager) {
        this.userController = userController;
        this.loginManager = loginManager;
        this.scanner = new Scanner(System.in);
    }

    public int getMenuChoice() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1; // Invalid input
        }
    }

    public boolean displaySignupForm() {
        try {
            System.out.println("\n=== User Registration ===");

            System.out.print("Enter username: ");
            String username = scanner.nextLine();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            System.out.print("Enter email: ");
            String email;
            while (true) {
                email = scanner.nextLine();

                // Check if user wants to cancel the signup
                if (email.equalsIgnoreCase("cancel")) {
                    System.out.println("Signup canceled.");
                    return false;  // Exit the method if "cancel" is entered
                }

                // Validate email format
                if (isValidEmail(email)) {
                    break;  // Exit the loop if the email is valid
                } else {
                    System.out.println("Invalid email format. Please enter a valid email address or type 'cancel' to exit.");
                }
            }

            System.out.print("Enter age: ");
            int age = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter height (in cm): ");
            double height = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter weight (in kg): ");
            double weight = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter workout preference: ");
            String workoutPreference = scanner.nextLine();
            System.out.println("\nDo you have any injuries or physical limitations?");
            System.out.println("(Enter body parts separated by commas, or press Enter if none)");
            System.out.println("Example: shoulder, knee");
            String injuryInfo = scanner.nextLine().trim();
            return loginManager.registerUser(username, password, email, age, height, weight, workoutPreference, injuryInfo);

        } catch (NumberFormatException e) {
            System.out.println("Invalid input format. Please enter numeric values for age, height, and weight.");
            return false;
        }
    }

    // Method to validate email format
    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


    public boolean displayLoginForm() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        // Use the updated authentication method
        return loginManager.authenticateUser(email, password);
    }

    public boolean profileMenu() {
        while (true) {
            System.out.println("\nProfile Menu");
            System.out.println("1. View Profile");
            System.out.println("2. Update Profile");
            System.out.println("3. Delete Account");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option: ");

            int choice = getMenuChoice();

            switch (choice) {
                case 1:
                    viewProfile();
                    break;
                case 2:
                    updateProfile();
                    break;
                case 3:
                    if (deleteAccount()) {
                        return true;
                    }
                    break;
                case 4:
                    return false;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void viewProfile() {
        User currentUser = userController.getCurrentUser();
        if (currentUser != null) {
            System.out.println("\n=== User Profile ===");
            System.out.println("Username: " + currentUser.getUsername());
            System.out.println("Email: " + currentUser.getEmail());
            System.out.println("Age: " + currentUser.getAge());
            System.out.println("Height: " + currentUser.getHeight() + " cm");
            System.out.println("Weight: " + currentUser.getWeight() + " kg");
            System.out.println("Workout Preference: " + currentUser.getWorkoutPreference());
        } else {
            System.out.println("Error: Could not retrieve profile data.");
        }
    }

    private void updateProfile() {
        while (true) {
            System.out.println("\nSelect information to update:");
            System.out.println("1. Email");
            System.out.println("2. Age");
            System.out.println("3. Height");
            System.out.println("4. Weight");
            System.out.println("5. Workout Preference");
            System.out.println("6. Injury Information");
            System.out.println("7. Back to Profile Menu");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1:
                        System.out.print("Enter new email: ");
                        String newEmail = scanner.nextLine();
                        if (userController.updateUserProfile("email", newEmail)) {
                            System.out.println("Email updated successfully.");
                        } else {
                            System.out.println("Error updating email.");
                        }
                        break;
                    case 2:
                        System.out.print("Enter new age: ");
                        int newAge = Integer.parseInt(scanner.nextLine());
                        if (userController.updateUserProfile("age", String.valueOf(newAge))) {
                            System.out.println("Age updated successfully.");
                        } else {
                            System.out.println("Error updating age.");
                        }
                        break;
                    case 3:
                        System.out.print("Enter new height (in cm): ");
                        double newHeight = Double.parseDouble(scanner.nextLine());
                        if (userController.updateUserProfile("height", String.valueOf(newHeight))) {
                            System.out.println("Height updated successfully.");
                        } else {
                            System.out.println("Error updating height.");
                        }
                        break;
                    case 4:
                        System.out.print("Enter new weight (in kg): ");
                        double newWeight = Double.parseDouble(scanner.nextLine());
                        if (userController.updateUserProfile("weight", String.valueOf(newWeight))) {
                            System.out.println("Weight updated successfully.");
                        } else {
                            System.out.println("Error updating weight.");
                        }
                        break;
                    case 5:
                        System.out.print("Enter new workout preference: ");
                        String newWorkoutPreference = scanner.nextLine();
                        if (userController.updateUserProfile("workoutPreference", newWorkoutPreference)) {
                            System.out.println("Workout preference updated successfully.");
                        } else {
                            System.out.println("Error updating workout preference.");
                        }
                        break;
                    case 6:
                        System.out.println("\nSelect the body part with injury (or 'none' if no injury):");
                        System.out.println("1. Arms");
                        System.out.println("2. Legs");
                        System.out.println("3. Core");
                        System.out.println("4. Shoulders");
                        System.out.println("5. Back");
                        System.out.println("6. None");
                        System.out.print("Enter your choice: ");
                        int injuryChoice = Integer.parseInt(scanner.nextLine().trim());
                        String injuryPart = "";
                        switch (injuryChoice) {
                            case 1:
                                injuryPart = "Arms";
                                break;
                            case 2:
                                injuryPart = "Legs";
                                break;
                            case 3:
                                injuryPart = "Core";
                                break;
                            case 4:
                                injuryPart = "Shoulders";
                                break;
                            case 5:
                                injuryPart = "Back";
                                break;
                            case 6:
                                injuryPart = "None";
                                break;
                            default:
                                System.out.println("Invalid choice. Skipping injury update.");
                                continue;
                        }
                        if (userController.updateUserProfile("injuryInfo", injuryPart)) {
                            System.out.println("Injury information updated successfully.");
                        } else {
                            System.out.println("Error updating injury information.");
                        }
                        break;
                    case 7:
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private boolean deleteAccount() {
        System.out.print("Are you sure you want to delete your account? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (confirmation.equals("yes")) {
            if (userController.deleteUser()) {
                loginManager.logout();
                System.out.println("Account deleted successfully.");
                return true;
            } else {
                System.out.println("Error deleting account.");
            }
        } else {
            System.out.println("Account deletion cancelled.");
        }
        return false;
    }
}