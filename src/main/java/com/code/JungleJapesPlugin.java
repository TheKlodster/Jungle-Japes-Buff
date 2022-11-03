package com.code;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

@Slf4j
@PluginDescriptor(
	name = "Jungle Japes Indicator"
)
public class JungleJapesPlugin extends Plugin {

	public Clip clip;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private JungleJapesConfig config;

	@Getter
	@VisibleForTesting
	private boolean inToaRaid;

	private static final int BANANA_GAME_OBJECT_ID = 45755;
	private static final int BANANA_SLIP_ANIMATION_ID = 4030;
	private static final int BANANA_GRAPHICS_ID = 1575;
	private static int BANANA_SLIP_DELAY, BANANA_SPAWN_DELAY = 0; // in game ticks.

	public JungleJapesPlugin() {}

	@Override
	protected void startUp() {
		inToaRaid = false;

		// if the plugin was turned on inside the raid, check if there is an invocation level.
		clientThread.invokeLater(this::checkInvocation);
	}

	@Override
	protected void shutDown() {
		inToaRaid = false; // plugin is off, set their raid status to false.
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if(BANANA_SLIP_DELAY > 0) BANANA_SLIP_DELAY--;
		if(BANANA_SPAWN_DELAY > 0) BANANA_SPAWN_DELAY--;
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged) throws InterruptedException {
		if(animationChanged.getActor() == null || animationChanged.getActor().getName() == null) return;
		if(BANANA_SLIP_DELAY > 0) return;

		for(Player player : client.getPlayers()) {
			if(player.getAnimation() == BANANA_SLIP_ANIMATION_ID || player.getGraphic() == BANANA_GRAPHICS_ID) {
				playSound("rallittelija");
				BANANA_SLIP_DELAY = 5;
				BANANA_SPAWN_DELAY++;
			}
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned) throws InterruptedException {
		if(BANANA_SPAWN_DELAY > 0) return;

		if(inToaRaid && gameObjectSpawned.getGameObject().getId() == BANANA_GAME_OBJECT_ID) {
			playSound("stuge");
			BANANA_SPAWN_DELAY = 3;
			BANANA_SLIP_DELAY++;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitValueChanged) {
		checkInvocation();
	}

	/**
	 * checkInvocation() checks whether there is an invocation level set on the raid. If the invocationState is 0,
	 * then they are either not in the raid, or have not turned on any invocations, including jungle japes, in which
	 * case the plugin will not be active for the sound effects.
	 */
	private void checkInvocation() {
		if(client.getGameState() != GameState.LOGGED_IN) return;
		int invocationState = client.getVarbitValue(Varbits.TOA_RAID_LEVEL); // get the invocation level.
		if(invocationState > 0) inToaRaid = true;
	}

	/**
	 * playSound() takes an input to decide which sound effect to play. This method loads the .wav audio files,
	 * outputting sound.
	 * @param audio - "stuge" or "rallittelija" depending on which reason it is used for.
	 */
	private void playSound(String audio) {
		String soundFile = "src/main/resources/" + audio + ".wav";

		if(clip != null) {
			clip.close();
		}

		Class c = null;
		AudioInputStream soundInputStream = null;
		try {
			c = Class.forName("com.code.JungleJapesPlugin");
			URL url = c.getClassLoader().getResource(soundFile);
			soundInputStream = AudioSystem.getAudioInputStream(url);
		} catch (UnsupportedAudioFileException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		if(soundInputStream == null) return;
		if(!tryToLoadFile(soundInputStream)) return;

		FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float volumeValue = config.volume() - 100;

		volume.setValue(volumeValue);
		clip.loop(0);
	}

	private boolean tryToLoadFile(AudioInputStream sound) {
		try	{
			clip = AudioSystem.getClip();
			clip.open(sound);
			return true;
		} catch (LineUnavailableException | IOException e) {log.warn("Could not load the file: ", e);}
		return false;
	}

	@Provides
	JungleJapesConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(JungleJapesConfig.class);
	}
}