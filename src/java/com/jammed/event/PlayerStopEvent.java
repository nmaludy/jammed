package com.jammed.event;

import com.jammed.app.MediaController;
import java.util.EventObject;

/**
 *
 * @author nmaludy
 */
public class PlayerStopEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	public PlayerStopEvent(MediaController source){
		super(source);
	}
}
