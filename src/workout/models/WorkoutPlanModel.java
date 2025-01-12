package workout.models;

import server.FileManager;
import user.User;
import exercise.Exercise;
import exercise.ExerciseModel;
import java.util.*;
import java.util.stream.Collectors;

public class WorkoutPlanModel {
    private static final String WORKOUT_PLANS_FILE = "workout_plans.json";
    private static final String WORKOUT_PLAN_COUNTER = "workout_plan_counter.txt";
    private final FileManager fileManager;
    private final ExerciseModel exerciseModel;
    private static final List<String> BODY_PARTS = Arrays.asList(
            "Chest", "Back", "Legs", "Shoulders", "Arms", "Core"
    );

    public WorkoutPlanModel() {
        this.fileManager = FileManager.getInstance();
        this.exerciseModel = new ExerciseModel();
    }

    public WorkoutPlan generateWorkoutPlan(User user, String targetGoal, int daysPerWeek, String injuryInfo) {
        try {
            // Deactivate current plans
            deactivateCurrentPlans(user.getUserID());

            // Generate new plan
            int planId = fileManager.getNextId(WORKOUT_PLAN_COUNTER);
            WorkoutPlan plan = new WorkoutPlan();
            plan.setPlanId(planId);
            plan.setUserId(user.getUserID());
            plan.setStartDate(new Date());
            plan.setActive(true);
            plan.setTargetGoal(targetGoal);
            plan.setTemplateName("Custom Plan - " + targetGoal);

            // Generate workout days
            List<WorkoutDay> workoutDays = generateWorkoutDays(daysPerWeek, injuryInfo);
            plan.setWorkoutDays(workoutDays);

            // Save plan
            List<WorkoutPlan> plans = getAllWorkoutPlans();
            plans.add(plan);
            fileManager.saveData(WORKOUT_PLANS_FILE, plans);

            return plan;
        } catch (Exception e) {
            System.out.println("Error generating workout plan: " + e.getMessage());
            return null;
        }
    }

    public List<WorkoutPlan> getUserWorkoutHistory(int userId) {
        return getAllWorkoutPlans().stream()
                .filter(plan -> plan.getUserId() == userId)
                .sorted(Comparator.comparing(WorkoutPlan::getStartDate).reversed())
                .collect(Collectors.toList());
    }

    public WorkoutPlan getCurrentWorkoutPlan(int userId) {
        return getAllWorkoutPlans().stream()
                .filter(plan -> plan.getUserId() == userId && plan.isActive())
                .findFirst()
                .orElse(null);
    }

    private List<WorkoutDay> generateWorkoutDays(int daysPerWeek, String injuryInfo) {
        List<WorkoutDay> workoutDays = new ArrayList<>();
        List<String> availableBodyParts = getAvailableBodyParts(injuryInfo);

        for (int i = 0; i < daysPerWeek; i++) {
            String bodyPart = availableBodyParts.get(i % availableBodyParts.size());

            WorkoutDay day = new WorkoutDay();
            day.setDayNumber(i + 1);
            day.setBodyPart(bodyPart);

            // Generate exercises for this day
            List<Exercise> exercises = exerciseModel.generateWorkout(bodyPart, true, 4);
            List<WorkoutExercise> workoutExercises = exercises.stream()
                    .map(e -> createWorkoutExercise(e, getExerciseParameters(bodyPart)))
                    .collect(Collectors.toList());

            day.setExercises(workoutExercises);
            workoutDays.add(day);
        }

        return workoutDays;
    }

    private List<String> getAvailableBodyParts(String injuryInfo) {
        if (injuryInfo == null || injuryInfo.isEmpty()) {
            return new ArrayList<>(BODY_PARTS);
        }

        String[] injuries = injuryInfo.toLowerCase().split(",");
        return BODY_PARTS.stream()
                .filter(bodyPart -> Arrays.stream(injuries)
                        .noneMatch(injury -> bodyPart.toLowerCase().contains(injury.trim())))
                .collect(Collectors.toList());
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
        return switch (bodyPart.toLowerCase()) {
            case "legs" -> new ExerciseParameters(4, 8, 12, 120);
            case "back" -> new ExerciseParameters(4, 8, 12, 90);
            case "chest" -> new ExerciseParameters(4, 8, 12, 90);
            case "shoulders" -> new ExerciseParameters(3, 10, 15, 60);
            case "arms" -> new ExerciseParameters(3, 12, 15, 45);
            case "core" -> new ExerciseParameters(3, 15, 20, 30);
            default -> new ExerciseParameters(3, 10, 12, 60);
        };
    }

    private void deactivateCurrentPlans(int userId) {
        List<WorkoutPlan> plans = getAllWorkoutPlans();
        boolean updated = false;

        for (WorkoutPlan plan : plans) {
            if (plan.getUserId() == userId && plan.isActive()) {
                plan.setActive(false);
                plan.setEndDate(new Date());
                updated = true;
            }
        }

        if (updated) {
            fileManager.saveData(WORKOUT_PLANS_FILE, plans);
        }
    }

    private List<WorkoutPlan> getAllWorkoutPlans() {
        List<WorkoutPlan> plans = fileManager.loadList(WORKOUT_PLANS_FILE, WorkoutPlan[].class);
        return plans != null ? plans : new ArrayList<>();
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
}