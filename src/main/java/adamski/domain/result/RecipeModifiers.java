package adamski.domain.result;

import adamski.domain.Recipe;
import adamski.domain.route.RecipeChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

public final class RecipeModifiers {
    private RecipeModifiers() {
    }

    /**
     * Rebuilds the chains with each recipe passed through the provided modifiers in list order.
     */
    public static List<RecipeChain> apply(List<RecipeChain> chains, List<UnaryOperator<Recipe>> modifiers) {
        final List<RecipeChain> applied = new ArrayList<>(chains.size());

        for (RecipeChain chain : chains) {
            final List<Recipe> recipes = new ArrayList<>(chain.getRecipes().size());

            for (Recipe recipe : chain.getRecipes()) {
                Recipe modified = recipe;
                for (UnaryOperator<Recipe> modifier : modifiers) {
                    modified = modifier.apply(modified);
                }
                recipes.add(modified);
            }

            applied.add(new RecipeChain(chain.getRootItemId(), recipes));
        }

        return applied;
    }

    /**
     * Replaces each recipe's output quantity.
     */
    public static UnaryOperator<Recipe> outputOverrides(Map<Integer, Double> outputByRecipeId) {
        return recipe -> {
            final Double output = outputByRecipeId.get(recipe.getId());
            return output == null ? recipe : recipe.withOutputQuantity(output);
        };
    }

    /**
     * Scales the specified recipe's ingredient's multiplicatively without affecting the output.
     * Scaling the primary changes the runs per banked item, and so the xp;
     * scaling a secondary only changes demand.
     *
     * @throws IllegalArgumentException when applied to a recipe that does not use a listed item
     */
    public static UnaryOperator<Recipe> inputScaling(Map<Integer, Set<Integer>> itemsByRecipeId, double factor) {
        return recipe -> {
            Recipe scaled = recipe;
            for (int itemId : itemsByRecipeId.getOrDefault(recipe.getId(), Set.of())) {
                scaled = scaled.withIngredientScaled(itemId, factor);
            }
            return scaled;
        };
    }

    /**
     * Scales each recipe's output multiplicatively without affecting inputs and secondaries.
     */
    public static UnaryOperator<Recipe> outputScaling(Map<Integer, Double> factorByRecipeId) {
        return recipe -> {
            final Double factor = factorByRecipeId.get(recipe.getId());
            return factor == null ? recipe : recipe.withOutputQuantity(recipe.getOutput().getQuantity() * factor);
        };
    }
}
