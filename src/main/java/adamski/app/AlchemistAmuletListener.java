package adamski.app;

import java.util.Map;

/**
 * Told when the user changes which potions the alchemist's amulet is used on.
 */
public interface AlchemistAmuletListener {
    /**
     * @param dosesPerPotionByRecipeId the size of potion each recipe the amulet is used on is made in
     */
    void onAmuletConfigurationChanged(Map<Integer, Integer> dosesPerPotionByRecipeId);
}
