
package com.jammed.app;

public class UnknownMessageException extends RuntimeException {
	private static final long serialVersionUID = 0x00;
	
	public UnknownMessageException (final String message) {
		super(message);
	}
	
}
