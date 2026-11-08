package adamski.ui.icon;

import net.runelite.client.util.AsyncBufferedImage;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * An item sprite drawn at half size, as a marker beside text rather than an item in its own right.
 */
public final class SmallSpriteIcon implements Icon {
    private static final int WIDTH = StackIcon.WIDTH / 2;
    private static final int HEIGHT = StackIcon.HEIGHT / 2;

    private final AsyncBufferedImage sprite;

    public SmallSpriteIcon(AsyncBufferedImage sprite) {
        this.sprite = sprite;
    }

    /**
     * Repaints the component once the sprite has been drawn, since it starts out blank.
     */
    public void repaintWhenLoaded(Component component) {
        sprite.onLoaded(component::repaint);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(sprite, x, y, WIDTH, HEIGHT, null);
        g2.dispose();
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
