package order;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import payment.PaymentController;
import payment.PaymentView;
import supplement.Supplement;
import supplement.SupplementController;

public class OrderView {
    private final OrderController orderController;
    private final SupplementController supplementController;
    private final PaymentView paymentView;
    private final Scanner scanner;

    public OrderView(OrderController orderController, SupplementController supplementController, PaymentView paymentView) {
        this.orderController = orderController;
        this.supplementController = supplementController;
        this.paymentView = paymentView;
        this.scanner = new Scanner(System.in);
    }

    public int createOrder(int userID, List<OrderSupplement> items) {
        System.out.print("Enter delivery location: ");
        String deliveryLocation = scanner.nextLine().trim();

        // Get payment method from user
        System.out.println("\nSelect Payment Method:");
        System.out.println("1. Credit Card");
        System.out.println("2. Debit Card");
        System.out.println("3. PayPal");
        System.out.print("Choose payment method: ");

        try {
            int paymentChoice = Integer.parseInt(scanner.nextLine().trim());
            String paymentType = switch (paymentChoice) {
                case 1 -> "Credit Card";
                case 2 -> "Debit Card";
                case 3 -> "PayPal";
                default -> null;
            };

            if (paymentType == null) {
                System.out.println("Invalid payment method selected.");
                return -1;
            }

            int orderID = orderController.createOrder(items, deliveryLocation, paymentType);
            if (orderID != -1) {
                System.out.println("Order created and payment processed successfully!");
                return orderID;
            } else {
                System.out.println("Failed to create order or process payment.");
                return -1;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Order cancelled.");
            return -1;
        }
    }

    public void displayOrderMenu(int userID) { // Pass userID as a parameter
        while (true) {
            System.out.println("\nOrder Management");
            System.out.println("1. View My Orders");
            System.out.println("2. Cancel Order");
            System.out.println("3. Payment Menu");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        viewOrders();
                        break;
                    case 2:
                        cancelOrder();
                        break;
                    case 3:
                        paymentMenu(userID);
                        break;
                    case 4:
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private static void paymentMenu(int userID) {
        // Create an instance of PaymentController if not already created
        PaymentController paymentController = new PaymentController();
        PaymentView paymentView = new PaymentView(paymentController);
        paymentView.paymentMenu(userID); // Call paymentMenu with userID
        return;
    }

    private void viewOrders() {
        List<Order> orders = orderController.getUserOrders();
        if (orders.isEmpty()) {
            System.out.println("You have no orders.");
            return;
        }

        System.out.println("\n=== Your Orders ===");
        System.out.println("Order ID | Date Created | Status | Total Price | Delivery Location");
        System.out.println("---------+-------------+--------+-------------+------------------");

        for (Order order : orders) {
            System.out.printf("%-9d| %s | %-7s| $%-10.2f| %s%n",
                    order.getOrderID(),
                    order.getDateCreated().toString(),
                    order.getStatus(),
                    order.getTotalPrice(),
                    order.getDeliveryLocation());
        }

        System.out.print("\nEnter Order ID for details (or 0 to go back): ");
        try {
            int orderID = Integer.parseInt(scanner.nextLine().trim());
            if (orderID > 0) {
                displayOrderDetails(orderID);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private void displayOrderDetails(int orderID) {
        Order order = orderController.getOrderDetails(orderID);
        if (order == null) {
            System.out.println("Order not found.");
            return;
        }

        System.out.println("\n=== Order Details ===");
        System.out.println("Order ID: " + order.getOrderID());
        System.out.println("Date: " + order.getDateCreated());
        System.out.println("Status: " + order.getStatus());
        System.out.println("Delivery Location: " + order.getDeliveryLocation());

        List<OrderSupplement> items = orderController.getOrderItems(orderID);
        if (items != null && !items.isEmpty()) {
            System.out.println("\nOrdered Items:");
            System.out.println("Supplement ID | Name | Quantity | Price");
            System.out.println("-------------+------+----------+-------");

            for (OrderSupplement item : items) {
                Supplement supplement = supplementController.getSupplementInfo(item.getSupplementID());
                if (supplement != null) {
                    System.out.printf("%-13d| %-20s| %-8d| $%.2f%n",
                            supplement.getSupplementID(),
                            supplement.getName(),
                            item.getQuantityOrdered(),
                            supplement.getPrice() * item.getQuantityOrdered());
                }
            }
        }

        System.out.printf("\nTotal Price: $%.2f%n", order.getTotalPrice());
    }

    private void cancelOrder() {
        List<Order> orders = orderController.getUserOrders();
        List<Order> pendingOrders = new ArrayList<>();

        System.out.println("\n=== Pending Orders ===");
        for (Order order : orders) {
            if (order.getStatus().equals("Pending")) {
                pendingOrders.add(order);
                System.out.printf("Order ID: %d | Date: %s | Total: $%.2f | Location: %s%n",
                        order.getOrderID(),
                        order.getDateCreated(),
                        order.getTotalPrice(),
                        order.getDeliveryLocation());
            }
        }

        if (pendingOrders.isEmpty()) {
            System.out.println("No pending orders available to cancel.");
            return;
        }

        System.out.print("\nEnter Order ID to cancel (or 0 to go back): ");
        try {
            int orderID = Integer.parseInt(scanner.nextLine().trim());
            if (orderID == 0) return;

            boolean validOrder = pendingOrders.stream()
                    .anyMatch(order -> order.getOrderID() == orderID);

            if (!validOrder) {
                System.out.println("Invalid order ID or order cannot be cancelled.");
                return;
            }

            System.out.print("Are you sure you want to cancel this order? (yes/no): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                if (orderController.cancelOrder(orderID)) {
                    System.out.println("Order cancelled successfully.");
                } else {
                    System.out.println("Error cancelling order. Please try again.");
                }
            } else {
                System.out.println("Cancellation aborted.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid order ID.");
        }
    }
}