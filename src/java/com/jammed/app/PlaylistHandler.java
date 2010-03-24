
package com.jammed.app;

import com.jammed.app.Protos.Playlist;

import com.google.protobuf.MessageLite;


public class PlaylistHandler implements PacketHandler<Playlist> {
	
	public PlaylistHandler() {
	}
	
	public boolean isMessageSupported (final MessageLite message) {
		return (message instanceof Playlist);
	}
	
	public int type (final Playlist playlist) {
		return playlist.getType().ordinal();
	}
	
	public boolean handleMessage (final Playlist playlist) {
		
		if (playlist.getHost().equals(Cloud.getInstance().getHostName())) {
			System.out.println("Received message I sent");
		}
		
		System.out.println("Received Playlist of size: " +
			playlist.getMediaList().size());
		
		System.out.println("from: " + playlist.getHost());
		
		return true;
	}
	
}
