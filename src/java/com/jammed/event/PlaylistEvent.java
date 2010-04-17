package com.jammed.event;

import com.jammed.gen.MediaProtos.Playlist;
import java.util.EventObject;

/**
 *
 * @author nmaludy
 */
public class PlaylistEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	public static enum Type {ADD, DELETE, REPLACE};
	private final Type type;
	private final int start;
	private final int end;

	private PlaylistEvent(Playlist source, Type type, int startIndex, int endIndex) {
		super(source);
		this.type = type;
		start = startIndex;
		end = endIndex;
	}

	public static PlaylistEvent create(Playlist source, Type type, int startIndex, int endIndex) {
		return new PlaylistEvent(source, type, startIndex, endIndex);
	}

	public Type getType() {
		return type;
	}

	public int getStartIndex() {
		return start;
	}

	public int getEndIndex() {
		return end;
	}

}
