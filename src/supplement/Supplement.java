package supplement;

import java.io.Serializable;

public class Supplement implements Serializable {
    private static final long serialVersionUID = 1L;

    private int supplementID;
    private String name;
    private String category;
    private double price;
    private int quantityAvailable;
    private String description;

    // Default constructor for serialization
    public Supplement() {}

    public Supplement(int supplementID, String name, String category,
                      double price, int quantityAvailable, String description) {
        this.supplementID = supplementID;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantityAvailable = quantityAvailable;
        this.description = description;
    }

    // Getters and setters
    public int getSupplementID() { return supplementID; }

    public String getName() { return name; }

    public String getCategory() { return category; }

    public double getPrice() { return price; }

    public int getQuantityAvailable() { return quantityAvailable; }

    public String getDescription() { return description; }
}