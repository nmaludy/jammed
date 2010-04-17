package com.jammed.event;

import com.jammed.event.StreamReceivedEvent;
import java.util.EventListener;

/**
 *
 * @author nmaludy
 */
public interface RTPReceiverListener extends EventListener {
	public void streamReceived(StreamReceivedEvent event);
}
