
package com.jammed.app;

import com.jammed.gen.Protos.Playlist;

import com.google.protobuf.MessageLite;


public class PlaylistHandler extends PacketHandler<Playlist> {
	
	public PlaylistHandler() {
	}
	
	public boolean isMessageSupported (final MessageLite message) {
		return (message instanceof Playlist);
	}
	
	public int type (final MessageLite message) {
		if (!(message instanceof Playlist)) {
			throw new IllegalArgumentException();
		}
		
		final Playlist playlist = (Playlist)message;
		
		return playlist.getType().ordinal();
	}
	
	public boolean handleMessage (final MessageLite message) {
		if (!(message instanceof Playlist)) {
			throw new IllegalArgumentException();
		}
		
		final Playlist playlist = (Playlist)message;
		
		if (playlist.getHost().equals(Cloud.getInstance().getHostName())) {
			System.out.println("Received message I sent");
		}
		
		System.out.println("Received Playlist of size: " +
			playlist.getMediaList().size());
		
		System.out.println("from: " + playlist.getHost());
		
		return true;
	}
	
}
