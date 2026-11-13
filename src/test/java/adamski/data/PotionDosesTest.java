package adamski.data;

import net.runelite.api.gameval.ItemID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PotionDosesTest {
    @Test
    public void aPotionIsShownInTheSizeAskedFor() {
        assertEquals(ItemID._4DOSE1ATTACK, PotionDoses.largestVariantOf(ItemID._1DOSE1ATTACK, 4));
        assertEquals(ItemID._3DOSE1ATTACK, PotionDoses.largestVariantOf(ItemID._1DOSE1ATTACK, 3));
        assertEquals(ItemID._1DOSE1ATTACK, PotionDoses.largestVariantOf(ItemID._1DOSE1ATTACK, 1));
    }

    @Test
    public void anyVariantFindsTheSameSize() {
        assertEquals(ItemID._2DOSE1ATTACK, PotionDoses.largestVariantOf(ItemID._4DOSE1ATTACK, 2));
    }

    @Test
    public void aMixStopsAtTwoDoses() {
        assertEquals(ItemID.BRUTAL_2DOSE1ATTACK, PotionDoses.largestVariantOf(ItemID.BRUTAL_1DOSE1ATTACK, 4));
    }

    @Test
    public void somethingThatIsNotAPotionIsItself() {
        assertEquals(ItemID.GUAM_LEAF, PotionDoses.largestVariantOf(ItemID.GUAM_LEAF, 4));
    }
}
