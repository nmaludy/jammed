
package com.jammed.handlers;

// psuedo-android
import com.jammed.android.ArrayAdapter;

import com.jammed.app.Cloud;
import com.jammed.app.PacketHandler;

import com.jammed.gen.MediaProtos.Media;
import com.jammed.gen.MediaProtos.Playlist;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import java.util.List;

public class MediaListHandler extends PacketHandler<Playlist> {
	
	private ArrayAdapter<Media> adapter;
	private boolean processed;
	
	static class MediaListHolder {
		static MediaListHandler instance = new MediaListHandler();
	}
	
	private MediaListHandler() {
	}
	
	public static MediaListHandler getInstance() {
		return MediaListHolder.instance;
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
	
	public void setAdapter (final ArrayAdapter<Media> adapter) {
		this.adapter   = adapter;
		this.processed = false;
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
		
		if (!processed) {
			processed = true;
			
			for (final Media media : playlist.getMediaList()) {
				adapter.add(media);
			}
		}
		
		return true;
	}
	
}
