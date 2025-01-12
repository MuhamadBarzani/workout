package payment;

import server.FileManager;
import java.util.ArrayList;
import java.util.List;

public class PaymentModel {
    private static final String PAYMENTS_FILE = "payments.json";
    private static final String PAYMENT_COUNTER = "payment_counter.txt";
    private final FileManager fileManager;

    public PaymentModel() {
        this.fileManager = FileManager.getInstance();
    }

    public boolean addPayment(PaymentMethod payment) {
        try {
            List<PaymentMethod> payments = getAllPayments();

            // Generate new payment ID
            int paymentId = fileManager.getNextId(PAYMENT_COUNTER);

            // Create new payment with generated ID
            PaymentMethod newPayment = new PaymentMethod(
                    paymentId,
                    payment.getUserID(),
                    payment.getOrderID(),
                    payment.getPaymentType(),
                    payment.getPaymentDate()
            );

            payments.add(newPayment);
            fileManager.saveData(PAYMENTS_FILE, payments);
            return true;
        } catch (Exception e) {
            System.out.println("Error processing payment: " + e.getMessage());
            return false;
        }
    }

    public boolean removePayment(int paymentID) {
        try {
            List<PaymentMethod> payments = getAllPayments();
            boolean removed = payments.removeIf(p -> p.getPaymentID() == paymentID);

            if (removed) {
                fileManager.saveData(PAYMENTS_FILE, payments);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error removing payment: " + e.getMessage());
            return false;
        }
    }

    public List<PaymentMethod> getAllPaymentsForUser(int userID) {
        List<PaymentMethod> allPayments = getAllPayments();
        return allPayments.stream()
                .filter(payment -> payment.getUserID() == userID)
                .toList();
    }

    private List<PaymentMethod> getAllPayments() {
        List<PaymentMethod> payments = fileManager.loadList(PAYMENTS_FILE, PaymentMethod[].class);
        return payments != null ? payments : new ArrayList<>();
    }
}