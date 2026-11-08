package adamski.domain.result;

import adamski.domain.Recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The alchemist's amulet: what it multiplies each recipe's output by, and what that costs and could
 * be used on, once the chains are calculated.
 * <p>
 * Its chance is of one extra dose per potion, whatever the potion's size, so a run gains
 * chance / doses per potion of what it makes: a whole 3-dose potion gains 5%, an upgrade made in
 * 1-dose potions 15%.
 */
public final class AlchemistAmuletCalculator {
    private AlchemistAmuletCalculator() {
    }

    /**
     * @param dosesPerPotionByRecipeId the size of potion each recipe is made in, for those the amulet
     *                                 is used on
     * @return recipe id to what its output is multiplied by, for {@code RecipeModifiers.outputScaling}
     */
    public static Map<Integer, Double> factorsByRecipeId(Map<Integer, Integer> dosesPerPotionByRecipeId,
                                                         double extraDoseChance) {
        final Map<Integer, Double> factors = new HashMap<>();
        dosesPerPotionByRecipeId.forEach((recipeId, dosesPerPotion) ->
                factors.put(recipeId, 1 + extraDoseChance / dosesPerPotion));
        return factors;
    }

    /**
     * The extra doses the amulet makes, one charge each. A scaled run makes its base output times
     * the factor, so the amulet's share of it is 1 - 1 / factor.
     *
     * @param factorsByRecipeId as {@link #factorsByRecipeId} made them, for the chains given
     */
    public static double sumCharges(List<ChainResult> chainResults, Map<Integer, Double> factorsByRecipeId) {
        double charges = 0;

        for (ChainResult chain : chainResults) {
            for (RecipeRun run : chain.getRecipeRuns()) {
                final Double factor = factorsByRecipeId.get(run.getRecipe().getId());
                if (factor != null) charges += run.getOutputQuantity() * (1 - 1 / factor);
            }
        }

        return charges;
    }

    /**
     * @param dosesPerPotionOptionsByRecipeId the recipes the amulet works on and the sizes each can
     *                                        be made in
     * @param dosesPerPotionByRecipeId        the sizes chosen, for those it is used on
     * @return every recipe the chains run that the amulet works on, in the order the options are
     * listed
     */
    public static List<AlchemistAmuletOption> findOptions(List<ChainResult> chainResults,
                                                          Map<Integer, List<Integer>> dosesPerPotionOptionsByRecipeId,
                                                          Map<Integer, Integer> dosesPerPotionByRecipeId) {
        final Map<Integer, Recipe> recipesRun = new HashMap<>();
        for (ChainResult chain : chainResults) {
            for (RecipeRun run : chain.getRecipeRuns()) {
                if (run.getRuns() > 0) recipesRun.put(run.getRecipe().getId(), run.getRecipe());
            }
        }

        final List<AlchemistAmuletOption> options = new ArrayList<>();
        dosesPerPotionOptionsByRecipeId.forEach((recipeId, sizes) -> {
            final Recipe recipe = recipesRun.get(recipeId);
            if (recipe == null) return;

            options.add(new AlchemistAmuletOption(recipeId, recipe.getOutput().getItemId(), sizes,
                    dosesPerPotionByRecipeId.get(recipeId)));
        });

        return options;
    }
}
