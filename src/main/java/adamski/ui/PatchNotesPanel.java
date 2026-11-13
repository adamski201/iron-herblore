package adamski.ui;

import adamski.ui.icon.CrossIcon;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * The release notes, at the top of the panel until closed. EDT only.
 */
class PatchNotesPanel extends JPanel {
    private static final Color CLOSE = new Color(0x8C, 0x8C, 0x8C);

    /**
     * @param releases newest first, each with at least one note
     * @param onClose  called after the panel has hidden itself
     */
    PatchNotesPanel(List<PatchNotes.Release> releases, Runnable onClose) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 8, 0));

        // one release is named in the heading; several are each named above their notes
        final boolean one = releases.size() == 1;
        final JLabel heading = new JLabel(one ? "What's new in " + releases.get(0).getVersion() : "What's new");
        heading.setFont(FontManager.getRunescapeSmallFont());
        heading.setForeground(ColorScheme.BRAND_ORANGE);

        final JLabel close = new JLabel(new CrossIcon(CLOSE));
        close.setVerticalAlignment(SwingConstants.TOP);
        close.setBorder(new EmptyBorder(2, 0, 0, 0));
        close.setToolTipText("Close");
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
                onClose.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                close.setIcon(new CrossIcon(Color.WHITE));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                close.setIcon(new CrossIcon(CLOSE));
            }
        });

        final JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading, BorderLayout.CENTER);
        top.add(close, BorderLayout.EAST);

        final JPanel box = new JPanel(new DynamicGridLayout(0, 1, 0, 1));
        box.setBackground(RowPanel.ROOT_SHADE);
        box.setBorder(new EmptyBorder(4, 8, 6, 6));
        box.add(top);
        for (PatchNotes.Release release : releases) {
            if (!one) {
                final JLabel version = new JLabel(release.getVersion());
                version.setFont(FontManager.getRunescapeSmallFont());
                version.setForeground(Color.GRAY);
                box.add(version);
            }

            for (String note : release.getNotes()) {
                box.add(lineOf(note));
            }
        }

        add(box, BorderLayout.CENTER);
    }

    private static JLabel lineOf(String text) {
        final JLabel label = new JLabel("- " + text);
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        return label;
    }
}
