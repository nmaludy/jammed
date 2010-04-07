
package com.jammed.app;

import com.jammed.gen.Protos.Media;
import com.jammed.gen.Protos.Playlist;

import java.io.File;

import java.util.Map;

public class iTunes extends AbstractPlaylist {
	
	protected Playlist.Builder playlist;
	
	public iTunes (final String host) {
		this.playlist = Playlist.newBuilder();
		this.playlist.setType(playlist.getType());
		this.playlist.setHost(host);
	}
	
	@Override
	public Playlist getPlaylist() {
		return playlist.build();
	}
	
	@Override
	public int size() {
		return playlist.getMediaList().size();
	}
	
	@Override
	public boolean open(final File file) {
		final iTunesParser parser = new iTunesParser();
		parser.parse(file);
		
		final Map<Integer, Media.Builder> mediaList = parser.getMediaList();
		
        for (final Map.Entry<Integer, Media.Builder> item :
			mediaList.entrySet()) {
		
			final Media.Builder media = item.getValue();
			
			if (media != null) {
				if (!media.hasTitle() && media.hasName()) {
					media.setTitle(media.getName());
				}
				
				playlist.addMedia(media);
			}
		}
		
		return true;
	}
	
}
