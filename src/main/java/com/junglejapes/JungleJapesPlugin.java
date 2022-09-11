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
import java.nio.file.Paths;

@Slf4j
@PluginDescriptor(
	name = "Jungle Japes Buff"
)
public class JungleJapesPlugin extends Plugin
{
	public Clip clip;

	@Inject
	private Client client;

	@Inject
	private JungleJapesConfig config;

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			playSound();
		}
	}

	private void playSound() {

		//String soundFile = "src/main/resources/bananasSoundEffect.wav";
		String soundFile = "src/main/resources/" + config.soundConfig().toString() + ".wav";

		if(clip != null) {
			clip.close();
		}

		AudioInputStream soundInputStream = null;
		try {
			URL url = Paths.get(soundFile).toUri().toURL();
			soundInputStream = AudioSystem.getAudioInputStream(url);

		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}

		if(soundInputStream == null) return;
		if(!tryToLoadFile(soundInputStream)) return;

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
