package workout.models;

import auth.DatabaseManager;

import java.sql.*;

public class WorkoutTemplate {
    private int templateID;
    private String name;
    private String targetGoal;
    private String experienceLevel;
    private String description;
    private String injuryInfo;

    public WorkoutTemplate(int templateID, String name, String targetGoal, String experienceLevel, String description, String injuryInfo) {
        this.templateID = templateID;
        this.name = name;
        this.targetGoal = targetGoal;
        this.experienceLevel = experienceLevel;
        this.description = description;
        this.injuryInfo = injuryInfo;
    }

    public int createTemplate(String name, String targetGoal, String experienceLevel) throws SQLException {
        String sql = "INSERT INTO workout_templates (name, targetGoal, experienceLevel) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, targetGoal);
            stmt.setString(3, experienceLevel);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public int getTemplateID() {
        return templateID;
    }

    public void setTemplateID(int templateID) {
        this.templateID = templateID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetGoal() {
        return targetGoal;
    }

    public void setTargetGoal(String targetGoal) {
        this.targetGoal = targetGoal;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInjuryInfo() {
        return injuryInfo;
    }

    public void setInjuryInfo(String injuryInfo) {
        this.injuryInfo = injuryInfo;
    }
}