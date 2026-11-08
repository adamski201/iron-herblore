package adamski.ui;

import lombok.Value;

import java.util.List;

/**
 * The alchemist's amulet section, every name resolved. It holds only the choices - the extra doses
 * they make show on the rows, so listing them here too would read as more on top.
 */
@Value
class AmuletView {
    /**
     * The charges the choices use, one per extra dose.
     */
    double charges;

    List<Line> lines;

    @Value
    static class Line {
        int recipeId;

        /**
         * The potion it makes.
         */
        String name;

        /**
         * The potion sizes it can be made in, largest first. More than one is an upgrade, offered as
         * a choice.
         */
        List<Integer> dosesPerPotionOptions;

        /**
         * The size chosen, or null if the amulet is not used on it.
         */
        Integer dosesPerPotion;

        boolean isUsed() {
            return dosesPerPotion != null;
        }
    }
}
