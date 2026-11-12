package adamski.ui;

import adamski.domain.result.AlchemistAmuletOption;
import adamski.domain.result.ChainItemXp;
import adamski.domain.result.ChainRecipeXp;
import adamski.domain.result.ChainResult;
import adamski.data.PotionDoses;
import adamski.domain.ItemQuantities;
import adamski.domain.Recipe;
import adamski.domain.RecipeTag;
import adamski.domain.balance.SecondaryBalance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Turns results into what the panel shows, every name resolved. Runs on the client thread, where
 * ItemManager can resolve names; tests pass a map instead.
 */
final class RowViewFactory {
    static final String MAKE_NOTHING = "Make nothing";
    static final String NEEDED = "Needed";
    static final String HELD = "Held";

    // everything is counted in single doses, so the "(1)" on a potion's name says nothing
    private static final Pattern ONE_DOSE_SUFFIX = Pattern.compile("\\s*\\(1\\)$");

    private static final List<RecipeTag> STEP_ORDER = List.of(RecipeTag.SEED, RecipeTag.DEGRIME, RecipeTag.UNF);

    private final IntFunction<String> nameOf;

    private final Map<Integer, List<Recipe>> productRecipesByItem;

    RowViewFactory(IntFunction<String> nameOf, Map<Integer, List<Recipe>> productRecipesByItem) {
        this.nameOf = itemId -> PotionDoses.isPotion(itemId)
                ? ONE_DOSE_SUFFIX.matcher(nameOf.apply(itemId)).replaceFirst("")
                : nameOf.apply(itemId);
        this.productRecipesByItem = productRecipesByItem;
    }

    RowView rowOf(ChainResult chain, boolean amuletUsed) {
        final int rootItemId = chain.getRootItemId();
        final List<RowView.ProductOption> options = new ArrayList<>();

        // picking the root as its own product is how the app is told to make nothing
        options.add(new RowView.ProductOption(rootItemId, MAKE_NOTHING));
        for (Recipe recipe : inMenuOrder(productRecipesByItem.getOrDefault(rootItemId, List.of()))) {
            final int productItemId = recipe.getOutput().getItemId();
            options.add(new RowView.ProductOption(productItemId, nameOf.apply(productItemId)));
        }

        // a stopped row's root is listed as held, but contributes nothing worth a line
        final List<RowView.Line> xpByItem = new ArrayList<>();
        for (ChainItemXp contribution : chain.getItemContributions()) {
            if (contribution.getXp() != 0) {
                xpByItem.add(new RowView.Line(contribution.getXp(),
                        Amounts.abbreviate(contribution.getQuantity()) + " × " + nameOf.apply(contribution.getItemId())));
            }
        }

        final List<RowView.RecipeLine> xpByRecipe = new ArrayList<>();
        for (ChainRecipeXp contribution : chain.getRecipeContributions()) {
            xpByRecipe.add(new RowView.RecipeLine(contribution.getXp(),
                    nameOf.apply(contribution.getRecipe().getPrimary().getItemId()),
                    nameOf.apply(contribution.getRecipe().getOutput().getItemId())));
        }

        final int productItemId = chain.getProductItemId();

        return new RowView(
                rootItemId,
                nameOf.apply(rootItemId),
                chain.getRootQuantity(),
                chain.getXp(),
                productItemId,
                chain.makesNothing(),
                chain.makesNothing() ? MAKE_NOTHING : nameOf.apply(productItemId),
                chain.getOutputQuantity(),
                amuletUsed,
                options,
                xpByItem,
                xpByRecipe,
                secondaryDemandLinesOf(chain.getSecondaryDemand()));
    }

    /**
     * The steps before a potion first - seed, degrime, unf - then the rest by level.
     */
    private static List<Recipe> inMenuOrder(List<Recipe> productRecipes) {
        final List<Recipe> ordered = new ArrayList<>(productRecipes);
        ordered.sort(Comparator
                .comparingInt(RowViewFactory::stepRankOf)
                .thenComparingInt(recipe -> isStep(recipe) ? 0 : recipe.getLevel()));
        return ordered;
    }

    private static int stepRankOf(Recipe recipe) {
        return isStep(recipe) ? STEP_ORDER.indexOf(recipe.getTag()) : STEP_ORDER.size();
    }

    private static boolean isStep(Recipe recipe) {
        return STEP_ORDER.contains(recipe.getTag());
    }

    /**
     * Shortfalls first, as negatives, then what is spare.
     */
    List<SecondaryView> secondariesOf(SecondaryBalance balance) {
        return balance.getEntries().stream()
                .sorted(Comparator.comparingDouble(SecondaryBalance.BalanceEntry::net))
                .map(this::secondaryOf)
                .collect(Collectors.toList());
    }

    private SecondaryView secondaryOf(SecondaryBalance.BalanceEntry secondary) {
        final List<RowView.Line> breakdown = new ArrayList<>();
        breakdown.add(new RowView.Line(secondary.getQtyDemanded(), NEEDED));
        breakdown.add(new RowView.Line(secondary.getQtyHeld(), HELD));

        secondary.getChainContributions().stream()
                .sorted(Comparator.comparingDouble(SecondaryBalance.ChainContribution::getMakes).reversed())
                .forEach(chain -> breakdown.add(new RowView.Line(chain.getMakes(), nameOf.apply(chain.getRootItemId()))));

        // every kind of bird nest is called "Bird nest", so precursors are summed by name
        final Map<String, double[]> heldAndMadeByName = new LinkedHashMap<>();
        for (SecondaryBalance.PrecursorContribution precursor : secondary.getPrecursorContributions()) {
            final double[] heldAndMade = heldAndMadeByName.computeIfAbsent(nameOf.apply(precursor.getPrecursorItemId()), name -> new double[2]);
            heldAndMade[0] += precursor.getHeld();
            heldAndMade[1] += precursor.getMakes();
        }

        heldAndMadeByName.entrySet().stream()
                .sorted(Comparator.comparingDouble((Map.Entry<String, double[]> entry) -> entry.getValue()[1]).reversed())
                .forEach(entry -> breakdown.add(new RowView.Line(entry.getValue()[1],
                        Amounts.abbreviate(entry.getValue()[0]) + " × " + entry.getKey())));

        final int secondaryItemId = secondary.getSecondaryItemId();
        return new SecondaryView(secondaryItemId, nameOf.apply(secondaryItemId), secondary.net(), breakdown);
    }

    AmuletView amuletOf(List<AlchemistAmuletOption> options, double charges) {
        final List<AmuletView.Line> lines = options.stream()
                .map(option -> new AmuletView.Line(
                        option.getRecipeId(),
                        nameOf.apply(option.getProductItemId()),
                        option.getDosesPerPotionOptions(),
                        option.getDosesPerPotion()))
                .collect(Collectors.toList());

        return new AmuletView(charges, lines);
    }

    /**
     * Rounded up: a fraction of a secondary still means gathering a whole one.
     */
    private List<RowView.Line> secondaryDemandLinesOf(ItemQuantities demand) {
        return demand.asMap().entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .map(entry -> new RowView.Line(Math.ceil(entry.getValue()), nameOf.apply(entry.getKey())))
                .collect(Collectors.toList());
    }
}
