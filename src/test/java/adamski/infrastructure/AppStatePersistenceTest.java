package adamski.infrastructure;

import adamski.domain.route.RecipeSelection;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * The encoding only - saving and loading go through ConfigManager, which needs a running client.
 */
public class AppStatePersistenceTest {
    private final AppStatePersistence persistence = new AppStatePersistence(null, null, new Gson(), null);

    @Test
    public void aSelectionSurvivesBeingSaved() {
        final RecipeSelection chosen = RecipeSelection.of(Map.of(249, 20, 199, RecipeSelection.STOP));

        assertEquals(chosen, persistence.decodeStoredRecipeSelection(persistence.encode(chosen)));
    }

    @Test
    public void nothingSavedIsTheDefaults() {
        assertEquals(RecipeSelection.ALL_DEFAULT, persistence.decodeStoredRecipeSelection(null));
        assertEquals(RecipeSelection.ALL_DEFAULT, persistence.decodeStoredRecipeSelection(""));
    }

    @Test
    public void anUnreadableSaveIsTheDefaults() {
        assertEquals(RecipeSelection.ALL_DEFAULT, persistence.decodeStoredRecipeSelection("{not json"));
        assertEquals(RecipeSelection.ALL_DEFAULT, persistence.decodeStoredRecipeSelection("[1, 2, 3]"));
    }

    @Test
    public void anEntryWithNoRecipeIsDropped() {
        assertEquals(RecipeSelection.of(Map.of(199, 20)), persistence.decodeStoredRecipeSelection("{\"249\": null, \"199\": 20}"));
    }

    @Test
    public void amuletChoicesSurviveBeingSaved() {
        final Map<Integer, Integer> chosen = Map.of(44, 3, 87, 1);

        assertEquals(chosen, persistence.decodeStoredAmuletConfiguration(persistence.encodeAmulet(chosen)));
    }

    @Test
    public void noAmuletChoicesSavedIsNone() {
        assertEquals(Map.of(), persistence.decodeStoredAmuletConfiguration(null));
        assertEquals(Map.of(), persistence.decodeStoredAmuletConfiguration(""));
    }

    @Test
    public void unreadableAmuletChoicesAreNone() {
        assertEquals(Map.of(), persistence.decodeStoredAmuletConfiguration("{not json"));
        assertEquals(Map.of(), persistence.decodeStoredAmuletConfiguration("[1, 2, 3]"));
    }

    @Test
    public void anAmuletChoiceWithNoSizeIsDropped() {
        assertEquals(Map.of(87, 2), persistence.decodeStoredAmuletConfiguration("{\"44\": null, \"87\": 2}"));
    }
}
