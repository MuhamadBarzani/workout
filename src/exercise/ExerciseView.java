package exercise;

import java.util.List;
import java.util.Scanner;

public class ExerciseView {
    private final Scanner scanner;
    private ExerciseController controller;

    public ExerciseView() {
        this.scanner = new Scanner(System.in);
    }

    public void setController(ExerciseController controller) {
        this.controller = controller;
    }

    public void displayExerciseMenu() {
        while (true) {
            System.out.println("\n=== Exercise Library ===");
            System.out.println("1. View All Exercises");
            System.out.println("2. Return to Main Menu");
            System.out.print("Choose an option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1:
                        controller.showAllExercises();
                        break;
                    case 2:
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    public void displayExercises(List<Exercise> exercises) {
        System.out.println("\n=== Available Exercises ===");
        for (Exercise exercise : exercises) {
            System.out.println("\nExercise ID: " + exercise.getExerciseID());
            System.out.println("Name: " + exercise.getExerciseName());
            System.out.println("Type: " + exercise.getType());
            System.out.println("Body Part: " + exercise.getBodyTargeted());
            System.out.println("Equipment Needed: " + (exercise.isEquipmentNeeded() ? "Yes" : "No"));
            System.out.println("Description: " + exercise.getDescription());
            System.out.println("------------------------");
        }
    }
}