package net.runelite.client.plugins.quinnmasterfarmers;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("quinnmasterfarmers")
public interface QuinnMasterFarmersConfig extends Config
{
    @ConfigItem(
            keyName = "maxItemsInInv",
            name = "Max items in inventory",
            description = "The number of items where you'll start dropping seeds"
    )
    default int maxItemsInInv()
    {
        return 26;
    }

    @ConfigItem(
            keyName = "dropSixMostCommonSeeds",
            name = "Drop six most common seeds",
            description = "Turn on to enable dropping of potato, onion, cabbage, tomato, barley, and hammerstone seeds."
    )
    default boolean dropSixMostCommonSeeds()
    {
        return true;
    }

    @ConfigItem(
            position = 3,
            keyName = "seedsToSkip",
            name = "Seeds to skip",
            description = "List of common seeds you don't want to drop."
    )
    default String seedsToSkip() {return "";}
}
