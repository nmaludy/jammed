package com.jammed.event;

import java.util.EventListener;

/**
 *
 * @author nmaludy
 */
public interface StreamListener extends EventListener  {
	public void streamUpdate(StreamEvent event);
}
