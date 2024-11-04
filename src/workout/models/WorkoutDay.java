package workout.models;

import java.io.Serializable;
import java.util.List;

public class WorkoutDay implements Serializable {
    private static final long serialVersionUID = 1L;
    private int dayId;
    private int dayNumber;
    private String bodyPart;
    private List<WorkoutExercise> exercises;

    // Getters and setters

    public void setDayId(int dayId) {
        this.dayId = dayId;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public String getBodyPart() {
        return bodyPart;
    }

    public void setBodyPart(String bodyPart) {
        this.bodyPart = bodyPart;
    }

    public List<WorkoutExercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<WorkoutExercise> exercises) {
        this.exercises = exercises;
    }
}