package adamski.ui;

import adamski.data.AlchemistsAmulet;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

/**
 * The alchemist's amulet section's potions, one line each: a box to use the amulet on it, and for an
 * upgrade, on the right, the potion sizes it can be made in. A whole potion is only ever made in
 * one size, so it gets no buttons - a lone 3 would say nothing.
 * <p>
 * Sizes are shown smallest first, so 3 always sits at the right edge and lines up down the list.
 * While the box is clear no size is pressed; ticking it picks 3, and clicking a size switches the
 * amulet on at that size.
 * <p>
 * Immutable once built, like a secondary: the panel builds a new one for every result, and a change
 * made here comes back as one.
 */
class AmuletPanel extends JPanel {
    /**
     * What each size gains per dose made, for its tooltip - one chance of a dose per potion.
     */
    private static final Map<Integer, String> GAIN_BY_DOSES = Map.of(1, "+15%", 2, "+7.5%", 3, "+5%");

    private static final Dimension SIZE_BUTTON = new Dimension(22, 18);

    /**
     * @param onSet   given a recipe and the size of potion it is now made in. Called on the EDT.
     * @param onClear given a recipe the amulet is no longer used on. Called on the EDT.
     */
    AmuletPanel(AmuletView amulet, BiConsumer<Integer, Integer> onSet, IntConsumer onClear) {
        setLayout(new DynamicGridLayout(0, 1, 0, 1));
        setOpaque(false);

        for (AmuletView.Line line : amulet.getLines()) {
            add(lineOf(line, onSet, onClear));
        }
    }

    private static JPanel lineOf(AmuletView.Line line, BiConsumer<Integer, Integer> onSet, IntConsumer onClear) {
        final JCheckBox used = new JCheckBox(line.getName(), line.isUsed());
        used.setFont(FontManager.getRunescapeSmallFont());
        used.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        used.setOpaque(false);
        used.setFocusPainted(false);
        used.addActionListener(e -> {
            if (used.isSelected()) onSet.accept(line.getRecipeId(), AlchemistsAmulet.DEFAULT_DOSES_PER_POTION);
            else onClear.accept(line.getRecipeId());
        });

        final JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);
        panel.add(used, BorderLayout.CENTER);

        if (line.getDosesPerPotionOptions().size() > 1) {
            // centred vertically against the box, rather than stretched to the line's height
            final JPanel sizes = new JPanel(new GridBagLayout());
            sizes.setOpaque(false);
            sizes.add(sizesOf(line, onSet));
            panel.add(sizes, BorderLayout.EAST);
        }

        return panel;
    }

    /**
     * One toggle button per size, grouped so one at most is pressed, packed together so they read as
     * one control. None is pressed while the amulet is off for the potion.
     */
    private static JPanel sizesOf(AmuletView.Line line, BiConsumer<Integer, Integer> onSet) {
        final List<Integer> smallestFirst = new ArrayList<>(line.getDosesPerPotionOptions());
        Collections.sort(smallestFirst);

        final JPanel buttons = new JPanel(new GridLayout(1, smallestFirst.size(), 0, 0));
        buttons.setOpaque(false);
        final ButtonGroup group = new ButtonGroup();

        for (int doses : smallestFirst) {
            final JToggleButton button = new JToggleButton(String.valueOf(doses),
                    Integer.valueOf(doses).equals(line.getDosesPerPotion()));
            button.setFont(FontManager.getRunescapeSmallFont());
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setFocusPainted(false);
            // every button the same size, so the sizes line up down the list
            button.setPreferredSize(SIZE_BUTTON);
            button.setToolTipText("Upgrade " + doses + "-dose potions: " + GAIN_BY_DOSES.get(doses) + " doses"
                    + (doses < AlchemistsAmulet.DEFAULT_DOSES_PER_POTION ? ", but more clicks" : ""));
            // pressing the size already chosen changes nothing, and the app ignores it
            button.addActionListener(e -> onSet.accept(line.getRecipeId(), doses));

            group.add(button);
            buttons.add(button);
        }

        return buttons;
    }
}
