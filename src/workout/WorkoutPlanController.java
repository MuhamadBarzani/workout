package workout;

import client.Client;
import client.ClientManager;
import server.ServerResponse;
import workout.models.WorkoutPlan;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.lang.reflect.Type;
public class WorkoutPlanController {
    private final Client client;
    private final Gson gson;
    private WorkoutPlanView view;

    public WorkoutPlanController() {
        ClientManager clientManager = ClientManager.getInstance();
        this.client = clientManager.getClient();
        this.gson = clientManager.getGson();
    }

    public void generateNewWorkoutPlan(int userId, String targetGoal, int daysPerWeek) {
        try {
            WorkoutPlanRequest request = new WorkoutPlanRequest(userId, targetGoal, daysPerWeek);
            ServerResponse response = client.sendRequest("GENERATE_WORKOUT_PLAN", request);

            if (response.isSuccess()) {
                WorkoutPlan plan = gson.fromJson(response.getMessage(), WorkoutPlan.class);
                if (plan != null) {
                    view.displayNewWorkoutPlan(plan);
                } else {
                    view.displayError("Unable to generate workout plan: Plan creation failed");
                }
            } else {
                view.displayError("Server error: " + response.getMessage());
            }
        } catch (Exception e) {
            view.displayError("Error generating workout plan: " + e.getMessage());
        }
    }
    public void setView(WorkoutPlanView view) {
        this.view = view;
        view.setController(this);
    }

    public void viewUserWorkoutHistory(int userId) {
        try {
            ServerResponse response = client.sendRequest("GET_WORKOUT_HISTORY", userId);
            if (response.isSuccess()) {
                Type listType = new TypeToken<List<WorkoutPlan>>(){}.getType();
                List<WorkoutPlan> workoutHistory = gson.fromJson(response.getMessage(), listType);
                view.displayWorkoutHistory(workoutHistory);
            } else {
                view.displayError("Failed to retrieve workout history");
            }
        } catch (Exception e) {
            view.displayError("Error retrieving workout history: " + e.getMessage());
        }
    }

    public void viewCurrentPlan(int userId) {
        try {
            ServerResponse response = client.sendRequest("GET_CURRENT_WORKOUT", userId);
            if (response.isSuccess()) {
                WorkoutPlan currentPlan = gson.fromJson(response.getMessage(), WorkoutPlan.class);
                if (currentPlan != null) {
                    view.displayCurrentWorkoutPlan(currentPlan);
                } else {
                    view.displayMessage("No active workout plan found");
                }
            } else {
                view.displayError("Failed to retrieve current plan");
            }
        } catch (Exception e) {
            view.displayError("Error retrieving current plan: " + e.getMessage());
        }
    }
}

