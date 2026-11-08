package adamski.domain.result;

import adamski.domain.ItemQuantities;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The final XP & output quantity produced by a chain of recipes.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class ChainResult {
    private final int rootItemId;
    private final int productItemId;

    /**
     * Total XP sliced by the items in this chain.
     */
    private final List<ChainItemXp> itemContributions;

    /**
     * Total XP sliced by the recipes in this chain (excluding 0 xp recipes).
     */
    private final List<ChainRecipeXp> recipeContributions;

    private final double outputQuantity;

    /**
     * How much of the root item is held, in 1-dose units.
     */
    private final double rootQuantity;

    /**
     * Every run in the chain, from every item - a recipe reached by several items appears once for
     * each.
     */
    private final List<RecipeRun> recipeRuns;

    private final ItemQuantities secondaryDemand;

    private final double xp;

    public ChainResult(int rootItemId,
                       int productItemId,
                       List<ChainItemXp> itemContributions,
                       List<ChainRecipeXp> recipeContributions,
                       double outputQuantity,
                       ItemQuantities secondaryDemand,
                       double xp) {
        if (itemContributions.isEmpty())
            throw new IllegalArgumentException("a chain result needs at least one item contributing to it");

        this.rootItemId = rootItemId;
        this.productItemId = productItemId;
        this.itemContributions = List.copyOf(itemContributions);
        this.recipeContributions = List.copyOf(recipeContributions);
        this.outputQuantity = outputQuantity;
        this.rootQuantity = this.itemContributions.stream()
                .filter(contribution -> contribution.getItemId() == rootItemId)
                .mapToDouble(ChainItemXp::getQuantity)
                .sum();
        this.recipeRuns = this.itemContributions.stream()
                .flatMap(contribution -> contribution.getRuns().stream())
                .collect(Collectors.toUnmodifiableList());
        this.secondaryDemand = secondaryDemand;
        this.xp = xp;
    }

    /**
     * Whether the chain runs any of these recipes at all.
     */
    public boolean runsAnyOf(Collection<Integer> recipeIds) {
        return recipeRuns.stream().anyMatch(run -> run.getRuns() > 0 && recipeIds.contains(run.getRecipe().getId()));
    }

    /**
     * Whether the chain ends where it starts - told to stop at its root, so it runs no recipes.
     */
    public boolean makesNothing() {
        return productItemId == rootItemId;
    }
}
