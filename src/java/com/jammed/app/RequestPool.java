
package com.jammed.app;

import com.jammed.gen.ProtoBuffer.Request;

import java.util.Collections;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

public class RequestPool {
	
	private static final long   TIME_OUT = 15 * 60 * 1000L; // 15 Minute Timeout
	private static final String hostName = Cloud.getInstance().getHostName();
	private static final Random r        = new Random();
	
	private final SortedMap<Integer, Long> requests;
	
	private RequestPool() {
		this.requests = Collections.synchronizedSortedMap(new TreeMap<Integer, Long>());
	}
	
	static class RequestPoolHolder {
		static RequestPool instance = new RequestPool();
	}
	
	public static RequestPool getInstance() {
		return RequestPoolHolder.instance;
	}
	
	public Request lease() {
		final Request.Builder builder = Request.newBuilder();
		final Long[]          id      = generateRequestID();
		
		builder.setId(id[0].intValue());
		builder.setOrigin(hostName);
		builder.setRelease(false);
		
		requests.put(id[0].intValue(), id[1]);
		
		return builder.build();
	}
	
	public void release (final Request request) {
		if (!request.getRelease()) {
			throw new IllegalArgumentException("Request is not marked for removal");
		}
		
		requests.remove(request.getId());
	}
	
	public Long[] generateRequestID() {
		final int request = r.nextInt(1024);
		
		if (requests.containsKey(request)) {
			return generateRequestID();
		}
		
		final long now = System.currentTimeMillis();
		
		return new Long[] {(long)request, now};
	}
}
