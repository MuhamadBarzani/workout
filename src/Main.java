// Main.java (Client Side)
import auth.LoginManager;
import exercise.ExerciseController;
import exercise.ExerciseView;
import order.OrderController;
import order.OrderView;
import payment.PaymentController;
import payment.PaymentView;
import supplement.*;
import user.*;
import workout.*;
import client.Client;

public class Main {
    private static WorkoutPlanView workoutPlanView;
    private static Client networkClient;

    public static void main(String[] args) {
        // Initialize network client
        try {
            networkClient = new Client();
            networkClient.connect();
            System.out.println("Connected to server successfully!");
        } catch (Exception e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
            System.exit(1);
        }

        // Initialize controllers with network client instead of models
        UserController userController = new UserController();  // Modified constructor
        LoginManager loginManager = new LoginManager(userController);
        UserView userView = new UserView(userController, loginManager);

        // Initialize workout plan components
        initializeWorkoutPlanComponents();

        while (true) {  // Main application loop
            // Check for auto-login
            if (loginManager.autoLogin()) {
                System.out.println("Welcome back! Redirecting to main menu...");
                if (!displayMainMenu(userView, loginManager, userController)) {
                    continue;  // Return to log in screen if account was not found
                }
            }

            // Show login or signup form
            boolean authenticated = false;
            while (!authenticated) {
                System.out.println("\nWelcome to the Workout App!");
                System.out.println("1. Sign Up");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");

                int choice = userView.getMenuChoice();
                switch (choice) {
                    case 1:
                        authenticated = userView.displaySignupForm();
                        break;
                    case 2:
                        authenticated = userView.displayLoginForm();
                        break;
                    case 3:
                        networkClient.disconnect();  // Clean disconnect from server
                        System.out.println("Goodbye!");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        }
    }

    private static void initializeWorkoutPlanComponents() {
        // Initialize with network-aware controller
        WorkoutPlanController workoutPlanController = new WorkoutPlanController();  // No model needed anymore
        workoutPlanView = new WorkoutPlanView();
        workoutPlanController.setView(workoutPlanView);
    }

    private static boolean displayMainMenu(UserView userView, LoginManager loginManager, UserController userController) {
        // Initialize controllers with network awareness instead of direct model access
        // In displayMainMenu method
        SupplementController supplementController = new SupplementController();

        // Initialize payment components
        PaymentController paymentController = new PaymentController();
        PaymentView paymentView = new PaymentView(paymentController);

        // Create order controller with payment controller and user ID
        OrderController orderController = new OrderController(
                paymentController,
                userController.getCurrentUser().getUserID()
        );

        while (true) {
            System.out.println("\nMain Menu");
            System.out.println("1. Workout Plan");
            System.out.println("2. Exercises");
            System.out.println("3. Supplement Info and Store");
            System.out.println("4. Order Management");
            System.out.println("5. Profile (View, Update, or Delete Account)");
            System.out.println("6. Logout");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");

            int option = userView.getMenuChoice();

            switch (option) {
                case 1:
                    workoutPlanView.displayWorkoutMenu(userController.getCurrentUser().getUserID());
                    break;
                case 2:
                    ExerciseView exerciseView = new ExerciseView();
                    ExerciseController exerciseController = new ExerciseController();
                    exerciseView.setController(exerciseController);
                    exerciseController.setView(exerciseView);
                    exerciseView.displayExerciseMenu();
                    break;
                case 3:
                    SupplementView supplementView = new SupplementView(supplementController, orderController);
                    supplementView.displaySupplementMenu();
                    break;
                case 4:
                    OrderView orderView = new OrderView(orderController, supplementController, paymentView);
                    orderView.displayOrderMenu(userController.getCurrentUser().getUserID());
                    break;
                case 5:
                    if (userView.profileMenu()) {
                        return false;  // Return to log in screen
                    }
                    break;
                case 6:
                    loginManager.logout();
                    System.out.println("Logged out successfully.");
                    return true;
                case 7:
                    networkClient.disconnect();  // Clean disconnect from server
                    System.out.println("Exiting the app. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option. Please choose again.");
            }
        }
    }
}