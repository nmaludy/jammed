
package com.jammed.app;

import com.jammed.gen.Protos.Playlist;
import com.jammed.gen.Protos.Media;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

public class Librarian {
	private final List<Playlist> localPlaylists;
	
	private final String host;
	
	static class LibrarianHolder {
		static Librarian instance = new Librarian();
	}
	
	public static Librarian getInstance() {
		return LibrarianHolder.instance;
	}
	
	private Librarian() {
		localPlaylists = new ArrayList<Playlist>();
		host = Checksum.fletcher16(Cloud.getInstance().getAddress()) + "";
	}
	
	public Playlist search (final String query) {
		final Playlist.Builder builder = Playlist.newBuilder();
		builder.setType(builder.getType());
		builder.setHost(host);
		
		for (final Playlist playlist : localPlaylists) {
			for (final Media media : playlist.getMediaList()) {
				if (media.getTitle().contains(query)) {
					builder.addMedia(media);
				} else if (media.hasAlbum()) {
					if (media.getAlbum().contains(query)) {
						builder.addMedia(media);
					}
				} else if (media.hasArtist()) {
					if (media.getArtist().contains(query)) {
						builder.addMedia(media);
					}
				} else if (media.hasName()) {
					if (media.getName().contains(query)) {
						builder.addMedia(media);
					}
				}
			}
		}
		
		return builder.build();
	}
	
	public void open (final File file) {
		final String name = file.getName().toLowerCase();
		AbstractPlaylist builder = null;
		
		if (name.endsWith(".pls")) {
			builder = new PLS(host);
		} else if (name.endsWith(".xml")) {
			builder = new iTunes(host);
		} else {
			throw new IllegalArgumentException("Unknown file type: " + name);
		}
		
		builder.open(file);
		
		final Playlist playlist = builder.getPlaylist();
		
		if (playlist.getMediaList().size() > 0) {
			localPlaylists.add(playlist);
		}
	}
	
}
