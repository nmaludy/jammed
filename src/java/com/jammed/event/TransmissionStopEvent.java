package com.jammed.event;

import com.jammed.app.RTPTransmitter;

/**
 *
 * @author nmaludy
 */
public class TransmissionStopEvent extends StreamEvent {
	private static final long serialVersionUID = 1L;

	private TransmissionStopEvent(RTPTransmitter source) {
		super(source);
	}

	public static TransmissionStopEvent create(RTPTransmitter source) {
		return new TransmissionStopEvent(source);
	}
}