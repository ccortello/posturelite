package net.runelite.client.plugins.blastfurnacehopping;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldClient;
import net.runelite.http.api.worlds.WorldResult;

import javax.inject.Inject;
import java.util.Comparator;

@PluginDescriptor(
        name = "Blast Furnace Hopping",
        description = "Hop to the next BF world in sequence."
)

@Slf4j
public class BlastFurnaceHoppingPlugin extends Plugin {

    public int counter = 0;
    public int myWorldsSize = 0;

    public World[] myWorlds;
    public WorldResult worldResult;

    private int displaySwitcherAttempts = 0;
    private net.runelite.api.World quickHopTargetWorld;

    @Inject
    private Client client;

    @Inject
    private KeyManager keyManager;

    @Inject
    private WorldClient worldClient;

    @Inject
    private BlastFurnaceHoppingConfig config;

    @Inject
    private ChatMessageManager chatMessageManager;

    private final HotkeyListener nextKeyListener = new HotkeyListener(() -> config.hopHotkey())
    {
        @Override
        public void hotkeyPressed() {
            hop();
        }
    };

    @Override
    protected void startUp() throws Exception {
        keyManager.registerKeyListener(nextKeyListener);
    }

    @Provides
    BlastFurnaceHoppingConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BlastFurnaceHoppingConfig.class);
    }

    private void hop(int worldId)
    {
        if (client.getGameState() != GameState.LOGGED_IN)
            return;

        // Don't try to hop if the world doesn't exist
        World world = worldResult.findWorld(worldId);
        if (world == null)
        {
            return;
        }

        final net.runelite.api.World rsWorld = client.createWorld();
        rsWorld.setActivity(world.getActivity());
        rsWorld.setAddress(world.getAddress());
        rsWorld.setId(world.getId());
        rsWorld.setPlayerCount(world.getPlayers());
        rsWorld.setLocation(world.getLocation());
        rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes()));

        if (client.getGameState() == GameState.LOGIN_SCREEN)
        {
            // on the login screen we can just change the world by ourselves
            client.changeWorld(rsWorld);
            return;
        }

        quickHopTargetWorld = rsWorld;
        displaySwitcherAttempts = 0;
    }

    private boolean hasntHopped = true;

    private void hop()
    {
        if (client.getGameState() != GameState.LOGGED_IN || worldClient == null)
            return;

        if (hasntHopped)
        {
            try {
                for (int i = 0; i < worldClient.lookupWorlds().getWorlds().size(); i++) {
                    {
                        try {
                            if (worldClient.lookupWorlds().getWorlds().get(i).getActivity().contains("Blast Furnace")) {
                                myWorldsSize++;
                            }
                        } catch (Exception ignored) {
                            System.out.println("Oh fuck off ya cunt...");
                        }
                    }
                }
                myWorlds = new World[myWorldsSize];

                int c = 0;

                for (int i = 0; i < worldClient.lookupWorlds().getWorlds().size(); i++) {
                    try {
                        if (worldClient.lookupWorlds().getWorlds().get(i).getActivity().contains("Blast Furnace"))
                        {

                            myWorlds[c] = worldClient.lookupWorlds().getWorlds().get(i);
                            c++;
                        }
                    } catch (Exception ignored) {
                        System.out.println("Oh fuck off ya cunt.");
                    }
                }
                hasntHopped = false;
            }
            catch(Exception ignored)
            {
                System.out.println("F U");
            }
        }

        if (worldResult == null || client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }


        World world = myWorlds[counter];

        counter++;
        if(counter >= myWorlds.length)
        {
            counter = 0;
        }

        hop(world.getId());
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (client.getGameState() != GameState.LOGGED_IN || worldClient == null)
            return;

        try
        {
            WorldResult worldResult = worldClient.lookupWorlds();

            if (worldResult != null)
            {
                worldResult.getWorlds().sort(Comparator.comparingInt(World::getId));
                this.worldResult = worldResult;
            }
        }
        catch (Exception e)
        {
            log.warn("Error looking up worlds", e);
            System.out.println("Error looking up worlds...");
        }

        if (quickHopTargetWorld == null)
        {
            return;
        }

        if (client.getWidget(WidgetInfo.WORLD_SWITCHER_LIST) == null)
        {
            client.openWorldHopper();

            if (++displaySwitcherAttempts >= 5)
            {
                String chatMessage = new ChatMessageBuilder()
                        .append(ChatColorType.NORMAL)
                        .append("Failed to quick-hop after ")
                        .append(ChatColorType.HIGHLIGHT)
                        .append(Integer.toString(displaySwitcherAttempts))
                        .append(ChatColorType.NORMAL)
                        .append(" attempts.")
                        .build();

                chatMessageManager
                        .queue(QueuedMessage.builder()
                        //.type(ChatMessageType.GAME)
                        .runeLiteFormattedMessage(chatMessage)
                        .build());

                resetQuickHopper();
            }
        }
        else
        {
            client.hopToWorld(quickHopTargetWorld);
            resetQuickHopper();
        }
    }

    private void resetQuickHopper()
    {
        displaySwitcherAttempts = 0;
        quickHopTargetWorld = null;
    }
}
