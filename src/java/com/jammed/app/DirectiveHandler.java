
package com.jammed.app;

import com.jammed.gen.Protos.Directive;

import com.google.protobuf.MessageLite;

import java.util.List;

public class DirectiveHandler extends PacketHandler<Directive> {
	
	private final MessageBox display;

	public DirectiveHandler(final MessageBox display) {
		this.display = display;
	}
	
	public boolean isMessageSupported (final MessageLite message) {
		return (message instanceof Directive);
	}
	
	public int type (final MessageLite message) {
		if (!(message instanceof Directive)) {
			throw new IllegalArgumentException();
		}
		
		final Directive directive = (Directive)message;
		
		return directive.getType().ordinal();
	}
	
	public boolean handleMessage (final MessageLite message) {
		if (!(message instanceof Directive)) {
			throw new IllegalArgumentException();
		}
		
		final Directive directive = (Directive)message;
		
		if (Cloud.getInstance().getHostName().equals(
			directive.getDestination())) {
		
			// This is a message for us
			final List<String> m = directive.getDirectiveList();
			display.setMessage(m);
		}
		
		return true;
	}
	
}
