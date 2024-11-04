
// PaymentView.java
package payment;

import java.util.List;
import java.util.Scanner;

public class PaymentView {
    private final PaymentController paymentController;
    private final Scanner scanner;

    public PaymentView(PaymentController paymentController) {
        this.paymentController = paymentController;
        this.scanner = new Scanner(System.in);
    }

    public void paymentMenu(int userID) {
        while (true) {
            System.out.println("\nPayment Management");
            System.out.println("1. View Payment History");
            System.out.println("2. Remove Payment Method");
            System.out.println("3. Back to Order Menu");
            System.out.print("Choose an option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1:
                        displayPaymentHistory(userID);
                        break;
                    case 2:
                        removePaymentMethod(userID);
                        break;
                    case 3:
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private void displayPaymentHistory(int userID) {
        List<PaymentMethod> payments = paymentController.getUserPayments(userID);
        if (payments.isEmpty()) {
            System.out.println("No payment history found.");
            return;
        }

        System.out.println("\n=== Payment History ===");
        System.out.println("Payment ID | Order ID | Type | Date");
        System.out.println("-----------+----------+------+------");

        for (PaymentMethod payment : payments) {
            System.out.printf("%-11d| %-9d| %-5s| %s%n",
                    payment.getPaymentID(),
                    payment.getOrderID(),
                    payment.getPaymentType(),
                    payment.getPaymentDate());
        }
    }

    private void removePaymentMethod(int userID) {
        List<PaymentMethod> payments = paymentController.getUserPayments(userID);
        if (payments.isEmpty()) {
            System.out.println("No payment methods to remove.");
            return;
        }

        System.out.println("\nSelect payment method to remove:");
        for (PaymentMethod payment : payments) {
            System.out.printf("%d: %s (Order ID: %d)%n",
                    payment.getPaymentID(),
                    payment.getPaymentType(),
                    payment.getOrderID());
        }

        System.out.print("Enter Payment ID to remove (0 to cancel): ");
        try {
            int paymentID = Integer.parseInt(scanner.nextLine().trim());
            if (paymentID == 0) return;

            if (paymentController.removePayment(paymentID)) {
                System.out.println("Payment method removed successfully.");
            } else {
                System.out.println("Failed to remove payment method.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid Payment ID.");
        }
    }
}