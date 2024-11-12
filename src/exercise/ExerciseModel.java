package exercise;
import server.FileManager;

import java.util.*;
import java.util.stream.Collectors;

public class ExerciseModel {
    private static final String EXERCISES_FILE = "exercises.json";
    private static final String EXERCISE_COUNTER = "exercise_counter.txt";
    private final FileManager fileManager;

    public ExerciseModel() {
        this.fileManager = FileManager.getInstance();
    }

    public List<Exercise> getAllExercises() {
        List<Exercise> exercises = fileManager.loadList(EXERCISES_FILE, Exercise[].class);
        return exercises != null ? exercises : new ArrayList<>();
    }

    public List<Exercise> generateWorkout(String bodyTarget, boolean hasEquipment, int exerciseCount) {
        List<Exercise> exercises = getAllExercises();

        // Filter exercises based on criteria
        List<Exercise> filtered = exercises.stream()
                .filter(e -> e.getBodyTargeted().equalsIgnoreCase(bodyTarget))
                .filter(e -> !e.isEquipmentNeeded() || hasEquipment)
                .collect(Collectors.toList());

        // Randomly select exercises
        if (filtered.size() <= exerciseCount) {
            return filtered;
        }

        // Shuffle and select required number of exercises
        Collections.shuffle(filtered);
        return filtered.subList(0, exerciseCount);
    }

    public boolean addExercise(Exercise exercise) {
        try {
            List<Exercise> exercises = getAllExercises();
            int id = fileManager.getNextId(EXERCISE_COUNTER);

            Exercise newExercise = new Exercise(
                    id,
                    exercise.getExerciseName(),
                    exercise.getType(),
                    exercise.getBodyTargeted(),
                    exercise.isEquipmentNeeded(),
                    exercise.getDescription()
            );

            exercises.add(newExercise);
            fileManager.saveData(EXERCISES_FILE, exercises);
            return true;
        } catch (Exception e) {
            System.out.println("Error adding exercise: " + e.getMessage());
            return false;
        }
    }

    public Exercise getExerciseById(int exerciseId) {
        return getAllExercises().stream()
                .filter(e -> e.getExerciseID() == exerciseId)
                .findFirst()
                .orElse(null);
    }
}