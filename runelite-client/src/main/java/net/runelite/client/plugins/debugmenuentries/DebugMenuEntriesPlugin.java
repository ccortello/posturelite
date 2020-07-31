package net.runelite.client.plugins.debugmenuentries;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

@PluginDescriptor(
        name = "Debug Menu Entries",
        description = "Outputs configurable debug information when menu entry clicked.",
        enabledByDefault = false
)

@Slf4j
public class DebugMenuEntriesPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private DebugMenuEntriesConfig config;

    @Provides
    DebugMenuEntriesConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(DebugMenuEntriesConfig.class);
    }

    @Subscribe
    public void onMenuOpened(MenuOpened event) {
        if (config.displayMenusOpened()) {
            MenuEntry[] entries = client.getMenuEntries();
            for (int i = entries.length-1; i >= 0; i--) {
                log.debug("opened menu entry#{} has option |{}|, target |{}|, id |{}|, type |{}|, param0 |{}|, and param1 |{}|",
                        i, entries[i].getOption(), entries[i].getTarget(), entries[i].getIdentifier(), entries[i].getType(), entries[i].getParam0(), entries[i].getParam1());
                if (config.outputToGameChat())
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "opened menu entry#"+i+" has option="
                            + entries[i].getOption() + ", target=" + entries[i].getTarget() + ", id="
                            + entries[i].getIdentifier() + ", type=" + entries[i].getType() + ", param0="
                            + entries[i].getParam0()+", and param1=" + entries[i].getParam1(), "");
            }
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (config.displayEntries() && !event.getOption().equals("Cancel"))
            log.debug("option={}, target={}, identifier={}, type={}, actionParam0={}, actionParam1={}",
                    event.getOption(), event.getTarget(), event.getIdentifier(), event.getType(), event.getActionParam0(), event.getActionParam1());
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (config.displayClickedEntries() && !event.getMenuOption().equals("Cancel")) {
            log.debug("option={}, target={}, identifier={}, widgitID={}, actionParam={}, menuaction={}",
                    event.getMenuOption(), event.getMenuTarget(), event.getId(), event.getWidgetId(), event.getActionParam(), event.getMenuAction());
            if (config.outputToGameChat())
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "option=" + event.getMenuOption() +
                        ", target=" + event.getMenuTarget() + ", identifier=" + event.getId() + ", widgitID=" +
                        event.getWidgetId() + ", actionParam=" + event.getActionParam() + ", menuaction=" + event.getMenuAction(), "");
        }
    }
}