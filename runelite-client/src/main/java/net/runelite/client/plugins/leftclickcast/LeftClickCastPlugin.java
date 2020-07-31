package net.runelite.client.plugins.leftclickcast;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("Duplicates")

@PluginDescriptor(
        name = "Left Click Cast",
        description = "Changes the left click option for specified monsters to cast a magic spell on them.",
        tags = {"npcs"},
        enabledByDefault = false
)
@Slf4j
public class LeftClickCastPlugin extends Plugin {

    private static final Splitter STRING_SPLITTER = Splitter.onPattern("[,\r?\n]+").trimResults().omitEmptyStrings();
    private static final Set<MenuAction> NPC_MENU_ACTIONS = ImmutableSet.of(MenuAction.NPC_FIRST_OPTION,
            MenuAction.NPC_SECOND_OPTION,
            MenuAction.NPC_THIRD_OPTION, MenuAction.NPC_FOURTH_OPTION, MenuAction.NPC_FIFTH_OPTION,
            MenuAction.SPELL_CAST_ON_NPC,
            MenuAction.ITEM_USE_ON_NPC);
    private static HashSet<String> monsters;
    private static HashSet<String> weapons;
    private static HashSet<String> itemsToAlch;
    private static String lastSpellClicked = "";
    private static boolean autocastOn = false;
    @Inject
    private Client client;
    @Inject
    private LeftClickCastConfig config;

    @Provides
    LeftClickCastConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(LeftClickCastConfig.class);
    }

    @Override
    public void startUp() {
        monsters = Sets.newHashSet(STRING_SPLITTER.splitToList(config.monsters().toLowerCase()));
        weapons = Sets.newHashSet(STRING_SPLITTER.splitToList(config.weapons().toLowerCase()));
        itemsToAlch = Sets.newHashSet(STRING_SPLITTER.splitToList(config.itemsToAlch().toLowerCase()));
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        switch (event.getKey()) {
            case ("monsters"):
                monsters = Sets.newHashSet(STRING_SPLITTER.splitToList(config.monsters().toLowerCase()));
            case ("weapons"):
                weapons = Sets.newHashSet(STRING_SPLITTER.splitToList(config.weapons().toLowerCase()));
            case ("itemsToAlch"):
                itemsToAlch = Sets.newHashSet(STRING_SPLITTER.splitToList(config.itemsToAlch().toLowerCase()));
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {

        final String option = Text.removeTags(event.getMenuOption()).toLowerCase();
        final String target = Text.removeTags(event.getMenuTarget()).toLowerCase();

        if (option.equals("cast") && !target.contains("(level-") && !target.contains(" -> ")) {
            lastSpellClicked = event.getMenuTarget();
//            log.debug("lastSpellClicked=|{}|", lastSpellClicked);
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (event.getContainerId() != InventoryID.EQUIPMENT.getId())
            return;

        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
            return;

        autocastOn = false;
        for (Item item : equipment.getItems()) {
            if (item.getId() == -1)
                continue;
            if (weapons.contains(client.getItemDefinition(item.getId()).getName().toLowerCase())) {
                autocastOn = true;
                return;
            }
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (client.getGameState() != GameState.LOGGED_IN || !event.getOption().equals("Examine"))
            return;

        final String target = Text.standardize(event.getTarget());
        final String option = Text.standardize(event.getOption());

        if (event.getActionParam1() == 9764864 && !(event.getIdentifier() == MenuAction.ITEM_USE_ON_WIDGET.getId()) && lastSpellClicked.contains("Alch")
                && itemsToAlch.contains(client.getItemDefinition(event.getIdentifier()).getName().toLowerCase())) {
            client.setMenuEntries(ObjectArrays.concat(client.getMenuEntries(),
                    setMenuEntry(lastSpellClicked + "<col=ffffff> -> " + event.getTarget(), event.getIdentifier(), 32
                            , event.getActionParam0(), 9764864)));
        } else if (!(event.getActionParam1() == 9764864) && config.leftClickTelegrabWines() && lastSpellClicked.contains("Telekinetic Grab")
                && (event.getIdentifier() == ItemID.WINE_OF_ZAMORAK || event.getIdentifier() == ItemID.WINE_OF_ZAMORAK_23489) && !target.contains("->")) {
            client.setMenuEntries(ObjectArrays.concat(client.getMenuEntries(),
                    setMenuEntry(lastSpellClicked + "<col=ffffff> -> " + event.getTarget(), event.getIdentifier(),
                            MenuAction.SPELL_CAST_ON_GROUND_ITEM.getId(), event.getActionParam0(),
                            event.getActionParam1())));
        } else if (autocastOn)
            for (String monster : monsters)
                if (!monster.equals("") && !lastSpellClicked.equals("") && !lastSpellClicked.contains("Alch")
                        && target.contains(monster + "  (level-") && !target.contains("->"))
                    client.setMenuEntries(ObjectArrays.concat(client.getMenuEntries(),
                            setMenuEntry(lastSpellClicked + "<col=ffffff> -> " + event.getTarget(),
                                    event.getIdentifier(), 8, 0, 0)));
    }


    private MenuEntry setMenuEntry(String target, int ID, int type, int param0, int param1) {
        MenuEntry e = new MenuEntry();
        e.setOption("Cast");
        e.setTarget(target);
        e.setIdentifier(ID);
        e.setType(type);
        e.setParam0(param0);
        e.setParam1(param1);
        return e;
    }
}