package adamski.domain.result;

import adamski.domain.Ingredient;
import adamski.domain.ItemQuantities;
import adamski.domain.Recipe;
import adamski.domain.RecipeTag;
import adamski.domain.route.RecipeChain;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class RecipeModifiersTest {
    private static final double DELTA = 1e-9;

    private static final int PRIMARY = 1;
    private static final int SECONDARY = 2;
    private static final int OUTPUT = 3;

    /**
     * 1 primary and 2 secondaries make 1 output, worth 10xp a run.
     */
    private static final Recipe MIX = new Recipe(10, new Ingredient(PRIMARY, 1),
            new Ingredient[]{new Ingredient(SECONDARY, 2)}, new Ingredient(OUTPUT, 1), RecipeTag.POTION, 10f, 0);

    // --- the chain walk ----------------------------------------------

    @Test
    public void modifiersRunInListOrder() {
        final UnaryOperator<Recipe> doubleOutput = recipe -> recipe.withOutputQuantity(recipe.getOutput().getQuantity() * 2);
        final UnaryOperator<Recipe> setOutput = recipe -> recipe.withOutputQuantity(5);

        assertEquals(10.0, outputOf(List.of(setOutput, doubleOutput)), DELTA);
        assertEquals(5.0, outputOf(List.of(doubleOutput, setOutput)), DELTA);
    }

    @Test
    public void aStoppedChainKeepsItsRoot() {
        final RecipeChain stopped = new RecipeChain(5, Collections.emptyList());

        final List<RecipeChain> applied = RecipeModifiers.apply(List.of(stopped),
                List.of(RecipeModifiers.yieldOverrides(Map.of(10, 0.85))));

        assertEquals(5, applied.get(0).getRootItemId());
    }

    // --- yield overrides ---------------------------------------------

    @Test
    public void aYieldReplacesItsRecipesOutput() {
        final Recipe yielded = RecipeModifiers.yieldOverrides(Map.of(10, 0.85)).apply(MIX);

        assertEquals(0.85, yielded.getOutput().getQuantity(), DELTA);
        assertEquals(OUTPUT, yielded.getOutput().getItemId());
    }

    @Test
    public void aRecipeWithNoYieldIsLeftAlone() {
        assertSame(MIX, RecipeModifiers.yieldOverrides(Map.of(99, 0.85)).apply(MIX));
    }

    @Test
    public void aYieldScalesTheOutputButNotTheXp() {
        final ChainResult result = calculate(RecipeModifiers.yieldOverrides(Map.of(10, 0.25)), 10);

        assertEquals(2.5, result.getOutputQuantity(), DELTA);
        assertEquals(100.0, result.getXp(), DELTA); // the attempts still run
    }

    // --- ingredient savings ------------------------------------------

    @Test
    public void savingThePrimaryMakesMoreFromWhatIsBanked() {
        final ChainResult result = calculate(RecipeModifiers.ingredientSavings(Map.of(10, Set.of(PRIMARY)), 0.1), 9);

        assertEquals(10.0, result.getOutputQuantity(), DELTA); // 9 / 0.9 runs
        assertEquals(100.0, result.getXp(), DELTA);
        assertEquals(20.0, result.getSecondaryDemand().get(SECONDARY), DELTA); // more runs, more secondaries
    }

    @Test
    public void savingASecondaryLowersDemandButNotXp() {
        final ChainResult result = calculate(RecipeModifiers.ingredientSavings(Map.of(10, Set.of(SECONDARY)), 0.1), 10);

        assertEquals(10.0, result.getOutputQuantity(), DELTA);
        assertEquals(100.0, result.getXp(), DELTA);
        assertEquals(18.0, result.getSecondaryDemand().get(SECONDARY), DELTA);
    }

    @Test
    public void aRecipeWithNothingSavedIsLeftAlone() {
        assertSame(MIX, RecipeModifiers.ingredientSavings(Map.of(99, Set.of(PRIMARY)), 0.1).apply(MIX));
    }

    @Test(expected = IllegalArgumentException.class)
    public void savingAnItemTheRecipeDoesNotUseIsAnError() {
        RecipeModifiers.ingredientSavings(Map.of(10, Set.of(OUTPUT)), 0.1).apply(MIX);
    }

    private static double outputOf(List<UnaryOperator<Recipe>> modifiers) {
        return RecipeModifiers.apply(List.of(new RecipeChain(List.of(MIX))), modifiers)
                .get(0).getRecipes().get(0).getOutput().getQuantity();
    }

    private static ChainResult calculate(UnaryOperator<Recipe> modifier, double banked) {
        final List<RecipeChain> chains = RecipeModifiers.apply(List.of(new RecipeChain(List.of(MIX))), List.of(modifier));

        return ChainResultCalculator.calculate(chains, ItemQuantities.of(Map.of(PRIMARY, banked))).get(0);
    }
}
