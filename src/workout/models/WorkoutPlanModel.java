package workout.models;

import user.User;
import auth.DatabaseManager;
import exercise.ExerciseModel;
import exercise.Exercise;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class WorkoutPlanModel {
    public WorkoutPlan generateWorkoutPlan(User user, String targetGoal, int daysPerWeek, String injuryInfo) {
        Connection conn = null;
        WorkoutPlan plan = null;
        try {
            // Get a new connection
            conn = DatabaseManager.getInstance().getConnection();

            // Ensure we're starting with autoCommit false
            conn.setAutoCommit(false);

            // Create new workout plan
            plan = new WorkoutPlan();
            plan.setUserId(user.getUserID());
            plan.setStartDate(new Date());
            plan.setActive(true);
            plan.setTargetGoal(targetGoal);

            // Get available body parts excluding injured areas
            List<String> availableBodyParts = getAvailableBodyParts(injuryInfo);

            // Generate workout days - pass connection to maintain transaction
            List<WorkoutDay> workoutDays = new ArrayList<>();
            for (int i = 0; i < daysPerWeek; i++) {
                WorkoutDay day = generateWorkoutDay(
                        conn,
                        i + 1,
                        availableBodyParts.get(i % availableBodyParts.size()),
                        user.getWorkoutPreference(),
                        targetGoal
                );
                workoutDays.add(day);
            }

            plan.setWorkoutDays(workoutDays);

            // Save the plan to database using the same connection
            savePlan(conn, plan);

            // If we got here without exception, commit the transaction
            conn.commit();

            return plan;

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                // Log rollback failure
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

    // Update generateWorkoutDay to accept Connection parameter
    private WorkoutDay generateWorkoutDay(
            Connection conn,
            int dayNumber,
            String bodyPart,
            String experienceLevel,
            String targetGoal
    ) throws SQLException {
        WorkoutDay day = new WorkoutDay();
        day.setDayNumber(dayNumber);
        day.setBodyPart(bodyPart);

        // Use ExerciseModel to get exercises - pass the connection
        boolean hasEquipment = true;
        List<Exercise> exercises = exerciseModel.generateWorkout(conn, bodyPart, hasEquipment, 4);

        List<WorkoutExercise> workoutExercises = new ArrayList<>();
        for (Exercise exercise : exercises) {
            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setExerciseId(exercise.getExerciseID());
            workoutExercise.setExerciseName(exercise.getExerciseName());
            workoutExercise.setBodyTargeted(exercise.getBodyTargeted());
            configureExerciseParameters(workoutExercise, targetGoal);
            workoutExercise.setNotes(exercise.getDescription());
            workoutExercises.add(workoutExercise);
        }

        day.setExercises(workoutExercises);
        return day;
    }
    private static final List<String> BODY_PARTS = Arrays.asList(
            "Chest", "Back", "Legs", "Shoulders", "Arms", "Core"
    );

    private final ExerciseModel exerciseModel;

    public WorkoutPlanModel() {
        this.exerciseModel = new ExerciseModel();
    }

    // [Rest of the methods remain the same, but we'll update the database access methods]

    public List<WorkoutPlan> getUserWorkoutHistory(int userId) throws SQLException {
        List<WorkoutPlan> history = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
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
                    WorkoutPlan plan = new WorkoutPlan();
                    plan.setPlanId(rs.getInt("userPlanID"));
                    plan.setUserId(userId);
                    plan.setStartDate(rs.getDate("startDate"));
                    plan.setEndDate(rs.getDate("endDate"));
                    plan.setActive(rs.getBoolean("isActive"));
                    plan.setTemplateName(rs.getString("name"));
                    plan.setTargetGoal(rs.getString("targetGoal"));

                    // Load workout days for this plan
                    plan.setWorkoutDays(getWorkoutDaysForPlan(conn, rs.getInt("templateID")));

                    history.add(plan);
                }
            }
            return history;
        } finally {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
    }

    public WorkoutPlan getCurrentWorkoutPlan(int userId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
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
                    WorkoutPlan plan = new WorkoutPlan();
                    plan.setPlanId(rs.getInt("userPlanID"));
                    plan.setUserId(userId);
                    plan.setStartDate(rs.getDate("startDate"));
                    plan.setEndDate(rs.getDate("endDate"));
                    plan.setActive(true);
                    plan.setTemplateName(rs.getString("name"));
                    plan.setTargetGoal(rs.getString("targetGoal"));

                    // Load workout days for current plan
                    plan.setWorkoutDays(getWorkoutDaysForPlan(conn, rs.getInt("templateID")));

                    return plan;
                }
            }
            return null;
        } finally {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
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
                    currentDay = new WorkoutDay();
                    currentDay.setDayId(dayId);
                    currentDay.setDayNumber(rs.getInt("dayNumber"));
                    currentDay.setBodyPart(rs.getString("bodyPart"));
                    currentDay.setExercises(new ArrayList<>());
                    days.add(currentDay);
                    currentDayId = dayId;
                }

                WorkoutExercise exercise = new WorkoutExercise();
                exercise.setExerciseId(rs.getInt("exerciseID"));
                exercise.setExerciseName(rs.getString("exerciseName"));
                exercise.setBodyTargeted(rs.getString("bodyTargeted"));
                exercise.setSets(rs.getInt("sets"));
                exercise.setRepRangeMin(rs.getInt("repRangeMin"));
                exercise.setRepRangeMax(rs.getInt("repRangeMax"));
                exercise.setRestSeconds(rs.getInt("restSeconds"));
                exercise.setNotes(rs.getString("notes"));
                currentDay.getExercises().add(exercise);
            }
        }

        return days;
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

        private void configureExerciseParameters(WorkoutExercise exercise, String targetGoal) {
            switch (targetGoal.toLowerCase()) {
                case "strength":
                    exercise.setSets(5);
                    exercise.setRepRangeMin(3);
                    exercise.setRepRangeMax(5);
                    exercise.setRestSeconds(180);
                    break;
                case "muscle gain":
                    exercise.setSets(4);
                    exercise.setRepRangeMin(8);
                    exercise.setRepRangeMax(12);
                    exercise.setRestSeconds(90);
                    break;
                case "weight loss":
                    exercise.setSets(3);
                    exercise.setRepRangeMin(15);
                    exercise.setRepRangeMax(20);
                    exercise.setRestSeconds(45);
                    break;
                case "endurance":
                    exercise.setSets(3);
                    exercise.setRepRangeMin(12);
                    exercise.setRepRangeMax(15);
                    exercise.setRestSeconds(60);
                    break;
                default: // General fitness
                    exercise.setSets(3);
                    exercise.setRepRangeMin(10);
                    exercise.setRepRangeMax(12);
                    exercise.setRestSeconds(90);
            }
        }


        private void savePlan(Connection conn, WorkoutPlan plan) throws SQLException {
        // First deactivate any current plans
        String deactivateQuery = "UPDATE user_workout_plans SET isActive = 0 WHERE userID = ? AND isActive = 1";
        try (PreparedStatement stmt = conn.prepareStatement(deactivateQuery)) {
            stmt.setInt(1, plan.getUserId());
            stmt.executeUpdate();
        }

        // Insert new plan
        String insertPlanQuery = """
            INSERT INTO workout_templates (name, targetGoal, experienceLevel)
            VALUES (?, ?, ?)
        """;

        int templateId;
        try (PreparedStatement stmt = conn.prepareStatement(insertPlanQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, "Custom Plan - " + plan.getTargetGoal());
            stmt.setString(2, plan.getTargetGoal());
            stmt.setString(3, "Intermediate");

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            templateId = rs.getInt(1);
        }

        // Insert user plan association
        String insertUserPlanQuery = """
            INSERT INTO user_workout_plans (userID, templateID, startDate, isActive)
            VALUES (?, ?, ?, 1)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(insertUserPlanQuery)) {
            stmt.setInt(1, plan.getUserId());
            stmt.setInt(2, templateId);
            stmt.setDate(3, new java.sql.Date(plan.getStartDate().getTime()));
            stmt.executeUpdate();
        }

        // Save workout days and exercises
        for (WorkoutDay day : plan.getWorkoutDays()) {
            saveWorkoutDay(conn, templateId, day);
        }
    }

    private void saveWorkoutDay(Connection conn, int templateId, WorkoutDay day) throws SQLException {
        String insertDayQuery = """
            INSERT INTO workout_days (templateID, dayNumber, bodyPart)
            VALUES (?, ?, ?)
        """;

        int dayId;
        try (PreparedStatement stmt = conn.prepareStatement(insertDayQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, templateId);
            stmt.setInt(2, day.getDayNumber());
            stmt.setString(3, day.getBodyPart());

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            dayId = rs.getInt(1);
        }

        // Save exercises for this day
        for (WorkoutExercise exercise : day.getExercises()) {
            saveWorkoutExercise(conn, dayId, exercise);
        }
    }

    private void saveWorkoutExercise(Connection conn, int dayId, WorkoutExercise exercise) throws SQLException {
        String insertExerciseQuery = """
            INSERT INTO workout_exercises 
            (dayID, exerciseID, sets, repRangeMin, repRangeMax, restSeconds, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(insertExerciseQuery)) {
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
}