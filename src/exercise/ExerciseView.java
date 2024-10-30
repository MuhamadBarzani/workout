
package exercise;

import java.util.List;
import java.util.Scanner;

public class ExerciseView {
    private Scanner scanner;
    private ExerciseController controller;

    public ExerciseView() {
        this.scanner = new Scanner(System.in);
    }

    public void setController(ExerciseController controller) {
        this.controller = controller;
    }

    public void displayExerciseMenu() {
        while (true) {
            System.out.println("\nExercise Menu");
            System.out.println("1. View All Exercises");
            System.out.println("2. Set Additional info");
            System.out.println("3. Return to Main Menu");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    controller.showAllExercises();
                    break;
                case 2:
                    generateWorkoutPrompt();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    public void displayExercises(List<Exercise> exercises) {
        System.out.println("\n=== Available Exercises ===");
        for (Exercise exercise : exercises) {
            System.out.println("\nExercise ID: " + exercise.getExerciseID());
            System.out.println("Name: " + exercise.getExerciseName());
            System.out.println("Type: " + exercise.getType());
            System.out.println("Injury: " + exercise.getBodyTargeted());
            System.out.println("Equipment Needed: " + (exercise.isEquipmentNeeded() ? "Yes" : "No"));
            System.out.println("Description: " + exercise.getDescription());
            System.out.println("------------------------");
        }
    }

    public void displayGeneratedWorkout(List<Exercise> workout) {
        System.out.println("\n=== Your Generated Workout ===");
        for (int i = 0; i < workout.size(); i++) {
            Exercise exercise = workout.get(i);
            System.out.println("\nExercise " + (i + 1) + ":");
            System.out.println("Name: " + exercise.getExerciseName());
            System.out.println("Description: " + exercise.getDescription());
            System.out.println("Equipment Needed: " + (exercise.isEquipmentNeeded() ? "Yes" : "No"));
            System.out.println("------------------------");
        }
    }

    private void generateWorkoutPrompt() {
        System.out.println("\n");
        System.out.println("Available Any Injuries: Arms, Legs, Chest, Back, Core, Full Body");
        System.out.print("Enter Injury: ");
        String bodyTarget = scanner.nextLine();

        System.out.print("Do you have equipment available? (yes/no): ");
        boolean hasEquipment = scanner.nextLine().toLowerCase().startsWith("y");

        System.out.print("How many exercises would you like? ");
        int exerciseCount = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        controller.generateWorkout(bodyTarget, hasEquipment, exerciseCount);
    }
}