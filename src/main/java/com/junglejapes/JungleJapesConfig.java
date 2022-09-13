package com.junglejapes;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Jungle Japes Indicator")
public interface JungleJapesConfig extends Config
{
	@ConfigItem(
		keyName = "Volume",
		name = "Volume",
		description = "The volume you play the sound effect."
	)
	default int volume()
	{
		return 100;
	}
}
