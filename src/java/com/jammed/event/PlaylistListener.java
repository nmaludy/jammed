package com.jammed.event;

import java.util.EventListener;

/**
 *
 * @author nmaludy
 */
public interface PlaylistListener extends EventListener {

	public void playlistChanged(PlaylistEvent event);
}
