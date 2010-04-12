
package com.jammed.app;

import com.google.protobuf.MessageLite;

import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.MulticastSocket;
import java.net.DatagramPacket;

import java.util.ArrayList;
import java.util.List;

/* TTL    |   Scope
 * -------------------------------------------------------------------------
 * = 0    |   Restricted to the same host. Won't be output by any interface.
 * = 1    |   Restricted to the same subnet. Won't be forwarded by a router.
 * < 32   |   Restricted to the same site, organization or department.
 * < 64   |   Restricted to the same region.
 * < 128  |   Restricted to the same continent.
 * < 255  |   Unrestricted in scope. (Global).
 */

public class MulticastSender {
	
	public static final int   LIMIT = 65000;
	public static final int   TTL   = 255;
	
	private static final long delay = 250L;
	
	private final InetAddress group;
	private final int port;
	private int source;
	
	private final List<PacketHandler<? extends MessageLite>> handlers;
	
	public MulticastSender (final String addressName, final int port) {
		try {
			this.group = InetAddress.getByName(addressName);
			this.port  = port;
		} catch (final UnknownHostException uhe) {
			throw new RuntimeException();
		}
		
		handlers = new ArrayList<PacketHandler<? extends MessageLite>>();
	}
	
	public void addMessageHandler (final PacketHandler<? extends MessageLite> handler) {
		this.handlers.add(handler);
	}
	
	public void setSource (final int source) {
		this.source = source;
	}
	
	public int getSource () {
		return source;
	}
	
	public void send (final MessageLite message) {
		send(message, 1);
	}
	
	public void send (final MessageLite message, final int request) {
		for (final PacketHandler<? extends MessageLite> handler : handlers) {
			if (handler.isMessageSupported(message)) {
				send(message, handler.type(message), request);
			}
		}
	}
	
	protected void send (final MessageLite messageLite, final int type,
		final int request) {
		
		final byte[] message = messageLite.toByteArray();
		send(message, type, request);
	}
	
	protected void send (final byte[] message, final int type,
		final int request) {
		
		final int size = message.length + PacketHeader.getSizeInBytes();
		final int numChunks = size / (LIMIT - 1) + 1;
		
		if (size > LIMIT) {
			sendChunks(arrayChunk(message, numChunks, type, request));
			return;
		}
		
		byte[] data = new byte[message.length + PacketHeader.getSizeInBytes()];
			
		final PacketHeader ph = new PacketHeader();
		
		ph.setType(type);
		ph.setSequence(0);
		ph.setRequest(request);
		ph.setSource(source);
		ph.setFinished(true);
		ph.setChunk(false);
		
		final byte[] header = ph.build();
		
		System.arraycopy(header, 0, data, 0, header.length);
		System.arraycopy(message, 0, data, header.length, message.length);
		
		final DatagramPacket packet = new DatagramPacket(data, data.length,
			group, port);
		
		try {
			final MulticastSocket ms = new MulticastSocket();
			
			ms.joinGroup(group);
			ms.setTimeToLive(TTL);
			ms.send(packet);
			ms.leaveGroup(group);
			
			ms.close();
		} catch (final IOException ioe) {
			ioe.printStackTrace();
		}
		
	}
	
	protected void sendChunks(final List<byte[]> chunks) {
		
		try {
			final MulticastSocket ms = new MulticastSocket();
			
			ms.joinGroup(group);
			ms.setTimeToLive(TTL);
			
			for (final byte[] data : chunks) {
				
				final DatagramPacket packet = new DatagramPacket(data,
					data.length, group, port);
				
				ms.send(packet);
				
				try {
					Thread.currentThread().sleep(delay);
				} catch (final InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			
			ms.leaveGroup(group);
			ms.close();
		} catch (final IOException ioe) {
			ioe.printStackTrace();
		}
		
	}
	
	private List<byte[]> arrayChunk(final byte[] source,
		final int numChunks, final int type, final int request) {

        final int smallSize = source.length / numChunks;
        final int largeSize = smallSize + 1;
        final int numLarge  = source.length % numChunks;
        final int numSmall  = numChunks - numLarge;

        final List<byte[]> result = new ArrayList<byte[]>(numChunks);
		
		final PacketHeader ph = new PacketHeader();
		
		ph.setType(type);
		ph.setRequest(request);
		ph.setSource(this.source);
		ph.setFinished(false);
		ph.setChunk(true);

        int chunk = 0;
        for (int i = 0; i < numChunks; i++) {
            final int size       = i < numSmall ? smallSize : largeSize;
            final byte[] sublist = new byte[size +
				PacketHeader.getSizeInBytes()];
            
			ph.setSequence(i);
			
            if (i == numChunks - 1) {
				ph.setFinished(true);
			}
			
			System.arraycopy(ph.build(), 0, sublist, 0,
				PacketHeader.getSizeInBytes());
            System.arraycopy(source, chunk, sublist,
				PacketHeader.getSizeInBytes(), size);

            result.add(sublist);
            chunk += size;
        }
        
        return result;
    }
	
}
