
package com.jammed.app;

import com.jammed.gen.MediaProtos.Playlist;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

public class PlaylistHandler extends PacketHandler<Playlist> {
	
	public PlaylistHandler() {
	}
	
	public boolean isMessageSupported (final MessageLite message) {
		return (message instanceof Playlist);
	}
	
	public boolean isMessageSupported (final int type) {
		final Playlist.Builder builder = Playlist.newBuilder();
		
		return builder.getType().ordinal() == type;
	}
	
	public int type (final MessageLite message) {
		if (!(message instanceof Playlist)) {
			throw new IllegalArgumentException();
		}
		
		final Playlist playlist = (Playlist)message;
		
		return playlist.getType().ordinal();
	}
	
	public Playlist mergeFrom (final byte[] data) {
		try {
			final Playlist.Builder builder = Playlist.newBuilder();
			builder.mergeFrom(data);
			
			return builder.build();
		} catch (final InvalidProtocolBufferException ipbe) {
			return null;
		}
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
