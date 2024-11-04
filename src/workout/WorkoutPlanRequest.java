package workout;

// Helper class for workout plan requests
public record WorkoutPlanRequest(int userId, String targetGoal, int daysPerWeek) {
}
