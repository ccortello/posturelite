package net.runelite.client.plugins.leftclickcast;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("leftclickcast")
public interface LeftClickCastConfig extends Config {
    @ConfigItem(
            keyName = "monsters",
            name = "Monsters",
            description = "Comma and newline delimited list of NPCs"
    )
    default String monsters() {
        return "";
    }

    @ConfigItem(
            keyName = "weapons",
            name = "Weapons",
            description = "Weapons to left click cast with"
    )
    default String weapons() {
        return "";
    }

    @ConfigItem(
            position = 3,
            keyName = "leftClickTelegrabWines",
            name = "Left Click Telegrab Wines",
            description = "Set left click to telegrab wines after first manual cast."
    )
    default boolean leftClickTelegrabWines() {
        return false;
    }

    @ConfigItem(
            position = 4,
            keyName = "leftClickAlch",
            name = "Left Click Alch",
            description = "Set left click to recent alchemy, high or low, for the configured items"
    )
    default boolean leftClickAlch() {
        return false;
    }

    @ConfigItem(
            position = 5,
            keyName = "itemsToAlch",
            name = "Items to Left Click Alch",
            description = "Items to left click alch"
    )
    default String itemsToAlch() {
        return "";
    }
}