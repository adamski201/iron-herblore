package adamski.ui;

import lombok.Value;

import java.util.List;

/**
 * Only the choices: the extra doses show on the rows, and repeating them here would read as more.
 */
@Value
class AmuletView {
    double charges;

    List<Line> lines;

    @Value
    static class Line {
        int recipeId;

        String name;

        /**
         * More than one is an upgrade, offered as a choice.
         */
        List<Integer> dosesPerPotionOptions;

        /**
         * Null if the amulet is not used on it.
         */
        Integer dosesPerPotion;

        boolean isUsed() {
            return dosesPerPotion != null;
        }
    }
}
