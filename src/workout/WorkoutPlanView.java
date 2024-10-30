package workout;

import workout.models.WorkoutDay;
import workout.models.WorkoutExercise;
import workout.models.WorkoutPlan;

import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.List;

public class WorkoutPlanView {
    private final SimpleDateFormat dateFormat;
    private final Scanner scanner;
    private WorkoutPlanController workoutController;

    public WorkoutPlanView() {
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        this.scanner = new Scanner(System.in);
    }

    public void setController(WorkoutPlanController controller) {
        this.workoutController = controller;
    }

    public void displayWorkoutMenu(int userId) {
        while (true) {
            System.out.println("\n╔════════ Workout Plan Manager ════════╗");
            System.out.println("║ 1. Generate New Workout Plan         ║");
            System.out.println("║ 2. View Current Plan                 ║");
            System.out.println("║ 3. View Workout History             ║");
            System.out.println("║ 4. Return to Main Menu              ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Enter your choice: ");

            int choice = getUserChoice();
            switch (choice) {
                case 1:
                    handleNewWorkoutPlan(userId);
                    break;
                case 2:
                    workoutController.viewCurrentPlan(userId);
                    break;
                case 3:
                    workoutController.viewUserWorkoutHistory(userId);
                    break;
                case 4:
                    return;
                default:
                    displayError("Invalid choice. Please try again.");
            }
        }
    }

    private void handleNewWorkoutPlan(int userId) {
        System.out.println("\n╔═══════ Create New Workout Plan ═══════╗");
        System.out.println("║ Select Your Target Goal:              ║");
        System.out.println("║ 1. Strength                          ║");
        System.out.println("║ 2. Muscle Gain                       ║");
        System.out.println("║ 3. Weight Loss                       ║");
        System.out.println("║ 4. General Fitness                   ║");
        System.out.println("║ 5. Endurance                         ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.print("Enter your choice: ");

        int goalChoice = getUserChoice();
        String targetGoal = convertGoalChoice(goalChoice);

        System.out.println("\n╔═══════ Weekly Schedule ═══════╗");
        System.out.println("║ Choose workout days per week  ║");
        System.out.println("║ (Minimum: 2, Maximum: 6)      ║");
        System.out.println("╚════════════════════════════════╝");
        System.out.print("Enter number of days: ");

        int daysPerWeek = getDaysPerWeek();
        workoutController.generateNewWorkoutPlan(userId, targetGoal, daysPerWeek);
    }

    public void displayNewWorkoutPlan(WorkoutPlan plan) {
        System.out.println("\n╔═══════ New Workout Plan Generated ═══════╗");
        displayPlanDetails(plan);
    }

    public void displayCurrentWorkoutPlan(WorkoutPlan plan) {
        System.out.println("\n╔═══════ Current Active Workout Plan ═══════╗");
        displayPlanDetails(plan);
    }

    public void displayWorkoutHistory(List<WorkoutPlan> history) {
        System.out.println("\n╔═══════ Workout Plan History ═══════╗");
        for (WorkoutPlan plan : history) {
            System.out.println("\n• Plan started on: " + dateFormat.format(plan.getStartDate()));
            System.out.println("• Template: " + plan.getTemplateName());
            System.out.println("• Goal: " + plan.getTargetGoal());
            System.out.println("• Status: " + (plan.isActive() ? "Active" : "Completed"));

            if (plan.getEndDate() != null) {
                System.out.println("• Completed on: " + dateFormat.format(plan.getEndDate()));
            }

            System.out.println("\n▼ Workout Schedule:");
            displayWorkoutDays(plan.getWorkoutDays());
            System.out.println("----------------------------------------");
        }
    }

    private void displayPlanDetails(WorkoutPlan plan) {
        System.out.println("• Target Goal: " + plan.getTargetGoal());
        System.out.println("• Start Date: " + dateFormat.format(plan.getStartDate()));
        System.out.println("\n▼ Workout Schedule:");
        displayWorkoutDays(plan.getWorkoutDays());
    }

    private void displayWorkoutDays(List<WorkoutDay> days) {
        for (WorkoutDay day : days) {
            System.out.println("\n◆ Day " + day.getDayNumber() + " - " + day.getBodyPart());

            for (WorkoutExercise exercise : day.getExercises()) {
                System.out.println("\n  ► Exercise: " + exercise.getExerciseName());
                System.out.println("    • Sets: " + exercise.getSets());
                System.out.println("    • Reps: " + exercise.getRepRangeMin() + "-" + exercise.getRepRangeMax());
                System.out.println("    • Rest: " + exercise.getRestSeconds() + " seconds");

                if (exercise.getNotes() != null && !exercise.getNotes().isEmpty()) {
                    System.out.println("    • Notes: " + exercise.getNotes());
                }
            }
        }
    }

    private String convertGoalChoice(int choice) {
        return switch (choice) {
            case 1 -> "Strength";
            case 2 -> "Muscle Gain";
            case 3 -> "Weight Loss";
            case 4 -> "General Fitness";
            case 5 -> "Endurance";
            default -> {
                displayMessage("Invalid choice. Defaulting to General Fitness.");
                yield "General Fitness";
            }
        };
    }

    private int getDaysPerWeek() {
        try {
            int days = Integer.parseInt(scanner.nextLine());
            if (days >= 2 && days <= 6) {
                return days;
            }
            displayMessage("Invalid number of days. Setting to 3 days per week.");
            return 3;
        } catch (NumberFormatException e) {
            displayMessage("Invalid input. Setting to 3 days per week.");
            return 3;
        }
    }

    private int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void displayError(String message) {
        System.err.println("⚠ Error: " + message);
    }

    public void displayMessage(String message) {
        System.out.println("ℹ " + message);
    }
}