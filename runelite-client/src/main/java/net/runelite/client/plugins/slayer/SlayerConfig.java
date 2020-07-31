/*
 * Copyright (c) 2017, Seth <Sethtroll3@gmail.com>
 * Copyright (c) 2018, Shaun Dreclin <shaundreclin@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.slayer;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("slayer")
public interface SlayerConfig extends Config
{
	@ConfigSection(
			name = "Render style",
			description = "The render style of NPC highlighting",
			position = 0
	)
	String renderStyleSection = "renderStyleSection";

	@ConfigItem(
			position = -2,
			keyName = "ignoreDeadNpcs",
			name = "Ignore dead NPCs",
			description = "Prevents highlighting NPCs after they are dead",
			section = renderStyleSection
	)
	default boolean ignoreDeadNpcs()
	{
		return true;
	}

	@ConfigItem(
			position = 0,
			keyName = "highlightHull",
			name = "Highlight hull",
			description = "Configures whether or not NPC should be highlighted by hull",
			section = renderStyleSection
	)
	default boolean highlightHull()
	{
		return true;
	}

	@ConfigItem(
			position = 1,
			keyName = "highlightTile",
			name = "Highlight tile",
			description = "Configures whether or not NPC should be highlighted by tile",
			section = renderStyleSection
	)
	default boolean highlightTile()
	{
		return false;
	}

	@ConfigItem(
			position = 2,
			keyName = "highlightTrueTile",
			name = "Highlight true tile",
			description = "Configures whether or not NPC should be highlighted by server-side tile",
			section = renderStyleSection
	)
	default boolean highlightTrueTile()
	{
		return false;
	}

	@ConfigItem(
			position = 3,
			keyName = "highlightCenterTile",
			name = "Highlight center tile",
			description = "Configures whether or not NPC should be highlighted by center tile",
			section = renderStyleSection
	)
	default boolean highlightCenterTile()
	{
		return false;
	}

	@ConfigItem(
			position = 4,
			keyName = "highlightTrueCenterTile",
			name = "Highlight center true tile",
			description = "Configures whether or not NPC should be highlighted by server-side center tile",
			section = renderStyleSection
	)
	default boolean highlightCenterTrueTile()
	{
		return false;
	}

	@ConfigItem(
			position = 5,
			keyName = "highlightSouthWestTile",
			name = "Highlight south west tile",
			description = "Configures whether or not NPC should be highlighted by south western tile",
			section = renderStyleSection
	)
	default boolean highlightSouthWestTile()
	{
		return false;
	}

	@ConfigItem(
			position = 6,
			keyName = "highlightTrueSouthWestTile",
			name = "Highlight south west true tile",
			description = "Configures whether or not NPC should be highlighted by server-side south western tile",
			section = renderStyleSection
	)
	default boolean highlightSouthWestTrueTile()
	{
		return false;
	}

//	@ConfigItem(
//			position = 0,
//			keyName = "showRespawnTimer",
//			name = "Show respawn timer",
//			description = "Show respawn timer of tagged NPCs")
//	default boolean showRespawnTimer()
//	{
//		return false;
//	}

	@ConfigItem(
		position = 1,
		keyName = "infobox",
		name = "Task InfoBox",
		description = "Display task information in an InfoBox"
	)
	default boolean showInfobox()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = "itemoverlay",
		name = "Count on Items",
		description = "Display task count remaining on slayer items"
	)
	default boolean showItemOverlay()
	{
		return true;
	}

	@ConfigItem(
		position = 3,
		keyName = "superiornotification",
		name = "Superior foe notification",
		description = "Toggles notifications on superior foe encounters"
	)
	default boolean showSuperiorNotification()
	{
		return true;
	}

	@ConfigItem(
		position = 4,
		keyName = "statTimeout",
		name = "InfoBox Expiry",
		description = "Set the time until the InfoBox expires"
	)
	@Units(Units.MINUTES)
	default int statTimeout()
	{
		return 5;
	}

	@Alpha
	@ConfigItem(
		position = 6,
		keyName = "targetColor",
		name = "Target Color",
		description = "Color of the highlighted targets"
	)
	default Color getTargetColor()
	{
		return Color.YELLOW;
	}

	@Alpha
	@ConfigItem(
		position = 6,
		keyName = "aggressiveTargetColor",
		name = "Aggressive Target Color",
		description = "Color of the highlighted and aggressive targets"
	)
	default Color getAggressiveTargetColor()
	{
		return Color.ORANGE;
	}

	@ConfigItem(
		position = 7,
		keyName = "weaknessPrompt",
		name = "Show Monster Weakness",
		description = "Show an overlay on a monster when it is weak enough to finish off (Only Lizards, Gargoyles & Rockslugs)"
	)
	default boolean weaknessPrompt()
	{
		return true;
	}

	@ConfigItem(
		position = 8,
		keyName = "taskCommand",
		name = "Task Command",
		description = "Configures whether the slayer task command is enabled<br> !task"
	)
	default boolean taskCommand()
	{
		return true;
	}

	// Stored data
	@ConfigItem(
		keyName = "taskName",
		name = "",
		description = "",
		hidden = true
	)
	default String taskName()
	{
		return "";
	}

	@ConfigItem(
		keyName = "taskName",
		name = "",
		description = ""
	)
	void taskName(String key);

	@ConfigItem(
		keyName = "amount",
		name = "",
		description = "",
		hidden = true
	)
	default int amount()
	{
		return -1;
	}

	@ConfigItem(
		keyName = "amount",
		name = "",
		description = ""
	)
	void amount(int amt);

	@ConfigItem(
		keyName = "initialAmount",
		name = "",
		description = "",
		hidden = true
	)
	default int initialAmount()
	{
		return -1;
	}
	@ConfigItem(
		keyName = "initialAmount",
		name = "",
		description = ""
	)
	void initialAmount(int initialAmount);

	@ConfigItem(
		keyName = "taskLocation",
		name = "",
		description = "",
		hidden = true
	)
	default String taskLocation()
	{
		return "";
	}

	@ConfigItem(
		keyName = "taskLocation",
		name = "",
		description = ""
	)
	void taskLocation(String key);

	@ConfigItem(
		keyName = "streak",
		name = "",
		description = "",
		hidden = true
	)
	default int streak()
	{
		return -1;
	}

	@ConfigItem(
		keyName = "streak",
		name = "",
		description = ""
	)
	void streak(int streak);

	@ConfigItem(
		keyName = "points",
		name = "",
		description = "",
		hidden = true
	)
	default int points()
	{
		return -1;
	}

	@ConfigItem(
		keyName = "points",
		name = "",
		description = ""
	)
	void points(int points);

	@ConfigItem(
		keyName = "expeditious",
		name = "",
		description = "",
		hidden = true
	)
	default int expeditious()
	{
		return -1;
	}

	@ConfigItem(
		keyName = "expeditious",
		name = "",
		description = ""
	)
	void expeditious(int expeditious);

	@ConfigItem(
		keyName = "slaughter",
		name = "",
		description = "",
		hidden = true
	)
	default int slaughter()
	{
		return -1;
	}

	@ConfigItem(
		keyName = "slaughter",
		name = "",
		description = ""
	)
	void slaughter(int slaughter);
}
