package net.runelite.client.plugins.fastgaming;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("fastgaming")
public interface FastGamingConfig extends Config {

    @ConfigItem(
            position = -2,
            keyName = "reverseOrder",
            name = "Reverse order",
            description = "Clean, drop, or open items from top of inventory first."
    )
    default boolean reverseOrder() {
        return true;
    }

    @ConfigItem(
            keyName = "fastSuperglassMake",
            name = "Fast Superglass Make",
            description = "Change left click options for noted sand, noted giant seaweed, and glassblowing pipe near the bank chest-wreck."
    )
    default boolean fastSuperglassMake() {return false;}

    @ConfigItem(
            keyName = "fastCleanHerbs",
            name = "Fast Clean Herbs",
            description = "Repeatedly clicking one grimy herb will clean all others."
    )
    default boolean fastCleanHerbs() {return false;}

    @ConfigItem(
            keyName = "fastOpenPacks",
            name = "Fast Open Packs",
            description = "Repeatedly clicking one item pack in inventory will open all others."
    )
    default boolean fastOpenPacks() {return false;}

    @ConfigItem(
            keyName = "fastCrushNests",
            name = "Fast Crush Bird Nests",
            description = "Repeatedly clicking pestle and mortar will crush all nests in inv.\r\nDouble-click pestle in inv" +
                    "to allow crushing."
    )
    default boolean fastCrushNests() {return false;}

    @ConfigItem(
            keyName = "fastSearchNests",
            name = "Fast Search Bird Nests",
            description = "Repeatedly clicking first nest will search all nests in inv."
    )
    default boolean fastSearchNests() {return false;}

    @ConfigItem(
            keyName = "fastZeahRC",
            name = "Fast Blood and Soul Crafting",
            description = "Repeatedly clicking chisel will crush dark essence."
    )
    default boolean fastZeahRC() {return false;}

    @ConfigItem(
            keyName = "fastSacredEels",
            name = "Fast Sacred Eels",
            description = "Repeatedly clicking knife will cut sacred eels."
    )
    default boolean fastSacredEels() {return false;}

    @ConfigItem(
            position = 97,
            keyName = "fastDrop",
            name = "Fast Drop",
            description = "Repeatedly clicking one item in inventory will drop all other fast drop items."
    )
    default boolean fastDrop() {return false;}

    @ConfigItem(
            position = 98,
            keyName = "fastDropStacks",
            name = "Fast drop item stacks",
            description = "Include matching stacked items from inv, such as noted items or seeds, when fast dropping."
    )
    default boolean fastDropStacks() {
        return true;
    }

    @ConfigItem(
            position = 99,
            keyName = "keepLastItem",
            name = "Keep last item",
            description = "Don't drop the last item when fast dropping."
    )
    default boolean keepLastItem() {
        return false;
    }

    @ConfigSection(
            name = "Items to fast drop",
            description = "List of items to fast drop, seperated by commas or any whitespace",
            position = 100,
            closedByDefault = true
    )
    String fastDropSection = "section";

    @ConfigItem(
            position = 101,
            keyName = "itemsToDrop",
            name = "Items to fast drop",
            description = "List of items to fast drop, seperated by commas or any whitespace",
            section = fastDropSection
    )
    default String itemsToDrop() {
        return "GOLD BAR\n" +
                "PAYDIRT\n" +
                "REDWOOD LOGS\n" +
                "LEAPING TROUT\n" +
                "LEAPING STURGEON\n" +
                "LEAPING SALMON\n" +
                "GRANITE (2KG)\n" +
                "GRANITE (5KG)\n" +
                "GRANITE (500G)\n" +
                "SANDSTONE (1KG)\n" +
                "SANDSTONE (2KG)\n" +
                "SANDSTONE (5KG)\n" +
                "SANDSTONE (10KG)\n" +
                "IRON ORE\n" +
                "EMPTY PLANT POT\n" +
                "EMPTY LIGHT ORB\n";
    }
}