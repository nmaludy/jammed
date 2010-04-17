package com.jammed.app;

import com.jammed.event.RTPReceiverListener;
import com.jammed.event.StreamReceivedEvent;
import com.jammed.gen.MediaProtos.Media;
import com.jammed.ui.PlayerPanel;
import java.io.File;
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
	private static final RTPSessionManager sessionManager = RTPSessionManager.getInstance();
	private static MediaController INSTANCE;
	private VideoHandler videoHandler;
	private MediaPlayer player;
	private MediaPlayer remoteVideoPlayer;
	private PlayerPanel panel;
	private RTPReceiver audioReceiver;
	private RTPReceiver videoReceiver;

	//Variables to manage controller state
	private boolean isPaused = false;
	private boolean sessionInProgress = false;

	static { //Ensure that INSTNACE is initialzed
		 INSTANCE = new MediaController();
	}
	

	private MediaController() {
		videoHandler = new VideoHandler();
		sessionManager.addReceiverListener(this);
	}

	public static MediaController getInstance() {
		return INSTANCE;
	}

	public void setPlayerPanel(PlayerPanel panel) {
		this.panel = panel;
	}

	public void playMedia(Media m) {
		if (m.getHostname().equals(Cloud.getInstance().getHostName())) {
			try {
				File f = new File(m.getLocation());
				String selectedUrl = f.toURI().toURL().toString();
				playLocalMedia(selectedUrl);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			destroyCurrent();
			sessionManager.requestReceiveSession(m, this);
		}
	}

	private void playLocalMedia(String mediaURL) {
		destroyCurrent();
		initPlayer(mediaURL);
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
		if (audioReceiver != null) {
			audioReceiver.stop();
			audioReceiver = null;
		}
		if (videoReceiver != null) {
			videoReceiver.stop();
			videoReceiver = null;
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
	}

	public void streamReceived(StreamReceivedEvent event) {
		if (event.isVideo()) {
			remoteVideoPlayer = MediaUtils.createMediaPlayer(event.getDataSource());
			remoteVideoPlayer.addControllerListener(videoHandler);
			remoteVideoPlayer.realize();
		} else {
			initRemotePlayer(event.getDataSource());
		}
	}

	private class VideoHandler implements ControllerListener {

		public void controllerUpdate(ControllerEvent event) {
			if (event instanceof RealizeCompleteEvent) {
				handleRealizeComplete((RealizeCompleteEvent) event);
			}
		}

		private void handleRealizeComplete(RealizeCompleteEvent event) {
			remoteVideoPlayer.prefetch();
			panel.setVideoPlayer(remoteVideoPlayer);
			remoteVideoPlayer.start();
			sessionInProgress = true;
		}
	}
}
