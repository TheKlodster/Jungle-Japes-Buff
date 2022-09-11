package com.junglejapes;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Jungle Japes Buff")
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

	enum OptionEnum
	{
	stuge,
	rallittelija
	}
	@ConfigItem(
			position = 0,
			keyName = "soundConfig",
			name = "Sound Effect",
			description = "Choose a sound effect for stepping on a banana."
	)
	default OptionEnum soundConfig() {
		return OptionEnum.stuge;
	}

}
