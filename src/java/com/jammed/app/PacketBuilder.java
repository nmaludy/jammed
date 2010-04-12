
package com.jammed.app;

import com.google.protobuf.MessageLite;

import java.util.ArrayList;
import java.util.List;

public class PacketBuilder {

	private final List<PacketHandler<? extends MessageLite>> handlers;
	
	static class PacketBuilderHolder {
		static PacketBuilder instance = new PacketBuilder();
	}
	
	private PacketBuilder () {
		handlers = new ArrayList<PacketHandler<? extends MessageLite>>();
	}
	
	public static PacketBuilder getInstance() {
		return PacketBuilderHolder.instance;
	}
	
	public void addMessageHandler (final PacketHandler<? extends MessageLite> handler) {
		this.handlers.add(handler);
	}
	
	public MessageLite buildMessage (final byte[] data, final int type) {
		for (final PacketHandler<? extends MessageLite> handler : handlers) {
			if (handler.isMessageSupported(type)) {
				return handler.mergeFrom(data);
			}
		}
		
		throw new UnknownMessageException("No handler for type: " + type);
	}
}
