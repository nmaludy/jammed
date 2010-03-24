
package com.jammed.app;

import com.jammed.app.Protos.Playlist;
import com.jammed.app.ProtocolMessage.Message;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.concurrent.Callable;

import java.net.DatagramPacket;

public class PacketExecutor implements Callable<Packet> {
	
	private final DatagramPacket packet;
	
	public PacketExecutor (final DatagramPacket dp) {
		this.packet = dp;
	}
	
	public Packet call() {
		final byte[] data = packet.getData();
		final int  offset = packet.getOffset();
		final int  length = packet.getLength();
		
		return handlePacket(data, offset, length);
	}
	
	protected Packet handlePacket (final byte[] data, final int offset,
		final int length) {
	
		byte[] header  = new byte[PacketHeader.getSizeInBytes()];
		byte[] message = new byte[length - PacketHeader.getSizeInBytes()];
		
		System.arraycopy(data, offset,
			header, 0, PacketHeader.getSizeInBytes());
		
		System.arraycopy(data, offset + PacketHeader.getSizeInBytes(),
			message, 0, length - PacketHeader.getSizeInBytes());
		
		return new Packet(header, message);
	}
	
}
