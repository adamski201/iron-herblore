package adamski.infrastructure;

import adamski.data.DegrimeMethod;
import adamski.data.Recipes;
import adamski.data.SecondaryPrecursors;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(HerbloreConfig.GROUP)
public interface HerbloreConfig extends net.runelite.client.config.Config
{
    String GROUP = "iron-herblore";

    @ConfigSection(
        name = "Secondaries",
        description = "What counts towards the secondaries the rows need",
        position = 10
    )
    String SECONDARIES = "secondaries";

    @ConfigSection(
        name = "Seeds",
        description = "Whether herb seeds and coral frags count, and how many herbs or coral each grows into."
            + " Yields depend on your Farming level, compost, magic secateurs and the patch, so set your own average",
        position = 11,
        closedByDefault = true
    )
    String SEEDS = "seeds";

    @ConfigSection(
        name = "Caviar",
        description = "How caviar is made from leaping sturgeon",
        position = 12,
        closedByDefault = true
    )
    String CAVIAR = "caviar";

    @ConfigItem(
        keyName = "usePrescriptionGoggles",
        name = "Use prescription goggles",
        description = "Assume prescription goggles are always worn: a 10% chance to"
            + " save the secondary where applicable (inc. torstol in the case of SCB and caviar for barbarian mixes).",
        position = 0
    )
    default boolean usePrescriptionGoggles()
    {
        return false;
    }

    @ConfigItem(
        keyName = "useAlchemistsAmulet",
        name = "Use alchemist's amulet",
        description = "Adds a section to the panel for choosing which potions you use the alchemist's amulet on:"
            + " a 15% chance of an extra dose, using a charge",
        position = 1
    )
    default boolean useAlchemistsAmulet()
    {
        return false;
    }

    @ConfigItem(
        keyName = "degrimeMethod",
        name = "Degrime method",
        description = "How you clean grimy herbs. The Degrime spell gives half the Herblore xp of cleaning by hand,"
            + " and paying Zahur gives none",
        position = 2
    )
    default DegrimeMethod degrimeMethod()
    {
        return DegrimeMethod.MANUAL;
    }

    @ConfigItem(
        keyName = "potionSize",
        name = "Show potions as",
        description = "How many doses a potion is shown and counted as in the panel.",
        position = 3
    )
    default PotionSizeSetting potionSize()
    {
        return PotionSizeSetting.ONE;
    }

    @Range(max = 100)
    @Units(Units.PERCENT)
    @ConfigItem(
        keyName = "caviarSuccessRate",
        name = "Caviar success rate",
        description = "How often cutting a leaping sturgeon gives caviar. Always 100% from 80 Cooking;"
            + " lower it if you will cut them below that",
        section = CAVIAR,
        position = 0
    )
    default int caviarSuccessRate()
    {
        return 100;
    }

    @ConfigItem(
        keyName = "countSecondariesMadeByRows",
        name = "Count secondaries made by rows",
        description = "Whether a row's output should contribute to the secondary balance e.g."
            + " super attacks from the irit row contributing to the secondaries needed for super combat potions",
        section = SECONDARIES,
        position = 0
    )
    default boolean countSecondariesMadeByRows()
    {
        return true;
    }

    @ConfigItem(
        keyName = "countSecondaryPrecursors",
        name = "Count secondary precursors",
        description = "Whether precursors to secondaries should be included in the secondary balance e.g. bird nests"
            + " towards crushed nests, unicorn horns towards unicorn horn dust",
        section = SECONDARIES,
        position = 1
    )
    default boolean countSecondaryPrecursors()
    {
        return true;
    }

    @ConfigItem(
        keyName = "lavaShardsPerScale",
        name = "Lava shards per scale",
        description = "How many lava shards each lava scale grinds into. It depends on your Herblore level"
            + " and whether the hard Wilderness diary is done, so set your own average",
        section = SECONDARIES,
        position = 2
    )
    default double lavaShardsPerScale()
    {
        return SecondaryPrecursors.DEFAULT_LAVA_SHARDS_PER_SCALE;
    }

    @ConfigItem(
        keyName = "countSeeds",
        name = "Count seeds and frags",
        description = "Whether herb seeds and coral frags count towards the xp",
        section = SEEDS,
        position = 0
    )
    default boolean countSeeds()
    {
        return true;
    }

    @ConfigItem(
        keyName = "guamSeedYield",
        name = "Guam",
        description = "How many herbs each guam seed grows into",
        section = SEEDS,
        position = 1
    )
    default double guamSeedYield()
    {
        return Recipes.DEFAULT_HERB_YIELD;
    }

    @ConfigItem(
        keyName = "marrentillSeedYield",
        name = "Marrentill",
        description = "How many herbs each marrentill seed grows into",
        section = SEEDS,
        position = 2
    )
    default double marrentillSeedYield()
    {
        return Recipes.DEFAULT_HERB_YIELD;
    }

    @ConfigItem(
        keyName = "tarrominSeedYield",
        name = "Tarromin",
        description = "How many herbs each tarromin seed grows into",
        section = SEEDS,
        position = 3
    )
    default double tarrominSeedYield()
    {
        return Recipes.DEFAULT_HERB_YIELD;
    }

    @ConfigItem(
        keyName = "harralanderSeedYield",
        name = "Harralander",
        description = "How many herbs each harralander seed grows into",
        section = SEEDS,
        position = 4
    )
    default double harralanderSeedYield()
    {
        return Recipes.DEFAULT_HERB_YIELD;
    }

    @ConfigItem(
        keyName = "ranarrSeedYield",
        name = "Ranarr",
        description = "How many herbs each ranarr seed grows into",
        section = SEEDS,
        position = 5
    )
    default double ranarrSeedYield()
    {
        return Recipes.DEFAULT_HERB_YIELD;
    }

    @ConfigItem(
        keyName = "toadflaxSeedYield",
        name = "Toadflax",
        description = "How many herbs each toadflax seed grows into",
        section = SEEDS,
        position = 6
    )
    default double toadflaxSeedYield()
    {
        return Recipes.DEFAULT_HERB_YIELD;
    }

    @ConfigItem(
        keyName = "iritSeedYield",
        name = "Irit",
        description = "How many herbs each irit seed grows into",
        section = SEEDS,
        position = 7
    )
    default double iritSeedYield()
    {
        return Recipes.DEFAULT_HERB_YIELD;
    }

    @ConfigItem(
        keyName = "avantoeSeedYield",
        name = "Avantoe",
        description = "How many herbs each avantoe seed grows into",
        section = SEEDS,
        position = 8
    )
    default double avantoeSeedYield()
    {
        return Recipes.DEFAULT_HERB_YIELD;
    }

    @ConfigItem(
        keyName = "kwuarmSeedYield",
        name = "Kwuarm",
        description = "How many herbs each kwuarm seed grows into",
        section = SEEDS,
        position = 9
    )
    default double kwuarmSeedYield()
    {
        return Recipes.DEFAULT_HERB_YIELD;
    }

    @ConfigItem(
        keyName = "huascaSeedYield",
        name = "Huasca",
        description = "How many herbs each huasca seed grows into",
        section = SEEDS,
        position = 10
    )
    default double huascaSeedYield()
    {
        return Recipes.DEFAULT_HERB_YIELD;
    }

    @ConfigItem(
        keyName = "snapdragonSeedYield",
        name = "Snapdragon",
        description = "How many herbs each snapdragon seed grows into",
        section = SEEDS,
        position = 11
    )
    default double snapdragonSeedYield()
    {
        return Recipes.DEFAULT_HERB_YIELD;
    }

    @ConfigItem(
        keyName = "cadantineSeedYield",
        name = "Cadantine",
        description = "How many herbs each cadantine seed grows into",
        section = SEEDS,
        position = 12
    )
    default double cadantineSeedYield()
    {
        return Recipes.DEFAULT_HERB_YIELD;
    }

    @ConfigItem(
        keyName = "lantadymeSeedYield",
        name = "Lantadyme",
        description = "How many herbs each lantadyme seed grows into",
        section = SEEDS,
        position = 13
    )
    default double lantadymeSeedYield()
    {
        return Recipes.DEFAULT_HERB_YIELD;
    }

    @ConfigItem(
        keyName = "dwarfWeedSeedYield",
        name = "Dwarf weed",
        description = "How many herbs each dwarf weed seed grows into",
        section = SEEDS,
        position = 14
    )
    default double dwarfWeedSeedYield()
    {
        return Recipes.DEFAULT_HERB_YIELD;
    }

    @ConfigItem(
        keyName = "torstolSeedYield",
        name = "Torstol",
        description = "How many herbs each torstol seed grows into",
        section = SEEDS,
        position = 15
    )
    default double torstolSeedYield()
    {
        return Recipes.DEFAULT_HERB_YIELD;
    }

    @ConfigItem(
        keyName = "elkhornCoralYield",
        name = "Elkhorn coral",
        description = "How much elkhorn coral each frag grows into",
        section = SEEDS,
        position = 100
    )
    default double elkhornCoralYield()
    {
        return Recipes.DEFAULT_CORAL_YIELD;
    }

    @ConfigItem(
        keyName = "pillarCoralYield",
        name = "Pillar coral",
        description = "How much pillar coral each frag grows into",
        section = SEEDS,
        position = 101
    )
    default double pillarCoralYield()
    {
        return Recipes.DEFAULT_CORAL_YIELD;
    }

    @ConfigItem(
        keyName = "umbralCoralYield",
        name = "Umbral coral",
        description = "How much umbral coral each frag grows into",
        section = SEEDS,
        position = 102
    )
    default double umbralCoralYield()
    {
        return Recipes.DEFAULT_CORAL_YIELD;
    }
}
