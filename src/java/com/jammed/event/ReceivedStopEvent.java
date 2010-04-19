package com.jammed.event;

import com.jammed.app.RTPReceiver;

/**
 *
 * @author nmaludy
 */
public class ReceivedStopEvent extends StreamEvent {
	private static final long serialVersionUID = 1L;

	private ReceivedStopEvent(RTPReceiver source) {
		super(source);
	}

	public static ReceivedStopEvent create(RTPReceiver source) {
		return new ReceivedStopEvent(source);
	}
}
