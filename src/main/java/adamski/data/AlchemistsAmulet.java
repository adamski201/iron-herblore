package adamski.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Which recipes a charged alchemist's amulet can add a dose to, and the potion sizes each can be
 * made in.
 */
public final class AlchemistsAmulet {
    public static final double EXTRA_DOSE_CHANCE = 0.15;

    /**
     * The size switching the amulet on picks - every recipe can be made in 3-dose potions, and it
     * is what most players decant to.
     */
    public static final int DEFAULT_DOSES_PER_POTION = 3;

    private static final List<Integer> WHOLE_POTION = List.of(3);
    private static final List<Integer> UPGRADE = List.of(3, 2, 1);

    private static final Map<Integer, List<Integer>> DOSES_PER_POTION_OPTIONS_BY_RECIPE_ID = new LinkedHashMap<>();

    static {
        for (int recipeId : new int[]{
                39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, // attack to super strength
                55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65,                 // super restore to battlemage
                70, 71, 72, 73,                                             // goading to compost potion
                75, 76, 77,                                                 // coral potions
                79,                                                         // haemostatic dressing
                80}) {                                                      // surge potion
            DOSES_PER_POTION_OPTIONS_BY_RECIPE_ID.put(recipeId, WHOLE_POTION);
        }

        for (int recipeId : new int[]{
                87, 88, 89, // stamina, extreme energy, extended stamina
                90, 91,     // anti-venom, extended anti-venom+
                92, 93,     // extended antifire, extended super antifire
                94}) {      // forgotten brew
            DOSES_PER_POTION_OPTIONS_BY_RECIPE_ID.put(recipeId, UPGRADE);
        }
    }

    private AlchemistsAmulet() {
    }

    /**
     * @return recipe id to the potion sizes it can be made in with the amulet, largest first.
     */
    public static Map<Integer, List<Integer>> dosesPerPotionOptionsByRecipeId() {
        return Collections.unmodifiableMap(DOSES_PER_POTION_OPTIONS_BY_RECIPE_ID);
    }
}
