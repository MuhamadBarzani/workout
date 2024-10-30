package order;
import payment.PaymentController;

import java.util.List;

public class OrderController {
    private final OrderModel model;
    private final PaymentController paymentController;
    private final int currentUserID;

    public OrderController(OrderModel model, PaymentController paymentController, int userID) {
        this.model = model;
        this.paymentController = paymentController;
        this.currentUserID = userID;
    }

    public List<Order> getUserOrders() {
        return model.getUserOrders(currentUserID);
    }

    public Order getOrderDetails(int orderID) {
        Order order = model.getOrderById(orderID);
        if (order != null && order.getUserID() == currentUserID) {
            return order;
        }
        return null;
    }

    public List<OrderSupplement> getOrderItems(int orderID) {
        Order order = getOrderDetails(orderID);
        if (order != null) {
            return model.getOrderItems(orderID);
        }
        return null;
    }

    public int createOrder(List<OrderSupplement> items, String deliveryLocation, String paymentType) {
        int orderID = model.createOrder(currentUserID, items, deliveryLocation);
        if (orderID != -1) {
            // Process payment after order creation
            if (!paymentController.processPayment(currentUserID, orderID, paymentType)) {
                // If payment fails, cancel the order
                model.cancelOrder(orderID);
                return -1;
            }
        }
        return orderID;
    }

    public boolean cancelOrder(int orderID) {
        Order order = getOrderDetails(orderID);
        if (order != null && order.getStatus().equals("Pending")) {
            return model.cancelOrder(orderID);
        }
        return false;
    }
}