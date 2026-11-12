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
 * One line per potion: its name, the sizes an upgrade can be made in, and a box to use the amulet on
 * it. Sizes run smallest first, so 3 sits by the box and lines up down the list. Ticking the box
 * picks 3. Rebuilt for every result.
 */
class AmuletPanel extends JPanel {
    // one 15% chance of a dose per potion, spread over its doses
    private static final Map<Integer, String> GAIN_BY_DOSES = Map.of(1, "+15%", 2, "+7.5%", 3, "+5%");

    private static final Dimension SIZE_BUTTON = new Dimension(22, 18);

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
        used.setBorder(new EmptyBorder(0, 0, 0, 0));
        used.addActionListener(e -> {
            if (used.isSelected()) onSet.accept(line.getRecipeId(), AlchemistsAmulet.DEFAULT_DOSES_PER_POTION);
            else onClear.accept(line.getRecipeId());
        });

        // clicking the name ticks the box too
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
            button.setPreferredSize(SIZE_BUTTON);
            button.setToolTipText("Upgrade " + doses + "-dose potions: " + GAIN_BY_DOSES.get(doses) + " doses"
                    + (doses < AlchemistsAmulet.DEFAULT_DOSES_PER_POTION ? ", but more clicks" : ""));
            button.addActionListener(e -> onSet.accept(line.getRecipeId(), doses));

            group.add(button);
            buttons.add(button);
        }

        return buttons;
    }
}
