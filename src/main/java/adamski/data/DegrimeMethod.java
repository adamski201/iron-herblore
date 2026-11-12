package adamski.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How grimy herbs are cleaned, and the share of the cleaning xp each way earns.
 */
@Getter
@RequiredArgsConstructor
public enum DegrimeMethod {
    MANUAL("Manual", 1),
    /**
     * The Arceuus spell gives half the Herblore xp of cleaning by hand.
     */
    SPELL("Degrime spell", 0.5),
    /**
     * Paying Zahur, in Nardah, gives no Herblore xp.
     */
    ZAHUR("Zahur", 0);

    private final String displayName;
    private final double xpFactor;

    @Override
    public String toString() {
        return displayName;
    }
}
