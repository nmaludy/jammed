
package com.jammed.handlers;

import com.jammed.app.PacketHandler;
import com.jammed.app.MessageBox;
import com.jammed.app.Cloud;
import com.jammed.app.DirectedPlaylist;

import com.jammed.gen.MessageProtos.Directive;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import java.util.List;

public class DirectiveHandler extends PacketHandler<Directive> {
	
	private final DirectedPlaylist playlist;

	public DirectiveHandler(final DirectedPlaylist p) {
		this.playlist = p;
	}
	
	public boolean isMessageSupported (final MessageLite message) {
		return (message instanceof Directive);
	}
	
	public boolean isMessageSupported (final int type) {
		final Directive.Builder builder = Directive.newBuilder();
		
		return builder.getType().ordinal() == type;
	}
	
	public int type (final MessageLite message) {
		if (!(message instanceof Directive)) {
			throw new IllegalArgumentException();
		}
		
		final Directive directive = (Directive)message;
		
		return directive.getType().ordinal();
	}
	
	public Directive mergeFrom (final byte[] data) {
		try {
			final Directive.Builder builder = Directive.newBuilder();
			builder.mergeFrom(data);
			
			return builder.build();
		} catch (final InvalidProtocolBufferException ipbe) {
			return null;
		}
	}
	
	public boolean handleMessage (final MessageLite message) {
		if (!(message instanceof Directive)) {
			throw new IllegalArgumentException();
		}
		
		if (this.playlist == null) {
			return false;
		}
		
		final Directive directive = (Directive)message;
		
		if (Cloud.getInstance().getHostName().equals(
			directive.getDestination())) {
		
			// This is a message for us
			if (directive.hasPlaylist()) {
				playlist.stop();
				playlist.setPlaylist(directive.getPlaylist());
				playlist.start();
			}
			
		}
		
		return true;
	}
	
}
