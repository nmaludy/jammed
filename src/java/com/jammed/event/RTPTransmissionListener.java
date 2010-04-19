package com.jammed.event;

import java.util.EventListener;

/**
 *
 * @author nmaludy
 */
public interface RTPTransmissionListener extends EventListener {
	public void transmissionStreamUpdate(StreamEvent event);
}
