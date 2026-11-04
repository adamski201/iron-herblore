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
     * Swaps the recipe's yield with the provided amount.
     */
    public static UnaryOperator<Recipe> yieldOverrides(Map<Integer, Double> yieldByRecipeId) {
        return recipe -> {
            final Double yield = yieldByRecipeId.get(recipe.getId());
            return yield == null ? recipe : recipe.withOutputQuantity(yield);
        };
    }

    /**
     * Each listed ingredient is used {@code 1 - saveChance} times as much - a chance to keep it,
     * as an average. Saving a primary means more runs per banked item, so more xp; saving a
     * secondary only lowers demand. Which role the item has does not matter here.
     *
     * @throws IllegalArgumentException when applied to a recipe that does not use a listed item
     */
    public static UnaryOperator<Recipe> ingredientSavings(Map<Integer, Set<Integer>> savedItemsByRecipeId,
                                                          double saveChance) {
        return recipe -> {
            Recipe saved = recipe;
            for (int itemId : savedItemsByRecipeId.getOrDefault(recipe.getId(), Set.of())) {
                saved = saved.withIngredientScaled(itemId, 1 - saveChance);
            }
            return saved;
        };
    }
}
