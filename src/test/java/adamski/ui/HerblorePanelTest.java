package adamski.ui;

import adamski.infrastructure.HerbloreConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HerblorePanelTest {
    @Test
    public void thePanelKeepsItsStateUnderThePluginsConfigGroup() {
        assertEquals(HerbloreConfig.GROUP, HerblorePanel.CONFIG_GROUP);
    }
}
