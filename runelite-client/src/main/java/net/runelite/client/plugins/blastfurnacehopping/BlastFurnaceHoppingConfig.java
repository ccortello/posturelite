package net.runelite.client.plugins.blastfurnacehopping;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

//@ConfigGroup(BlastFurnaceHoppingConfig.GROUP)
public interface BlastFurnaceHoppingConfig extends Config {
//    String GROUP = "BlastFurnaceHoppingConfig";

    @ConfigItem(
            keyName = "hopHotkey",
            name = "Hop Hotkey",
            description = "When you press this key you'll hop to the next world"
    )
    default Keybind hopHotkey() {
        return new Keybind(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK);
    }
}