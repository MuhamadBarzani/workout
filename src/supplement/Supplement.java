// Supplement.java
package supplement;

public class Supplement {
    private int supplementID;
    private String name;
    private String category;
    private double price;
    private int quantityAvailable;
    private String description;

    // Constructors
    public Supplement() {}

    public Supplement(int supplementID, String name, String category, double price,
                      int quantityAvailable, String description) {
        this.supplementID = supplementID;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantityAvailable = quantityAvailable;
        this.description = description;
    }

    // Getters and Setters
    public int getSupplementID() {
        return supplementID;
    }

    public void setSupplementID(int supplementID) {
        this.supplementID = supplementID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(int quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
