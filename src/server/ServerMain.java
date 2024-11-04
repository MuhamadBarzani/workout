// ServerMain.java
package server;

import user.UserModel;
import workout.models.WorkoutPlanModel;
import exercise.ExerciseModel;
import supplement.SupplementModel;
import order.OrderModel;

public class ServerMain {
    public static void main(String[] args) {
        // Initialize all models
        initializeModels();

        // Start the server
        Server server = new Server();
        System.out.println("Starting server...");
        server.start();
    }

    private static void initializeModels() {
        // Initialize all your models here
        try {
            // These will be used by the server to handle requests
            UserModel userModel = new UserModel();
            WorkoutPlanModel workoutPlanModel = new WorkoutPlanModel();
            ExerciseModel exerciseModel = new ExerciseModel();
            SupplementModel supplementModel = new SupplementModel();
            OrderModel orderModel = new OrderModel();

            System.out.println("All models initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing models: " + e.getMessage());
            System.exit(1);
        }
    }
}