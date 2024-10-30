
// Exercise.java
package exercise;

public class Exercise {
    private int exerciseID;
    private String exerciseName;
    private String type;
    private String bodyTargeted;
    private boolean equipmentNeeded;
    private String description;
    private String difficulty;
    private int recommendedRest;

    public Exercise(int exerciseID, String exerciseName, String type, String bodyTargeted, boolean equipmentNeeded, String description) {
        this.exerciseID = exerciseID;
        this.exerciseName = exerciseName;
        this.type = type;
        this.bodyTargeted = bodyTargeted;
        this.equipmentNeeded = equipmentNeeded;
        this.description = description;
    }

    public Exercise(int exerciseID, String exerciseName, String type, String bodyTargeted, boolean equipmentNeeded, String description, String difficulty, int recommendedRest) {
        this.exerciseID = exerciseID;
        this.exerciseName = exerciseName;
        this.type = type;
        this.bodyTargeted = bodyTargeted;
        this.equipmentNeeded = equipmentNeeded;
        this.description = description;
        this.difficulty = difficulty;
        this.recommendedRest = recommendedRest;
    }

    // Getters
    public int getExerciseID() { return exerciseID; }
    public String getExerciseName() { return exerciseName; }
    public String getType() { return type; }
    public String getBodyTargeted() { return bodyTargeted; }
    public boolean isEquipmentNeeded() { return equipmentNeeded; }
    public String getDescription() { return description; }
}
