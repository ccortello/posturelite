package net.runelite.client.plugins.debugmenuentries;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("debugmenuentries")
public interface DebugMenuEntriesConfig extends Config {
    @ConfigItem(
            keyName = "displayMenusOpened",
            name = "Display Menus Opened",
            description = "Logs opened menus to debugger"
    )
    default boolean displayMenusOpened() { return false; }

    @ConfigItem(
            keyName = "displayEntries",
            name = "Display Entries",
            description = "Logs all created entries to debugger"
    )
    default boolean displayEntries() { return false; }

    @ConfigItem(
            keyName = "displayClickedEntries",
            name = "Display Clicked Entries",
            description = "Logs clicked menu entries to debugger"
    )
    default boolean displayClickedEntries() { return false; }

    @ConfigItem(
            keyName = "outputToGameChat",
            name = "Output debug to game chat",
            description = "Creates game chat messages with selected debug information"
    )
    default boolean outputToGameChat() { return false; }
}
