package exercise;

import java.util.List;
import java.sql.Connection;

public class ExerciseController {
    private ExerciseModel model;
    private ExerciseView view;

    public ExerciseController(ExerciseModel model, ExerciseView view) {
        this.model = model;
        this.view = view;
    }

    public void showAllExercises() {
        List<Exercise> exercises = model.getAllExercises();
        view.displayExercises(exercises);
    }

    public void generateWorkout(String bodyTarget, boolean hasEquipment, int exerciseCount) {
        List<Exercise> workout = model.generateWorkout(bodyTarget, hasEquipment, exerciseCount);
        view.displayGeneratedWorkout(workout);
    }

    // New method for when we need to use an existing connection
    public List<Exercise> generateWorkoutWithConnection(Connection conn, String bodyTarget,
                                                        boolean hasEquipment, int exerciseCount) {
        return model.generateWorkout(conn, bodyTarget, hasEquipment, exerciseCount);
    }
}