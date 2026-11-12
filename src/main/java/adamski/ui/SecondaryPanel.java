package adamski.ui;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * One secondary's net, folding open to its breakdown. Rebuilt for every result.
 */
class SecondaryPanel extends JPanel {
    private static final Color HOVER = RowPanel.ROOT_HOVER;


    // on the line rather than the block, so the hover reaches the block's edges
    private static final int SIDE_PADDING = 6;

    // taller than a plain line, so it is easier to click
    private static final float EXTRA_HEIGHT = 0.2f;

    SecondaryPanel(SecondaryView secondary, boolean expanded, Consumer<Boolean> onToggle) {
        setLayout(new BorderLayout());
        setOpaque(false);

        final Color netColor = secondary.getNet() < 0
                ? ColorScheme.PROGRESS_ERROR_COLOR
                : ColorScheme.PROGRESS_COMPLETE_COLOR;
        final JPanel summary = RowPanel.lineOf(secondary.getName(), Amounts.abbreviate(secondary.getNet()), netColor);
        summary.setBackground(HOVER);

        final int padding = Math.round(summary.getPreferredSize().height * EXTRA_HEIGHT / 2);
        summary.setBorder(new EmptyBorder(padding, SIDE_PADDING, padding, SIDE_PADDING));

        final JPanel detail = new JPanel(new DynamicGridLayout(0, 1, 0, 1));
        detail.setBackground(RowPanel.DETAIL_SHADE);
        detail.setBorder(new EmptyBorder(3, SIDE_PADDING + 8, 4, SIDE_PADDING));
        for (RowView.Line line : secondary.getBreakdown()) {
            detail.add(RowPanel.lineOf(line.getName(), Amounts.abbreviate(line.getAmount()), ColorScheme.LIGHT_GRAY_COLOR));
        }
        detail.setVisible(expanded);

        summary.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final boolean nowExpanded = !detail.isVisible();
                detail.setVisible(nowExpanded);
                onToggle.accept(nowExpanded);
                revalidate();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                summary.setOpaque(true);
                summary.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                summary.setOpaque(false);
                summary.repaint();
            }
        });
        summary.setToolTipText("Click for what is needed and held");
        summary.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        add(summary, BorderLayout.NORTH);
        add(detail, BorderLayout.CENTER);
    }
}
