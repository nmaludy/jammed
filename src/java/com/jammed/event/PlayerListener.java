package com.jammed.event;

import java.util.EventListener;

/**
 *
 * @author nmaludy
 */
public interface PlayerListener extends EventListener  {
	public void playerUpdate(PlayerStopEvent event);
}

