package adamski.infrastructure;

import adamski.app.AlchemistAmuletListener;
import adamski.app.HerbloreApp;
import adamski.app.SelectionListener;
import adamski.domain.route.RecipeSelection;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Saves apps state, as opposed to the settings in
 * {@link HerbloreConfig} - in the RuneLite profile of that account. Saves on every change; loads on
 * login, on switching account, and when the plugin starts.
 */
@Slf4j
@Singleton
public class AppStatePersistence implements SelectionListener, AlchemistAmuletListener {
    static final String CONFIG_GROUP = HerbloreConfig.GROUP;
    static final String SELECTION_KEY = "selection";
    static final String AMULET_KEY = "amulet";

    private static final Type INTEGER_BY_INTEGER = new TypeToken<Map<Integer, Integer>>() {
    }.getType();

    private final ConfigManager configManager;
    private final ClientThread clientThread;
    private final Gson gson;
    private final HerbloreApp app;

    @Inject
    public AppStatePersistence(ConfigManager configManager, ClientThread clientThread, Gson gson, HerbloreApp app) {
        this.configManager = configManager;
        this.clientThread = clientThread;
        this.gson = gson;
        this.app = app;
    }

    @Subscribe
    public void onRuneScapeProfileChanged(RuneScapeProfileChanged event) {
        // the app is only driven from the client thread; invoke runs at once if already there
        clientThread.invoke(this::load);
    }

    /**
     * Restores the current account's saved choices, or the defaults if it has none. Client thread
     * only.
     */
    public void load() {
        app.restoreSelection(decodeStoredRecipeSelection(configManager.getRSProfileConfiguration(CONFIG_GROUP, SELECTION_KEY)));
        app.restoreAmuletDoses(decodeStoredAmuletConfiguration(configManager.getRSProfileConfiguration(CONFIG_GROUP, AMULET_KEY)));
    }

    @Override
    public void onAmuletConfigurationChanged(Map<Integer, Integer> dosesPerPotionByRecipeId) {
        if (dosesPerPotionByRecipeId.isEmpty()) {
            configManager.unsetRSProfileConfiguration(CONFIG_GROUP, AMULET_KEY);
        } else {
            configManager.setRSProfileConfiguration(CONFIG_GROUP, AMULET_KEY, encodeAmulet(dosesPerPotionByRecipeId));
        }
    }

    String encodeAmulet(Map<Integer, Integer> dosesPerPotionByRecipeId) {
        return gson.toJson(dosesPerPotionByRecipeId, INTEGER_BY_INTEGER);
    }

    Map<Integer, Integer> decodeStoredAmuletConfiguration(String saved) {
        return decodeIntegerMap(saved, "amulet choices");
    }

    @Override
    public void onRecipeSelectionChanged(RecipeSelection selection) {
        if (selection.recipeByItemId().isEmpty()) {
            configManager.unsetRSProfileConfiguration(CONFIG_GROUP, SELECTION_KEY);
        } else {
            configManager.setRSProfileConfiguration(CONFIG_GROUP, SELECTION_KEY, encode(selection));
        }
    }

    String encode(RecipeSelection selection) {
        return gson.toJson(selection.recipeByItemId(), INTEGER_BY_INTEGER);
    }

    RecipeSelection decodeStoredRecipeSelection(String saved) {
        // an empty map is the defaults
        return RecipeSelection.of(decodeIntegerMap(saved, "selection"));
    }

    /**
     * Reads back a saved integer-to-integer map. Nothing saved, an unreadable save and null entries all
     * read as missing, never as an error - a bad save must not break the plugin.
     *
     * @param what names the save in the warning logged when it cannot be read
     */
    private Map<Integer, Integer> decodeIntegerMap(String saved, String what) {
        if (saved == null || saved.isEmpty()) return Map.of();

        try {
            final Map<Integer, Integer> decoded = gson.fromJson(saved, INTEGER_BY_INTEGER);
            if (decoded == null) return Map.of();

            final Map<Integer, Integer> readable = new HashMap<>(decoded);
            readable.values().removeIf(value -> value == null);

            return readable;
        } catch (JsonParseException e) {
            log.warn("ignoring unreadable saved {}: {}", what, saved, e);
            return Map.of();
        }
    }
}
