package payment;// In shared/src/models/payment/PaymentMethod.java

import java.io.Serializable;
import java.time.LocalDateTime;

public class PaymentMethod implements Serializable {
    private static final long serialVersionUID = 1L;

    private int paymentID;
    private int userID;
    private int orderID;
    private String paymentType;
    private LocalDateTime paymentDate;

    // Default constructor for serialization
    public PaymentMethod() {}

    // Constructor without ID for new payments
    public PaymentMethod(int userID, int orderID, String paymentType, LocalDateTime paymentDate) {
        this.userID = userID;
        this.orderID = orderID;
        this.paymentType = paymentType;
        this.paymentDate = paymentDate;
    }

    // Full constructor
    public PaymentMethod(int paymentID, int userID, int orderID, String paymentType, LocalDateTime paymentDate) {
        this.paymentID = paymentID;
        this.userID = userID;
        this.orderID = orderID;
        this.paymentType = paymentType;
        this.paymentDate = paymentDate;
    }

    // Getters and setters
    public int getPaymentID() { return paymentID; }
    public void setPaymentID(int paymentID) { this.paymentID = paymentID; }

    public int getUserID() { return userID; }
    public void setUserID(int userID) { this.userID = userID; }

    public int getOrderID() { return orderID; }
    public void setOrderID(int orderID) { this.orderID = orderID; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
}