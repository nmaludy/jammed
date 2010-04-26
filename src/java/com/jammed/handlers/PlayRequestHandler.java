
package com.jammed.handlers;

import com.jammed.app.Cloud;
import com.jammed.app.FileManager;
import com.jammed.app.PacketHandler;

import com.jammed.gen.MediaProtos.Media;
import com.jammed.gen.MessageProtos.PlayRequest;
import com.jammed.gen.MessageProtos.PlayResponse;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import java.io.File;

public class PlayRequestHandler extends PacketHandler<PlayRequest> {
	
	public PlayRequestHandler() {
	}
	
	public boolean isMessageSupported (final MessageLite message) {
		return (message instanceof PlayRequest);
	}
	
	public boolean isMessageSupported (final int type) {
		final PlayRequest.Builder builder = PlayRequest.newBuilder();
		
		return builder.getType().ordinal() == type;
	}
	
	public int type (final MessageLite message) {
		if (!(message instanceof PlayRequest)) {
			throw new IllegalArgumentException();
		}
		
		final PlayRequest playRequest = (PlayRequest)message;
		
		return playRequest.getType().ordinal();
	}
	
	public PlayRequest mergeFrom (final byte[] data) {
		try {
			final PlayRequest.Builder builder = PlayRequest.newBuilder();
			builder.mergeFrom(data);
			
			return builder.build();
		} catch (final InvalidProtocolBufferException ipbe) {
			return null;
		}
	}
	
	public boolean handleMessage (final MessageLite message) {
		if (!(message instanceof PlayRequest)) {
			throw new IllegalArgumentException();
		}
		
		final PlayRequest playRequest = (PlayRequest)message;
		
		// Handle PlayRequest
		final PlayResponse.Builder builder = PlayResponse.newBuilder();
		
		
		builder.setRequest(playRequest.getRequest());
		builder.setAddress(playRequest.getRequest().getOrigin());
		
		if (playRequest.getStream()) {
			// Request to steam media.
			builder.setAudioPort(2000);
		} else {
			// Request to transfer media.
			builder.setAudioPort(-1);
			
			final Media.Builder media = Media.newBuilder();
			media.setLocation(playRequest.getMedia().getLocation());
			media.setTitle(playRequest.getMedia().getTitle());
			media.setHostname(playRequest.getMedia().getHostname());
			
			final File source  = new File(playRequest.getMedia().getLocation());
			final byte[] bytes = FileManager.getBytesFromFile(source);
			media.setFile(ByteString.copyFrom(bytes));
			
			builder.setMedia(media);
		}
		
		Cloud.getInstance().send(builder.build(), playRequest.getRequest().getId());
		
		return true;
	}
	
}
