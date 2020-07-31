package net.runelite.client.plugins.fastgaming;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.google.inject.Provides;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("Duplicates")

@PluginDescriptor(
        name = "Fast gaming",
        description = "Repeated clicks of one item cleans, drops, or opens others. Double-click knife or chisel or pestle first.",
        tags = {"inventory"},
        enabledByDefault = false
)

@Slf4j
public class FastGamingPlugin extends Plugin {
    private static final ImmutableSet<Integer> ITEM_PACKS = ImmutableSet.of(ItemID.ADAMANT_ARROW_PACK,
            ItemID.AIR_RUNE_PACK, ItemID.AMYLASE_PACK, ItemID.BAIT_PACK, ItemID.BASKET_PACK, ItemID.BIRD_SNARE_PACK,
            ItemID.BONE_BOLT_PACK, ItemID.BOX_TRAP_PACK, ItemID.BROAD_ARROWHEAD_PACK, ItemID.CATALYTIC_RUNE_PACK,
            ItemID.CHAOS_RUNE_PACK, ItemID.COMPOST_PACK, ItemID.EARTH_RUNE_PACK, ItemID.ELEMENTAL_RUNE_PACK,
            ItemID.EMPTY_BUCKET_PACK, ItemID.EMPTY_JUG_PACK, ItemID.EMPTY_VIAL_PACK, ItemID.EYE_OF_NEWT_PACK,
            ItemID.FEATHER_PACK, ItemID.FIRE_RUNE_PACK, ItemID.MAGIC_IMP_BOX_PACK, ItemID.MIND_RUNE_PACK,
            ItemID.OLIVE_OIL_PACK, ItemID.PLANT_POT_PACK, ItemID.RUNE_ARROW_PACK, ItemID.SACK_PACK,
            ItemID.SANDWORMS_PACK, ItemID.SEED_PACK, ItemID.SOFT_CLAY_PACK, ItemID.SOFT_CLAY_PACK_12010,
            ItemID.UNFINISHED_BROAD_BOLT_PACK, ItemID.WATER_RUNE_PACK, ItemID.WATERFILLED_VIAL_PACK);
    private static final ImmutableSet<Integer> GRIMY_HERBS = ImmutableSet.of(ItemID.GRIMY_ARDRIGAL,
            ItemID.GRIMY_AVANTOE, ItemID.GRIMY_BUCHU_LEAF, ItemID.GRIMY_CADANTINE, ItemID.GRIMY_DWARF_WEED,
            ItemID.GRIMY_GOLPAR, ItemID.GRIMY_GUAM_LEAF, ItemID.GRIMY_HARRALANDER, ItemID.GRIMY_IRIT_LEAF,
            ItemID.GRIMY_KWUARM, ItemID.GRIMY_LANTADYME, ItemID.GRIMY_MARRENTILL, ItemID.GRIMY_NOXIFER,
            ItemID.GRIMY_RANARR_WEED, ItemID.GRIMY_ROGUES_PURSE, ItemID.GRIMY_SITO_FOIL, ItemID.GRIMY_SNAKE_WEED,
            ItemID.GRIMY_SNAPDRAGON, ItemID.GRIMY_TARROMIN, ItemID.GRIMY_TOADFLAX, ItemID.GRIMY_TORSTOL,
            ItemID.GRIMY_VOLENCIA_MOSS);
    private static final ImmutableSet<Integer> FULL_NESTS = ImmutableSet.of(ItemID.BIRD_NEST_5074, ItemID.BIRD_NEST_22800);
    private static final ImmutableSet<Integer> AERIAL_FISHING_FISH = ImmutableSet.of(ItemID.BLUEGILL, ItemID.MOTTLED_EEL,
            ItemID.COMMON_TENCH, ItemID.GREATER_SIREN);
    private static final HashSet<Integer> itemsToSkip = new HashSet<Integer>();
    private static HashSet<String> itemsAllowedToDrop = new HashSet<String>();
    private static boolean enableFastNestCrushing = false;
    private static boolean enableFastEssenceChiseling = false;
    private static boolean enableFastSacredEels = false;
    private static boolean lastClickedPipe = false;
    private static boolean justDroppedEmptylightorbs = false;
    private static boolean justCastSuperglassmake = false;
    private static String lastItemClicked = "";
    private static Item[] items = new Item[0];
    private ItemContainer inventory;

    @Inject
    private ShiftClickInputListener inputListener;
    @Setter
    private boolean shiftModifier = false;
    @Inject
    private KeyManager keyManager;

    @Inject
    private FastGamingConfig config;

    @Inject
    private Client client;

    @Provides
    FastGamingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FastGamingConfig.class);
    }

    @Override
    public void startUp() {
        keyManager.registerKeyListener(inputListener);
        inventory = client.getItemContainer(InventoryID.INVENTORY);
        items = (inventory != null) ? inventory.getItems() : new Item[0];
        itemsAllowedToDrop =
                new HashSet<String>(Arrays.asList(config.itemsToDrop().toLowerCase().split("[,\\r?\\n]+")));
    }

    @Subscribe
    public void onFocusChanged(FocusChanged event) {
        if (!event.isFocused()) {
            shiftModifier = false;
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getKey().equals("itemsToDrop"))
            itemsAllowedToDrop =
                    new HashSet<String>(Arrays.asList(config.itemsToDrop().toLowerCase().split("[,\\r?\\n]+")));
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        inventory = client.getItemContainer(InventoryID.INVENTORY);
        items = (inventory != null) ? inventory.getItems() : new Item[0];
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {

        if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
            return;

        if (event.getMenuAction().equals(MenuAction.ITEM_DROP) && isLastDropItem(event.getActionParam(), config.keepLastItem() ?1:0))
            assert true; // stop skipping at last item
        else if (event.getWidgetId() == 9764864 && (event.getMenuAction().equals(MenuAction.ITEM_DROP) || event.getMenuAction().equals(MenuAction.ITEM_FIRST_OPTION)))
            itemsToSkip.add(event.getActionParam());
        else
            itemsToSkip.clear();



        if (event.getWidgetId() == 17694741) //don't use pipe after "Make light orb" command
            lastClickedPipe = false;

        // Don't cast superglass make after the first time
        justCastSuperglassmake = event.getMenuTarget().contains("Superglass Make");

        // osrs remembers last item that you clicked use on, so only enable fast use when correct item was last clicked
        if (event.getWidgetId() == 9764864) {
            int id = event.getId();
            enableFastEssenceChiseling = (id == ItemID.CHISEL || id == ItemID.DARK_ESSENCE_BLOCK);
            enableFastSacredEels = (id == ItemID.KNIFE || id == ItemID.SACRED_EEL);
            enableFastNestCrushing = (id == ItemID.PESTLE_AND_MORTAR || id == ItemID.BIRD_NEST_5075);
            lastClickedPipe = (id == ItemID.GLASSBLOWING_PIPE || id == ItemID.MOLTEN_GLASS);
            justDroppedEmptylightorbs = (event.getMenuAction().equals(MenuAction.ITEM_DROP) && event.getId() == ItemID.EMPTY_LIGHT_ORB
                    && !inventory.contains(ItemID.MOLTEN_GLASS));
            lastItemClicked = Text.standardize(client.getItemDefinition(event.getId()).getName());
        }
    }

    private boolean isLastDropItem(int itemSlot, int itemsToKeep) {

        int lastItemToDrop = -1;
        int foundItems = 0;

        List<Integer> itemSlots = IntStream.rangeClosed(0, items.length - 1).boxed().collect(Collectors.toList());
        if (!config.reverseOrder())
            itemSlots = Lists.reverse(itemSlots);

        for (int i : itemSlots) {
            if (itemsAllowedToDrop.contains(client.getItemDefinition(items[i].getId()).getName().toLowerCase())) {
                foundItems++;
                if (foundItems-1 == itemsToKeep) {
                    lastItemToDrop = i;
                    break;
                }
            }
        }
        return lastItemToDrop == itemSlot;
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null || !event.getOption().equals("Examine") || shiftModifier)
            return;

        String target = Text.standardize(client.getItemDefinition(event.getIdentifier()).getName());
        String option = Text.standardize(event.getOption());
        int identifier = event.getIdentifier();

        MenuEntry customEntry = null;
        List<Integer> inventoryIDs = IntStream.rangeClosed(0, items.length - 1).boxed().collect(Collectors.toList());
        if (config.reverseOrder())
            inventoryIDs = Lists.reverse(inventoryIDs);

        if (event.getActionParam1() == 9764864) {
            if (identifier == ItemID.PESTLE_AND_MORTAR && config.fastCrushNests()
                    && inventory.contains(ItemID.BIRD_NEST_5075) && enableFastNestCrushing) {
                for (int i = items.length - 1; i > 0; i--) {
                    int currentID = items[i].getId();
                    if (currentID == ItemID.BIRD_NEST_5075) {
                        customEntry = setMenuEntry("Fast Crush", "<col=ff9040>Pestle and mortar<col=ffffff> -> " +
                                "<col=ff9040>" + client.getItemDefinition(currentID).getName(), currentID, 31, i, 9764864);
                        break;
                    }
                }
            } else if (identifier == ItemID.CHISEL && config.fastZeahRC() && inventory.contains(ItemID.DARK_ESSENCE_BLOCK)
                    && enableFastEssenceChiseling) {
                for (int i = items.length - 1; i > 0; i--) {
                    int currentID = items[i].getId();
                    if (currentID == ItemID.DARK_ESSENCE_BLOCK) {
                        customEntry = setMenuEntry("Fast Chisel", "<col=ff9040>Chisel<col=ffffff> -> <col=ff9040>" +
                                client.getItemDefinition(currentID).getName(), currentID, 31, i, 9764864);
                        break;
                    }
                }
            } else if (identifier == ItemID.KNIFE && config.fastSacredEels() && inventory.contains(ItemID.SACRED_EEL)
                    && enableFastSacredEels) {
                for (int i = items.length - 1; i > 0; i--) {
                    int currentID = items[i].getId();
                    if (currentID == ItemID.SACRED_EEL) {
                        customEntry = setMenuEntry("Fast Cut", "<col=ff9040>Knife<col=ffffff> -> <col=ff9040>" +
                                client.getItemDefinition(currentID).getName(), currentID, 31, i, 9764864);
                        break;
                    }
                }
            } else if (itemsAllowedToDrop.contains(target) && config.fastDrop() && (items[event.getActionParam0()].getQuantity() == 1 || config.fastDropStacks())) {
                for (int i : inventoryIDs) {

                    int currentID = items[i].getId();
                    String name = client.getItemDefinition(currentID).getName();

                    if (itemsAllowedToDrop.contains(Text.standardize(name)) && !itemsToSkip.contains(i)
                            && (!isLastDropItem(i, config.keepLastItem() ? 0 : -1)) // won't drop last item if config.keeplastitem
                            && (items[i].getQuantity() == 1 || config.fastDropStacks())) {
                        customEntry = setMenuEntry("Fast Drop", "<col=ff9040>" + name, currentID, 37, i, 9764864);
                        break;
                    }
                }
            } else if (GRIMY_HERBS.contains(identifier) && config.fastCleanHerbs()) {
                for (int i : inventoryIDs) {

                    int currentID = items[i].getId();
                    String name = client.getItemDefinition(currentID).getName();

                    if (!itemsToSkip.contains(i) && GRIMY_HERBS.contains(currentID) && (items[i].getQuantity() == 1)) {
                        customEntry = setMenuEntry("Fast Clean", "<col=ff9040>" + name, currentID, 33, i, 9764864);
                        break;
                    }
                }
            } else if (ITEM_PACKS.contains(identifier) && config.fastOpenPacks()) {
                for (int i : inventoryIDs) {

                    int currentID = items[i].getId();
                    String name = client.getItemDefinition(currentID).getName();

                    if (!itemsToSkip.contains(i) && ITEM_PACKS.contains(currentID) && (items[i].getQuantity() == 1)) {
                        customEntry = setMenuEntry("Fast Open", "<col=ff9040>" + name, currentID, 33, i, 9764864);
                        break;
                    }
                }
            } else if (FULL_NESTS.contains(identifier) && config.fastSearchNests()) {
                for (int i : inventoryIDs) {

                    int currentID = items[i].getId();
                    String name = client.getItemDefinition(currentID).getName();

                    if (FULL_NESTS.contains(currentID) && !itemsToSkip.contains(i) && (items[i].getQuantity() == 1)) {
                        customEntry = setMenuEntry("Fast Search", "<col=ff9040>" + name, currentID, 33, i, 9764864);
                        break;
                    }
                }
            }
        }
        
        if (config.fastSuperglassMake()) {

            if (event.getActionParam1() == 9764864) { // clicking in inventory
                if (target.equals("glassblowing pipe")) {
                    if (justDroppedEmptylightorbs && !justCastSuperglassmake)
                        customEntry = setMenuEntry("Cast", "<col=00ff00>Superglass Make</col>", 1, MenuAction.CC_OP.getId(), -1, 14286965);
                    else if (client.getWidget(270, 21) != null)
                        customEntry = setMenuEntry("Make", "<col=ff9040>Light orb</col>", 1, MenuAction.CC_OP.getId(), -1, 17694741);
                    else if (inventory.contains(ItemID.MOLTEN_GLASS) && lastClickedPipe)
                        customEntry = setMenuEntry("Use", "<col=ff9040>Glassblowing pipe<col=ffffff> -> <col=ff9040>Molten glass",
                                ItemID.MOLTEN_GLASS, 31, getClosestItem(event.getActionParam0(), ItemID.MOLTEN_GLASS), 9764864);
                    else if (!justCastSuperglassmake && inventory.contains(ItemID.GIANT_SEAWEED) && inventory.contains(ItemID.BUCKET_OF_SAND) && !inventory.contains(ItemID.MOLTEN_GLASS)) {
                        for (Item item : items) {
                            if (item.getId() == ItemID.GIANT_SEAWEED && item.getQuantity() == 1) {
                                for (Item secondItem : items) {
                                    if (secondItem.getId() == ItemID.BUCKET_OF_SAND && secondItem.getQuantity() == 1) {
                                        customEntry = setMenuEntry("Cast", "<col=00ff00>Superglass Make</col>", 1, MenuAction.CC_OP.getId(), -1, 14286965);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else if ((target.equals("giant seaweed") || target.equals("bucket of sand"))
                        && (lastItemClicked.equals("giant seaweed") || lastItemClicked.equals("bucket of sand"))
                        && client.getLocalPlayer().getWorldLocation().getRegionID() == 14908 && client.getLocalPlayer().getWorldLocation().equals(new WorldPoint(3771, 3898, 0))
                        && inventory.getItem(event.getActionParam0()) != null && inventory.getItem(event.getActionParam0()).getQuantity() > 1) {
                    if (client.getWidget(219, 1) != null)
                        customEntry = setMenuEntry("Unnote " + event.getTarget(), "", 0, MenuAction.WIDGET_TYPE_6.getId(), 1, 14352385);
                    else
                        customEntry = setMenuEntry("Use", event.getTarget() + "<col=ffffff> -> <col=ffff>Bank Chest-wreck",
                               30796, MenuAction.ITEM_USE_ON_GAME_OBJECT.getId(), 52, 50);
                }
            }

            if (client.getWidget(219, 1) != null) { // not clicking inventory item, rowboat widgit open
                switch (event.getIdentifier()) {
                    case 30914:
                        customEntry = setMenuEntry("Row north", "", 0, MenuAction.WIDGET_TYPE_6.getId(), 2, 14352385);
                        break;
                    case 30915:
                        customEntry = setMenuEntry("Row out to sea", "", 0, MenuAction.WIDGET_TYPE_6.getId(), 3, 14352385);
                        break;
                    case 30919:
                        customEntry = setMenuEntry("Dive anyway", "", 0, MenuAction.WIDGET_TYPE_6.getId(), 1, 14352385);
                        break;
                }
            }
        }

        MenuEntry[] entries = client.getMenuEntries();
        if (entries != null && customEntry != null)
            client.setMenuEntries(ObjectArrays.concat(client.getMenuEntries(), customEntry));
    }

    private int getClosestItem(int currentSlot, int targetID) {
        ArrayList<Integer> slotsToTryFirst = new ArrayList<Integer>();

        // check slots in order down, up, right, left
        if (currentSlot < 24)
            slotsToTryFirst.add(currentSlot + 4);
        if (currentSlot > 3)
            slotsToTryFirst.add(currentSlot - 4);
        if (currentSlot%4 != 3)
            slotsToTryFirst.add(currentSlot + 1);
        if (currentSlot%4 != 0)
            slotsToTryFirst.add(currentSlot - 1);

        for (int trySlot : slotsToTryFirst) {
            Item tryItem = inventory.getItem(trySlot);
            if (tryItem != null && tryItem.getId() == targetID)
                return trySlot;
        }

        // if close item not found, iterate from top
        for (int i=0; i<items.length; i++) {
            Item currentItem = items[i];
            if (currentItem != null && currentItem.getId() == targetID)
                return i;
        }

        return -1;
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