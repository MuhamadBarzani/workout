package payment;

import server.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentModel {
    private static final String INSERT_PAYMENT = "INSERT INTO paymentmethods (userID, orderID, paymentType, paymentDate) VALUES (?, ?, ?, ?)";
    private static final String DELETE_PAYMENT = "DELETE FROM paymentmethods WHERE paymentID = ?";
    private static final String SELECT_USER_PAYMENTS = "SELECT * FROM paymentmethods WHERE userID = ?";

    public boolean addPayment(PaymentMethod payment) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_PAYMENT)) {

            stmt.setInt(1, payment.getUserID());
            stmt.setInt(2, payment.getOrderID());
            stmt.setString(3, payment.getPaymentType());
            stmt.setTimestamp(4, Timestamp.valueOf(payment.getPaymentDate()));

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Detailed error processing payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean removePayment(int paymentID) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_PAYMENT)) {

            stmt.setInt(1, paymentID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error removing payment: " + e.getMessage());
            return false;
        }
    }

    public List<PaymentMethod> getAllPaymentsForUser(int userID) {
        List<PaymentMethod> payments = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_USER_PAYMENTS)) {

            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                payments.add(createPaymentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving payments: " + e.getMessage());
        }
        return payments;
    }

    private PaymentMethod createPaymentFromResultSet(ResultSet rs) throws SQLException {
        return new PaymentMethod(
                rs.getInt("paymentID"),
                rs.getInt("userID"),
                rs.getInt("orderID"),
                rs.getString("paymentType"),
                rs.getTimestamp("paymentDate").toLocalDateTime()
        );
    }
}