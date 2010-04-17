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
	private final boolean isVideo;

	private StreamReceivedEvent(RTPReceiver source, DataSource dataSource, boolean isVideo) {
		super(source);
		this.dataSource = dataSource;
		this.isVideo = isVideo;
	}

	public static StreamReceivedEvent create(RTPReceiver source, DataSource dataSource, boolean isVideo) {
		return new StreamReceivedEvent(source, dataSource, isVideo);
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public boolean isVideo() {
		return isVideo;
	}

}
