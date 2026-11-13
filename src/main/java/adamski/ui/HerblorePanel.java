package adamski.ui;

import adamski.app.HerbloreApp;
import adamski.app.ResultListener;
import adamski.domain.Recipe;
import adamski.app.HerbloreResult;
import adamski.ui.icon.CogIcon;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Fields are touched on the EDT only, unless noted.
 */
public class HerblorePanel extends PluginPanel implements ResultListener {
    private static final int LOGO_SIZE = 20;

    // RuneLite's eye icons (Screen Markers, Loot Tracker; BSD-2), copied in and recoloured to match the cog
    private static final int EYE_SIZE = 14;
    private static final BufferedImage EYE_OPEN = ImageUtil.resizeImage(
            ImageUtil.loadImageResource(HerblorePanel.class, "eye_open.png"), EYE_SIZE, EYE_SIZE);
    private static final BufferedImage EYE_SHUT = ImageUtil.resizeImage(
            ImageUtil.loadImageResource(HerblorePanel.class, "eye_shut.png"), EYE_SIZE, EYE_SIZE);

    private static final Color BUTTON = new Color(0x8C, 0x8C, 0x8C);
    private static final Color BUTTON_HOVER = Color.WHITE;

    private final ItemManager itemManager;
    private final ClientThread clientThread;
    private final HerbloreApp app;
    private final Map<Integer, List<Recipe>> productRecipesByItem;

    // client thread, both: replaced when the potion size changes, which redraws the last result
    private RowViewFactory rowViewFactory;
    private HerbloreResult lastResult;
    private PotionSize potionSize = PotionSize.ONE;

    private final JLabel noData = new JLabel("Open your bank and/or seed vault");
    private final XpBar xpBar = new XpBar();
    private final JPanel rows = new JPanel(new DynamicGridLayout(0, 1, 0, 3));
    private final JPanel secondaries = new JPanel(new DynamicGridLayout(0, 1, 0, 1));
    private final JPanel amulet = new JPanel(new BorderLayout(0, 2));

    // What is open is kept here, because rows and sections are rebuilt for every result
    private final Set<Integer> expandedRootItemIds = new HashSet<>();
    private final Set<Integer> expandedSecondaryItemIds = new HashSet<>();
    private boolean amuletExpanded;
    private boolean recipesExpanded = true;
    private boolean secondariesExpanded = true;

    private final RowOrder rowOrder = new RowOrder();

    /**
     * A row whose view has not changed keeps its panel, so its hover and open menu survive.
     */
    private Map<Integer, RowView> shownRowByRootItemId = new HashMap<>();
    private Map<Integer, RowPanel> panelByRootItemId = new HashMap<>();

    /**
     * Kept here because ItemManager's cache holds only 128 across all plugins, and an evicted sprite
     * comes back blank for a moment, which flickers.
     */
    private final Map<Integer, AsyncBufferedImage> spriteByItemId = new HashMap<>();

    /**
     * Client thread. Set around selectProduct, which publishes before returning, so a result seen
     * while it is set is the one the pick produced.
     */
    private Integer pickingOnRootItemId;

    private boolean offRowsShown;
    private final Consumer<Boolean> offRowsShownSaver;

    /**
     * Every row including those switched off, so the eye can redraw without waiting for a result.
     */
    private List<Integer> arrangedRootItemIds = List.of();

    // built once, so its hover survives redraws
    private final JPanel recipesHeading;
    private final JLabel eye = new JLabel();

    private boolean active;

    /**
     * The latest result to arrive while the panel was closed. Skipping earlier ones loses nothing:
     * a closed panel cannot be picked on.
     */
    private Runnable pendingRender;

    /**
     * @param productRecipesByItem for each item, the recipe making each product it could end at -
     *                             what a row's menu offers
     */
    public HerblorePanel(ItemManager itemManager, ClientThread clientThread, HerbloreApp app,
                         Map<Integer, List<Recipe>> productRecipesByItem, BufferedImage icon,
                         Runnable openSettings, boolean offRowsShown, Consumer<Boolean> offRowsShownSaver) {
        this.itemManager = itemManager;
        this.clientThread = clientThread;
        this.app = app;
        this.offRowsShown = offRowsShown;
        this.offRowsShownSaver = offRowsShownSaver;
        this.productRecipesByItem = productRecipesByItem;
        this.rowViewFactory = new RowViewFactory(this::name, productRecipesByItem, potionSize.getDoses());

        setLayout(new BorderLayout());

        noData.setFont(FontManager.getRunescapeFont());
        noData.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        noData.setHorizontalAlignment(JLabel.CENTER);

        xpBar.setVisible(false);
        final JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        header.add(titleOf(icon, openSettings), BorderLayout.NORTH);
        header.add(noData, BorderLayout.CENTER);
        header.add(xpBar, BorderLayout.SOUTH);

        recipesHeading = recipesHeading();

        rows.setOpaque(false);
        amulet.setOpaque(false);
        amulet.setBorder(new EmptyBorder(10, 0, 0, 0));
        secondaries.setOpaque(false);
        secondaries.setBorder(new EmptyBorder(10, 0, 0, 0));

        final JPanel below = new JPanel(new BorderLayout());
        below.setOpaque(false);
        below.add(amulet, BorderLayout.NORTH);
        below.add(secondaries, BorderLayout.CENTER);

        final JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(header, BorderLayout.NORTH);
        content.add(rows, BorderLayout.CENTER);
        content.add(below, BorderLayout.SOUTH);

        add(content, BorderLayout.NORTH);
    }

    private static JPanel titleOf(BufferedImage icon, Runnable openSettings) {
        final JLabel name = new JLabel("Iron Herblore", JLabel.CENTER);
        name.setFont(FontManager.getRunescapeBoldFont());
        name.setForeground(Color.WHITE);

        final JLabel logo = new JLabel(new ImageIcon(ImageUtil.resizeImage(icon, LOGO_SIZE, LOGO_SIZE, true)));

        final JLabel settings = new JLabel(new CogIcon(BUTTON));
        settings.setToolTipText("Settings");
        settings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        settings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openSettings.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                settings.setIcon(new CogIcon(BUTTON_HOVER));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                settings.setIcon(new CogIcon(BUTTON));
            }
        });

        // the icon and the cog take the same width, so the name centres on the panel
        final Dimension corner = new Dimension(LOGO_SIZE, LOGO_SIZE);
        logo.setPreferredSize(corner);
        settings.setPreferredSize(corner);

        final JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);
        title.setBorder(new CompoundBorder(
                new CompoundBorder(new EmptyBorder(0, 0, 8, 0),
                        new MatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR)),
                new EmptyBorder(0, 0, 6, 0)));
        title.add(logo, BorderLayout.WEST);
        title.add(name, BorderLayout.CENTER);
        title.add(settings, BorderLayout.EAST);
        return title;
    }

    /**
     * Client thread. Display only, so the app is not asked to recalculate.
     */
    public void setPotionSize(PotionSize potionSize) {
        if (potionSize == this.potionSize) return;

        this.potionSize = potionSize;
        rowViewFactory = new RowViewFactory(this::name, productRecipesByItem, potionSize.getDoses());
        if (lastResult != null) onResultChanged(lastResult);
    }

    @Override
    public void onResultChanged(HerbloreResult result) {
        lastResult = result;
        final XpBarView xpBarView = XpBarView.of(result.getCurrentXp(), result.getTotalXp());

        // Names resolve here, on the client thread - ItemManager cannot be touched from the EDT.
        // Rows worth nothing stay listed: a row stopped at an unf vial still needs its picker.
        final List<RowView> rowViews = result.getChainResults().stream()
                .map(chain -> rowViewFactory.rowOf(chain,
                        result.getAmuletChainRootItemIds().contains(chain.getRootItemId())))
                .collect(Collectors.toList());

        final List<SecondaryView> secondaryViews = rowViewFactory.secondariesOf(result.getSecondaryBalance());
        final AmuletView amuletView = rowViewFactory.amuletOf(result.getAmuletOptions(), result.getAmuletCharges());
        final Integer pickedOnRootItemId = pickingOnRootItemId;

        SwingUtilities.invokeLater(() -> {
            final Runnable render = () -> render(xpBarView, rowViews, pickedOnRootItemId, secondaryViews, amuletView);

            if (active) {
                render.run();
            } else {
                pendingRender = render;
            }
        });
    }

    @Override
    public void onActivate() {
        active = true;

        if (pendingRender != null) {
            pendingRender.run();
            pendingRender = null;
        }
    }

    @Override
    public void onDeactivate() {
        active = false;
    }

    private void render(XpBarView xpBarView, List<RowView> rowViews, Integer pickedOnRootItemId,
                        List<SecondaryView> secondaryViews, AmuletView amuletView) {
        noData.setVisible(false);
        xpBar.setVisible(true);
        xpBar.setView(xpBarView);

        final Map<Integer, RowView> rowByRootItemId = new HashMap<>();
        for (RowView row : rowViews) {
            rowByRootItemId.put(row.getRootItemId(), row);
        }

        arrangedRootItemIds = rowOrder.arrange(
                rowViews.stream().map(RowView::getRootItemId).collect(Collectors.toList()),
                pickedOnRootItemId);
        drawRows(rowByRootItemId);

        amulet.removeAll();
        if (!amuletView.getLines().isEmpty()) {
            final AmuletPanel choices = new AmuletPanel(amuletView, this::setAmuletDoses, this::clearAmuletDoses);
            choices.setVisible(amuletExpanded);
            amulet.add(amuletHeadingOf(amuletView, choices), BorderLayout.NORTH);
            amulet.add(choices, BorderLayout.CENTER);
        }

        secondaries.removeAll();
        if (!secondaryViews.isEmpty()) {
            final JPanel list = new JPanel(new DynamicGridLayout(0, 1, 0, 1));
            list.setBackground(RowPanel.ROOT_SHADE);
            list.setBorder(new EmptyBorder(3, 0, 3, 0));
            list.setVisible(secondariesExpanded);

            secondaries.add(new SectionHeading("Secondaries", secondariesExpanded, expanded -> {
                secondariesExpanded = expanded;
                list.setVisible(expanded);
                secondaries.revalidate();
                secondaries.repaint();
            }, null, null));
            secondaries.add(list);

            for (SecondaryView secondary : secondaryViews) {
                final int itemId = secondary.getItemId();
                list.add(new SecondaryPanel(secondary, expandedSecondaryItemIds.contains(itemId),
                        expanded -> {
                            if (expanded) expandedSecondaryItemIds.add(itemId);
                            else expandedSecondaryItemIds.remove(itemId);
                        }));
            }
        }

        revalidate();
        repaint();
    }

    /**
     * The app is only driven from the client thread; the new result comes back through
     * onResultChanged.
     */
    private void selectProduct(int rootItemId, int productItemId) {
        clientThread.invoke(() -> {
            pickingOnRootItemId = rootItemId;
            try {
                app.selectProduct(rootItemId, productItemId);
            } finally {
                pickingOnRootItemId = null;
            }
        });
    }

    private void setAmuletDoses(int recipeId, int dosesPerPotion) {
        clientThread.invoke(() -> app.setAmuletDoses(recipeId, dosesPerPotion));
    }

    private void clearAmuletDoses(int recipeId) {
        clientThread.invoke(() -> app.clearAmuletDoses(recipeId));
    }

    private JPanel amuletHeadingOf(AmuletView amuletView, AmuletPanel choices) {
        final JLabel charges = new JLabel(Math.round(amuletView.getCharges()) == 0
                ? ""
                : Amounts.abbreviate(Math.ceil(amuletView.getCharges())) + " charges");
        charges.setFont(FontManager.getRunescapeSmallFont());
        charges.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        charges.setToolTipText("One charge per extra dose the amulet makes, on average");

        return new SectionHeading("Alchemist's amulet", amuletExpanded, expanded -> {
            amuletExpanded = expanded;
            choices.setVisible(expanded);
            amulet.revalidate();
            amulet.repaint();
        }, charges, null);
    }

    private void drawRows(Map<Integer, RowView> rowByRootItemId) {
        final Map<Integer, RowPanel> panels = new HashMap<>();
        rows.removeAll();
        if (!arrangedRootItemIds.isEmpty()) {
            rows.add(recipesHeading);
        }

        for (int rootItemId : arrangedRootItemIds) {
            final RowView row = rowByRootItemId.get(rootItemId);
            if (!offRowsShown && row.isSwitchedOff()) continue;

            RowPanel panel = panelByRootItemId.get(rootItemId);
            if (panel == null || !row.equals(shownRowByRootItemId.get(rootItemId))) {
                panel = new RowPanel(row, expandedRootItemIds.contains(rootItemId),
                        expanded -> {
                            if (expanded) expandedRootItemIds.add(rootItemId);
                            else expandedRootItemIds.remove(rootItemId);
                        },
                        this::selectProduct,
                        this::spriteOf);
            }

            panels.put(rootItemId, panel);
            // kept even while folded, so opening the section rebuilds nothing
            if (recipesExpanded) rows.add(panel);
        }

        shownRowByRootItemId = rowByRootItemId;
        panelByRootItemId = panels;
    }

    private JPanel recipesHeading() {
        updateEye(false);
        eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eye.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                offRowsShown = !offRowsShown;
                offRowsShownSaver.accept(offRowsShown);
                updateEye(true);
                drawRows(shownRowByRootItemId);
                revalidate();
                repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                updateEye(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateEye(false);
            }
        });

        return new SectionHeading("Recipes", recipesExpanded, expanded -> {
            recipesExpanded = expanded;
            drawRows(shownRowByRootItemId);
            revalidate();
            repaint();
        }, null, eye);
    }

    private void updateEye(boolean hovered) {
        final BufferedImage image = offRowsShown ? EYE_OPEN : EYE_SHUT;
        eye.setIcon(new ImageIcon(tint(image, hovered ? BUTTON_HOVER : BUTTON)));
        eye.setToolTipText(offRowsShown ? "Hide rows switched off" : "Show rows switched off");
    }

    /**
     * Recolours while keeping each pixel's transparency. ImageUtil.fillImage makes every visible
     * pixel solid, which turns the eye, drawn in transparency, into a blob.
     */
    private static BufferedImage tint(BufferedImage image, Color color) {
        final BufferedImage tinted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final int rgb = color.getRGB() & 0xFFFFFF;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                tinted.setRGB(x, y, (image.getRGB(x, y) & 0xFF000000) | rgb);
            }
        }

        return tinted;
    }

    private AsyncBufferedImage spriteOf(int itemId) {
        return spriteByItemId.computeIfAbsent(itemId, itemManager::getImage);
    }

    private String name(int itemId) {
        return itemManager.getItemComposition(itemId).getName();
    }
}
