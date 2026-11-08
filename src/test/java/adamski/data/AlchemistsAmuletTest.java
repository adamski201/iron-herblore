package adamski.data;

import adamski.domain.Recipe;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AlchemistsAmuletTest {
    private static final Map<Integer, Recipe> RECIPES_BY_ID = Recipes.all().stream()
            .collect(Collectors.toMap(Recipe::getId, Function.identity()));

    /**
     * The amulet adds a dose, so what it is used on must make something dosed - not always a
     * potion: the haemostatic dressing is one too.
     */
    @Test
    public void everyListedRecipeMakesSomethingDosed() {
        AlchemistsAmulet.dosesPerPotionOptionsByRecipeId().keySet().forEach(recipeId -> {
            final Recipe recipe = RECIPES_BY_ID.get(recipeId);
            assertNotNull("no recipe " + recipeId, recipe);
            assertTrue("recipe " + recipeId + " makes nothing dosed", PotionDoses.isPotion(recipe.getOutput().getItemId()));
        });
    }

    /**
     * A whole potion's size is what the recipe makes, so it must make 3 - fewer than 4, and the
     * size the extra dose is reckoned against.
     */
    @Test
    public void aWholePotionMakesThreeDoses() {
        AlchemistsAmulet.dosesPerPotionOptionsByRecipeId().forEach((recipeId, options) -> {
            if (options.equals(List.of(3))) {
                assertEquals("recipe " + recipeId, 3.0, RECIPES_BY_ID.get(recipeId).getOutput().getQuantity(), 0);
            }
        });
    }

    /**
     * An upgrade is one dose in, one dose out, so the player's potion size is what it is made in.
     */
    @Test
    public void anUpgradeIsOneDoseAtATime() {
        AlchemistsAmulet.dosesPerPotionOptionsByRecipeId().forEach((recipeId, options) -> {
            if (options.size() > 1) {
                final Recipe recipe = RECIPES_BY_ID.get(recipeId);
                assertEquals("recipe " + recipeId, 1.0, recipe.getPrimary().getQuantity(), 0);
                assertEquals("recipe " + recipeId, 1.0, recipe.getOutput().getQuantity(), 0);
            }
        });
    }

    /**
     * Ticking a potion always picks 3, so every recipe must offer it.
     */
    @Test
    public void everyRecipeCanBeMadeInTheDefaultSize() {
        AlchemistsAmulet.dosesPerPotionOptionsByRecipeId().forEach((recipeId, options) ->
                assertTrue("recipe " + recipeId, options.contains(AlchemistsAmulet.DEFAULT_DOSES_PER_POTION)));
    }

    @Test
    public void noSizeIsFourOrMore() {
        AlchemistsAmulet.dosesPerPotionOptionsByRecipeId().forEach((recipeId, options) ->
                options.forEach(doses -> assertEquals("recipe " + recipeId, true, doses >= 1 && doses <= 3)));
    }
}
