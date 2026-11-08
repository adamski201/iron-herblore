package adamski.app;

import adamski.domain.route.RecipeChainResolver;
import adamski.domain.route.RecipeSelection;
import adamski.domain.route.RecipeGraph;
import adamski.domain.route.RecipeSelector;
import adamski.domain.result.AlchemistAmuletCalculator;
import adamski.domain.result.ChainResult;
import adamski.domain.result.ChainResultCalculator;
import adamski.domain.result.RecipeModifiers;
import adamski.data.AlchemistsAmulet;
import adamski.data.PrescriptionGoggles;
import adamski.data.Recipes;
import adamski.domain.ItemQuantities;
import adamski.domain.ItemSource;
import adamski.domain.Recipe;
import adamski.domain.SecondaryPrecursor;
import adamski.domain.balance.SecondaryBalanceCalculator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Orchestrator - receives changes from the adapter, updates state via the store, runs the
 * calculators and publishes to listeners.
 */
@Slf4j
@Singleton
public class HerbloreApp {
    private static final Set<ItemSource> SOURCES =
            EnumSet.of(ItemSource.Bank, ItemSource.PotionStorage, ItemSource.SeedVault);

    private final List<ResultListener> resultListeners = new CopyOnWriteArrayList<>();
    private final List<SelectionListener> selectionListeners = new CopyOnWriteArrayList<>();
    private final List<AlchemistAmuletListener> amuletListeners = new CopyOnWriteArrayList<>();

    private final HerbloreStore store;
    private final RecipeChainResolver chainResolver;
    private final RecipeSelector selector;
    private final List<SecondaryPrecursor> precursors;

    @Getter
    private volatile HerbloreResult result;

    @Inject
    public HerbloreApp(HerbloreStore store, RecipeGraph graph, List<SecondaryPrecursor> precursors) {
        this.store = store;
        this.chainResolver = new RecipeChainResolver(graph);
        this.selector = new RecipeSelector(graph);
        this.precursors = List.copyOf(precursors);
    }

    /**
     * Replaces every setting at once, and republishes against what is already held if any changed.
     */
    public void applySettings(HerbloreSettings settings) {
        if (!store.updateSettings(settings)) return;

        log.debug("settings changed: {}", settings);
        if (result == null) return;

        result = recalculate();
        publishResult(result);
    }

    /**
     * Points one chain at a product and republishes against what is already held.
     * @param rootItemId    the item this chain starts from
     * @param productItemId the item the chain will end at, {@code rootItemId} itself to make nothing, or
     *                      {@link RecipeSelection#DEFAULT} for its default
     */
    public void selectProduct(int rootItemId, int productItemId) {
        var newSelection = selector.select(store.selection(), rootItemId, productItemId);
        var selectionChanged = store.updateSelection(newSelection);

        if (!selectionChanged) return;

        log.debug("item {} set to make item {}", rootItemId, productItemId);
        publishSelection(newSelection);

        if (result == null) return;

        result = recalculate();
        publishResult(result);
    }

    /**
     * Replaces the whole selection with one saved earlier, and republishes the result.
     */
    public void restoreSelection(RecipeSelection saved) {
        if (!store.updateSelection(saved)) return;

        log.debug("selection restored: {}", saved);
        if (result == null) return;

        result = recalculate();
        publishResult(result);
    }

    /**
     * Applies the alchemist's amulet to a recipe and recalculates.
     */
    public void setAmuletDoses(int recipeId, int dosesPerPotion) {
        final List<Integer> options = AlchemistsAmulet.dosesPerPotionOptionsByRecipeId().get(recipeId);
        if (options == null || !options.contains(dosesPerPotion)) return;

        final Map<Integer, Integer> next = new HashMap<>(store.amuletDoses());
        next.put(recipeId, dosesPerPotion);
        updateAmuletDoses(next);
    }

    /**
     * Stops using the alchemist's amulet on one recipe and recalculates.
     */
    public void clearAmuletDoses(int recipeId) {
        final Map<Integer, Integer> next = new HashMap<>(store.amuletDoses());
        next.remove(recipeId);
        updateAmuletDoses(next);
    }

    private void updateAmuletDoses(Map<Integer, Integer> next) {
        if (!store.updateAmuletDoses(next)) return;

        log.debug("amulet doses per potion by recipe: {}", next);
        publishAmulet(store.amuletDoses());

        if (result == null) return;

        result = recalculate();
        publishResult(result);
    }

    /**
     * Replaces every amulet choice with ones saved earlier, and republishes the result.
     */
    public void restoreAmuletDoses(Map<Integer, Integer> saved) {
        final Map<Integer, Integer> allowed = new HashMap<>();
        saved.forEach((recipeId, dosesPerPotion) -> {
            final List<Integer> options = AlchemistsAmulet.dosesPerPotionOptionsByRecipeId().get(recipeId);
            if (options != null && options.contains(dosesPerPotion)) allowed.put(recipeId, dosesPerPotion);
        });

        if (!store.updateAmuletDoses(allowed)) return;

        log.debug("amulet choices restored: {}", allowed);
        if (result == null) return;

        result = recalculate();
        publishResult(result);
    }

    public void updateItems(Map<ItemSource, ItemQuantities> changed) {
        if (!store.updateItems(changed)) return;

        log.debug("sources read: {}", changed.keySet());

        result = recalculate();
        publishResult(result);
    }

    public void updateCurrentXp(int xp) {
        if (!store.updateCurrentXp(xp)) return;

        log.debug("herblore xp: {}", xp);
        if (result == null) return;

        result = recalculate();
        publishResult(result);
    }

    public void addResultListener(ResultListener listener) {
        resultListeners.add(listener);
    }
    public void removeResultListener(ResultListener listener) {
        resultListeners.remove(listener);
    }
    public void addSelectionListener(SelectionListener listener) {
        selectionListeners.add(listener);
    }
    public void removeSelectionListener(SelectionListener listener) {
        selectionListeners.remove(listener);
    }
    public void addAmuletListener(AlchemistAmuletListener listener) {
        amuletListeners.add(listener);
    }
    public void removeAmuletListener(AlchemistAmuletListener listener) {
        amuletListeners.remove(listener);
    }

    private HerbloreResult recalculate() {
        final var settings = store.settings();

        // Gather all owned items across item sources e.g. bank, seed vault
        final var ownedItems = mergeSources(store.itemsBySource());

        // Determine which recipe chains will be used (based on product selection)
        final var recipeChains = chainResolver.resolve(ownedItems, store.selection());

        // Derive the effect of the alchemist's amulet, if enabled
        final Map<Integer, Integer> amuletDoses = settings.isAlchemistsAmuletUsed() ? store.amuletDoses() : Map.of();
        final Map<Integer, Double> amuletFactors = AlchemistAmuletCalculator.factorsByRecipeId(
                amuletDoses, AlchemistsAmulet.EXTRA_DOSE_CHANCE);

        // Apply modifiers to the selected recipes depending on the player's settings
        final var modifiedChains = RecipeModifiers.apply(recipeChains, getRecipeModifiers(settings, amuletFactors));

        // Calculate the XP & quantity result for each chain
        final var chainResults = ChainResultCalculator.calculate(modifiedChains, ownedItems);

        // Calculate the total surplus/deficit of secondaries
        final var secondaryBalance = SecondaryBalanceCalculator.calculate(
                chainResults,
                ownedItems,
                settings.isChainProductsCounted(),
                settings.isPrecursorsCounted() ? precursors : List.of());

        return new HerbloreResult(ownedItems, store.currentXp(), chainResults, secondaryBalance,
                settings.isAlchemistsAmuletUsed()
                        ? AlchemistAmuletCalculator.findOptions(chainResults,
                                AlchemistsAmulet.dosesPerPotionOptionsByRecipeId(), amuletDoses)
                        : List.of(),
                AlchemistAmuletCalculator.sumCharges(chainResults, amuletFactors),
                chainResults.stream()
                        .filter(chain -> chain.runsAnyOf(amuletDoses.keySet()))
                        .map(ChainResult::getRootItemId)
                        .collect(Collectors.toSet()));
    }

    private static List<UnaryOperator<Recipe>> getRecipeModifiers(HerbloreSettings settings,
                                                                  Map<Integer, Double> amuletFactors) {
        final List<UnaryOperator<Recipe>> modifiers = new ArrayList<>();
        modifiers.add(RecipeModifiers.outputOverrides(getYieldsByRecipeId(settings)));

        if (settings.isPrescriptionGogglesUsed()) {
            modifiers.add(RecipeModifiers.inputScaling(
                    PrescriptionGoggles.savedItemsByRecipeId(), 1 - PrescriptionGoggles.SAVE_CHANCE));
        }

        modifiers.add(RecipeModifiers.outputScaling(amuletFactors));

        return modifiers;
    }

    private static Map<Integer, Double> getYieldsByRecipeId(HerbloreSettings settings) {
        final Map<Integer, Double> overrides = new HashMap<>();
        overrides.put(Recipes.CUT_LEAPING_STURGEON_RECIPE_ID, settings.getCaviarSuccessRate());
        settings.getSeedYieldBySeedItemId().forEach((seedItemId, yield) ->
                overrides.put(Recipes.seedRecipeIdOf(seedItemId), yield));

        return overrides;
    }

    private static ItemQuantities mergeSources(Map<ItemSource, ItemQuantities> snapshot) {
        ItemQuantities merged = ItemQuantities.EMPTY;

        for (ItemSource source : SOURCES) {
            merged = merged.plus(snapshot.getOrDefault(source, ItemQuantities.EMPTY));
        }

        return merged;
    }

    private void publishSelection(RecipeSelection selection) {
        for (SelectionListener listener : selectionListeners) {
            try {
                listener.onRecipeSelectionChanged(selection);
            } catch (Exception e) {
                log.warn("selection listener {} threw", listener.getClass().getSimpleName(), e);
            }
        }
    }

    private void publishAmulet(Map<Integer, Integer> dosesPerPotionByRecipeId) {
        for (AlchemistAmuletListener listener : amuletListeners) {
            try {
                listener.onAmuletConfigurationChanged(dosesPerPotionByRecipeId);
            } catch (Exception e) {
                log.warn("amulet listener {} threw", listener.getClass().getSimpleName(), e);
            }
        }
    }

    private void publishResult(HerbloreResult result) {
        for (ResultListener listener : resultListeners) {
            try {
                listener.onResultChanged(result);
            } catch (Exception e) {
                log.warn("listener {} threw", listener.getClass().getSimpleName(), e);
            }
        }
    }
}
