package adamski.ui;

import lombok.Value;

import java.util.List;

/**
 * One row as the panel shows it, with every name already resolved. Built on the client thread,
 * where ItemManager can be used, and drawn on the EDT, where it cannot.
 */
@Value
class RowView {
    /**
     * What a product is picked against - the app needs it back. Quantities are in the potion size
     * chosen to show, not doses.
     */
    int rootItemId;
    String rootItemName;
    double rootQuantity;

    /**
     * The item whose sprite stands for the root - for a potion, the size potions are shown as.
     */
    int rootIconItemId;


    double xp;

    int productItemId;
    int productIconItemId;

    boolean switchedOff;
    String productName;
    double outputQuantity;
    boolean amuletUsed;

    List<ProductOption> productOptions;

    List<Line> xpByItem;

    List<RecipeLine> xpByRecipe;

    List<Line> secondaryDemand;

    @Value
    static class ProductOption {
        int itemId;
        int iconItemId;
        String name;
    }

    @Value
    static class Line {
        double amount;
        String name;
    }

    @Value
    static class RecipeLine {
        double xp;
        String inputName;
        String outputName;
    }
}
