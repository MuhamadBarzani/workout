package supplement;

import server.FileManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class SupplementModel {
    private static final String SUPPLEMENTS_FILE = "supplements.json";
    private static final String SUPPLEMENT_COUNTER = "supplement_counter.txt";
    private final FileManager fileManager;

    public SupplementModel() {
        this.fileManager = FileManager.getInstance();
    }

    public List<Supplement> getAllSupplements() {
        List<Supplement> supplements = fileManager.loadList(SUPPLEMENTS_FILE, Supplement[].class);
        if (supplements == null) {
            return new ArrayList<>();
        }

        // Sort by category and name
        return supplements.stream()
                .sorted(Comparator.comparing(Supplement::getCategory)
                        .thenComparing(Supplement::getName))
                .collect(Collectors.toList());
    }

    public List<Supplement> getSupplementsByCategory(String category) {
        List<Supplement> allSupplements = getAllSupplements();
        return allSupplements.stream()
                .filter(s -> s.getCategory().equals(category))
                .sorted(Comparator.comparing(Supplement::getName))
                .collect(Collectors.toList());
    }

    public Supplement getSupplementById(int supplementID) {
        return getAllSupplements().stream()
                .filter(s -> s.getSupplementID() == supplementID)
                .findFirst()
                .orElse(null);
    }

    public boolean updateQuantity(int supplementID, int newQuantity) {
        try {
            List<Supplement> supplements = getAllSupplements();
            boolean updated = false;

            for (Supplement supplement : supplements) {
                if (supplement.getSupplementID() == supplementID) {
                    // Create new supplement with updated quantity
                    Supplement updatedSupplement = new Supplement(
                            supplementID,
                            supplement.getName(),
                            supplement.getCategory(),
                            supplement.getPrice(),
                            newQuantity,
                            supplement.getDescription()
                    );

                    // Replace old supplement with updated one
                    int index = supplements.indexOf(supplement);
                    supplements.set(index, updatedSupplement);
                    updated = true;
                    break;
                }
            }

            if (updated) {
                fileManager.saveData(SUPPLEMENTS_FILE, supplements);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error updating supplement quantity: " + e.getMessage());
            return false;
        }
    }

    public boolean addSupplement(Supplement supplement) {
        try {
            List<Supplement> supplements = getAllSupplements();

            // Generate new supplement ID
            int supplementId = fileManager.getNextId(SUPPLEMENT_COUNTER);

            // Create new supplement with generated ID
            Supplement newSupplement = new Supplement(
                    supplementId,
                    supplement.getName(),
                    supplement.getCategory(),
                    supplement.getPrice(),
                    supplement.getQuantityAvailable(),
                    supplement.getDescription()
            );

            supplements.add(newSupplement);
            fileManager.saveData(SUPPLEMENTS_FILE, supplements);
            return true;
        } catch (Exception e) {
            System.out.println("Error adding supplement: " + e.getMessage());
            return false;
        }
    }
}