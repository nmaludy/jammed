
package com.jammed.app;

import com.jammed.gen.MediaProtos.Playlist;
import com.jammed.gen.MediaProtos.Media;
import com.jammed.gen.ProtoBuffer.Message.Type;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.EventListenerList;

public class Librarian implements ScannerListener {
	private File libraryRoot = null;
	private final File libraryFile;
	private Playlist library;
	private final EventListenerList libraryListener;
	private final List<Playlist> localPlaylists;
	private final List<EventListenerList> localPlaylistListeners;
	private final String host;
	
	static class LibrarianHolder {
		static Librarian instance = new Librarian();
	}
	
	public static Librarian getInstance() {
		return LibrarianHolder.instance;
	}
	
	private Librarian() {
		libraryFile = new File("./library");
		if(libraryFile.exists()) {
			library = readLibrary();
		} else {
			library = createEmptyPlaylist();
		}
		libraryListener = new EventListenerList();
		localPlaylists = new ArrayList<Playlist>();
		localPlaylistListeners = new ArrayList<EventListenerList>();
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
			localPlaylistListeners.add(new EventListenerList());
		}
	}

	public Playlist createEmptyPlaylist() {
		Playlist.Builder builder = Playlist.newBuilder()
														.setHost(host)
														.setType(Type.PLAYLIST);
		return builder.build();
	}

	public int addEmptyPlaylist() {
		localPlaylists.add(createEmptyPlaylist());
		localPlaylistListeners.add(new EventListenerList());
		return localPlaylists.size() - 1;
	}

	private Playlist readLibrary() {
		Playlist.Builder lib = Playlist.newBuilder();

		// Read the existing address book.
		try {
			lib.mergeFrom(new FileInputStream(libraryFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lib.build();
	}

	private void saveLibrary() {
		try {
			FileOutputStream output = new FileOutputStream(libraryFile);
			library.writeTo(output);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Playlist getLibrary() {
		return library;
	}

	public void refreshLibrary() {
		FileSystemScanner.start(libraryRoot, this);
	}

	public void setLibraryRoot(File root) {
		libraryRoot = root;
		refreshLibrary();
	}

	public Playlist getPlaylist(int index) {
		return localPlaylists.get(index);
	}

	public void scanCompleted(Playlist result) {
		library = result;
		PlaylistEvent e = PlaylistEvent.create(result, PlaylistEvent.Type.REPLACE, 0, 0);
		firePlaylistEvent(libraryListener, e);

		saveLibrary();
	}

	public void addLibraryListener(PlaylistListener l) {
		libraryListener.add(PlaylistListener.class, l);
	}

	public void removeLibraryListener(PlaylistListener l) {
		libraryListener.remove(PlaylistListener.class, l);
	}

	public void addPlaylistListener(PlaylistListener l, int playlistIndex) {
		localPlaylistListeners.get(playlistIndex).add(PlaylistListener.class, l);
	}

	public void removePlaylistListener(PlaylistListener l, int playlistIndex) {
		localPlaylistListeners.get(playlistIndex).remove(PlaylistListener.class, l);
	}

	protected void firePlaylistEvent(EventListenerList list, PlaylistEvent event) {
		Object[] listeners = list.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == PlaylistListener.class) {
				((PlaylistListener) listeners[i + 1]).playlistChanged(event);
			}
		}
	}

	public void addMediaToPlaylist(Media m, int playlistIndex) {
		List<Media> list = new ArrayList<Media>();
		list.add(m);
		addMediaToPlaylist(list, playlistIndex);
	}

	public void addMediaToPlaylist(List<Media> m, int playlistIndex) {
		Playlist list = localPlaylists.get(playlistIndex);
		int startIndex = list.getMediaCount();

		Playlist.Builder builder = Playlist.newBuilder(list);
		list = builder.addAllMedia(m).build();
		localPlaylists.set(playlistIndex, list);

		int endIndex = list.getMediaCount() - 1;
		PlaylistEvent e = PlaylistEvent.create(list, PlaylistEvent.Type.ADD, startIndex,  endIndex);
		firePlaylistEvent(localPlaylistListeners.get(playlistIndex), e);
	}
}
