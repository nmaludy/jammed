
package com.jammed.handlers;

import com.jammed.app.Cloud;
import com.jammed.app.PacketHandler;

import com.jammed.gen.MessageProtos.PlayRequest;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

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
		
		return true;
	}
	
}
