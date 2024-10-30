package supplement;

import java.util.List;

public class SupplementController {
    private SupplementModel model;

    public SupplementController(SupplementModel model) {
        this.model = model;
    }

    public List<Supplement> getAllSupplements() {
        return model.getAllSupplements();
    }

    public List<Supplement> getSupplementsByCategory(String category) {
        return model.getSupplementsByCategory(category);
    }

    public Supplement getSupplementInfo(int supplementID) {
        return model.getSupplementById(supplementID);
    }

    public boolean purchaseSupplement(int supplementID, int quantity) {
        Supplement supplement = model.getSupplementById(supplementID);
        if (supplement == null) {
            return false;
        }

        if (supplement.getQuantityAvailable() < quantity) {
            return false;
        }

        int newQuantity = supplement.getQuantityAvailable() - quantity;
        return model.updateQuantity(supplementID, newQuantity);
    }
}
