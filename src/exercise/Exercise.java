
// Exercise.java
package exercise;

public class Exercise {
    private final int exerciseID;
    private final String exerciseName;
    private final String type;
    private final String bodyTargeted;
    private final boolean equipmentNeeded;
    private final String description;

    public Exercise(int exerciseID, String exerciseName, String type, String bodyTargeted, boolean equipmentNeeded, String description) {
        this.exerciseID = exerciseID;
        this.exerciseName = exerciseName;
        this.type = type;
        this.bodyTargeted = bodyTargeted;
        this.equipmentNeeded = equipmentNeeded;
        this.description = description;
    }

    // Getters
    public int getExerciseID() { return exerciseID; }
    public String getExerciseName() { return exerciseName; }
    public String getType() { return type; }
    public String getBodyTargeted() { return bodyTargeted; }
    public boolean isEquipmentNeeded() { return equipmentNeeded; }
    public String getDescription() { return description; }
}
