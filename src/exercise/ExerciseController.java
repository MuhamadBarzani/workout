package exercise;

import client.Client;
import server.ServerResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.List;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class ExerciseController {
    private ExerciseView view;
    private final Client client;
    private final Gson gson;

    public ExerciseController() {
        this.client = new Client();
        this.gson = new Gson();
        try {
            this.client.connect();
        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }

    public void setView(ExerciseView view) {
        this.view = view;
    }

    public void showAllExercises() {
        try {
            ServerResponse response = client.sendRequest("GET_ALL_EXERCISES", "");
            if (response.isSuccess()) {
                Type listType = new TypeToken<List<Exercise>>(){}.getType();
                List<Exercise> exercises = gson.fromJson(response.getMessage(), listType);
                view.displayExercises(exercises);
            } else {
                System.out.println("Error fetching exercises: " + response.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}