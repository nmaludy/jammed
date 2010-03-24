
package com.jammed.app;

import com.jammed.app.Protos.Playlist;

import java.io.File;

public abstract class AbstractPlaylist {
	public abstract Playlist getPlaylist();
    public abstract int size();
    public abstract boolean open(final File file);
}

