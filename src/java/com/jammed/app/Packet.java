
package com.jammed.app;

import com.jammed.app.Protos.Playlist;
import com.jammed.app.Protos.Search;

import com.jammed.app.ProtocolMessage.Message;

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
			this.message = getMessage(data, this.header.getType());
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
	
	public static MessageLite getMessage (final byte[] data, final int type) {
		try {
			switch (Message.Type.valueOf(type)) {
				case PLAYLIST:
					final Playlist.Builder playlistBuilder =
						Playlist.newBuilder();
					playlistBuilder.mergeFrom(data);
					
					return playlistBuilder.build();
					
				case SEARCH:
					final Search.Builder searchBuilder = Search.newBuilder();
					searchBuilder.mergeFrom(data);
					
					return searchBuilder.build();
					
				default:
					return null;
			}
		} catch (final InvalidProtocolBufferException ipbe) {
			ipbe.printStackTrace();
			return null;
		}
	}
	
	public int compareTo (final Packet other) {
		return this.getPacketHeader().compareTo(other.getPacketHeader());
	}
}
