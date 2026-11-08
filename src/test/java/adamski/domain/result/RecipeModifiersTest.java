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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
                List.of(RecipeModifiers.outputOverrides(Map.of(10, 0.85))));

        assertEquals(5, applied.get(0).getRootItemId());
    }

    // --- output overrides --------------------------------------------

    @Test
    public void anOverrideReplacesItsRecipesOutput() {
        final Recipe overridden = RecipeModifiers.outputOverrides(Map.of(10, 0.85)).apply(MIX);

        assertEquals(0.85, overridden.getOutput().getQuantity(), DELTA);
        assertEquals(OUTPUT, overridden.getOutput().getItemId());
    }

    @Test
    public void aRecipeWithNoOverrideIsLeftAlone() {
        assertSame(MIX, RecipeModifiers.outputOverrides(Map.of(99, 0.85)).apply(MIX));
    }

    @Test
    public void anOverrideChangesTheOutputButNotTheXp() {
        final ChainResult result = calculate(RecipeModifiers.outputOverrides(Map.of(10, 0.25)), 10);

        assertEquals(2.5, result.getOutputQuantity(), DELTA);
        assertEquals(100.0, result.getXp(), DELTA); // the attempts still run
    }

    // --- input scaling ----------------------------------------------

    @Test
    public void scalingThePrimaryDownMakesMoreFromWhatIsBanked() {
        final ChainResult result = calculate(RecipeModifiers.inputScaling(Map.of(10, Set.of(PRIMARY)), 0.9), 9);

        assertEquals(10.0, result.getOutputQuantity(), DELTA); // 9 / 0.9 runs
        assertEquals(100.0, result.getXp(), DELTA);
        assertEquals(20.0, result.getSecondaryDemand().get(SECONDARY), DELTA); // more runs, more secondaries
    }

    @Test
    public void scalingASecondaryChangesDemandButNotXp() {
        final ChainResult result = calculate(RecipeModifiers.inputScaling(Map.of(10, Set.of(SECONDARY)), 0.9), 10);

        assertEquals(10.0, result.getOutputQuantity(), DELTA);
        assertEquals(100.0, result.getXp(), DELTA);
        assertEquals(18.0, result.getSecondaryDemand().get(SECONDARY), DELTA);
    }

    @Test
    public void aRecipeWithNothingListedIsLeftAlone() {
        assertSame(MIX, RecipeModifiers.inputScaling(Map.of(99, Set.of(PRIMARY)), 0.9).apply(MIX));
    }

    @Test(expected = IllegalArgumentException.class)
    public void scalingAnItemTheRecipeDoesNotUseIsAnError() {
        RecipeModifiers.inputScaling(Map.of(10, Set.of(OUTPUT)), 0.9).apply(MIX);
    }

    // --- output scaling ---------------------------------------------

    @Test
    public void outputScalingMultipliesWhatARunMakesButUsesNothingMore() {
        final ChainResult result = calculate(RecipeModifiers.outputScaling(Map.of(10, 1.15)), 10);

        assertEquals(11.5, result.getOutputQuantity(), DELTA);
        assertEquals(100.0, result.getXp(), DELTA); // no extra xp for the extra dose
        assertEquals(20.0, result.getSecondaryDemand().get(SECONDARY), DELTA);
    }

    @Test
    public void outputScalingScalesAnOverrideRatherThanReplacingIt() {
        assertEquals(0.5 * 1.15, outputOf(List.of(
                RecipeModifiers.outputOverrides(Map.of(10, 0.5)),
                RecipeModifiers.outputScaling(Map.of(10, 1.15)))), DELTA);
    }

    @Test
    public void aRecipeWithNoFactorIsLeftAlone() {
        assertSame(MIX, RecipeModifiers.outputScaling(Map.of(99, 1.15)).apply(MIX));
    }

    // --- runs ---------------------------------------------------------

    @Test
    public void aChainSaysWhetherItRunsARecipe() {
        final ChainResult result = calculate(UnaryOperator.identity(), 10);

        assertTrue(result.runsAnyOf(Set.of(10, 99)));
        assertFalse(result.runsAnyOf(Set.of(99)));
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
