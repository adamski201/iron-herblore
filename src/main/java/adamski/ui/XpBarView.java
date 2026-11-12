package adamski.ui;

import lombok.Value;
import net.runelite.api.Experience;

/**
 * Virtual levels count, up to 126.
 */
@Value
class XpBarView {
    int currentLevel;

    int bankedLevel;

    int nextLevel;

    /**
     * How far through the banked level towards the next, 0 to 1.
     */
    double progress;

    /**
     * How far through the level the player already is when the bank gains no level; otherwise 0.
     */
    double startProgress;

    double bankedXp;

    static XpBarView of(int currentXp, double bankedXp) {
        final int afterXp = (int) Math.min(Experience.MAX_SKILL_XP, currentXp + Math.max(0, bankedXp));
        final int bankedLevel = Experience.getLevelForXp(afterXp);

        final int currentLevel = Experience.getLevelForXp(currentXp);

        return new XpBarView(currentLevel, bankedLevel,
                Math.min(bankedLevel + 1, Experience.MAX_VIRT_LEVEL),
                progressThrough(bankedLevel, afterXp),
                bankedLevel > currentLevel ? 0 : progressThrough(currentLevel, currentXp),
                bankedXp);
    }

    boolean levelGained() {
        return bankedLevel > currentLevel;
    }

    private static double progressThrough(int level, int xp) {
        if (level >= Experience.MAX_VIRT_LEVEL) return 1;

        final int start = Experience.getXpForLevel(level);
        final int end = Experience.getXpForLevel(level + 1);

        return (double) (xp - start) / (end - start);
    }
}
