package adamski.ui;

import net.runelite.api.MenuAction;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayMenuEntry;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * Opens the plugin's settings, for the panel's cog. RuneLite offers a plugin no direct way, so this
 * fakes an overlay's "Configure" click - which needs the plugin itself, injected as a plain
 * {@link Plugin} that the plugin binds to itself.
 */
class SettingsOpener {
    private final Plugin plugin;
    private final EventBus eventBus;

    @Inject
    SettingsOpener(Plugin plugin, EventBus eventBus) {
        this.plugin = plugin;
        this.eventBus = eventBus;
    }

    void openSettings() {
        eventBus.post(new OverlayMenuClicked(
                new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, null, null), new SettingsOverlay(plugin)));
    }

    private static final class SettingsOverlay extends Overlay {
        SettingsOverlay(Plugin plugin) {
            super(plugin);
        }

        @Override
        public Dimension render(Graphics2D graphics) {
            return null;
        }
    }
}
