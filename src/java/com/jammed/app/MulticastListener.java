
package com.jammed.app;

import com.jammed.app.Protos.Media;
import com.jammed.app.Protos.Playlist;
import com.jammed.app.ProtocolMessage.Message;

import static com.jammed.app.MulticastSender.TTL;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.HashMap;
import java.util.TreeSet;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.MulticastSocket;
import java.net.DatagramPacket;

import java.nio.ByteBuffer;

public class MulticastListener implements Runnable {
	
	private final InetAddress group;
	private final int port;
	
	private static final int poolSize = 5;
	
	private final ExecutorService pool;
	private final List<Future<Packet>> results;
	private final List<PacketHandler> handlers;
	private final Map<Integer, SortedSet<Packet>> completed;
	
	public MulticastListener (final String addressName, final int port) {
		try {
			this.group = InetAddress.getByName(addressName);
			this.port  = port;
		} catch (final UnknownHostException uhe) {
			throw new RuntimeException();
		}
		
		final ThreadPoolExecutor tpe = new ThreadPoolExecutor(
                poolSize, poolSize, 50000L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
		
        tpe.prestartAllCoreThreads();
		
		pool      = tpe;
		results   = Collections.synchronizedList(new ArrayList<Future<Packet>>());
		completed = new HashMap<Integer, SortedSet<Packet>>();
		handlers  = new ArrayList<PacketHandler>();
		
		(new Thread(this)).start();
	}
	
	public void addMessageHandler (final PacketHandler handler) {
		this.handlers.add(handler);
	}
	
	private class ListenerThread extends Thread {
		
		@Override
		public void run() {
			MulticastSocket ms = null;
			
			try {
				ms = new MulticastSocket(port);
				ms.joinGroup(group);
				ms.setTimeToLive(TTL);
				
				final byte[] buffer = new byte[MulticastSender.LIMIT];
				
				while (true) {
					final DatagramPacket dp = new DatagramPacket(buffer,
						buffer.length);
					
					ms.receive(dp);
					
					final Callable<Packet> callable   = new PacketExecutor(dp);
					final Future<Packet> futurePacket = pool.submit(callable);
					
					results.add(futurePacket);
				}
			} catch (final IOException ioe) {
				ioe.printStackTrace();
			} finally {
				if (ms != null) {
					try {
						ms.leaveGroup(group);
						ms.close();
					} catch (final IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}
		}
	}
	
	public void listen() {
		(new ListenerThread()).start();
		(new ListenerThread()).start();
	}
	
	public void run() {
		while (true) {
			
			final int request = isResultsComplete();
			
			if (request > 0) {
				handlePackets(completed.remove(request));
			}
		}
	}
	
	protected int isResultsComplete() {
		
		if (results.size() == 0) {
			return -1;
		}
		
		try {
			synchronized (results) {
				for (final Iterator<Future<Packet>> iter = results.iterator();
						iter.hasNext();) {
				
					final Future<Packet> futurePacket = iter.next();
					
					if (futurePacket.isDone()) {
						final Packet packet = futurePacket.get();
						final int request   =
							packet.getPacketHeader().getRequest();
						
						SortedSet<Packet> packets = completed.remove(request);
						
						if (packets == null) {
							packets = new TreeSet<Packet>();
						}
						
						synchronized (packets) {
							if (!packets.contains(packet)) {
								packets.add(packet);
							}
						}
						
						completed.put(request, packets);
						iter.remove();
						
						if (packet.isFinished()) {
							return request;
						}
					}
				}
			}
		} catch (final Exception e) {
			return -1;
		}
		
		return -1;
	}
	
	protected void handlePackets (final SortedSet<Packet> set) {
		final Packet[] packets = set.toArray(new Packet[set.size()]);
		
		if (packets.length == 1) {
			handleMessage(packets[0].getMessage());
			return;
		}
		
		final int finalSequence =
			packets[packets.length - 1].getPacketHeader().getSequence();
		
		if (finalSequence != packets.length - 1) {
			System.err.println("Received incomplete request: " +
				packets[0].getPacketHeader().getRequest());
			
			// We cannot handle this request so abort.
			return;
		}
		
		final int type = packets[0].getPacketHeader().getType();
		int size       = 0;
		int offset     = 0;
		byte[] data;
		
		for (final Packet packet : packets) {
			size += packet.getData().length;
		}
		
		data = new byte[size];
		
		for (final Packet packet : packets) {
			System.arraycopy(packet.getData(), 0, data,
				offset, packet.getData().length);
			
			offset += packet.getData().length;
		}
		
		handleMessage(Packet.getMessage(data, type));
	}
	
	protected void handleMessage (final MessageLite message) {
		for (final PacketHandler handler : handlers) {
			if (handler.isMessageSupported(message)) {
				handler.handleMessage(message);
			}
		}
	}
	
}
