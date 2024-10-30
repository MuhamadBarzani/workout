package payment;

import java.time.LocalDateTime;

public class PaymentMethod {
    private int paymentID;
    private int userID;
    private int orderID;
    private String paymentType;
    private LocalDateTime paymentDate;

    public PaymentMethod(int paymentID, int userID, int orderID, String paymentType, LocalDateTime paymentDate) {
        this.paymentID = paymentID;
        this.userID = userID;
        this.orderID = orderID;
        this.paymentType = paymentType;
        this.paymentDate = paymentDate;
    }

    // Constructor for new payments (without ID)
    public PaymentMethod(int userID, int orderID, String paymentType, LocalDateTime paymentDate) {
        this(-1, userID, orderID, paymentType, paymentDate);
    }

    // Getters
    public int getPaymentID() { return paymentID; }
    public int getUserID() { return userID; }
    public int getOrderID() { return orderID; }
    public String getPaymentType() { return paymentType; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
}
