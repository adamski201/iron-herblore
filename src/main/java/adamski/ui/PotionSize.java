package adamski.ui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How many doses a potion is shown as. Display only - everything is still counted in single doses.
 * The constant names are saved in the config, so never rename them.
 */
@Getter
@RequiredArgsConstructor
public enum PotionSize {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4);

    private final int doses;

    @Override
    public String toString() {
        return "(" + doses + ")";
    }
}
