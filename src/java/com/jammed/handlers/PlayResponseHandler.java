
package com.jammed.handlers;

import com.jammed.app.Cloud;
import com.jammed.app.PacketHandler;

import com.jammed.gen.MessageProtos.PlayResponse;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

public class PlayResponseHandler extends PacketHandler<PlayResponse> {
	
	public PlayResponseHandler() {
	}
	
	public boolean isMessageSupported (final MessageLite message) {
		return (message instanceof PlayResponse);
	}
	
	public boolean isMessageSupported (final int type) {
		final PlayResponse.Builder builder = PlayResponse.newBuilder();
		
		return builder.getType().ordinal() == type;
	}
	
	public int type (final MessageLite message) {
		if (!(message instanceof PlayResponse)) {
			throw new IllegalArgumentException();
		}
		
		final PlayResponse playRequest = (PlayResponse)message;
		
		return playRequest.getType().ordinal();
	}
	
	public PlayResponse mergeFrom (final byte[] data) {
		try {
			final PlayResponse.Builder builder = PlayResponse.newBuilder();
			builder.mergeFrom(data);
			
			return builder.build();
		} catch (final InvalidProtocolBufferException ipbe) {
			return null;
		}
	}
	
	public boolean handleMessage (final MessageLite message) {
		if (!(message instanceof PlayResponse)) {
			throw new IllegalArgumentException();
		}
		
		final PlayResponse playRequest = (PlayResponse)message;
		
		// Handle PlayResponse
		
		return true;
	}
	
}
