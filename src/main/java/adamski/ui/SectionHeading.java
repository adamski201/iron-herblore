package adamski.ui;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * A section's heading, which folds the section open and shut: its title on the left and, on the
 * right, an arrow saying which it is. Hovering turns the title and arrow orange, so it reads as
 * something to click.
 * <p>
 * Between them it can show an {@code info} component, which folds the section like the rest of the
 * heading (the amulet's charges), and a {@code button}, which keeps its own clicks (the recipes'
 * eye). EDT only.
 */
final class SectionHeading extends JPanel {
    private static final Color TEXT = Color.WHITE;
    private static final Color HOVER = ColorScheme.BRAND_ORANGE;

    private final JLabel title;

    // the default font, since the RuneScape ones may lack the arrows
    private final JLabel arrow = new JLabel();

    private boolean expanded;

    /**
     * @param onToggle told whether the section is now open, after the arrow has turned
     * @param info     shown on the right, folding the section when clicked; may be null
     * @param button   shown on the right with clicks of its own; may be null
     */
    SectionHeading(String text, boolean expanded, Consumer<Boolean> onToggle, JComponent info, JComponent button) {
        super(new BorderLayout());
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        this.expanded = expanded;

        title = new JLabel(text);
        title.setFont(FontManager.getRunescapeBoldFont());
        title.setForeground(TEXT);

        arrow.setForeground(TEXT);
        arrow.setText(arrowOf(expanded));

        final JPanel extras = new JPanel(new BorderLayout(6, 0));
        extras.setOpaque(false);
        if (info != null) extras.add(info, BorderLayout.CENTER);
        if (button != null) extras.add(button, BorderLayout.EAST);

        final JPanel right = new JPanel(new BorderLayout(6, 0));
        right.setOpaque(false);
        right.add(extras, BorderLayout.CENTER);
        right.add(arrow, BorderLayout.EAST);

        add(title, BorderLayout.WEST);
        add(right, BorderLayout.EAST);

        final MouseAdapter folds = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SectionHeading.this.expanded = !SectionHeading.this.expanded;
                arrow.setText(arrowOf(SectionHeading.this.expanded));
                onToggle.accept(SectionHeading.this.expanded);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setHovered(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setHovered(false);
            }
        };
        addMouseListener(folds);
        // a tooltip makes a label take its own mouse events, so the info needs telling too
        if (info != null) info.addMouseListener(folds);
    }

    private void setHovered(boolean hovered) {
        final Color color = hovered ? HOVER : TEXT;
        title.setForeground(color);
        arrow.setForeground(color);
    }

    private static String arrowOf(boolean expanded) {
        return expanded ? "▾" : "▸";
    }
}
