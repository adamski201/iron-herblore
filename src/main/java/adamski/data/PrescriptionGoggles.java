package adamski.data;

import net.runelite.api.gameval.ItemID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Which ingredient is saved per recipe when using Prescription Goggles.
 */
public final class PrescriptionGoggles {
    public static final double SAVE_CHANCE = 0.1;

    private static final Map<Integer, Set<Integer>> SAVED_ITEMS_BY_RECIPE_ID = new HashMap<>();

    static {
        saves(32, ItemID.TOADFLAX);
        saves(33, ItemID.IRIT_LEAF);
        saves(34, ItemID.CACTUS_SPINE);
        saves(35, ItemID.NIGHTSHADE);
        saves(39, ItemID.EYE_OF_NEWT);
        saves(40, ItemID.UNICORN_HORN_DUST);
        saves(41, ItemID.LIMPWURT_ROOT);
        saves(42, ItemID.RED_SPIDERS_EGGS);
        saves(43, ItemID.CHOCOLATE_DUST);
        saves(44, ItemID.WHITE_BERRIES);
        saves(45, ItemID.TOADS_LEGS);
        saves(46, ItemID.GROUND_DESERT_GOAT_HORN);
        saves(47, ItemID.SNAPE_GRASS);
        saves(48, ItemID.EYE_OF_NEWT);
        saves(49, ItemID.UNICORN_HORN_DUST);
        saves(50, ItemID.SNAPE_GRASS);
        saves(51, ItemID.MORTMYREMUSHROOM);
        saves(52, ItemID.HUNTINGBEAST_SABRETEETH_DUST);
        saves(53, ItemID.LIMPWURT_ROOT);
        saves(54, ItemID.DRAGON_SCALE_DUST);
        saves(55, ItemID.RED_SPIDERS_EGGS);
        saves(56, ItemID.WHITE_BERRIES);
        saves(57, ItemID.DRAGON_SCALE_DUST);
        saves(58, ItemID.WINE_OF_ZAMORAK);
        saves(59, ItemID.CACTUS_POTATO);
        saves(60, ItemID.JANGERBERRIES);
        saves(61, ItemID.CRUSHED_BIRD_NEST);
        saves(62, ItemID.NIHIL_DUST);
        saves(63, ItemID.LILY_OF_THE_SANDS);
        saves(64, ItemID.WINE_OF_ZAMORAK);
        saves(65, ItemID.CACTUS_POTATO);
        saves(66, ItemID.YEW_ROOTS);
        saves(67, ItemID.MAGIC_ROOTS);
        saves(68, ItemID.RED_SPIDERS_EGGS);
        saves(69, ItemID.POISONIVY_BERRIES);
        saves(70, ItemID.ALDARIUM);
        saves(71, ItemID.ALDARIUM);
        saves(73, ItemID.FOSSIL_VOLCANIC_ASH);
        saves(74, ItemID.GARLIC, ItemID.SILVER_DUST);
        saves(75, ItemID.HADDOCK_EYE);
        saves(76, ItemID.CRAB_PASTE);
        saves(77, ItemID.RAINBOW_CRAB_PASTE);
        saves(78, ItemID.SQUID_PASTE);
        saves(79, ItemID.COTTON_YARN);
        saves(80, ItemID.DEMONIC_TALLOW);
        saves(83, ItemID.CRUSHED_DRAGON_BONES);
        saves(84, ItemID.UNICORN_HORN_DUST, ItemID.NAIL_BEAST_NAIL);
        saves(87, ItemID.AMYLASE);
        saves(88, ItemID.YELLOW_FIN);
        saves(89, ItemID.MARLIN_SCALES);
        saves(90, ItemID.SNAKEBOSS_SCALE);
        saves(91, ItemID.ARAXYTE_VENOM_SACK);
        saves(92, ItemID.LAVA_SHARD);
        saves(93, ItemID.LAVA_SHARD);
        saves(94, ItemID.ANCIENT_ESSENCE);
        saves(136, ItemID.CRUSHED_DRAGON_BONES);
        saves(81, ItemID.TORSTOL);
        saves(82, ItemID.TORSTOLVIAL);
        saves(85, ItemID.TORSTOL);

        for (int recipeId = 95; recipeId <= 117; recipeId++) {
            saves(recipeId, ItemID.BRUT_CAVIAR);
        }
    }

    private PrescriptionGoggles() {
    }

    public static Map<Integer, Set<Integer>> savedItemsByRecipeId() {
        return Collections.unmodifiableMap(SAVED_ITEMS_BY_RECIPE_ID);
    }

    private static void saves(int recipeId, Integer... itemIds) {
        SAVED_ITEMS_BY_RECIPE_ID.put(recipeId, Set.of(itemIds));
    }
}
