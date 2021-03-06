
package com.jammed.app;

import com.google.protobuf.MessageLite;

import com.jammed.handlers.DirectiveHandler;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;

import java.util.Enumeration;

public class Cloud implements Runnable {
	
	private static final String DEFAULT_ADDRESS = "ALL-SYSTEMS.MCAST.NET";
	private static final int    DEFAULT_PORT    = 4000;
	
	private static final byte[] host     = getHostAddress();
	private static final String hostName = Checksum.fletcher16(host) + "";
	
	private final PacketBuilder builder  = PacketBuilder.getInstance();
	private final MulticastListener listener;
	private final MulticastSender   sender;
	
	// Allows for thread safe access
	static class CloudHolder {
		static Cloud instance = new Cloud();
	}
	
	public static Cloud getInstance() {
		return CloudHolder.instance;
	}
	
	// Singleton class
	private Cloud () {
		sender   = new MulticastSender(DEFAULT_ADDRESS,   DEFAULT_PORT);
		listener = new MulticastListener(DEFAULT_ADDRESS, DEFAULT_PORT);
		
		sender.setSource(Checksum.fletcher16(host));
		
		addMessageHandler(new DirectiveHandler(null));
		
		(new Thread(this)).start();
	}
	
	public void send (final MessageLite message, final int request) {
		sender.send(message, request);
	}
	
	public void addMessageHandler (final PacketHandler<? extends MessageLite> handler) {
		sender.addMessageHandler(handler);
		listener.addMessageHandler(handler);
		builder.addMessageHandler(handler);
	}
	
	public void removeMessageHandler (final PacketHandler<? extends MessageLite> handler) {
		sender.removeMessageHandler(handler);
		listener.removeMessageHandler(handler);
		builder.removeMessageHandler(handler);
	}

	public void run() {
		listener.listen();
	}
	
	public byte[] getAddress() {
		return host;	
	}
	
	public String getHostName() {
		return hostName;
	}
	
	protected static byte[] getHostAddress() {
		try {
			final Enumeration<NetworkInterface> interfaces =
					NetworkInterface.getNetworkInterfaces();
					
			while (interfaces.hasMoreElements()) {
				for (final InterfaceAddress addresses :
					interfaces.nextElement().getInterfaceAddresses()) {
					
					final byte[] bytes = addresses.getAddress().getAddress();
					
					if (bytes.length == 4 && bytes[0] != 127) {
						return bytes;
					}
				}
			}
		} catch (final Exception e) {
			// Do nothing and we will return 127.0.0.1 (loopback)
		}
		
		return new byte[] {
			127, 0, 0, 1
		};
	}
}
