package workout.models;

import user.User;
import server.DatabaseManager;
import exercise.ExerciseModel;
import exercise.Exercise;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class WorkoutPlanModel {
    private final ExerciseModel exerciseModel;
    private static final List<String> BODY_PARTS = Arrays.asList(
            "Chest", "Back", "Legs", "Shoulders", "Arms", "Core"
    );

    public WorkoutPlanModel() {
        this.exerciseModel = new ExerciseModel();
    }

    public WorkoutPlan generateWorkoutPlan(User user, String targetGoal, int daysPerWeek, String injuryInfo) {
        Connection conn = null;
        WorkoutPlan plan = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            plan = new WorkoutPlan();
            plan.setUserId(user.getUserID());
            plan.setStartDate(new Date());
            plan.setActive(true);
            plan.setTargetGoal(targetGoal);

            // Deactivate current plans
            deactivateCurrentPlans(conn, user.getUserID());

            // Create template
            int templateId = createWorkoutTemplate(conn, targetGoal);

            // Create user plan association
            createUserPlanAssociation(conn, user.getUserID(), templateId, plan.getStartDate());

            // Generate and save workout days
            List<WorkoutDay> workoutDays = generateWorkoutDays(conn, templateId, daysPerWeek, injuryInfo);
            plan.setWorkoutDays(workoutDays);

            conn.commit();
            return plan;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<WorkoutPlan> getUserWorkoutHistory(int userId) throws SQLException {
        List<WorkoutPlan> history = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String query = """
                SELECT uwp.*, wt.name, wt.targetGoal
                FROM user_workout_plans uwp
                JOIN workout_templates wt ON uwp.templateID = wt.templateID
                WHERE uwp.userID = ?
                ORDER BY uwp.startDate DESC
            """;

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    WorkoutPlan plan = createPlanFromResultSet(rs);
                    plan.setWorkoutDays(getWorkoutDaysForPlan(conn, rs.getInt("templateID")));
                    history.add(plan);
                }
            }
            return history;
        }
    }

    public WorkoutPlan getCurrentWorkoutPlan(int userId) throws SQLException {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String query = """
                SELECT uwp.*, wt.name, wt.targetGoal
                FROM user_workout_plans uwp
                JOIN workout_templates wt ON uwp.templateID = wt.templateID
                WHERE uwp.userID = ? AND uwp.isActive = 1
            """;

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    WorkoutPlan plan = createPlanFromResultSet(rs);
                    plan.setWorkoutDays(getWorkoutDaysForPlan(conn, rs.getInt("templateID")));
                    return plan;
                }
            }
            return null;
        }
    }

    private List<WorkoutDay> getWorkoutDaysForPlan(Connection conn, int templateId) throws SQLException {
        List<WorkoutDay> days = new ArrayList<>();
        String query = """
            SELECT wd.*, we.*, e.exerciseName, e.bodyTargeted
            FROM workout_days wd
            JOIN workout_exercises we ON wd.dayID = we.dayID
            JOIN exercises e ON we.exerciseID = e.exerciseID
            WHERE wd.templateID = ?
            ORDER BY wd.dayNumber, we.workoutExerciseID
        """;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, templateId);
            ResultSet rs = stmt.executeQuery();

            WorkoutDay currentDay = null;
            int currentDayId = -1;

            while (rs.next()) {
                int dayId = rs.getInt("dayID");
                if (currentDayId != dayId) {
                    currentDay = createWorkoutDayFromResultSet(rs);
                    days.add(currentDay);
                    currentDayId = dayId;
                }
                currentDay.getExercises().add(createWorkoutExerciseFromResultSet(rs));
            }
        }
        return days;
    }
    private List<WorkoutDay> generateWorkoutDays(Connection conn, int templateId, int daysPerWeek, String injuryInfo)
            throws SQLException {
        List<WorkoutDay> workoutDays = new ArrayList<>();
        List<String> availableBodyParts = getAvailableBodyParts(injuryInfo);

        for (int i = 0; i < daysPerWeek; i++) {
            String bodyPart = availableBodyParts.get(i % availableBodyParts.size());

            // Create workout day
            String insertDayQuery = "INSERT INTO workout_days (templateID, dayNumber, bodyPart) VALUES (?, ?, ?)";
            int dayId;
            try (PreparedStatement stmt = conn.prepareStatement(insertDayQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, templateId);
                stmt.setInt(2, i + 1);
                stmt.setString(3, bodyPart);
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                rs.next();
                dayId = rs.getInt(1);
            }

            // Create day object
            WorkoutDay day = new WorkoutDay();
            day.setDayId(dayId);
            day.setDayNumber(i + 1);
            day.setBodyPart(bodyPart);
            day.setExercises(new ArrayList<>());

            // Generate exercises for this day
            List<Exercise> exercises = exerciseModel.generateWorkout(conn, bodyPart, true, 4);
            for (Exercise exercise : exercises) {
                WorkoutExercise workoutExercise = createWorkoutExercise(exercise, getExerciseParameters(day.getBodyPart()));
                saveWorkoutExercise(conn, dayId, workoutExercise);
                day.getExercises().add(workoutExercise);
            }

            workoutDays.add(day);
        }
        return workoutDays;
    }

    private List<String> getAvailableBodyParts(String injuryInfo) {
        List<String> available = new ArrayList<>(BODY_PARTS);
        if (injuryInfo != null && !injuryInfo.isEmpty()) {
            String[] injuries = injuryInfo.toLowerCase().split(",");
            available.removeIf(bodyPart ->
                    Arrays.stream(injuries)
                            .anyMatch(injury -> bodyPart.toLowerCase().contains(injury.trim())));
        }
        return available;
    }

    private WorkoutExercise createWorkoutExercise(Exercise exercise, ExerciseParameters params) {
        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setExerciseId(exercise.getExerciseID());
        workoutExercise.setExerciseName(exercise.getExerciseName());
        workoutExercise.setBodyTargeted(exercise.getBodyTargeted());
        workoutExercise.setSets(params.sets);
        workoutExercise.setRepRangeMin(params.repMin);
        workoutExercise.setRepRangeMax(params.repMax);
        workoutExercise.setRestSeconds(params.rest);
        workoutExercise.setNotes(exercise.getDescription());
        return workoutExercise;
    }

    private ExerciseParameters getExerciseParameters(String bodyPart) {
        // Default parameters based on body part
        return switch (bodyPart.toLowerCase()) {
            case "legs" -> new ExerciseParameters(4, 8, 12, 120); // Heavier compound movements
            case "back" -> new ExerciseParameters(4, 8, 12, 90);
            case "chest" -> new ExerciseParameters(4, 8, 12, 90);
            case "shoulders" -> new ExerciseParameters(3, 10, 15, 60);
            case "arms" -> new ExerciseParameters(3, 12, 15, 45);
            case "core" -> new ExerciseParameters(3, 15, 20, 30);
            default -> new ExerciseParameters(3, 10, 12, 60); // Default parameters
        };
    }

    private void saveWorkoutExercise(Connection conn, int dayId, WorkoutExercise exercise) throws SQLException {
        String sql = """
            INSERT INTO workout_exercises
            (dayID, exerciseID, sets, repRangeMin, repRangeMax, restSeconds, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dayId);
            stmt.setInt(2, exercise.getExerciseId());
            stmt.setInt(3, exercise.getSets());
            stmt.setInt(4, exercise.getRepRangeMin());
            stmt.setInt(5, exercise.getRepRangeMax());
            stmt.setInt(6, exercise.getRestSeconds());
            stmt.setString(7, exercise.getNotes());
            stmt.executeUpdate();
        }
    }

    // Helper class for exercise parameters
    private static class ExerciseParameters {
        final int sets;
        final int repMin;
        final int repMax;
        final int rest;

        ExerciseParameters(int sets, int repMin, int repMax, int rest) {
            this.sets = sets;
            this.repMin = repMin;
            this.repMax = repMax;
            this.rest = rest;
        }
    }
    // Helper methods for database operations
    private void deactivateCurrentPlans(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE user_workout_plans SET isActive = 0 WHERE userID = ? AND isActive = 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    private int createWorkoutTemplate(Connection conn, String targetGoal) throws SQLException {
        String sql = "INSERT INTO workout_templates (name, targetGoal, experienceLevel) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, "Custom Plan - " + targetGoal);
            stmt.setString(2, targetGoal);
            stmt.setString(3, "Intermediate");
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
    }

    private void createUserPlanAssociation(Connection conn, int userId, int templateId, Date startDate) throws SQLException {
        String sql = "INSERT INTO user_workout_plans (userID, templateID, startDate, isActive) VALUES (?, ?, ?, 1)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, templateId);
            stmt.setDate(3, new java.sql.Date(startDate.getTime()));
            stmt.executeUpdate();
        }
    }

    // Helper methods for creating objects from ResultSet
    private WorkoutPlan createPlanFromResultSet(ResultSet rs) throws SQLException {
        WorkoutPlan plan = new WorkoutPlan();
        plan.setPlanId(rs.getInt("userPlanID"));
        plan.setUserId(rs.getInt("userID"));

        // Convert SQL Date to java.util.Date
        java.sql.Date sqlStartDate = rs.getDate("startDate");
        if (sqlStartDate != null) {
            plan.setStartDate(new java.util.Date(sqlStartDate.getTime()));
        }

        java.sql.Date sqlEndDate = rs.getDate("endDate");
        if (sqlEndDate != null) {
            plan.setEndDate(new java.util.Date(sqlEndDate.getTime()));
        }

        plan.setActive(rs.getBoolean("isActive"));
        plan.setTemplateName(rs.getString("name"));
        plan.setTargetGoal(rs.getString("targetGoal"));
        return plan;
    }

    private WorkoutDay createWorkoutDayFromResultSet(ResultSet rs) throws SQLException {
        WorkoutDay day = new WorkoutDay();
        day.setDayId(rs.getInt("dayID"));
        day.setDayNumber(rs.getInt("dayNumber"));
        day.setBodyPart(rs.getString("bodyPart"));
        day.setExercises(new ArrayList<>());
        return day;
    }

    private WorkoutExercise createWorkoutExerciseFromResultSet(ResultSet rs) throws SQLException {
        WorkoutExercise exercise = new WorkoutExercise();
        exercise.setExerciseId(rs.getInt("exerciseID"));
        exercise.setExerciseName(rs.getString("exerciseName"));
        exercise.setBodyTargeted(rs.getString("bodyTargeted"));
        exercise.setSets(rs.getInt("sets"));
        exercise.setRepRangeMin(rs.getInt("repRangeMin"));
        exercise.setRepRangeMax(rs.getInt("repRangeMax"));
        exercise.setRestSeconds(rs.getInt("restSeconds"));
        exercise.setNotes(rs.getString("notes"));
        return exercise;
    }
}