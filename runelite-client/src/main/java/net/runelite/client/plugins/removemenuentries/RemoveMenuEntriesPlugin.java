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

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

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
    private static final Set<MenuAction> GROUND_OPTIONS = ImmutableSet.of(MenuAction.EXAMINE_ITEM_GROUND,
            MenuAction.GROUND_ITEM_FIRST_OPTION, MenuAction.GROUND_ITEM_SECOND_OPTION, MenuAction.GROUND_ITEM_THIRD_OPTION,
            MenuAction.GROUND_ITEM_FOURTH_OPTION, MenuAction.GROUND_ITEM_FIFTH_OPTION, MenuAction.ITEM_USE_ON_GROUND_ITEM,
            MenuAction.SPELL_CAST_ON_GROUND_ITEM);
    private static final Set<MenuAction> EXAMINE_OPTIONS = ImmutableSet.of(MenuAction.EXAMINE_ITEM_GROUND,
            MenuAction.EXAMINE_NPC, MenuAction.EXAMINE_OBJECT);
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
        if (event.getGroup().equals("removemenuentries")) {
            NPCsToRemove = Sets.newHashSet(STRING_SPLITTER.splitToList(config.NPCsToRemove().toLowerCase()));
            lootToRemove = Sets.newHashSet(STRING_SPLITTER.splitToList(config.lootToRemove().toLowerCase()));
        }
    }

    @Subscribe
    public void onMenuOpened(MenuOpened event) {

        if (client.getLocalPlayer() != null && config.removeExamine())
            client.setMenuEntries(Arrays.stream(event.getMenuEntries()).filter(this::examineFilter).toArray(MenuEntry[]::new));
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

    private boolean examineFilter(MenuEntry entry) {
        return !(config.removeExamine() && EXAMINE_OPTIONS.contains(MenuAction.of(entry.getType())));
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
                && !(config.removeLoot() && GROUND_OPTIONS.contains(MenuAction.of(entryType)) && lootToRemove.contains(client.getItemDefinition(entry.getIdentifier()).getName().toLowerCase()))
                && !(config.shiftWalkUnder() && shiftModifier && onNPC(entryType) && (entry.getIdentifier() != 0 && !RUNELITE_ACTIONS.contains(entryType))));
    }

    // Copied from NpcIndicatorsPlugin
    private boolean onNPC(int eventType) {
        if (eventType >= MENU_ACTION_DEPRIORITIZE_OFFSET)
            eventType -= MENU_ACTION_DEPRIORITIZE_OFFSET;
        return NPC_MENU_ACTIONS.contains(MenuAction.of(eventType));
    }
}