package adamski.app;

import adamski.data.SecondaryPrecursors;
import lombok.Value;
import lombok.With;

import java.util.Map;

@Value
@With
public class HerbloreSettings {
    public static final HerbloreSettings DEFAULT = new HerbloreSettings(true, true,
            SecondaryPrecursors.DEFAULT_LAVA_SHARDS_PER_SCALE, 1, Map.of(), true, false, false);

    /**
     * Whether secondaries made by a recipe chain count towards the secondaries other chains need
     * e.g. super attacks and super combat potions.
     */
    boolean chainProductsCounted;

    /**
     * Whether items that are refined into secondaries - bird nests for crushed nests - are considered
     * alongside the secondary itself.
     */
    boolean precursorsCounted;

    /**
     * How many lava shards one lava scale grinds into.
     */
    double lavaShardsPerScale;

    /**
     * How often cutting a leaping sturgeon gives caviar, from 0 to 1.
     */
    double caviarSuccessRate;

    /**
     * Yield overrides for harvesting seeds.
     */
    Map<Integer, Double> seedYieldByItemId;

    /**
     * Whether held seeds and coral frags are considered in calculations.
     */
    boolean seedsCounted;

    /**
     * Whether prescription goggles are worn.
     */
    boolean prescriptionGogglesUsed;

    /**
     * Whether the alchemist's amulet is used.
     */
    boolean alchemistsAmuletUsed;
}
