package adamski.ui;

import adamski.data.AlchemistsAmulet;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

/**
 * The alchemist's amulet section's potions, one line each: the potion's name, and on the right a box
 * to use the amulet on it, with, for an upgrade, the potion sizes it can be made in just left of the
 * box. A whole potion is only ever made in one size, so it gets no buttons - a lone 3 would say
 * nothing. Clicking the name ticks the box too.
 * <p>
 * The box sits at the right edge so every line's box lines up, and sizes are shown smallest first,
 * so 3 always sits next to the box and lines up down the list too.
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
        // on the panel's own grey, not a block like the secondaries': the theme's checkboxes are
        // drawn for that grey and all but vanish on the rows' shade
        setOpaque(false);

        for (AmuletView.Line line : amulet.getLines()) {
            add(lineOf(line, onSet, onClear));
        }
    }

    private static JPanel lineOf(AmuletView.Line line, BiConsumer<Integer, Integer> onSet, IntConsumer onClear) {
        final JCheckBox used = new JCheckBox((String) null, line.isUsed());
        used.setOpaque(false);
        used.setFocusPainted(false);
        // no room around the box, so it sits flush with the section's right padding
        used.setBorder(new EmptyBorder(0, 0, 0, 0));
        used.addActionListener(e -> {
            if (used.isSelected()) onSet.accept(line.getRecipeId(), AlchemistsAmulet.DEFAULT_DOSES_PER_POTION);
            else onClear.accept(line.getRecipeId());
        });

        // the name is its own label now the box is on the other side, so it ticks the box when clicked
        final JLabel name = new JLabel(line.getName());
        name.setFont(FontManager.getRunescapeSmallFont());
        name.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        name.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        name.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                used.doClick();
            }
        });

        // centred vertically, rather than stretched to the line's height
        final JPanel controls = new JPanel(new GridBagLayout());
        controls.setOpaque(false);
        final GridBagConstraints next = new GridBagConstraints();
        next.insets = new Insets(0, 0, 0, 6);
        if (line.getDosesPerPotionOptions().size() > 1) {
            controls.add(sizesOf(line, onSet), next);
        }
        next.insets = new Insets(0, 0, 0, 0);
        controls.add(used, next);

        final JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setOpaque(false);
        panel.add(name, BorderLayout.CENTER);
        panel.add(controls, BorderLayout.EAST);

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
