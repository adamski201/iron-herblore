package adamski.data;

import adamski.domain.Ingredient;
import adamski.domain.Recipe;
import adamski.domain.RecipeTag;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PrescriptionGogglesTest {
    private static final Map<Integer, Recipe> RECIPES_BY_ID = Recipes.all().stream()
            .collect(Collectors.toMap(Recipe::getId, Function.identity()));

    /**
     * Catches typos, and a recipe whose ingredients change under the list.
     */
    @Test
    public void everySavedItemIsUsedByItsRecipe() {
        PrescriptionGoggles.savedItemsByRecipeId().forEach((recipeId, itemIds) -> {
            final Recipe recipe = RECIPES_BY_ID.get(recipeId);
            assertNotNull("no recipe " + recipeId, recipe);

            final Set<Integer> used = recipe.getSecondaries().stream()
                    .map(Ingredient::getItemId)
                    .collect(Collectors.toSet());
            used.add(recipe.getPrimary().getItemId());

            assertTrue("recipe " + recipeId + " does not use all of " + itemIds, used.containsAll(itemIds));
        });
    }

    /**
     * The goggles work when mixing, not when growing, cleaning or cutting.
     */
    @Test
    public void nothingIsSavedOutsideMixing() {
        final Set<RecipeTag> notMixing = EnumSet.of(RecipeTag.SEED, RecipeTag.DEGRIME);

        PrescriptionGoggles.savedItemsByRecipeId().keySet().forEach(recipeId ->
                assertFalse("recipe " + recipeId + " is not a mix",
                        notMixing.contains(RECIPES_BY_ID.get(recipeId).getTag())));
    }
}
