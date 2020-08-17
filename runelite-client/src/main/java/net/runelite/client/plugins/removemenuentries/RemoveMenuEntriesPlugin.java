package net.runelite.client.plugins.removemenuentries;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Provides;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.*;

import static net.runelite.api.MenuAction.MENU_ACTION_DEPRIORITIZE_OFFSET;

@PluginDescriptor(
        name = "Remove Menu Entries",
        description = "Removes configurable context-specific entries",
        tags = {},
        enabledByDefault = false
)

@Slf4j
public class RemoveMenuEntriesPlugin extends Plugin {

    private static final Splitter STRING_SPLITTER = Splitter.onPattern("[,\r?\n]+").trimResults().omitEmptyStrings();
    private static final Splitter.MapSplitter MAP_SPLITTER = Splitter.onPattern("[,\\r?\\n]+").trimResults().omitEmptyStrings().withKeyValueSeparator(":");
    private static final Set<Integer> RUNELITE_ACTIONS = ImmutableSet.of(MenuAction.RUNELITE.getId(),
            MenuAction.RUNELITE_INFOBOX.getId(), MenuAction.RUNELITE_OVERLAY.getId(),
            MenuAction.RUNELITE_OVERLAY_CONFIG.getId(), MenuAction.RUNELITE_PLAYER.getId());
    private static final Set<MenuAction> NPC_MENU_ACTIONS = ImmutableSet.of(MenuAction.NPC_FIRST_OPTION,
            MenuAction.NPC_SECOND_OPTION, MenuAction.NPC_THIRD_OPTION, MenuAction.NPC_FOURTH_OPTION,
            MenuAction.NPC_FIFTH_OPTION, MenuAction.SPELL_CAST_ON_NPC, MenuAction.ITEM_USE_ON_NPC);
    private static Set<String> NPCsToRemove;
    private static Set<String> lootToRemove;
    private static Map<String, String> customEntries;

    @Inject
    private Client client;
    @Inject
    private RemoveMenuEntriesConfig config;
    @Inject
    private ShiftClickInputListener inputListener;
    @Setter
    private boolean shiftModifier = false;
    @Inject
    private KeyManager keyManager;

    @Provides
    RemoveMenuEntriesConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(RemoveMenuEntriesConfig.class);
    }

    @Subscribe
    public void onFocusChanged(FocusChanged event) {
        if (!event.isFocused())
            shiftModifier = false;
    }

    @Override
    public void startUp() {
        keyManager.registerKeyListener(inputListener);
        NPCsToRemove = Sets.newHashSet(STRING_SPLITTER.splitToList(config.NPCsToRemove().toLowerCase()));
        lootToRemove = Sets.newHashSet(STRING_SPLITTER.splitToList(config.lootToRemove().toLowerCase()));
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals("removemenuentries"))
            return;

        NPCsToRemove = Sets.newHashSet(STRING_SPLITTER.splitToList(config.NPCsToRemove().toLowerCase()));
        lootToRemove = Sets.newHashSet(STRING_SPLITTER.splitToList(config.lootToRemove().toLowerCase()));
    }

    @Subscribe
    public void onMenuOpened(MenuOpened event) {

        if (client.getLocalPlayer() == null || !config.removeExamine())
            return;

        MenuEntry[] entries = event.getMenuEntries();
        if (entries == null)
            return;

        List<MenuEntry> newEntries = new ArrayList<>();
        for (MenuEntry entry : entries) {

            String option = Text.standardize(entry.getOption());
            String target = Text.standardize(entry.getTarget());

            if (!(config.removeExamine() && entry.getOption().equals("Examine")))
                newEntries.add(entry);
        }

        client.setMenuEntries(newEntries.toArray(new MenuEntry[0]));
    }

    @Subscribe
    private void onMenuEntryAdded(MenuEntryAdded event) {
        if (client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN)
            return;

        MenuEntry[] entries = client.getMenuEntries();
        if (entries == null)
            return;

        client.setMenuEntries(Arrays.stream(entries).filter(this::customEntryFilter).toArray(MenuEntry[]::new));
    }

    private boolean customEntryFilter(MenuEntry entry) {

        NPC npc = null;
        if (client.getCachedNPCs().length > entry.getIdentifier())
            npc = client.getCachedNPCs()[entry.getIdentifier()];

        int entryType = entry.getType();

        return (!(config.removeDeadNPCs() && onNPC(entryType) && npc != null && npc.isDead())
                && !(config.removeItemsOnPlayers() && entryType == MenuAction.ITEM_USE_ON_PLAYER.getId())
                && !(config.removePlayerTrade() && entryType == MenuAction.TRADE.getId())
                && !(config.removePlayerFollow() && entryType == MenuAction.FOLLOW.getId())
                && !(config.removeNPCs() && npc != null && npc.getName() != null && NPCsToRemove.contains(npc.getName().toLowerCase()))
                && !(config.removeLoot() && (entryType == MenuAction.EXAMINE_ITEM_GROUND.getId() || entryType == MenuAction.GROUND_ITEM_THIRD_OPTION.getId()) && lootToRemove.contains(client.getItemDefinition(entry.getIdentifier()).getName().toLowerCase()))
                && !(config.shiftWalkUnder() && shiftModifier && onNPC(entryType) && (entry.getIdentifier() != 0 && !RUNELITE_ACTIONS.contains(entryType))));
    }

    private boolean onNPC(int eventType) {
//      check if event is on npc. Copied from NpcIndicatorsPlugin
        if (eventType >= MENU_ACTION_DEPRIORITIZE_OFFSET)
            eventType -= MENU_ACTION_DEPRIORITIZE_OFFSET;
        return NPC_MENU_ACTIONS.contains(MenuAction.of(eventType));
    }
}