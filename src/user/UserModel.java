
// UserModel.java
package user;

import auth.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserModel {

    public boolean createUser(User user) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String sql = "INSERT INTO users (username, password, email, age, height, weight, workoutPreference) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.setInt(4, user.getAge());
            statement.setDouble(5, user.getHeight());
            statement.setDouble(6, user.getWeight());
            statement.setString(7, user.getWorkoutPreference());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error creating user: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUser(User user) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String sql = "UPDATE users SET email = ?, age = ?, height = ?, weight = ?, " +
                    "workoutPreference = ? WHERE userID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user.getEmail());
            statement.setInt(2, user.getAge());
            statement.setDouble(3, user.getHeight());
            statement.setDouble(4, user.getWeight());
            statement.setString(5, user.getWorkoutPreference());
            statement.setInt(6, user.getUserID());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
    private User loadUserFromResultSet(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("userID"),
                resultSet.getString("username"),
                resultSet.getString("password"),
                resultSet.getString("email"),
                resultSet.getInt("age"),
                resultSet.getDouble("height"),
                resultSet.getDouble("weight"),
                resultSet.getString("workoutPreference")
        );
    }
    public User getUserDetails(int userID) throws SQLException {
        String sql = "SELECT * FROM users WHERE userID = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserID(rs.getInt("userID"));
                user.setUsername(rs.getString("username"));
                user.setWorkoutPreference(rs.getString("workoutPreference"));
                user.setHeight(rs.getDouble("height"));
                user.setWeight(rs.getDouble("weight"));
                user.setAge(rs.getInt("age"));
                return user;
            }
        }
        return null;
    }

    public String getInjuryInfo(int userID) throws SQLException {
        String sql = "SELECT wt.injuryInfo FROM user_workout_plans uwp " +
                "JOIN workout_templates wt ON uwp.templateID = wt.templateID " +
                "WHERE uwp.userID = ? AND uwp.isActive = 1";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("injuryInfo");
            }
        }
        return null;
    }
    public User getUserByEmail(String email) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT * FROM users WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return loadUserFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving user: " + e.getMessage());
        }
        return null;
    }
    public boolean deleteUser(int userID) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String sql = "DELETE FROM users WHERE userID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userID);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
}