package adamski.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public final class Recipe {
    private final int id;
    private final Ingredient primary;
    private final List<Ingredient> secondaries;
    private final Ingredient output;
    private final RecipeTag tag;
    private final float xp;
    private final int level;

    public Recipe(int id, Ingredient primary, Ingredient[] secondaries, Ingredient output,
                  RecipeTag tag, float xp, int level) {
        this.id = id;
        this.primary = primary;
        this.secondaries = Collections.unmodifiableList(Arrays.asList(secondaries.clone()));
        this.output = output;
        this.tag = tag;
        this.xp = xp;
        this.level = level;
    }

    /**
     * @return this recipe making {@code quantity} of its output per run instead
     */
    public Recipe withOutputQuantity(double quantity) {
        return new Recipe(id, primary, secondaries.toArray(new Ingredient[0]),
                new Ingredient(output.getItemId(), quantity), tag, xp, level);
    }

    /**
     * @return this recipe earning {@code xp} per run instead
     */
    public Recipe withXp(float xp) {
        return new Recipe(id, primary, secondaries.toArray(new Ingredient[0]), output, tag, xp, level);
    }

    /**
     * @return this recipe using {@code factor} times as much of one of its ingredients per run -
     * primary or secondary
     * @throws IllegalArgumentException if the recipe does not use the item
     */
    public Recipe withIngredientScaled(int itemId, double factor) {
        boolean found = primary.getItemId() == itemId;
        final Ingredient scaledPrimary = found ? scale(primary, factor) : primary;

        final Ingredient[] scaledSecondaries = new Ingredient[secondaries.size()];
        for (int i = 0; i < scaledSecondaries.length; i++) {
            final Ingredient secondary = secondaries.get(i);
            final boolean matches = secondary.getItemId() == itemId;
            scaledSecondaries[i] = matches ? scale(secondary, factor) : secondary;
            found |= matches;
        }

        if (!found) throw new IllegalArgumentException("recipe " + id + " does not use item " + itemId);

        return new Recipe(id, scaledPrimary, scaledSecondaries, output, tag, xp, level);
    }

    private static Ingredient scale(Ingredient ingredient, double factor) {
        return new Ingredient(ingredient.getItemId(), ingredient.getQuantity() * factor);
    }
}
