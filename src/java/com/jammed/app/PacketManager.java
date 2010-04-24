
package com.jammed.app;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class PacketManager {
	
	private static final long PERIOD = 5 * 1000 * 60;

	private final Timer timer = new Timer();
	private final Map<Integer, Long> packets = Collections.synchronizedMap(new HashMap<Integer, Long>());
	
	static class PacketManagerHolder {
		static PacketManager instance = new PacketManager();
	}
	
	private PacketManager() {
		final TimerTask task = new PacketWatcher();
		timer.scheduleAtFixedRate(task, 0L, 500L);
	}
	
	public boolean receive (final PacketHeader header) {
		
		final int  checksum = header.getChecksum();
		final long now      = System.currentTimeMillis();
		
		if (packets.containsKey(checksum)) {
			return false;
		}
		
		packets.put(checksum, now);
		return true;
		
	}
	
	public static PacketManager getInstance() {
		return PacketManagerHolder.instance;
	}
	
	private class PacketWatcher extends TimerTask {
		
		public void run() {
			final long now = System.currentTimeMillis();
			
			final Set<Map.Entry<Integer, Long>> entries = packets.entrySet();
			final Iterator<Map.Entry<Integer, Long>> iter = entries.iterator();
			
			while (iter.hasNext()) {
				final Map.Entry<Integer, Long> entry = iter.next();
				final long duration = now - entry.getValue();
				
				if (duration > PERIOD) {
					System.out.println("Removing: " + Integer.toHexString(entry.getKey()));
					iter.remove();
				}

			}
		}
		
	}
}
