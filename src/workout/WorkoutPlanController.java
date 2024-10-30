package workout;

import user.User;
import user.UserModel;
import workout.models.WorkoutPlan;
import workout.models.WorkoutPlanModel;

import java.util.List;

public class WorkoutPlanController {
    private final WorkoutPlanModel workoutPlanModel;
    private final UserModel userModel;
    private WorkoutPlanView view;

    public WorkoutPlanController(WorkoutPlanModel workoutPlanModel, UserModel userModel) {
        this.workoutPlanModel = workoutPlanModel;
        this.userModel = userModel;
    }

    public void setView(WorkoutPlanView view) {
        this.view = view;
        view.setController(this);
    }

    public void generateNewWorkoutPlan(int userId, String targetGoal, int daysPerWeek) {
        try {
            // Get user details including any injury information
            User user = userModel.getUserDetails(userId);
            String injuryInfo = userModel.getInjuryInfo(userId);

            // Generate the workout plan
            WorkoutPlan plan = workoutPlanModel.generateWorkoutPlan(
                    user,
                    targetGoal,
                    daysPerWeek,
                    injuryInfo
            );

            if (plan != null) {
                view.displayNewWorkoutPlan(plan);
            } else {
                view.displayError("Unable to generate workout plan");
            }
        } catch (Exception e) {
            view.displayError("Error generating workout plan: " + e.getMessage());
        }
    }

    public void viewUserWorkoutHistory(int userId) {
        try {
            List<WorkoutPlan> workoutHistory = workoutPlanModel.getUserWorkoutHistory(userId);
            view.displayWorkoutHistory(workoutHistory);
        } catch (Exception e) {
            view.displayError("Error retrieving workout history: " + e.getMessage());
        }
    }

    public void viewCurrentPlan(int userId) {
        try {
            WorkoutPlan currentPlan = workoutPlanModel.getCurrentWorkoutPlan(userId);
            if (currentPlan != null) {
                view.displayCurrentWorkoutPlan(currentPlan);
            } else {
                view.displayMessage("No active workout plan found");
            }
        } catch (Exception e) {
            view.displayError("Error retrieving current plan: " + e.getMessage());
        }
    }
}