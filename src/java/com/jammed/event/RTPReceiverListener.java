package com.jammed.event;

import java.util.EventListener;

/**
 *
 * @author nmaludy
 */
public interface RTPReceiverListener extends EventListener {
	public void streamReceived(StreamReceivedEvent event);
}
