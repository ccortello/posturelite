package net.runelite.client.plugins.titheclicks;

import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.Objects;

@PluginDescriptor(
        name = "Tithe Planting",
        description = "Left patch to plant.",
        tags = {"inventory", "farming"},
        enabledByDefault = false
)
public class TitheClicksPlugin extends Plugin {

    @Inject
    private Client client;

    private static Item[] items;
    private static boolean addEntry;

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        items = Objects.requireNonNull(event.getItemContainer().getItems());
        int offset = 0;
        addEntry = false;
        for (Item item : items) {
            if (item.getId() == -1)
                offset++;
            if (item.getId() == ItemID.LOGAVANO_SEED)
                addEntry = true;
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (client.getGameState() != GameState.LOGGED_IN || !addEntry) {
            return;
        }

        final String option = Text.removeTags(event.getOption()).toLowerCase();
        final String target = Text.removeTags(event.getTarget()).toLowerCase();

        MenuEntry[] entries = client.getMenuEntries();
        MenuEntry temp1 = setMenuEntry("temp", "", 0, 0, 0, 0);

        for (MenuEntry menuEntry : entries) {
            if (menuEntry == null || menuEntry.getTarget() == null)
                continue;
            if (menuEntry.getTarget().equals("<col=ffff>Tithe patch")) {
                temp1 = menuEntry;
            }
        }

        if(!temp1.getOption().contains("temp") && client.getMenuEntries().length >= 3)
        {
            MenuEntry[] patch = new MenuEntry[entries.length + 1];

            for (Item item : items) {
                if (item.getId() == 13425) {
                    patch[patch.length-1] = setMenuEntry("Use", "<col=ff9040>Logavano seed<col=ffffff> -> <col=ffff>Tithe patch", 27383, 1, temp1.getParam0(), temp1.getParam1());
                    break;
                }
            }

            //for (int i = 0; i<patch.length-1; i++)
            //    patch[i] = entries[i];
            System.arraycopy(entries, 0, patch, 0, patch.length - 1);

            if (addEntry)
                client.setMenuEntries(patch);

            temp1 = setMenuEntry("temp", "", 0, 0, 0, 0);
        }
    }


    private MenuEntry setMenuEntry(String option, String target, int ID, int type, int param0, int param1)
    {
        MenuEntry e = new MenuEntry();
        e.setOption(option);
        e.setTarget(target);
        e.setIdentifier(ID);
        e.setType(type);
        e.setParam0(param0);
        e.setParam1(param1);
        return e;
    }
}