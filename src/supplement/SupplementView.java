// SupplementView.java
package supplement;

import order.OrderController;
import order.OrderSupplement;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SupplementView {
    private final SupplementController supplementController;
    private final OrderController orderController;
    private final Scanner scanner;

    public SupplementView(SupplementController supplementController, OrderController orderController) {
        this.supplementController = supplementController;
        this.orderController = orderController;
        this.scanner = new Scanner(System.in);
    }

    public void displaySupplementMenu() {
        while (true) {
            System.out.println("\nSupplement Store and Information");
            System.out.println("1. View All Supplements");
            System.out.println("2. View Supplements by Category");
            System.out.println("3. View Supplement Details");
            System.out.println("4. Purchase Supplements");
            System.out.println("5. Back to Main Menu");
            System.out.print("Choose an option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        displayAllSupplements();
                        break;
                    case 2:
                        displaySupplementCategories();
                        break;
                    case 3:
                        displaySupplementDetails();
                        break;
                    case 4:
                        handlePurchase();
                        break;
                    case 5:
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private void displayAllSupplements() {
        List<Supplement> supplements = supplementController.getAllSupplements();
        if (supplements.isEmpty()) {
            System.out.println("No supplements available.");
            return;
        }

        System.out.println("\n=== Available Supplements ===");
        displaySupplementsList(supplements);
    }

    private void displaySupplementCategories() {
        System.out.println("\nAvailable Categories:");
        System.out.println("1. Protein");
        System.out.println("2. Pre-Workout");
        System.out.println("3. Vitamins");
        System.out.println("4. Amino Acids");
        System.out.print("Select a category: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            String category = switch (choice) {
                case 1 -> "Protein";
                case 2 -> "Pre-Workout";
                case 3 -> "Vitamins";
                case 4 -> "Amino Acids";
                default -> null;
            };

            if (category != null) {
                List<Supplement> supplements = supplementController.getSupplementsByCategory(category);
                if (supplements.isEmpty()) {
                    System.out.println("No supplements available in this category.");
                    return;
                }
                System.out.println("\n=== " + category + " Supplements ===");
                displaySupplementsList(supplements);
            } else {
                System.out.println("Invalid category selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
    }

    private void displaySupplementsList(List<Supplement> supplements) {
        System.out.println("\nID  | Name                 | Category    | Price  | Available");
        System.out.println("----+----------------------+-------------+--------+-----------");
        for (Supplement supp : supplements) {
            System.out.printf("%-4d| %-20s | %-11s | $%-6.2f| %d%n",
                    supp.getSupplementID(),
                    supp.getName(),
                    supp.getCategory(),
                    supp.getPrice(),
                    supp.getQuantityAvailable());
        }
    }

    private void displaySupplementDetails() {
        System.out.print("\nEnter supplement ID for details: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Supplement supplement = supplementController.getSupplementInfo(id);

            if (supplement != null) {
                System.out.println("\n=== Supplement Details ===");
                System.out.println("Name: " + supplement.getName());
                System.out.println("Category: " + supplement.getCategory());
                System.out.println("Price: $" + String.format("%.2f", supplement.getPrice()));
                System.out.println("Available: " + supplement.getQuantityAvailable());
                System.out.println("\nDescription:");
                System.out.println(supplement.getDescription());
            } else {
                System.out.println("Supplement not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid ID.");
        }
    }
    private void handlePurchase() {
        System.out.print("\nEnter supplement ID to purchase: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Supplement supplement = supplementController.getSupplementInfo(id);

            if (supplement != null) {
                System.out.println("Selected: " + supplement.getName());
                System.out.println("Price: $" + String.format("%.2f", supplement.getPrice()));
                System.out.println("Available quantity: " + supplement.getQuantityAvailable());

                if (supplement.getQuantityAvailable() == 0) {
                    System.out.println("Sorry, this supplement is out of stock.");
                    return;
                }

                System.out.print("Enter quantity to purchase: ");
                int quantity = Integer.parseInt(scanner.nextLine().trim());

                if (quantity <= 0) {
                    System.out.println("Invalid quantity.");
                    return;
                }

                if (quantity > supplement.getQuantityAvailable()) {
                    System.out.println("Not enough stock available.");
                    return;
                }

                double totalCost = supplement.getPrice() * quantity;
                System.out.printf("Total cost: $%.2f%n", totalCost);

                System.out.print("Enter delivery location: ");
                String deliveryLocation = scanner.nextLine().trim();
                if (deliveryLocation.isEmpty()) {
                    System.out.println("Delivery location cannot be empty.");
                    return;
                }

                // Payment method selection
                System.out.println("\nSelect Payment Method:");
                System.out.println("1. Credit Card");
                System.out.println("2. Debit Card");
                System.out.println("3. PayPal");
                System.out.print("Choose payment method: ");

                int paymentChoice = Integer.parseInt(scanner.nextLine().trim());
                String paymentType = switch (paymentChoice) {
                    case 1 -> "Credit Card";
                    case 2 -> "Debit Card";
                    case 3 -> "PayPal";
                    default -> null;
                };

                if (paymentType == null) {
                    System.out.println("Invalid payment method selected.");
                    return;
                }

                System.out.println("\nOrder Summary:");
                System.out.printf("Item: %s x%d%n", supplement.getName(), quantity);
                System.out.printf("Total: $%.2f%n", totalCost);
                System.out.printf("Delivery to: %s%n", deliveryLocation);
                System.out.printf("Payment Method: %s%n", paymentType);

                System.out.print("Confirm purchase (yes/no): ");

                if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                    List<OrderSupplement> orderItems = new ArrayList<>();
                    orderItems.add(new OrderSupplement(0, id, quantity));

                    int orderID = orderController.createOrder(orderItems, deliveryLocation, paymentType);

                    if (orderID > 0) {
                        System.out.println("Purchase successful!");
                        System.out.printf("Order ID: %d%n", orderID);
                        System.out.printf("Total paid: $%.2f%n", totalCost);
                    } else {
                        System.out.println("Error processing purchase or payment.");
                    }
                } else {
                    System.out.println("Purchase cancelled.");
                }
            } else {
                System.out.println("Supplement not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid numbers.");
        }
    }
}