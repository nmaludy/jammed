
package com.jammed.app;

import com.jammed.event.ReceivedStopEvent;
import com.jammed.event.RTPReceiverListener;
import com.jammed.event.StreamEvent;

public class DirectedPlaylist implements RTPReceiverListener {
	
	public DirectedPlaylist() {
		System.out.println("Creating Directed Playlist");
	}
	
	public void receivedStreamUpdate(final StreamEvent event) {
		System.out.println("Stream Event");
		
		if (event instanceof ReceivedStopEvent) {
			// Song has finished playing
			
			System.out.println("Song is finished");
		}
	}
}
