package adamski.ui;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

/**
 * The level now and the level once the bank is made ("85 ▸ 92", centred on the arrow, which is
 * drawn rather than typed since the RuneScape font has no good one; just "85" when the bank gains
 * none) above a full-width line showing how far through the banked level towards the
 * next the bank reaches, in bright blue. When the bank gains no level, the part of the level
 * already held shows in muted blue before the bank's share. Under it, the banked level on the
 * left and the next level on the right - the two ends of the line - with the banked xp between
 * them. EDT only.
 */
final class XpBar extends JComponent {
    private static final Color TRACK = ColorScheme.LIGHT_GRAY_COLOR;
    private static final Color FILL = new Color(0x3A, 0x8E, 0xE6);

    /**
     * What the player already has of the level, when the bank gains no level.
     */
    private static final Color COVERED = new Color(0x3A, 0x58, 0x7A);
    private static final Color LEVEL_TEXT = ColorScheme.LIGHT_GRAY_COLOR;
    private static final Color XP_TEXT = Color.WHITE;

    private static final int LINE_THICKNESS = 2;

    /**
     * Space between the line and the text either side of it.
     */
    private static final int TEXT_GAP = 6;

    /**
     * Space either side of the title's arrow.
     */
    private static final int ARROW_GAP = 5;

    private XpBarView view;

    XpBar() {
        // a line of text above the bar and one below
        final FontMetrics above = getFontMetrics(FontManager.getRunescapeFont());
        final FontMetrics below = getFontMetrics(FontManager.getRunescapeFont());
        setPreferredSize(new Dimension(0, heightOf(above) + TEXT_GAP + LINE_THICKNESS + TEXT_GAP + heightOf(below)));
    }

    void setView(XpBarView view) {
        this.view = view;
        setToolTipText(String.format("Level %d now, %d once %,.0f banked xp is made",
                view.getCurrentLevel(), view.getBankedLevel(), view.getBankedXp()));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        if (view == null) return;

        final Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            final int width = getWidth();

            // where you are and where the bank takes you
            g.setFont(FontManager.getRunescapeFont());
            final FontMetrics above = g.getFontMetrics();
            paintTitle(g, above, width);

            final int lineTop = heightOf(above) + TEXT_GAP;
            // what is already held of the level shows in the muted blue, with the bank's share in
            // bright blue after it; once a level is gained nothing is held of it, so it is all bank
            final int start = (int) Math.round(width * view.getStartProgress());
            final int end = (int) Math.round(width * view.getProgress());
            g.setColor(TRACK);
            g.fillRect(0, lineTop, width, LINE_THICKNESS);
            g.setColor(COVERED);
            g.fillRect(0, lineTop, start, LINE_THICKNESS);
            g.setColor(FILL);
            g.fillRect(start, lineTop, end - start, LINE_THICKNESS);

            g.setFont(FontManager.getRunescapeFont());
            final FontMetrics metrics = g.getFontMetrics();
            final int baseline = lineTop + LINE_THICKNESS + TEXT_GAP + metrics.getAscent();

            final String banked = String.valueOf(view.getBankedLevel());
            final String next = String.valueOf(view.getNextLevel());
            final String xp = xpText(view.getBankedXp());

            // the two ends of the line: the banked level, and the level the fill is heading for
            g.setColor(LEVEL_TEXT);
            g.drawString(banked, 0, baseline);
            g.drawString(next, width - metrics.stringWidth(next), baseline);

            g.setColor(XP_TEXT);
            g.drawString(xp, (width - metrics.stringWidth(xp)) / 2, baseline);
        } finally {
            g.dispose();
        }
    }

    private static int heightOf(FontMetrics metrics) {
        return metrics.getAscent() + metrics.getDescent();
    }

    /**
     * "85 ▸ 92" along the top with the arrow in the middle, so a three-digit level does not push
     * it off centre, or "85" alone when the bank gains no level.
     */
    private void paintTitle(Graphics2D g, FontMetrics metrics, int width) {
        final String from = String.valueOf(view.getCurrentLevel());
        final int baseline = metrics.getAscent();
        g.setColor(XP_TEXT);

        if (!view.levelGained()) {
            g.drawString(from, (width - metrics.stringWidth(from)) / 2, baseline);
            return;
        }

        final String to = String.valueOf(view.getBankedLevel());

        // sized and centred on the digits either side of it
        final Rectangle2D digit = g.getFont().createGlyphVector(g.getFontRenderContext(), "0").getVisualBounds();
        final int halfHeight = Math.max(2, (int) Math.round(digit.getHeight() * 0.3));
        final int arrowWidth = halfHeight + 1;
        final int arrowMiddle = baseline + (int) Math.round(digit.getY() + digit.getHeight() / 2);

        final int arrowLeft = (width - arrowWidth) / 2;
        g.drawString(from, arrowLeft - ARROW_GAP - metrics.stringWidth(from), baseline);
        g.drawString(to, arrowLeft + arrowWidth + ARROW_GAP, baseline);

        g.setColor(LEVEL_TEXT);
        paintArrow(g, arrowLeft, arrowMiddle, halfHeight);
    }

    /**
     * A solid triangle pointing right, built from one-pixel columns so it stays as crisp as the
     * pixel font around it: its left edge at {@code left}, 2 x {@code halfHeight} + 1 tall.
     */
    private static void paintArrow(Graphics2D g, int left, int middle, int halfHeight) {
        for (int column = 0; column <= halfHeight; column++) {
            final int reach = halfHeight - column;
            g.fillRect(left + column, middle - reach, 1, 2 * reach + 1);
        }
    }

    /**
     * Short but precise enough to watch move: "+3.179m xp", "+450.1k xp", "+820 xp".
     */
    static String xpText(double xp) {
        final String amount;
        if (xp >= 1_000_000) {
            amount = String.format("%.3fm", xp / 1_000_000);
        } else if (xp >= 1_000) {
            amount = String.format("%.1fk", xp / 1_000);
        } else {
            amount = String.format("%.0f", xp);
        }

        return "+" + amount + " xp";
    }
}
