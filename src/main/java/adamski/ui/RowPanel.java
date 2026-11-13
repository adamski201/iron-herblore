package adamski.ui;

import adamski.data.PotionDoses;
import adamski.ui.icon.CaretIcon;
import adamski.ui.icon.CornerArrowIcon;
import adamski.ui.icon.NoEntryIcon;
import adamski.ui.icon.RightArrowIcon;
import adamski.ui.icon.SmallSpriteIcon;
import adamski.ui.icon.StackIcon;
import net.runelite.api.gameval.ItemID;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.AsyncBufferedImage;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * One row, two lines tall:
 * <pre>
 *   [root]  Root item name           |        |
 *   [icon]  ↳ Product name      xp   | [prod] |
 * </pre>
 * The root's half folds the xp breakdown open; the product's half opens a menu of what else the row
 * could make. A row set to make nothing shrinks to one dim line, kept because it is the only place to
 * switch the row back on.
 * <p>
 * Immutable: a row whose view changes is replaced, not updated.
 */
class RowPanel extends JPanel {
    // also behind the secondaries section, so it reads as the same kind of thing as a row
    static final Color ROOT_SHADE = new Color(0x1d1d1d);
    static final Color ROOT_HOVER = new Color(0x262626);

    // behind a breakdown: darker than the line it folds out of, since lighter means hovered
    static final Color DETAIL_SHADE = new Color(0x181818);

    // the panel's own background, so a switched-off row all but disappears into it
    private static final Color OFF_SHADE = ColorScheme.DARK_GRAY_COLOR;
    private static final Color OFF_HOVER = ColorScheme.DARKER_GRAY_COLOR;
    private static final Color OFF_TEXT = Color.GRAY;

    private static final Dimension ROOT_ICON_CELL = new Dimension(40, 38);
    private static final Dimension PRODUCT_ICON_CELL = new Dimension(46, 38);

    private static final int MENU_MAX_HEIGHT = 240;

    private static final int MENU_OPTION_HEIGHT = 36;

    private static final Icon MAKE_NOTHING_ICON = new NoEntryIcon(ColorScheme.PROGRESS_ERROR_COLOR);

    /**
     * @param iconOf an item's plain sprite. Asked on the EDT, so ItemManager queues the drawing for
     *               the client thread rather than doing it there and then
     */
    RowPanel(RowView row, boolean expanded, Consumer<Boolean> onToggle,
                  BiConsumer<Integer, Integer> onProductSelected, IntFunction<AsyncBufferedImage> iconOf) {
        setLayout(new BorderLayout());
        setOpaque(false);

        final Supplier<JPopupMenu> menu = () -> buildMenu(row, onProductSelected, iconOf);

        if (row.isSwitchedOff()) {
            add(buildSwitchedOff(row, menu), BorderLayout.NORTH);
            return;
        }

        final JPanel detail = buildDetail(row);
        final boolean hasDetail = detail.getComponentCount() > 0;
        detail.setVisible(hasDetail && expanded);

        // the root's half
        final JLabel rootIcon = new JLabel();
        rootIcon.setHorizontalAlignment(SwingConstants.CENTER);
        rootIcon.setPreferredSize(ROOT_ICON_CELL);
        // no quantity on the root: it would count the root item alone, not everything on the row
        setIcon(rootIcon, new StackIcon(iconOf.apply(row.getRootIconItemId())));

        final JLabel rootName = new JLabel(row.getRootItemName());
        rootName.setFont(FontManager.getRunescapeBoldFont());
        rootName.setForeground(Color.WHITE);
        rootName.setBorder(new EmptyBorder(0, 2, 0, 6));
        rootName.setVerticalAlignment(SwingConstants.BOTTOM);

        // the product's half
        final JLabel arrow = new JLabel(new CornerArrowIcon(ColorScheme.BRAND_ORANGE));

        final JLabel xp = new JLabel(Amounts.abbreviate(row.getXp()));
        xp.setFont(FontManager.getRunescapeSmallFont());
        xp.setForeground(Color.WHITE);

        final JPanel notch = new JPanel(new BorderLayout(3, 0));
        notch.setBackground(ROOT_SHADE);
        notch.setBorder(new EmptyBorder(0, 3, 0, 6));
        notch.add(arrow, BorderLayout.WEST);
        notch.add(smallLabel(row.getProductName()), BorderLayout.CENTER);

        if (row.isAmuletUsed()) {
            // the amulet's extra doses are in the product's stack, so say where they came from
            final SmallSpriteIcon amulet = new SmallSpriteIcon(iconOf.apply(ItemID.AMULET_OF_CHEMISTRY_IMBUED_CHARGED));
            final JLabel marker = new JLabel(amulet);
            amulet.repaintWhenLoaded(marker);

            final JPanel east = new JPanel(new BorderLayout(3, 0));
            east.setOpaque(false);
            east.add(marker, BorderLayout.WEST);
            east.add(xp, BorderLayout.EAST);
            notch.add(east, BorderLayout.EAST);
        } else {
            notch.add(xp, BorderLayout.EAST);
        }

        final JLabel productIcon = new JLabel();
        productIcon.setHorizontalAlignment(SwingConstants.RIGHT);
        // a stack of single doses keeps a decimal; a stack of whole potions does not
        final int productIconItemId = row.getProductIconItemId();
        setStack(productIcon, iconOf.apply(productIconItemId), row.getOutputQuantity(),
                PotionDoses.isPotion(productIconItemId) && PotionDoses.doses(productIconItemId) == 1);

        final JLabel caret = new JLabel(new CaretIcon(ColorScheme.LIGHT_GRAY_COLOR));
        caret.setVerticalAlignment(SwingConstants.BOTTOM);
        caret.setBorder(new EmptyBorder(0, 0, 4, 3));

        final JPanel product = new JPanel(new BorderLayout());
        product.setBackground(ROOT_SHADE);
        product.setPreferredSize(PRODUCT_ICON_CELL);
        product.add(productIcon, BorderLayout.CENTER);
        product.add(caret, BorderLayout.EAST);

        final JPanel lines = new JPanel(new GridLayout(2, 1));
        lines.setOpaque(false);
        lines.add(rootName);
        lines.add(notch);

        final JPanel summary = new JPanel(new BorderLayout());
        summary.setBackground(ROOT_SHADE);
        summary.add(rootIcon, BorderLayout.WEST);
        summary.add(lines, BorderLayout.CENTER);
        summary.add(product, BorderLayout.EAST);

        detail.setBackground(DETAIL_SHADE);

        final String held = held(row);
        if (hasDetail) {
            final MouseAdapter toggle = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    final boolean nowExpanded = !detail.isVisible();
                    detail.setVisible(nowExpanded);
                    onToggle.accept(nowExpanded);
                    revalidate();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    summary.setBackground(ROOT_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    summary.setBackground(ROOT_SHADE);
                }
            };
            listenOn(toggle, held + " - click for the xp breakdown", rootIcon, rootName);
        } else {
            rootIcon.setToolTipText(held);
            rootName.setToolTipText(held);
        }

        final String makes = "Makes " + Amounts.abbreviate(row.getOutputQuantity()) + " × " + row.getProductName()
                + (row.isAmuletUsed() ? ", with the alchemist's amulet" : "");
        listenOn(new MenuOpener(menu, summary, hovered -> {
            notch.setBackground(hovered ? ROOT_HOVER : ROOT_SHADE);
            product.setBackground(hovered ? ROOT_HOVER : ROOT_SHADE);
        }), makes + " - click to change", notch, product);

        add(summary, BorderLayout.NORTH);
        add(detail, BorderLayout.CENTER);
    }

    private static JPanel buildSwitchedOff(RowView row, Supplier<JPopupMenu> menu) {
        final JLabel name = smallLabel(row.getRootItemName());
        name.setForeground(OFF_TEXT);

        final JLabel off = smallLabel("off");
        off.setForeground(OFF_TEXT);
        off.setIcon(new CaretIcon(OFF_TEXT));
        off.setHorizontalTextPosition(SwingConstants.LEFT);
        off.setIconTextGap(4);

        final JPanel line = new JPanel(new BorderLayout(6, 0));
        line.setBackground(OFF_SHADE);
        line.setBorder(new CompoundBorder(
                new LineBorder(ColorScheme.DARKER_GRAY_COLOR),
                new EmptyBorder(3, 8, 3, 6)));
        line.add(name, BorderLayout.CENTER);
        line.add(off, BorderLayout.EAST);

        listenOn(new MenuOpener(menu, line, hovered -> line.setBackground(hovered ? OFF_HOVER : OFF_SHADE)),
                held(row) + " - makes nothing, click to choose a product", line);

        return line;
    }

    private static void setStack(JLabel label, AsyncBufferedImage sprite, double quantity, boolean doses) {
        setIcon(label, new StackIcon(sprite, quantity, doses));
    }

    private static void setIcon(JLabel label, StackIcon icon) {
        label.setIcon(icon);
        icon.repaintWhenLoaded(label);
    }

    private static String held(RowView row) {
        return Amounts.abbreviate(row.getRootQuantity()) + " × " + row.getRootItemName();
    }

    /**
     * Their child labels have no listeners of their own, so clicks on them reach these.
     */
    private static void listenOn(MouseAdapter listener, String toolTip, JComponent... components) {
        for (JComponent component : components) {
            component.addMouseListener(listener);
            component.setToolTipText(toolTip);
            component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    /**
     * A scrolling list in a popup, since a plain popup menu grows to fit, and caviar alone has enough
     * options to cover the panel.
     */
    private static JPopupMenu buildMenu(RowView row, BiConsumer<Integer, Integer> onProductSelected,
                                        IntFunction<AsyncBufferedImage> iconOf) {
        final List<RowView.ProductOption> options = row.getProductOptions();
        final JList<RowView.ProductOption> list = new JList<>(options.toArray(new RowView.ProductOption[0]));

        final List<Icon> icons = new ArrayList<>(options.size());
        for (RowView.ProductOption option : options) {
            if (option.getItemId() == row.getRootItemId()) {
                icons.add(MAKE_NOTHING_ICON);
                continue;
            }

            final AsyncBufferedImage image = iconOf.apply(option.getIconItemId());
            image.onLoaded(list::repaint);
            icons.add(new ImageIcon(image));
        }

        list.setFixedCellHeight(MENU_OPTION_HEIGHT);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(l, value, index, isSelected, false);
                final RowView.ProductOption option = (RowView.ProductOption) value;
                setText(option.getName());
                setIcon(icons.get(index));
                setBorder(new EmptyBorder(2, 4, 2, 8));
                if (option.getItemId() == row.getProductItemId()) setFont(getFont().deriveFont(Font.BOLD));
                return this;
            }
        });

        final JScrollPane scroll = new JScrollPane(list) {
            @Override
            public Dimension getPreferredSize() {
                final Dimension options = list.getPreferredSize();
                if (options.height <= MENU_MAX_HEIGHT) return options;

                return new Dimension(options.width + getVerticalScrollBar().getPreferredSize().width, MENU_MAX_HEIGHT);
            }
        };
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(MENU_OPTION_HEIGHT);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        final JPopupMenu menu = new JPopupMenu();
        menu.setLayout(new BorderLayout());
        menu.add(scroll, BorderLayout.CENTER);

        final MouseAdapter picker = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                list.setSelectedIndex(list.locationToIndex(e.getPoint()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                list.clearSelection();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                final int index = list.locationToIndex(e.getPoint());
                if (index < 0) return;

                menu.setVisible(false);
                final int productItemId = options.get(index).getItemId();
                if (productItemId != row.getProductItemId()) {
                    onProductSelected.accept(row.getRootItemId(), productItemId);
                }
            }
        };
        list.addMouseListener(picker);
        list.addMouseMotionListener(picker);

        return menu;
    }

    private static JPanel buildDetail(RowView row) {
        final JPanel detail = new JPanel(new DynamicGridLayout(0, 1, 0, 1));
        detail.setBorder(new EmptyBorder(4, 8, 6, 8));

        if (!row.getXpByItem().isEmpty()) {
            detail.add(heading("By item"));
            for (RowView.Line line : row.getXpByItem()) {
                detail.add(lineOf(line.getName(), Amounts.abbreviate(line.getAmount()), ColorScheme.LIGHT_GRAY_COLOR));
            }
        }

        if (!row.getXpByRecipe().isEmpty()) {
            detail.add(heading("By recipe"));
            for (RowView.RecipeLine line : row.getXpByRecipe()) {
                detail.add(recipeLineOf(line));
            }
        }

        if (!row.getSecondaryDemand().isEmpty()) {
            detail.add(heading("Secondaries"));
            for (RowView.Line line : row.getSecondaryDemand()) {
                detail.add(lineOf(line.getName(), Amounts.abbreviate(line.getAmount()), ColorScheme.LIGHT_GRAY_COLOR));
            }
        }

        return detail;
    }

    private static JLabel heading(String text) {
        final JLabel label = smallLabel(text);
        label.setForeground(ColorScheme.BRAND_ORANGE);

        return label;
    }

    /**
     * The output is what gets cut short when the panel is narrow, so the input always reads in full.
     */
    private static JPanel recipeLineOf(RowView.RecipeLine recipe) {
        final JLabel input = smallLabel(recipe.getInputName());
        input.setIcon(new RightArrowIcon(ColorScheme.LIGHT_GRAY_COLOR));
        input.setHorizontalTextPosition(SwingConstants.LEFT);
        input.setIconTextGap(4);

        final JPanel names = new JPanel(new BorderLayout(4, 0));
        names.setOpaque(false);
        names.add(input, BorderLayout.WEST);
        names.add(smallLabel(recipe.getOutputName()), BorderLayout.CENTER);

        final JPanel line = new JPanel(new BorderLayout(6, 0));
        line.setOpaque(false);
        line.add(names, BorderLayout.CENTER);
        line.add(smallLabel(Amounts.abbreviate(recipe.getXp())), BorderLayout.EAST);

        return line;
    }

    /**
     * The name is what gets cut short when the panel is narrow, so the amount always shows.
     */
    static JPanel lineOf(String name, String amount, Color amountColor) {
        final JLabel amountLabel = smallLabel(amount);
        amountLabel.setForeground(amountColor);

        final JPanel line = new JPanel(new BorderLayout(6, 0));
        line.setOpaque(false);
        line.add(smallLabel(name), BorderLayout.CENTER);
        line.add(amountLabel, BorderLayout.EAST);

        return line;
    }

    private static JLabel smallLabel(String text) {
        final JLabel label = new JLabel(text);
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        return label;
    }
}
