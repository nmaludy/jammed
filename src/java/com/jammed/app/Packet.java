
package com.jammed.app;

import com.jammed.gen.MediaProtos.Playlist;
import com.jammed.gen.MessageProtos.Search;
import com.jammed.gen.MessageProtos.Directive;

import com.jammed.gen.ProtoBuffer.Message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

public class Packet implements Comparable<Packet> {
	private final byte[] data;
	private final PacketHeader header;
	private final MessageLite message;
	
	public Packet(final byte[] header, final byte[] data) {
		this.header = new PacketHeader(header);
		
		if (isFinished() && (this.header.getSequence() == 0)) {
			// Single packet message
			this.message = PacketBuilder.getInstance().buildMessage(data, this.header.getType());
			this.data    = null;
		} else {
			// Multi-packet message
			this.data    = data;
			this.message = null;
		}
		
	}
	
	public boolean isChunk() {
		return header.isChunk();
	}
	
	public boolean isFinished() {
		return header.isFinished();
	}
	
	public PacketHeader getPacketHeader() {
		return header;
	}
	
	public MessageLite getMessage() {
		return message;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public int compareTo (final Packet other) {
		return this.getPacketHeader().compareTo(other.getPacketHeader());
	}
	
	@Override
	public int hashCode() {
		return header.getChecksum() * 17;
	}
	
	@Override
	public boolean equals (final Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Packet)) return false;
		
		final Packet other = (Packet)obj;
		if (other.getPacketHeader().getChecksum() == 
			this.getPacketHeader().getChecksum()) {
		
			return true;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		
		sb.append(this.getPacketHeader().toString());
		
		return sb.toString();
	}
}
