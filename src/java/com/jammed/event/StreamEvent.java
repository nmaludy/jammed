package com.jammed.event;

import java.util.EventObject;

/**
 *
 * @author nmaludy
 */
public class StreamEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	public StreamEvent(Object source){
		super(source);
	}
}
