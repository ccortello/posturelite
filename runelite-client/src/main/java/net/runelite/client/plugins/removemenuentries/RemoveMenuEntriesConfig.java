package net.runelite.client.plugins.removemenuentries;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

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
            description = "Holding shift removes all npc entries"
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

    @ConfigSection(
            name = "Dead NPC Blacklist",
            description = "List of NPCs to not remove when dead",
            position = 101,
            closedByDefault = true
    )
    String deadNPCblacklistSection = "deadNPCblacklistSection";

    @ConfigItem(
            keyName = "deadNPCblacklist",
            name = "Dead NPC Blacklist",
            description = "List of NPCs to not remove when dead.",
            section = deadNPCblacklistSection
    )
    default String NPCsBlacklist() {return "chompy";}

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
            position = 102,
            keyName = "removeNPCs",
            name = "Remove NPCs",
            description = "Remove all entries for specified NPCs"
    )
    default boolean removeNPCs() {return false;}

    @ConfigSection(
            name = "NPCs to Remove",
            description = "",
            position = 103,
            closedByDefault = true
    )
    String NPCsToRemoveSection = "NPCsToRemoveSection";

    @ConfigItem(
            keyName = "NPCsToRemove",
            name = "NPCs to Remove",
            description = "List of NPCs to remove if Remove Specific NPC Entries is enabled.",
            section = NPCsToRemoveSection
    )
    default String NPCsToRemove() {return "";}

    @ConfigItem(
            position = 103,
            keyName = "removeLoot",
            name = "Remove Ground Items",
            description = "Remove ground options for specific items"
    )
    default boolean removeLoot() {return false;}

    @ConfigSection(
            name = "Ground Items to Remove",
            description = "List of items to remove ground options for.",
            position = 104,
            closedByDefault = true
    )
    String lootToRemoveSection = "lootToRemoveSection";

    @ConfigItem(
            keyName = "lootToRemove",
            name = "Ground Items to Hide",
            description = "List of items to remove ground options for.",
            section = lootToRemoveSection
    )
    default String lootToRemove() {return "";}

    @ConfigItem(
            keyName = "removeCustomEntries",
            name = "Remove Custom Entries",
            description = "Enable removal of custom entries",
            position = 105
    )
    default boolean removeCustomEntries() {return false;}

    @ConfigSection(
            name = "Custom Entries to Remove",
            description = "Custom entries to remove, in the form option:target",
            position = 106,
            closedByDefault = true
    )
    String customEntriesSection = "customEntriesSection";

    @ConfigItem(
            keyName = "customEntries",
            name = "Custom Entries",
            description = "Custom entries to remove, in the form option:target",
            section = customEntriesSection
    )
    default String customEntries() {return "";}
}