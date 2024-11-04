package workout.models;

import java.io.Serial;
import java.io.Serializable;

public class WorkoutExercise implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int exerciseId;
    private String exerciseName;
    private String bodyTargeted;
    private int sets;
    private int repRangeMin;
    private int repRangeMax;
    private int restSeconds;
    private String notes;

    // Getters and setters

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public void setBodyTargeted(String bodyTargeted) {
        this.bodyTargeted = bodyTargeted;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getRepRangeMin() {
        return repRangeMin;
    }

    public void setRepRangeMin(int repRangeMin) {
        this.repRangeMin = repRangeMin;
    }

    public int getRepRangeMax() {
        return repRangeMax;
    }

    public void setRepRangeMax(int repRangeMax) {
        this.repRangeMax = repRangeMax;
    }

    public int getRestSeconds() {
        return restSeconds;
    }

    public void setRestSeconds(int restSeconds) {
        this.restSeconds = restSeconds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}