package net.runelite.client.plugins.tickmanip;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("tickmanip")
public interface TickManipConfig extends Config {
    @ConfigItem(
            keyName = "enable1tAltar",
            name = "Enable 1t Bones -> Altar",
            description = "Left-click use bones on altar."
    )
    default boolean enable1tAltar() { return false; }
    @ConfigItem(
            keyName = "bonesTo1t",
            name = "Bones to 1t",
            description = "List of bones to use on altar."
    )
    default String bonesTo1t() { return "dragon bones, babydragon bones\r\nsuperior dragon bones"; }
    @ConfigItem(
            keyName = "enable3tFishing",
            name = "Enable 3t Fishing",
            description = "Left-click use knife on teak or mahogany logs when fishing."
    )
    default boolean enable3tFishing() { return false; }
    @ConfigItem(
            keyName = "enable1tKarams",
            name = "Enable 1t Karams",
            description = "Double click last karambwan in inventory, then click on fire to use karams every other tick."
    )
    default boolean enable1tKarams() { return false; }
    @ConfigItem(
            keyName = "enable3tMining",
            name = "Enable 3t Mining",
            description = "Left-click use knife on teak or mahogany logs when mining."
    )
    default boolean enable3tMining() { return false; }
    @ConfigItem(
            keyName = "excludeSandstone",
            name = "Exclude Sandstone",
            description = "Don't enable left click Knife->Logs for sandstone rocks."
    )
    default boolean excludeSandstone() { return false; }
    @ConfigItem(
            keyName = "excludeEmptyRocks",
            name = "Exclude Empty Rocks",
            description = "Don't enable left click Knife->Logs for empty rocks."
    )
    default boolean excludeEmptyRocks() { return false; }
}