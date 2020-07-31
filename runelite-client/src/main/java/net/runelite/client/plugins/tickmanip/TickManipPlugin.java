package net.runelite.client.plugins.tickmanip;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
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

@PluginDescriptor(
        name = "Tick Manip",
        description = "Easier 3t barb fishing, 3t mining, and 1t karambwans. If left click doesn't work," +
                "double-click knife in inventory or last karambwan in inventory.",
        tags = {"inventory", "fishing", "mining", "cooking"},
        enabledByDefault = false
)

@Slf4j
public class TickManipPlugin extends Plugin {

    private static final ImmutableSet<Integer> PICKAXES = ImmutableSet.of(ItemID.ADAMANT_PICKAXE, ItemID.BLACK_PICKAXE,
            ItemID.BRONZE_PICKAXE, ItemID.CRYSTAL_PICKAXE, ItemID.DRAGON_PICKAXE, ItemID.GILDED_PICKAXE,
            ItemID.INFERNAL_PICKAXE, ItemID.IRON_PICKAXE, ItemID.MITHRIL_PICKAXE, ItemID.RUNE_PICKAXE,
            ItemID.STEEL_PICKAXE, ItemID.CRYSTAL_PICKAXE_23863, ItemID.CRYSTAL_PICKAXE_INACTIVE,
            ItemID.DRAGON_PICKAXE_12797, ItemID.DRAGON_PICKAXEOR, ItemID.INFERNAL_PICKAXE_UNCHARGED);
    private static final ImmutableSet<Integer> FISHING_RODS = ImmutableSet.of(ItemID.BARBARIAN_ROD, ItemID.FISHING_ROD,
            ItemID.FLY_FISHING_ROD, ItemID.OILY_FISHING_ROD, ItemID.OILY_PEARL_FISHING_ROD, ItemID.PEARL_FISHING_ROD,
            ItemID.PEARL_FLY_FISHING_ROD);
    private static final Splitter STRING_SPLITTER = Splitter.onPattern("[,\r?\n]+").trimResults().omitEmptyStrings();
    private static HashSet<String> bones;
    private static ItemContainer inventory;
    private static ItemContainer equipment;
    private static Item[] items;
    private static boolean enable3tickFishing = false;
    private static boolean enable3tickMining = false;
    private static boolean hasntCooked = true;
    private static boolean hasnt1tBones = true;
    private static int lastUsedBones;
    private static boolean lastClickedKnife = false;
    private static String fishingState = "inactive";
    private static String miningState = "inactive";
    @Inject
    private Client client;
    @Inject
    private TickManipConfig config;

    @Provides
    TickManipConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TickManipConfig.class);
    }

    @Override
    public void startUp() {
        inventory = client.getItemContainer(InventoryID.INVENTORY);
        equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        items = (inventory != null) ? inventory.getItems() : new Item[0];
        enable3tickMining = enable3tickMining();
        enable3tickFishing = enable3tickFishing();
        bones = Sets.newHashSet(STRING_SPLITTER.splitToList(config.bonesTo1t().toLowerCase()));
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getKey().equals("bonesTo1t"))
            bones = Sets.newHashSet(STRING_SPLITTER.splitToList(config.bonesTo1t().toLowerCase()));
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        inventory = client.getItemContainer(InventoryID.INVENTORY);
        equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        items = (inventory != null) ? inventory.getItems() : new Item[0];
        enable3tickMining = enable3tickMining();
        enable3tickFishing = enable3tickFishing();
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        hasntCooked = true;
        hasnt1tBones = true;
    }

    private boolean enable3tickMining() {
        if (inventory == null || equipment == null)
            return false;

        for (int pickaxe : PICKAXES)
            if (inventory.contains(pickaxe) || equipment.contains(pickaxe))
                return lastClickedKnife && inventory.contains(ItemID.KNIFE) && lastClickedKnife
                        && (inventory.contains(ItemID.TEAK_LOGS) || inventory.contains(ItemID.MAHOGANY_LOGS));
        return false;
    }

    private boolean enable3tickFishing() {
        if (inventory == null || equipment == null)
            return false;

        for (int rod : FISHING_RODS)
            if (inventory.contains(rod) || equipment.contains(rod))
                return inventory.contains(ItemID.KNIFE)
                        && (inventory.contains(ItemID.TEAK_LOGS) || inventory.contains(ItemID.MAHOGANY_LOGS));
        return false;
    }

    private boolean enable1tBones() {
        if (inventory == null)
            return false;

        for (Item item : items)
            if (bones.contains(client.getItemDefinition(item.getId()).getName().toLowerCase()))
                return true;
        return false;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {

        if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
            return;

        final String option = Text.removeTags(event.getMenuOption()).toLowerCase();
        final String target = Text.removeTags(event.getMenuTarget()).toLowerCase();

        // osrs remembers last item that you clicked use on, so only enable fast use when correct item was last clicked
        if (event.getWidgetId() == 9764864 && event.getMenuTarget().equals("Use")) {
            lastClickedKnife = (event.getId() == ItemID.KNIFE || event.getId() == ItemID.TEAK_LOGS || event.getId() == ItemID.MAHOGANY_LOGS);

            if (bones.contains(client.getItemDefinition(event.getId()).getName().toLowerCase()))
                lastUsedBones = event.getActionParam();
            else
                lastUsedBones = -1;
            log.debug("lastUsedBones={}", lastUsedBones);
        }

        if (target.contains("fire") && hasntCooked)
            hasntCooked = false;
        if (option.equals("mine") && target.equals("rocks")) {
            miningState = "mining";
//            log.debug("mining state={}", miningState);
        } else if (option.equals("use-rod") && target.equals("fishing spot") && lastClickedKnife) {
            fishingState = "fishing";
//            log.debug("fishing state={}", fishingState);
        } else if (option.equals("use") && (target.equals("knife -> teak logs") || target.equals("knife -> mahogany " +
                "logs"))) {
            miningState = "fletching";
            fishingState = "fletching";
//            log.debug("fishing state={}, mining state={}", fishingState, miningState);
        } else if (event.getMenuAction().equals(MenuAction.ITEM_DROP)) {
            miningState = "dropping";
            fishingState = "dropping";
        } else if (event.getId() != 1){
            miningState = "inactive";
            fishingState = "inactive";
//            log.debug("fishing state={}, mining state={}", fishingState, miningState);
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {

        if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
            return;

        final String option = Text.standardize(event.getOption());
        final String target = Text.standardize(event.getTarget());

        MenuEntry customEntry = null;

        if (config.enable1tKarams() && target.equals("fire") && option.equals("examine") && hasntCooked && inventory.contains(ItemID.RAW_KARAMBWAN)) {
            customEntry = setMenuEntry("Use", "<col=ff9040>Raw karambwan<col=ffffff> -> <col=ffff>Fire",
                    event.getIdentifier(), 1, event.getActionParam0(), event.getActionParam1());
        }
        if (config.enable1tAltar() && target.contains("altar") && option.contains("pray") && enable1tBones()) {
            for (int i=0; i<items.length; i++) {
                Item item = items[i];
                String name = client.getItemDefinition(item.getId()).getName();
                if (item.getQuantity() == 1 && bones.contains(name.toLowerCase()) && (lastUsedBones != i || countBones() == 1)) {
                    customEntry = setMenuEntry("Use", "<col=ff9040>" + name + "<col=ffffff> -> <col=ffff>" + event.getTarget(),
                            item.getId(), 38, i, 9764864);
                    break;
                }
            }
        }

        if (config.enable3tMining() && enable3tickMining && option.equals("examine") && target.equals("rocks")
                && (miningState.equals("inactive") || (miningState.equals("mining")))
                && (!config.excludeSandstone() || event.getIdentifier() != 11386)
                && (!config.excludeEmptyRocks() || (event.getIdentifier() != 11390 && event.getIdentifier() != 11391))) {
            for (int i = 0; i < items.length - 1; i++) {
                int currentID = items[i].getId();
                if (currentID == ItemID.TEAK_LOGS || currentID == ItemID.MAHOGANY_LOGS) {
                    customEntry = setMenuEntry("Use", "<col=ff9040>Knife<col=ffffff> -> <col=ff9040>" +
                            client.getItemDefinition(currentID).getName(),currentID, 31, i, 9764864);
                    break;
                }
            }
        }

        if (config.enable3tFishing() && enable3tickFishing && target.equals("fishing spot") && option.equals("use-rod")
                && fishingState.equals("fishing")) {
            for (int i = 0; i < items.length - 1; i++) {
                int currentID = items[i].getId();
                if (currentID == ItemID.TEAK_LOGS || currentID == ItemID.MAHOGANY_LOGS) {
                    customEntry = setMenuEntry("Use", "<col=ff9040>Knife<col=ffffff> -> <col=ff9040>" +
                            client.getItemDefinition(currentID).getName(),currentID, 31, i, 9764864);
                    break;
                }
            }
        }
        if (customEntry != null)
            client.setMenuEntries(ObjectArrays.concat(client.getMenuEntries(), customEntry));
    }

    private int countBones() {
        if (inventory == null)
            return -1;

        int count = 0;
        for (Item item : inventory.getItems())
            if (item.getQuantity()==1 && bones.contains(client.getItemDefinition(item.getId()).getName().toLowerCase()))
                count++;

        return count;
    }

    private MenuEntry setMenuEntry(String option, String target, int ID, int type, int param0, int param1) {
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