package net.runelite.client.plugins.removemenuentries;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("removemenuentries")
public interface RemoveMenuEntriesConfig extends Config {

    @ConfigItem(
            keyName = "removeItemsOnPlayers",
            name = "Remove Use Item on Players",
            description = "Removes all entries of Use {item} -> Player"
    )
    default boolean removeItemsOnPlayers() {return false;}

    @ConfigItem(
            keyName = "removeDeadNPCs",
            name = "Remove Dead NPC Entries",
            description = "Remove entries of NPCs with 0 hitpoints"
    )
    default boolean removeDeadNPCs() {
        return false;
    }

    @ConfigItem(
            keyName = "shiftWalkUnder",
            name = "Shift Walk Under",
            description = "Holding shift removes all options except Walk Here and Cancel"
    )
    default boolean shiftWalkUnder() {
        return false;
    }

    @ConfigItem(
            keyName = "removeExamine",
            name = "Remove Examine",
            description = "Remove all Examine entries"
    )
    default boolean removeExamine() {return false;}

    @ConfigItem(
            keyName = "removePlayerTrade",
            name = "Remove Trade with Players",
            description = "Remove all Trade entries for players"
    )
    default boolean removePlayerTrade() {return false;}

    @ConfigItem(
            keyName = "removePlayerFollow",
            name = "Remove Follow with Players",
            description = "Remove all Follow entries for players"
    )
    default boolean removePlayerFollow() {return false;}

    @ConfigItem(
            keyName = "reanimateOnlyHeads",
            name = "Reanimate only heads",
            description = "Remove all entries without 'Ensouled' when reanimating"
    )
    default boolean reanimateOnlyHeads() {return false;}

    @ConfigItem(
            position = 100,
            keyName = "removeNPCs",
            name = "Remove specific NPC entries",
            description = "Remove all entries for specified NPCs"
    )
    default boolean removeNPCs() {return false;}

    @ConfigItem(
            position = 101,
            keyName = "NPCsToRemove",
            name = "NPCs to Remove",
            description = "List of NPCs to remove if Remove Specific NPC Entries is enabled."
    )
    default String NPCsToRemove() {return "";}

    @ConfigItem(
            position = 102,
            keyName = "removeLoot",
            name = "Remove Loot",
            description = "Remove ground options for specific items"
    )
    default boolean removeLoot() {return false;}

    @ConfigItem(
            position = 103,
            keyName = "lootToRemove",
            name = "Loot to Remove",
            description = "List of items to remove ground options for."
    )
    default String lootToRemove() {return "";}
}