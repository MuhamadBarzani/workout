package order;

public class OrderSupplement {
    private int orderID;        // Foreign key to Order
    private int supplementID;    // Foreign key to Supplement
    private int quantityOrdered; // Number of supplements ordered

    public int getSupplementID() {
        return supplementID;
    }

    public int getQuantityOrdered() {
        return quantityOrdered;
    }

    public OrderSupplement(int orderID, int supplementID, int quantityOrdered) {
        this.orderID = orderID;
        this.supplementID = supplementID;
        this.quantityOrdered = quantityOrdered;
    }
}
