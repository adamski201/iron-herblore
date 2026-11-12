package adamski.ui.icon;

import adamski.ui.Amounts;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.AsyncBufferedImage;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

/**
 * An item sprite with its quantity painted on, in the game's stack colours. ItemManager can draw the
 * quantity itself, but caches per quantity, so every bank change re-rendered every sprite.
 */
public final class StackIcon implements Icon {
    static final int WIDTH = 36;
    static final int HEIGHT = 32;

    private static final Color YELLOW = new Color(0xffff00);
    private static final Color GREEN = new Color(0x00ff80);

    private final AsyncBufferedImage sprite;
    private final String quantity;
    private final Color color;

    public StackIcon(AsyncBufferedImage sprite) {
        this(sprite, 0, false);
    }

    /**
     * @param doses whether the stack is a potion's doses, which keep a decimal up to 50k
     */
    public StackIcon(AsyncBufferedImage sprite, double quantity, boolean doses) {
        this.sprite = sprite;

        final long whole = Math.round(quantity);
        if (whole < 1) {
            this.quantity = null;
        } else {
            this.quantity = doses ? Amounts.abbreviateDoses(whole) : Amounts.abbreviate(whole);
        }

        if (whole < 100_000) {
            this.color = YELLOW;
        } else if (whole < 10_000_000) {
            this.color = Color.WHITE;
        } else {
            this.color = GREEN;
        }
    }

    // the sprite starts out blank until the client thread draws it
    public void repaintWhenLoaded(Component component) {
        sprite.onLoaded(component::repaint);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.drawImage(sprite, x, y, null);

        if (quantity == null) return;

        g.setFont(FontManager.getRunescapeSmallFont());
        g.setColor(Color.BLACK);
        g.drawString(quantity, x + 1, y + 11);
        g.setColor(color);
        g.drawString(quantity, x, y + 10);
    }

    @Override
    public int getIconWidth() {
        return WIDTH;
    }

    @Override
    public int getIconHeight() {
        return HEIGHT;
    }
}
