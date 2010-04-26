
package com.jammed.app;

import com.jammed.gen.MediaProtos.Media;
import com.jammed.gen.MediaProtos.Playlist;

import com.jammed.event.ReceivedStopEvent;
import com.jammed.event.RTPReceiverListener;
import com.jammed.event.StreamEvent;

public class DirectedPlaylist implements RTPReceiverListener {
	
	private final RTPSessionManager manager = RTPSessionManager.getInstance();
	private final MessageBox display;
	
	private Playlist playlist;
	private int current;
	
	public DirectedPlaylist(final MessageBox message) {
		System.out.println("Creating Directed Playlist");
		
		this.current  = -1;
		this.display  = message;
		this.playlist = null;
	}
	
	public void setPlaylist(final Playlist p) {
		this.playlist = p;
	}
	
	public void start() {
		if (playlist != null && current < 0) {
			// We are not playing anthing
			current = 0;
			next();
		}
	}
	
	public void stop() {
		playlist = null;
		current  = -1;
	}
	
	public void receivedStreamUpdate(final StreamEvent event) {
		System.out.println("Stream Event");
		
		if (event instanceof ReceivedStopEvent) {
			// Song has finished playing
			System.out.println("Song is finished");
			if (playlist != null) {
				next();
			}
		}
	}
	
	protected void next() {
		if (current < playlist.getMediaCount()) { 
			final Media media = playlist.getMedia(current);
			
			display.setMessage(media);
			manager.requestReceiveSession(media, this);
			current++;
		} else {
			// We are done with the current playlist
			current = -1;
		}
	}
}
