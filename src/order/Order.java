package order;

import java.time.LocalDateTime;

public class Order {
        private int orderID;
        private int userID;
        private double totalPrice;
        private String status;
        private String deliveryLocation;
        private LocalDateTime dateCreated;

    public Order(int orderID, int userID, double totalPrice, String status, String deliveryLocation, LocalDateTime dateCreated) {
        this.orderID = orderID;
        this.userID = userID;
        this.totalPrice = totalPrice;
        this.status = status;
        this.deliveryLocation = deliveryLocation;
        this.dateCreated = dateCreated;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getOrderID() {
        return orderID;
    }

    public int getUserID() {
        return userID;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

}
