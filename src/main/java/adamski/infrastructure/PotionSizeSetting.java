package adamski.infrastructure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The choices of the "Show potions as" setting. The panel never sees this type - the plugin hands it
 * {@link #getDoses()}, so only a plain number crosses from the config to the panel. The constant names
 * are saved in the config, so never rename them.
 */
@Getter
@RequiredArgsConstructor
public enum PotionSizeSetting {
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
