
package com.jammed.app;

import com.jammed.app.Protos.Directive;

import com.google.protobuf.MessageLite;

import java.util.List;

public class DirectiveHandler implements PacketHandler<Directive> {
	
	private final MessageBox display;

	public DirectiveHandler(final MessageBox display) {
		this.display = display;
	}
	
	public boolean isMessageSupported (final MessageLite message) {
		return (message instanceof Directive);
	}
	
	public int type (final Directive directive) {
		return directive.getType().ordinal();
	}
	
	public boolean handleMessage (final Directive directive) {
		
		if (Cloud.getInstance().getHostName().equals(
			directive.getDestination())) {
		
			// This is a message for us
			final List<String> message = directive.getMessageList();
			display.setMessage(message);
		}
		
		return true;
	}
	
}
