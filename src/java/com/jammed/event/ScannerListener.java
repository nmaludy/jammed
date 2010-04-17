package com.jammed.event;

import com.jammed.gen.MediaProtos.Playlist;

/**
 *
 * @author nmaludy
 */
public interface ScannerListener {
	public void scanCompleted(Playlist result);
}
