package com.junglejapes;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.sound.sampled.*;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.io.IOException;
import java.net.URL;

@Slf4j
@PluginDescriptor(
	name = "Jungle Japes Buff"
)
public class JungleJapesPlugin extends Plugin
{
	public Clip clip;
	public String soundFile = "bananasSoundEffect.wav";

	@Inject
	private Client client;

	@Inject
	private JungleJapesConfig config;

	private void playSound() {
		if(clip != null) {
			clip.close();
		}

		Class c = null;
		AudioInputStream soundFileAudioInputStream = null;
		try {
			c = Class.forName("com.code.OefPlugin");
			URL url = c.getClassLoader().getResource(soundFile);
			soundFileAudioInputStream = AudioSystem.getAudioInputStream(url);
		} catch (ClassNotFoundException | UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}

		if(soundFileAudioInputStream == null) return;
		if(!tryToLoadFile(soundFileAudioInputStream)) return;

		//volume
		FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float volumeValue = config.volume() - 100;

		volume.setValue(volumeValue);
		clip.loop(0);
	}

	private boolean tryToLoadFile(AudioInputStream sound)
	{
		try
		{
			clip = AudioSystem.getClip();
			clip.open(sound);
			return true;
		} catch (LineUnavailableException | IOException e) {log.warn("Could not load the file: ", e);}
		return false;
	}

	@Provides
	JungleJapesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(JungleJapesConfig.class);
	}
}
