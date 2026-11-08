package adamski.app;

import adamski.domain.route.RecipeSelection;

/**
 * Told when the user changes what a row makes.
 */
public interface SelectionListener {
    void onRecipeSelectionChanged(RecipeSelection selection);
}
