
package com.jammed.app;

import static com.jammed.app.MulticastSender.TTL;

import com.google.protobuf.MessageLite;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.HashMap;
import java.util.TreeSet;

import java.util.concurrent.Callable;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MulticastListener implements Runnable {
	
	private final InetAddress group;
	private final int port;
	
	private static final int poolSize = 10;
	
	private final ExecutorService pool;
	private final BlockingQueue<Future<Packet>> results;
	private final List<PacketHandler<? extends MessageLite>> handlers;
	private final Map<Integer, SortedSet<Packet>> completed;
	private final Lock socketLock = new ReentrantLock();
	
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
		results   = new LinkedBlockingQueue<Future<Packet>>();
		completed = Collections.synchronizedMap(new HashMap<Integer, SortedSet<Packet>>());
		handlers  = new ArrayList<PacketHandler<? extends MessageLite>>();
		
		(new Thread(this)).start();
	}
	
	public void addMessageHandler (final PacketHandler<? extends MessageLite> handler) {
		this.handlers.add(handler);
	}
	
	public void removeMessageHandler (final PacketHandler<? extends MessageLite> handler) {
		this.handlers.remove(handler);
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

					socketLock.lock();
					try {
						ms.receive(dp);
					} finally {
						socketLock.unlock(); //always unlock!
					}
					
					final Callable<Packet> callable   = new PacketExecutor(dp);
					final Future<Packet> futurePacket = pool.submit(callable);
					
					results.put(futurePacket);
				}
			} catch (final IOException ioe) {
				ioe.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
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
			for (final Integer request : completedRequests()) {
				handlePackets(completed.remove(request));
			}
		}
	}
	
	protected boolean isPoolFinished() {
		synchronized (results) {
			for (final Future<Packet> futurePacket : results) {
				if (!futurePacket.isDone()) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	protected List<Integer> completedRequests() {

		final List<Integer> finishedRequests = new ArrayList<Integer>();

//		if (results.size() == 0 || !isPoolFinished()) {
//			return finishedRequests;
//		}

		try {
			final Future<Packet> futurePacket = results.take();
			final Packet packet = futurePacket.get();
			final int request = packet.getPacketHeader().getRequest();

			SortedSet<Packet> packets = completed.remove(request);

			if (packets == null) {
				packets = new TreeSet<Packet>();
			}

			synchronized (packets) {
				if (!packet.isEmpty()) {
					packets.add(packet);
				}
			}

			completed.put(request, packets);

			if (packet.isFinished() && !packet.isEmpty()) {
				finishedRequests.add(request);
			}
			
		} catch (final Exception e) {
		}

		return finishedRequests;
	}
	
	protected synchronized void handlePackets (final SortedSet<Packet> set) {
		if (set == null) return;
		
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
		
		handleMessage(PacketBuilder.getInstance().buildMessage(data, type));
	}
	
	protected void handleMessage (final MessageLite message) {
		for (final PacketHandler<? extends MessageLite> handler : handlers) {
			if (handler.isMessageSupported(message)) {
				handler.handleMessage(message);
			}
		}
	}
	
}
