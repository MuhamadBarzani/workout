package order;

import auth.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderModel {

    public List<Order> getUserOrders(int userID) {
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT * FROM orders WHERE userID = ? ORDER BY dateCreated DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(loadOrderFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving orders: " + e.getMessage());
        }
        return orders;
    }

    public Order getOrderById(int orderID) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = "SELECT * FROM orders WHERE orderID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, orderID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return loadOrderFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving order: " + e.getMessage());
        }
        return null;
    }

    public List<OrderSupplement> getOrderItems(int orderID, Connection conn) throws SQLException {
        List<OrderSupplement> items = new ArrayList<>();
        String sql = "SELECT * FROM ordersupplements WHERE orderID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(new OrderSupplement(
                        rs.getInt("orderID"),
                        rs.getInt("supplementID"),
                        rs.getInt("quantityOrdered")
                ));
            }
        }
        return items;
    }

    // Original getOrderItems method - for display purposes
    public List<OrderSupplement> getOrderItems(int orderID) {
        List<OrderSupplement> items = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            items = getOrderItems(orderID, conn);
        } catch (SQLException e) {
            System.out.println("Error retrieving order items: " + e.getMessage());
        }
        return items;
    }

    public int createOrder(int userID, List<OrderSupplement> items, String deliveryLocation) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Calculate total price
            double totalPrice = calculateTotalPrice(conn, items);

            // Create order and get generated order ID
            int orderID = insertOrder(conn, userID, totalPrice, deliveryLocation);
            if (orderID == -1) {
                conn.rollback();
                return -1;
            }

            // Insert order items and update supplement quantities
            insertOrderItems(conn, orderID, items);
            updateSupplementQuantities(conn, items);

            conn.commit();
            return orderID;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error rolling back transaction: " + ex.getMessage());
            }
            System.out.println("Error creating order: " + e.getMessage());
            return -1;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error resetting connection: " + e.getMessage());
            }
        }
    }

    private int insertOrder(Connection conn, int userID, double totalPrice, String deliveryLocation) throws SQLException {
        String orderSql = "INSERT INTO orders (userID, totalPrice, status, deliveryLocation, dateCreated) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userID);
            stmt.setDouble(2, totalPrice);
            stmt.setString(3, "Pending");
            stmt.setString(4, deliveryLocation);
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    private void insertOrderItems(Connection conn, int orderID, List<OrderSupplement> items) throws SQLException {
        String itemSql = "INSERT INTO ordersupplements (orderID, supplementID, quantityOrdered) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
            for (OrderSupplement item : items) {
                stmt.setInt(1, orderID);
                stmt.setInt(2, item.getSupplementID());
                stmt.setInt(3, item.getQuantityOrdered());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void updateSupplementQuantities(Connection conn, List<OrderSupplement> items) throws SQLException {
        String updateSql = "UPDATE supplements SET quantityAvailable = quantityAvailable - ? WHERE supplementID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            for (OrderSupplement item : items) {
                stmt.setInt(1, item.getQuantityOrdered());
                stmt.setInt(2, item.getSupplementID());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }


    public boolean cancelOrder(int orderID) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Check if order can be cancelled (is still pending)
            String checkSql = "SELECT status FROM orders WHERE orderID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, orderID);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next() || !rs.getString("status").equals("Pending")) {
                return false;
            }

            // Update order status
            String updateOrderSql = "UPDATE orders SET status = 'Cancelled' WHERE orderID = ?";
            PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderSql);
            updateOrderStmt.setInt(1, orderID);
            updateOrderStmt.executeUpdate();

            // Restore supplement quantities
            List<OrderSupplement> items = getOrderItems(orderID,conn);
            String restoreSql = "UPDATE supplements SET quantityAvailable = quantityAvailable + ? WHERE supplementID = ?";
            PreparedStatement restoreStmt = conn.prepareStatement(restoreSql);

            for (OrderSupplement item : items) {
                restoreStmt.setInt(1, item.getQuantityOrdered());
                restoreStmt.setInt(2, item.getSupplementID());
                restoreStmt.addBatch();
            }

            restoreStmt.executeBatch();

            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error rolling back transaction: " + ex.getMessage());
            }
            System.out.println("Error cancelling order: " + e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error resetting connection: " + e.getMessage());
            }
        }
    }

    private Order loadOrderFromResultSet(ResultSet rs) throws SQLException {
        return new Order(
                rs.getInt("orderID"),
                rs.getInt("userID"),
                rs.getDouble("totalPrice"),
                rs.getString("status"),
                rs.getString("deliveryLocation"),
                rs.getTimestamp("dateCreated").toLocalDateTime()
        );
    }

    private double calculateTotalPrice(Connection conn, List<OrderSupplement> items) throws SQLException {
        double total = 0;
        String sql = "SELECT price FROM supplements WHERE supplementID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (OrderSupplement item : items) {
                stmt.setInt(1, item.getSupplementID());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        total += rs.getDouble("price") * item.getQuantityOrdered();
                    }
                }
            }
        }
        return total;
    }
}