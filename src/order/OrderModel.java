package order;
import server.FileManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class OrderModel {
    private static final String ORDERS_FILE = "orders.json";
    private static final String ORDER_COUNTER = "order_counter.txt";
    private static final String ORDER_ITEMS_FILE = "order_items.json";
    private final FileManager fileManager;

    public OrderModel() {
        this.fileManager = FileManager.getInstance();
    }

    public List<Order> getUserOrders(int userID) {
        List<Order> allOrders = getAllOrders();
        return allOrders.stream()
                .filter(order -> order.getUserID() == userID)
                .sorted(Comparator.comparing(Order::getDateCreated).reversed())
                .collect(Collectors.toList());
    }

    public Order getOrderById(int orderID) {
        return getAllOrders().stream()
                .filter(order -> order.getOrderID() == orderID)
                .findFirst()
                .orElse(null);
    }

    public List<OrderSupplement> getOrderItems(int orderID) {
        List<OrderSupplement> allItems = getAllOrderItems();
        return allItems.stream()
                .filter(item -> item.getOrderID() == orderID)
                .collect(Collectors.toList());
    }

    public int createOrder(int userID, List<OrderSupplement> items, String deliveryLocation) {
        try {
            // Generate new order ID
            int orderID = fileManager.getNextId(ORDER_COUNTER);

            // Calculate total price
            double totalPrice = calculateTotalPrice(items);

            // Create new order
            Order order = new Order(
                    orderID,
                    userID,
                    totalPrice,
                    "Pending",
                    deliveryLocation,
                    LocalDateTime.now()
            );

            // Save order
            List<Order> orders = getAllOrders();
            orders.add(order);
            fileManager.saveData(ORDERS_FILE, orders);

            // Save order items with new orderID
            List<OrderSupplement> allOrderItems = getAllOrderItems();
            for (OrderSupplement item : items) {
                OrderSupplement orderItem = new OrderSupplement(
                        orderID,
                        item.getSupplementID(),
                        item.getQuantityOrdered()
                );
                allOrderItems.add(orderItem);
            }
            fileManager.saveData(ORDER_ITEMS_FILE, allOrderItems);

            return orderID;
        } catch (Exception e) {
            System.out.println("Error creating order: " + e.getMessage());
            return -1;
        }
    }

    public boolean cancelOrder(int orderID) {
        List<Order> orders = getAllOrders();
        for (Order order : orders) {
            if (order.getOrderID() == orderID && order.getStatus().equals("Pending")) {
                order.setStatus("Cancelled");
                fileManager.saveData(ORDERS_FILE, orders);
                return true;
            }
        }
        return false;
    }

    private List<Order> getAllOrders() {
        List<Order> orders = fileManager.loadList(ORDERS_FILE, Order[].class);
        return orders != null ? orders : new ArrayList<>();
    }

    private List<OrderSupplement> getAllOrderItems() {
        List<OrderSupplement> items = fileManager.loadList(ORDER_ITEMS_FILE, OrderSupplement[].class);
        return items != null ? items : new ArrayList<>();
    }

    private double calculateTotalPrice(List<OrderSupplement> items) {
        // In a real application, you would load supplement prices from a supplements file
        // For now, we'll use a dummy price of $10 per item
        return items.stream()
                .mapToDouble(item -> 10.0 * item.getQuantityOrdered())
                .sum();
    }
}