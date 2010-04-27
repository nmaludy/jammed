package com.jammed.app;

import com.jammed.event.RTPReceiverListener;
import com.jammed.event.ReceivedStopEvent;
import com.jammed.event.ReceivedStreamEvent;
import com.jammed.event.StreamEvent;
import com.jammed.gen.MediaProtos.Media;
import com.jammed.ui.PlayerPanel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.media.CachingControlEvent;
import javax.media.Control;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DurationUpdateEvent;
import javax.media.EndOfMediaEvent;
import javax.media.GainControl;
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
import javax.swing.SwingUtilities;

/**
 *
 * @author nmaludy
 */
public class MediaController implements ControllerListener, RTPReceiverListener {
	private static final RTPSessionManager sessionManager = RTPSessionManager.getInstance();
	private static MediaController INSTANCE;
	private List<ControllerListener> controllerListeners;
	private RemoteVideoHandler videoHandler;
	private RemoteAudioHandler audioHandler;
	private MediaPlayer player;
	private MediaPlayer remoteVideoPlayer;
	private PlayerPanel panel;
	private RTPReceiver audioReceiver;
	private RTPReceiver videoReceiver;
	private float preferredGain;

	//Variables to manage controller state
	private boolean isPaused = false;
	private boolean sessionInProgress = false;

	static { //Ensure that INSTNACE is initialzed
		 INSTANCE = new MediaController();
	}
	

	private MediaController() {
		controllerListeners = new ArrayList<ControllerListener>();
		videoHandler = new RemoteVideoHandler();
		audioHandler = new RemoteAudioHandler();
		sessionManager.addReceiverListener(this);
		preferredGain = 0.6f;
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

	public void play() {
		if (player!= null) {
			player.start();
		}
		if (remoteVideoPlayer != null) {
			remoteVideoPlayer.start();
		}
		sessionInProgress = true;
	}

	public void pause() {
		if (player != null) {
			player.stop();
		}
		if (remoteVideoPlayer != null) {
			remoteVideoPlayer.stop();
			remoteVideoPlayer = null;
		}
	}

	public boolean isSessionInProgress() {
		return sessionInProgress;
	}

	private void destroyCurrent() {
		if (player != null) {
			player.stop();
			player.close();
			//player.deallocate();
			player = null;
		}
		if (remoteVideoPlayer != null) {
			remoteVideoPlayer.stop();
			remoteVideoPlayer.close();
			//remoteVideoPlayer.deallocate();
			remoteVideoPlayer = null;
		}
		if (audioReceiver != null) {
			audioReceiver.stop();
			audioReceiver = null;
		}
		if (videoReceiver != null) {
			videoReceiver.stop();
			videoReceiver = null;
		}
		if (panel != null) {
			panel.resetAll();
		}
		sessionInProgress = false;
	}

	/*
	 * TODO: Handle all of the control events
	 */
	public void controllerUpdate(ControllerEvent event) {
		System.out.println(event.getClass().toString());
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
		} else if (event instanceof EndOfMediaEvent) {
			fireControllerEvent(event);
			destroyCurrent();
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
		setGain(preferredGain);
		play();
	}

	public void receivedStreamUpdate(StreamEvent event) {
		if (event instanceof ReceivedStreamEvent) {
			ReceivedStreamEvent rs = (ReceivedStreamEvent) event;
			if (rs.isVideo()) {
				remoteVideoPlayer = MediaUtils.createMediaPlayer(rs.getDataSource());
				remoteVideoPlayer.addControllerListener(videoHandler);
				remoteVideoPlayer.realize();
			} else {
				player = MediaUtils.createMediaPlayer(rs.getDataSource());
				player.addControllerListener(audioHandler);
				player.realize();
			}
		} else if (event instanceof ReceivedStopEvent) {
			destroyCurrent();
		}
	}

	public void addControllerListener(final ControllerListener c) {
		controllerListeners.add(c);
	}

	public void removeControllerListener(final ControllerListener c) {
		controllerListeners.remove(c);
	}

	protected void fireControllerEvent(final ControllerEvent event) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				if (controllerListeners == null) {
					return;
				}

				for(ControllerListener l : controllerListeners) {
					l.controllerUpdate(event);
					System.out.println("fire event");
				}
			}
		});
	}

	public void mute() {
		if (player == null) {
			return;
		}
		GainControl g = player.getGainControl();
		if (g != null) {
			g.setMute(true);
		}
	}

	public void unmute() {
		if (player == null) {
			return;
		}
		GainControl g = player.getGainControl();
		if (g != null) {
			g.setMute(false);
		}
	}

	public void setGain(float gain) {
		preferredGain = gain;
		if (player == null) {
			return;
		}
		GainControl g = player.getGainControl();
		if (g != null) {
			g.setLevel(preferredGain);
		}
	}

	public double getMediaTime() {
		return player.getMediaTime().getSeconds();
	}

	public double getMediaDuration() {
		return player.getDuration().getSeconds();
	}

	private class RemoteVideoHandler implements ControllerListener {

		public void controllerUpdate(ControllerEvent event) {
			if (event instanceof RealizeCompleteEvent) {
				handleVideoRealizeComplete((RealizeCompleteEvent) event);
			}
		}

		private void handleVideoRealizeComplete(RealizeCompleteEvent event) {
			remoteVideoPlayer.prefetch();
			panel.setVideoPlayer(remoteVideoPlayer);
			remoteVideoPlayer.start();
			sessionInProgress = true;
		}
	}

	private class RemoteAudioHandler implements ControllerListener {

		public void controllerUpdate(ControllerEvent event) {
			if (event instanceof RealizeCompleteEvent) {
				handleAudioRealizeComplete((RealizeCompleteEvent) event);
			}
		}

		private void handleAudioRealizeComplete(RealizeCompleteEvent event) {
			player.prefetch();
			panel.setAudioPlayer(player);
			player.start();
			sessionInProgress = true;
		}
	}
}
