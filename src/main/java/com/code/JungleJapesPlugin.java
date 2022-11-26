package com.code;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

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
	private boolean invocationChanged = false;

	private static final int BANANA_GAME_OBJECT_ID = 45755;
	private static final int BANANA_SLIP_ANIMATION_ID = 4030;
	private static final int BANANA_GRAPHICS_ID = 1575;
	private static final int TOA_LOBBY_ID = 13454;
	private static final int TOA_NEXUS_ID = 14160;
	private static final int TOA_APMEKEN_ID = 15186;
	private static final int TOA_BABA_ID = 15188;
	private static int BANANA_DELAY = 0; // in game ticks.
	private int invocationLevel = 0;
	private int invocationVarbit = 0;

	public JungleJapesPlugin() {}

	@Override
	protected void startUp() {
		inToaRaid = false;

		// if the plugin was turned on inside the raid, check if there is an invocation level.
		clientThread.invokeLater(this::checkInvocation);
	}

	@Override
	protected void shutDown() {
		inToaRaid = false;
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if(BANANA_DELAY > 0) BANANA_DELAY--;
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged) {
		if(animationChanged.getActor() == null || animationChanged.getActor().getName() == null) return;
		if(BANANA_DELAY > 0) return;
		if(!inToaRaid) return;

		for(Player player : client.getPlayers()) {
			if(player.getAnimation() == BANANA_SLIP_ANIMATION_ID || player.getGraphic() == BANANA_GRAPHICS_ID) {
				playSound("rallittelija");
				BANANA_DELAY = 4;
			}
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned) {
		if(BANANA_DELAY > 0) return;
		if(!inToaRaid) return;

		if(gameObjectSpawned.getGameObject().getId() == BANANA_GAME_OBJECT_ID) {
			playSound("stuge");
			BANANA_DELAY = 4;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitValueChanged) {
		invocationVarbit = client.getVarbitValue(Varbits.TOA_RAID_LEVEL); // get the invocation level.
		if(getRegion() == TOA_LOBBY_ID) resetState();
		if(getRegion() == TOA_NEXUS_ID || getRegion() == TOA_APMEKEN_ID || getRegion() == TOA_BABA_ID) invocationChanged = true;
		checkInvocation();
	}

	/**
	 * checkInvocation() checks whether there is an invocation level set on the raid. If the invocation level is 0,
	 * then they have not turned on any invocations, including jungle japes, or they are not in a raid, in which case
	 * the plugin will not be active for the sound effects.
	 */
	private void checkInvocation() {
		if(client.getGameState() != GameState.LOGGED_IN) return;

		if(invocationChanged) {
			invocationLevel = invocationVarbit;
			invocationChanged = false;
		}

		if(invocationLevel > 0) inToaRaid = true;
		else resetState();
	}

	/**
	 * resetState() resets the raid state by setting their in raid status to false and the invocation level to 0.
	 */
	private void resetState() {
		invocationLevel = 0;
		inToaRaid = false;
	}

	/**
	 * Gets the map region ID of the local player's location and returns it.
	 * @return Map region ID.
	 */
	private int getRegion() {
		LocalPoint localPoint = client.getLocalPlayer().getLocalLocation();
		return WorldPoint.fromLocalInstance(client, localPoint).getRegionID();
	}

	/**
	 * playSound() takes an input to decide which sound effect to play. This method loads the .wav audio files,
	 * outputting sound.
	 * @param audio - "stuge" or "rallittelija" depending on which reason it is used for.
	 */
	private void playSound(String audio) {
		String soundFile = "/" + audio + ".wav";
		if(clip != null) clip.close();

		AudioInputStream soundInputStream = null;
		try {
			InputStream input = JungleJapesPlugin.class.getResourceAsStream(soundFile);
			InputStream bufferedInput = new BufferedInputStream(input);  // support the optional mark/reset for AudioInputStream
			soundInputStream = AudioSystem.getAudioInputStream(bufferedInput);
		} catch (UnsupportedAudioFileException e) {
			log.warn("The specified audio file is not supported.");
			e.printStackTrace();
		} catch (IOException e) {
			log.warn("Failed to load sound.");
			e.printStackTrace();
		} catch (NullPointerException e) {
			log.warn("Audio file not found.");
			e.printStackTrace();
		}

		if(soundInputStream == null) return;
		if(!tryToLoadFile(soundInputStream)) return;

		// volume
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
