package com.jammed.app;

import java.util.EventObject;
import javax.media.protocol.DataSource;

/**
 *
 * @author nmaludy
 */
public class StreamReceivedEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	public static enum Type {ADD, DELETE, REPLACE};
	private final DataSource dataSource;

	private StreamReceivedEvent(RTPReceiver source, DataSource dataSource) {
		super(source);
		this.dataSource = dataSource;
	}

	public static StreamReceivedEvent create(RTPReceiver source, DataSource dataSource) {
		return new StreamReceivedEvent(source, dataSource);
	}

	public DataSource getDataSource() {
		return dataSource;
	}

}
