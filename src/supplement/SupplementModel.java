
// SupplementModel.java
package supplement;

import server.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplementModel {

    public List<Supplement> getAllSupplements() {
        List<Supplement> supplements = new ArrayList<>();
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT * FROM supplements ORDER BY category, name";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                supplements.add(loadSupplementFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving supplements: " + e.getMessage());
        }
        return supplements;
    }

    public List<Supplement> getSupplementsByCategory(String category) {
        List<Supplement> supplements = new ArrayList<>();
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT * FROM supplements WHERE category = ? ORDER BY name";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, category);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                supplements.add(loadSupplementFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving supplements by category: " + e.getMessage());
        }
        return supplements;
    }

    public Supplement getSupplementById(int supplementID) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT * FROM supplements WHERE supplementID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, supplementID);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return loadSupplementFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving supplement: " + e.getMessage());
        }
        return null;
    }

    public boolean updateQuantity(int supplementID, int newQuantity) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String sql = "UPDATE supplements SET quantityAvailable = ? WHERE supplementID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, newQuantity);
            statement.setInt(2, supplementID);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating supplement quantity: " + e.getMessage());
            return false;
        }
    }

    private Supplement loadSupplementFromResultSet(ResultSet resultSet) throws SQLException {
        return new Supplement(
                resultSet.getInt("supplementID"),
                resultSet.getString("name"),
                resultSet.getString("category"),
                resultSet.getDouble("price"),
                resultSet.getInt("quantityAvailable"),
                resultSet.getString("description")
        );
    }
}
