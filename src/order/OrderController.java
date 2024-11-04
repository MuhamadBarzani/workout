package order;
import client.Client;
import client.ClientManager;
import payment.PaymentController;
import server.ServerResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.lang.reflect.Type;
import java.util.ArrayList;
public class OrderController {
    private final Client client;
    private final PaymentController paymentController;
    private final int currentUserID;
    private final Gson gson;

    public OrderController(PaymentController paymentController, int userID) {
        ClientManager clientManager = ClientManager.getInstance();
        this.client = clientManager.getClient();
        this.gson = clientManager.getGson();
        this.paymentController = paymentController;
        this.currentUserID = userID;
    }

    public List<Order> getUserOrders() {
        ServerResponse response = client.sendRequest("GET_USER_ORDERS", currentUserID);
        if (response.isSuccess()) {
            Type listType = new TypeToken<List<Order>>(){}.getType();
            return gson.fromJson(response.getMessage(), listType);
        }
        return new ArrayList<>();
    }

    public Order getOrderDetails(int orderID) {
        ServerResponse response = client.sendRequest("GET_ORDER_DETAILS", orderID);
        if (response.isSuccess()) {
            Order order = gson.fromJson(response.getMessage(), Order.class);
            if (order != null && order.getUserID() == currentUserID) {
                return order;
            }
        }
        return null;
    }

    public List<OrderSupplement> getOrderItems(int orderID) {
        Order order = getOrderDetails(orderID);
        if (order != null) {
            ServerResponse response = client.sendRequest("GET_ORDER_ITEMS", orderID);
            if (response.isSuccess()) {
                Type listType = new TypeToken<List<OrderSupplement>>(){}.getType();
                return gson.fromJson(response.getMessage(), listType);
            }
        }
        return null;
    }

    public int createOrder(List<OrderSupplement> items, String deliveryLocation, String paymentType) {
        if (items == null || items.isEmpty()) {
            System.err.println("Cannot create order: No items provided");
            return -1;
        }

        try {
            OrderCreateRequest request = new OrderCreateRequest(
                    currentUserID,
                    items,
                    deliveryLocation,
                    paymentType
            );

            ServerResponse response = client.sendRequest("CREATE_ORDER", request);
            if (response.isSuccess()) {
                int orderID = gson.fromJson(response.getMessage(), Integer.class);
                if (orderID != -1) {
                    if (!paymentController.processPayment(currentUserID, orderID, paymentType)) {
                        System.err.println("Payment processing failed. Cancelling order...");
                        cancelOrder(orderID);
                        return -1;
                    }
                    return orderID;
                }
            }
            System.err.println("Order creation failed: " + response.getMessage());

        } catch (Exception e) {
            System.err.println("Error creating order: " + e.getMessage());
        }
        return -1;
    }

    public boolean cancelOrder(int orderID) {
        Order order = getOrderDetails(orderID);
        if (order != null && order.getStatus().equals("Pending")) {
            ServerResponse response = client.sendRequest("CANCEL_ORDER", orderID);
            return response.isSuccess();
        }
        return false;
    }
}

