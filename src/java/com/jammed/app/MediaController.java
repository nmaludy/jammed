package com.jammed.app;

import javax.media.CachingControlEvent;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DurationUpdateEvent;
import javax.media.MediaTimeSetEvent;
import javax.media.PrefetchCompleteEvent;
import javax.media.RateChangeEvent;
import javax.media.RealizeCompleteEvent;
import javax.media.SizeChangeEvent;
import javax.media.StartEvent;
import javax.media.StopTimeChangeEvent;
import javax.media.TransitionEvent;
import javax.media.bean.playerbean.MediaPlayer;
import javax.media.format.FormatChangeEvent;

/**
 *
 * @author nmaludy
 */
public class MediaController implements ControllerListener{
	private static MediaController INSTANCE;
	private MediaPlayer player;
	private PlayerPanel panel;
	private String mediaURL;
	private String remoteHostname;
	private int remotePort;

	//Variables to manage controller state
	private boolean isPaused = false;
	private boolean sessionInProgress = false;

	static { //Ensure that INSTNACE is initialzed
		 INSTANCE = new MediaController();
	}
	

	private MediaController() {

	}

	public static MediaController getInstance() {
		return INSTANCE;
	}

	public void setPlayerPanel(PlayerPanel panel) {
		this.panel = panel;
	}

	public void playLocalMedia(String mediaURL) {
		this.mediaURL = mediaURL;
		initPlayer();
	}

	public void playRemoteMedia(String hostname, int port) {
		this.remoteHostname = hostname;
		this.remotePort = port;
		initPlayer();
	}

	private void initPlayer() {
		if (player != null) {
			player.close();
		}
		player = MediaUtils.createMediaPlayer(mediaURL);
		player.addControllerListener(this);
		player.realize();
	}

	public void playOrPause() {
		if (isPaused || !sessionInProgress) {
			play();
		} else {
			pause();
		}
	}

	public void play() {
		if (isPaused) {
			player.restoreMediaTime();
			isPaused = false;
		}
		player.start();
		sessionInProgress = true;
	}

	public void pause() {
		player.saveMediaTime();
		player.stop();
		isPaused = true;
	}

	public void stop() {
		player.stop();
		player.deallocate();
		sessionInProgress = false;
	}

	public void destroy() {
		player.close();
	}

	/*
	 * TODO: Handle all of the control events
	 */
	public synchronized void controllerUpdate(ControllerEvent event) {
		if (event instanceof RealizeCompleteEvent) {
			handleRealizeComplete((RealizeCompleteEvent) event);
		} else if (event instanceof PrefetchCompleteEvent) {
			//processPrefetchComplete ( (PrefetchCompleteEvent) event );
		} else if (event instanceof ControllerErrorEvent) {
			//processControllerError ( (ControllerErrorEvent) event );
		} else if (event instanceof ControllerClosedEvent) {
			// processControllerClosed ( (ControllerClosedEvent) event );
		} else if (event instanceof DurationUpdateEvent) {
			// Time t = ((DurationUpdateEvent)event).getDuration();
		} else if (event instanceof CachingControlEvent) {
			// processCachingControl ( (CachingControlEvent) event );
		} else if (event instanceof StartEvent) {
		} else if (event instanceof MediaTimeSetEvent) {
		} else if (event instanceof TransitionEvent) {
		} else if (event instanceof RateChangeEvent) {
		} else if (event instanceof StopTimeChangeEvent) {
		} else if (event instanceof FormatChangeEvent) {
			//processFormatChange ( (FormatChangeEvent) event );
		} else if (event instanceof SizeChangeEvent) {
		}
	}

	protected void handleRealizeComplete(RealizeCompleteEvent event) {
		player.prefetch();
		panel.setPlayer(player);
		play();
	}

}
