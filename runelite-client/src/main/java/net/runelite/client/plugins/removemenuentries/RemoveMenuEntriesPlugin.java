package net.runelite.client.plugins.removemenuentries;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
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
import java.util.Arrays;
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
//    private static final Splitter.MapSplitter MAP_SPLITTER = Splitter.onPattern("[,\\r?\\n]+").trimResults().omitEmptyStrings().withKeyValueSeparator(":");
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
    private static Set<String> deadNPCblacklist;
    private static Set<String> NPCsToRemove;
    private static Set<String> lootToRemove;
    private static final SetMultimap<String, String> CUSTOM_ENTRIES = MultimapBuilder.SetMultimapBuilder.hashKeys().hashSetValues().build();

    @Inject
    private Client client;
    @Inject
    private RemoveMenuEntriesConfig config;
    @Inject
    private ShiftClickInputListener inputListener;
    @Inject
    private KeyManager keyManager;

    @Setter
    private boolean shiftModifier = false;

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
        deadNPCblacklist = Sets.newHashSet(STRING_SPLITTER.splitToList(config.NPCsBlacklist().toLowerCase()));
        NPCsToRemove = Sets.newHashSet(STRING_SPLITTER.splitToList(config.NPCsToRemove().toLowerCase()));
        lootToRemove = Sets.newHashSet(STRING_SPLITTER.splitToList(config.lootToRemove().toLowerCase()));
        for (String s : STRING_SPLITTER.split(config.customEntries().toLowerCase()))
            CUSTOM_ENTRIES.put(s.substring(0, s.indexOf(":")), s.substring(s.indexOf(":") + 1));
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("removemenuentries")) {
            deadNPCblacklist = Sets.newHashSet(STRING_SPLITTER.splitToList(config.NPCsBlacklist().toLowerCase()));
            NPCsToRemove = Sets.newHashSet(STRING_SPLITTER.splitToList(config.NPCsToRemove().toLowerCase()));
            lootToRemove = Sets.newHashSet(STRING_SPLITTER.splitToList(config.lootToRemove().toLowerCase()));
            CUSTOM_ENTRIES.clear();
            for (String s : STRING_SPLITTER.split(config.customEntries().toLowerCase()))
                CUSTOM_ENTRIES.put(s.substring(0, s.indexOf(":")), s.substring(s.indexOf(":") + 1));
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

        int entryType = entry.getType();
        int entryIdentifier = entry.getIdentifier();
        String target = Text.standardize(entry.getTarget());
        String option = Text.standardize(entry.getOption());

        NPC npc = null;
        NPC[] cachedNPCs = client.getCachedNPCs();
        if ((config.removeDeadNPCs() || config.removeNPCs()) && onNPC(entryType) && cachedNPCs.length-1 > entry.getIdentifier())
            npc = cachedNPCs[entry.getIdentifier()];

        return (!(config.removeDeadNPCs() && npc != null && npc.getName() != null && npc.isDead() && deadNPCblacklist.contains(npc.getName().toLowerCase()))
                && !(config.removeNPCs() && npc != null && npc.getName() != null && NPCsToRemove.contains(npc.getName().toLowerCase()))
                && !(config.removeItemsOnPlayers() && entryType == MenuAction.ITEM_USE_ON_PLAYER.getId())
                && !(config.removePlayerTrade() && entryType == MenuAction.TRADE.getId())
                && !(config.removePlayerFollow() && entryType == MenuAction.FOLLOW.getId())
                && !(config.removeLoot() && GROUND_OPTIONS.contains(MenuAction.of(entryType)) && lootToRemove.contains(client.getItemDefinition(entryIdentifier).getName().toLowerCase()))
                && !(config.shiftWalkUnder() && shiftModifier && onNPC(entryType) && (entryIdentifier != 0 && !RUNELITE_ACTIONS.contains(entryType)))
                && !(config.reanimateOnlyHeads() && entryType == MenuAction.SPELL_CAST_ON_GROUND_ITEM.getId() && target.contains("Reanimate")
                    && !(client.getItemDefinition(entryIdentifier).getName().contains("Ensouled")))
                && !(config.removeCustomEntries() && CUSTOM_ENTRIES.containsKey(option) && CUSTOM_ENTRIES.get(option).contains(target)));
    }

    // Copied from NpcIndicatorsPlugin
    private boolean onNPC(int eventType) {
        if (eventType >= MENU_ACTION_DEPRIORITIZE_OFFSET)
            eventType -= MENU_ACTION_DEPRIORITIZE_OFFSET;
        return NPC_MENU_ACTIONS.contains(MenuAction.of(eventType));
    }
}