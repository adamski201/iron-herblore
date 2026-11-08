package adamski.domain.result;

import adamski.domain.Ingredient;
import adamski.domain.ItemQuantities;
import adamski.domain.Recipe;
import adamski.domain.RecipeTag;
import adamski.domain.route.RecipeChain;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AlchemistAmuletCalculatorTest {
    private static final double DELTA = 1e-9;
    private static final double CHANCE = 0.15;

    private static final int UNF = 1;
    private static final int POTION = 2;
    private static final int UPGRADED = 3;

    /**
     * An unf makes a whole 3-dose potion.
     */
    private static final Recipe WHOLE = new Recipe(10, new Ingredient(UNF, 1), new Ingredient[0],
            new Ingredient(POTION, 3), RecipeTag.POTION, 10f, 0);

    /**
     * The potion is upgraded a dose at a time.
     */
    private static final Recipe UPGRADE = new Recipe(11, new Ingredient(POTION, 1), new Ingredient[0],
            new Ingredient(UPGRADED, 1), RecipeTag.POTION, 1f, 0);

    /**
     * The chance is per potion, so the smaller the potion upgraded, the more each dose gains.
     */
    @Test
    public void theSmallerThePotionTheMoreADoseGains() {
        assertEquals(1.05, factorOf(3), DELTA);
        assertEquals(1.075, factorOf(2), DELTA);
        assertEquals(1.15, factorOf(1), DELTA);
    }

    @Test
    public void aRecipeGivenNoSizeHasNoFactor() {
        assertEquals(Map.of(), AlchemistAmuletCalculator.factorsByRecipeId(Map.of(), CHANCE));
    }

    /**
     * 10 unfs make 10 potions; the amulet adds 0.15 of a dose to each, one charge per dose.
     */
    @Test
    public void theChargesAreTheExtraDosesMade() {
        final Map<Integer, Double> factors = AlchemistAmuletCalculator.factorsByRecipeId(Map.of(10, 3), CHANCE);

        assertEquals(1.5, AlchemistAmuletCalculator.sumCharges(calculate(factors, 10), factors), DELTA);
    }

    @Test
    public void aRecipeTheChainsDoNotRunIsNotAnOption() {
        final Map<Integer, List<Integer>> sizes = new LinkedHashMap<>();
        sizes.put(10, List.of(3));
        sizes.put(99, List.of(3));

        final List<AlchemistAmuletOption> options = AlchemistAmuletCalculator.findOptions(
                calculate(Map.of(), 10), sizes, Map.of());

        assertEquals(1, options.size());
        assertEquals(10, options.get(0).getRecipeId());
        assertEquals(POTION, options.get(0).getProductItemId());
        assertNull(options.get(0).getDosesPerPotion());
    }

    @Test
    public void anOptionCarriesTheSizeChosen() {
        final List<AlchemistAmuletOption> options = AlchemistAmuletCalculator.findOptions(
                calculate(Map.of(), 10), Map.of(11, List.of(3, 2, 1)), Map.of(11, 2));

        assertEquals(Integer.valueOf(2), options.get(0).getDosesPerPotion());
    }

    private static double factorOf(int dosesPerPotion) {
        return AlchemistAmuletCalculator.factorsByRecipeId(Map.of(10, dosesPerPotion), CHANCE).get(10);
    }

    private static List<ChainResult> calculate(Map<Integer, Double> factors, double unfs) {
        final List<RecipeChain> chains = RecipeModifiers.apply(List.of(new RecipeChain(List.of(WHOLE, UPGRADE))),
                List.of(RecipeModifiers.outputScaling(factors)));

        return ChainResultCalculator.calculate(chains, ItemQuantities.of(Map.of(UNF, unfs)));
    }
}
