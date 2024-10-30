package exercise;

import auth.DatabaseManager;
import java.sql.*;
import java.util.*;

public class ExerciseModel {
    public List<Exercise> getAllExercises(Connection existingConnection) {
        List<Exercise> exercises = new ArrayList<>();
        String query = "SELECT * FROM exercises";

        boolean useExistingConnection = (existingConnection != null);
        Connection connection = null;

        try {
            // Use existing connection if provided, otherwise create new one
            connection = useExistingConnection ? existingConnection :
                    DatabaseManager.getInstance().getConnection();

            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Exercise exercise = new Exercise(
                            rs.getInt("exerciseID"),
                            rs.getString("exerciseName"),
                            rs.getString("type"),
                            rs.getString("bodyTargeted"),
                            rs.getBoolean("equipmentNeeded"),
                            rs.getString("description")
                    );
                    exercises.add(exercise);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Only close the connection if we created it
            if (!useExistingConnection && connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return exercises;
    }

    // Overloaded method for backward compatibility
    public List<Exercise> getAllExercises() {
        return getAllExercises(null);
    }

    public List<Exercise> generateWorkout(Connection existingConnection, String bodyTarget,
                                          boolean hasEquipment, int exerciseCount) {
        List<Exercise> workout = new ArrayList<>();
        String query = "SELECT * FROM exercises WHERE bodyTargeted = ? AND equipmentNeeded <= ? " +
                "ORDER BY RAND() LIMIT ?";

        boolean useExistingConnection = (existingConnection != null);
        Connection connection = null;

        try {
            // Use existing connection if provided, otherwise create new one
            connection = useExistingConnection ? existingConnection :
                    DatabaseManager.getInstance().getConnection();

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, bodyTarget);
                stmt.setBoolean(2, hasEquipment);
                stmt.setInt(3, exerciseCount);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Exercise exercise = new Exercise(
                                rs.getInt("exerciseID"),
                                rs.getString("exerciseName"),
                                rs.getString("type"),
                                rs.getString("bodyTargeted"),
                                rs.getBoolean("equipmentNeeded"),
                                rs.getString("description")
                        );
                        workout.add(exercise);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Only close the connection if we created it
            if (!useExistingConnection && connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return workout;
    }

    // Overloaded method for backward compatibility
    public List<Exercise> generateWorkout(String bodyTarget, boolean hasEquipment, int exerciseCount) {
        return generateWorkout(null, bodyTarget, hasEquipment, exerciseCount);
    }
}
