package adamski.domain.result;

import lombok.Value;

import java.util.List;

/**
 * One potion the alchemist's amulet could be used on - one the bank makes, and the amulet can add
 * a dose to.
 */
@Value
public class AlchemistAmuletOption {
    int recipeId;

    int productItemId;

    /**
     * The potion sizes it can be made in, largest first - just 3 for a whole potion, a choice for an
     * upgrade.
     */
    List<Integer> dosesPerPotionOptions;

    /**
     * The size chosen, or null if the amulet is not used on it.
     */
    Integer dosesPerPotion;

    public boolean isUsed() {
        return dosesPerPotion != null;
    }
}
