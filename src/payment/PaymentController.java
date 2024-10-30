package payment;

import java.time.LocalDateTime;
import java.util.List;

public class PaymentController {
    private final PaymentModel paymentModel;

    public PaymentController() {
        this.paymentModel = new PaymentModel();
    }

    public boolean processPayment(int userID, int orderID, String paymentType) {
        PaymentMethod payment = new PaymentMethod(userID, orderID, paymentType, LocalDateTime.now());
        return paymentModel.addPayment(payment);
    }

    public boolean removePayment(int paymentID) {
        return paymentModel.removePayment(paymentID);
    }

    public List<PaymentMethod> getUserPayments(int userID) {
        return paymentModel.getAllPaymentsForUser(userID);
    }

    public PaymentMethod getOrderPayment(int orderID) {
        return paymentModel.getOrderPayment(orderID);
    }
}
