package com.junglejapes;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Slf4j
@PluginDescriptor(
	name = "Jungle Japes Indicator"
)
public class JungleJapesPlugin extends Plugin {

	/**
	 * BABA(15188),
	 * TOA_PATH_OF_APMEKEN_BOSS(3776, 5439, 3839, 5376)
	 */

	public Clip clip;

	@Inject
	private Client client;

	@Inject
	private JungleJapesConfig config;

	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged) {
		if(client.getGameState() == GameState.LOGGED_IN && client.getLocalPlayer().getAnimation() == 4030) { // BANANA_PEEL stun animation ID = 4030
			playSound("rallittelija");
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned) {
		if(gameObjectSpawned.getGameObject().getId() == 45755) { // STATIC INT BANANA_PEEL = 45755
			playSound("stuge");
		}
	}

	private void playSound(String audio) {
		String soundFile = "src/main/resources/" + audio + ".wav";

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
