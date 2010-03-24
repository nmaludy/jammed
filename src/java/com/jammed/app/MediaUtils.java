package com.jammed.app;

import javax.media.MediaLocator;
import javax.media.bean.playerbean.MediaPlayer;

/**
 * TODO: Abstract more media handling utilities to this class
 *
 * @author nmaludy
 */
public class MediaUtils {

	public static MediaPlayer createMediaPlayer(String nameUrl) {
		MediaLocator mediaLocator = null;
		MediaPlayer mediaPlayer = null;

		mediaLocator = new MediaLocator(nameUrl);
		if (mediaLocator == null || nameUrl.equals("")) {
			System.err.println("ERROR - MediaUtils.createMediaPlayer - Invalid media URL: " + nameUrl);
			return (null);
		}

		mediaPlayer = new MediaPlayer();
		mediaPlayer.setMediaLocator(mediaLocator);
		mediaPlayer.setPopupActive(false);
		mediaPlayer.setControlPanelVisible(true);

		if (mediaPlayer.getPlayer() == null) {
			System.err.println("ERROR - MediaUtils.createMediaPlayer - Player creation failed: " + nameUrl);
			return (null);
		}

		return (mediaPlayer);
	}
}
