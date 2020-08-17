package net.runelite.client.plugins.quinnmasterfarmers;

import com.google.common.base.Splitter;
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
import java.util.Arrays;
import java.util.HashSet;

@PluginDescriptor(
        name = "Quinn Master Farmer",
        description = "Changes the left click option on the master farmer to whatever action you need to take next.",
        tags = {"inventory"},
        enabledByDefault = false
)

@Slf4j
public class QuinnMasterFarmersPlugin extends Plugin {
    private static final Splitter STRING_SPLITTER = Splitter.onPattern("[,\r?\n]+").trimResults().omitEmptyStrings();
    private static final HashSet<Integer> sixMostCommonSeeds = new HashSet<>(Arrays.asList(ItemID.BARLEY_SEED,
            ItemID.CABBAGE_SEED, ItemID.HAMMERSTONE_SEED, ItemID.ONION_SEED, ItemID.POTATO_SEED, ItemID.TOMATO_SEED));
    private static HashSet<Integer> shitSeeds = new HashSet<>(Arrays.asList(ItemID.ASGARNIAN_SEED,
            ItemID.BELLADONNA_SEED, ItemID.CACTUS_SEED, ItemID.CADAVABERRY_SEED, ItemID.DWELLBERRY_SEED,
            ItemID.GUAM_SEED, ItemID.HARRALANDER_SEED, ItemID.JANGERBERRY_SEED, ItemID.JUTE_SEED,
            ItemID.KRANDORIAN_SEED, ItemID.LIMPWURT_SEED, ItemID.MARIGOLD_SEED, ItemID.MARRENTILL_SEED, ItemID.MUSHROOM_SPORE,
            ItemID.NASTURTIUM_SEED, ItemID.POISON_IVY_SEED, ItemID.POTATO_CACTUS_SEED, ItemID.REDBERRY_SEED,
            ItemID.ROSEMARY_SEED, ItemID.SNAPE_GRASS_SEED, ItemID.STRAWBERRY_SEED, ItemID.SWEETCORN_SEED, ItemID.TARROMIN_SEED,
            ItemID.WATERMELON_SEED, ItemID.WHITEBERRY_SEED, ItemID.WILDBLOOD_SEED, ItemID.WOAD_SEED,
            ItemID.YANILLIAN_SEED, ItemID.TOADFLAX_SEED, ItemID.SNAPE_GRASS_SEED, ItemID.IRIT_SEED, ItemID.RANARR_SEED,
            ItemID.AVANTOE_SEED));
    private static HashSet<String> seedsToKeep;
    private static Item[] items;
    private static int maxItemsInInv = 0;
    private static boolean timeToDrop = false;
    @Inject
    private Client client;
    @Inject
    private QuinnMasterFarmersConfig config;
    private final HashSet<Integer> itemsToKeep = new HashSet<Integer>();

    @Provides
    QuinnMasterFarmersConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(QuinnMasterFarmersConfig.class);
    }

    @Override
    public void startUp() {
        maxItemsInInv = config.maxItemsInInv();
        if (config.dropSixMostCommonSeeds())
            shitSeeds.addAll(sixMostCommonSeeds);

        seedsToKeep = Sets.newHashSet(STRING_SPLITTER.splitToList(config.seedsToKeep().toLowerCase()));
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals("quinnmasterfarmers"))
                return;

        if (event.getKey().equals("maxItemsInInv"))
            maxItemsInInv = config.maxItemsInInv();

        shitSeeds = new HashSet<Integer>(Arrays.asList(ItemID.ASGARNIAN_SEED,
                ItemID.BELLADONNA_SEED, ItemID.CACTUS_SEED, ItemID.CADAVABERRY_SEED, ItemID.DWELLBERRY_SEED,
                ItemID.GUAM_SEED, ItemID.HARRALANDER_SEED, ItemID.JANGERBERRY_SEED, ItemID.JUTE_SEED,
                ItemID.KRANDORIAN_SEED, ItemID.MARIGOLD_SEED, ItemID.MARRENTILL_SEED, ItemID.MUSHROOM_SPORE,
                ItemID.NASTURTIUM_SEED, ItemID.POISON_IVY_SEED, ItemID.POTATO_CACTUS_SEED, ItemID.REDBERRY_SEED,
                ItemID.ROSEMARY_SEED, ItemID.STRAWBERRY_SEED, ItemID.SWEETCORN_SEED, ItemID.TARROMIN_SEED,
                ItemID.WATERMELON_SEED, ItemID.WHITEBERRY_SEED, ItemID.WILDBLOOD_SEED, ItemID.WOAD_SEED,
                ItemID.YANILLIAN_SEED));
        if (config.dropSixMostCommonSeeds())
            shitSeeds.addAll(sixMostCommonSeeds);

        seedsToKeep = Sets.newHashSet(STRING_SPLITTER.splitToList(config.seedsToKeep().toLowerCase()));
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {

        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        items = (inventory != null) ? inventory.getItems() : new Item[0];

        int currentItems = 0;
        for (Item item : items)
            if (item.getId() > 0)
                currentItems++;


        boolean foundShitSeeds = false;
        if (currentItems >= Math.min(28, maxItemsInInv)) {
            timeToDrop = true;
            itemsToKeep.clear();
        } else if (timeToDrop) {
            for (Item item : items)
                if (shitSeeds.contains(item.getId()) && !seedsToKeep.contains(client.getItemDefinition(item.getId()).getName().toLowerCase())) {
                    foundShitSeeds = true;
                    break;
                }
            timeToDrop = foundShitSeeds;
        }

//        log.debug("currentItems={}, timeToDrop={}, foundShitSeeds={}", currentItems, timeToDrop, foundShitSeeds);

        if (timeToDrop)
            for (Item item : items)
                if (item.getId() != -1)
                    log.debug("found item called {} with quantity {}", client.getItemDefinition(item.getId()).getName(), item.getQuantity());
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getWidgetId() == 9764864 && event.getMenuAction().equals(MenuAction.ITEM_DROP))
            itemsToKeep.add(event.getId());
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (client.getGameState() != GameState.LOGGED_IN)
            return;

        final String option = Text.removeTags(event.getOption()).toLowerCase();
        final String target = Text.removeTags(event.getTarget()).toLowerCase();

        if (target.equals("master farmer") && option.equals("pickpocket") && timeToDrop) {
            for (int i = items.length-1; i >= 0; i--) {
                int currentID = items[i].getId();
                MenuEntry[] entries = client.getMenuEntries();
                String name = client.getItemDefinition(currentID).getName();
                if (!itemsToKeep.contains(currentID) && shitSeeds.contains(currentID)
                        && !seedsToKeep.contains(name.toLowerCase())
                        && !entries[entries.length-1].getOption().equals("Drop")) {
                    client.setMenuEntries(ObjectArrays.concat(entries, setMenuEntry(
                            "Drop", "<col=ff9040>" + name, currentID, 37, i, 9764864)));
                }
            }
        }
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