package order;

import java.util.List;
import java.io.Serializable;

public class OrderCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userID;
    private List<OrderSupplement> items;
    private String deliveryLocation;
    private String paymentType;

    public OrderCreateRequest() {} // Default constructor for Gson

    public OrderCreateRequest(int userID, List<OrderSupplement> items,
                              String deliveryLocation, String paymentType) {
        this.userID = userID;
        this.items = items;
        this.deliveryLocation = deliveryLocation;
        this.paymentType = paymentType;
    }

    // Getters and setters
    public int getUserID() { return userID; }

    public List<OrderSupplement> getItems() { return items; }

    public String getDeliveryLocation() { return deliveryLocation; }

}