package com.jammed.app;

import com.jammed.ui.PlayerPanel;
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
import javax.media.protocol.DataSource;

/**
 *
 * @author nmaludy
 */
public class MediaController implements ControllerListener, RTPReceiverListener {
	private static MediaController INSTANCE;
	private MediaPlayer player;
	private PlayerPanel panel;
	private RTPReceiver receiver;

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
		destroyCurrent();
		initPlayer(mediaURL);
	}

	public void playRemoteMedia(String hostname, int port) {
		destroyCurrent();
		receiver = RTPReceiver.create(hostname, port);
		receiver.addReceiverListener(this);
		receiver.start();
	}

	private void initPlayer(String mediaURL) {
		player = MediaUtils.createMediaPlayer(mediaURL);
		player.addControllerListener(this);
		player.realize();
	}

	private void initRemotePlayer(DataSource ds) {
		player = MediaUtils.createMediaPlayer(ds);
		player.addControllerListener(this);
		player.realize();
	}

	private void playOrPause() {
		if (isPaused || !sessionInProgress) {
			play();
		} else {
			pause();
		}
	}

	private void play() {
		if (isPaused) {
			player.restoreMediaTime();
			isPaused = false;
		}
		player.start();
		sessionInProgress = true;
	}

	private void pause() {
		player.saveMediaTime();
		player.stop();
		isPaused = true;
	}

	private void destroyCurrent() {
		if (player != null) {
			player.close();
			player = null;
		}
		if (receiver != null) {
			receiver.stop();
			receiver = null;
		}
		sessionInProgress = false;
	}

	/*
	 * TODO: Handle all of the control events
	 */
	public void controllerUpdate(ControllerEvent event) {
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

	private void handleRealizeComplete(RealizeCompleteEvent event) {
		player.prefetch();
		panel.setPlayer(player);
		play();
		System.out.println("Playing");
	}

	public void streamReceived(StreamReceivedEvent event) {
		initRemotePlayer(event.getDataSource());
		System.out.println("Realizing");
	}

}
