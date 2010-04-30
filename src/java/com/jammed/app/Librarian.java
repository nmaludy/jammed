
package com.jammed.app;

import com.jammed.event.PlaylistEvent;
import com.jammed.event.PlaylistListener;
import com.jammed.event.ScannerListener;
import com.google.protobuf.MessageLite;
import com.jammed.gen.MediaProtos.Playlist;
import com.jammed.gen.MediaProtos.Media;
import com.jammed.gen.MessageProtos.Search;
import com.jammed.gen.ProtoBuffer.Message.Type;
import com.jammed.gen.ProtoBuffer.Request;
import com.jammed.handlers.SearchHandler;
import com.jammed.ui.PlaylistPanel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

public class Librarian extends SearchHandler implements ScannerListener {

	private File libraryRoot = null;
	private final File libraryFile;
	private Playlist library;
	private Playlist searchList;
	private final EventListenerList libraryListener;
	private final EventListenerList searchListener;
	private final List<Playlist> localPlaylists;
	private final List<EventListenerList> localPlaylistListeners;
	private static final String host = Checksum.fletcher16(Cloud.getInstance().getAddress()) + "";

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
		searchList = createEmptyPlaylist();
		searchListener = new EventListenerList();
		libraryListener = new EventListenerList();
		localPlaylists = new ArrayList<Playlist>();
		localPlaylistListeners = new ArrayList<EventListenerList>();
		Cloud.getInstance().addMessageHandler(this);
	}

	@Override
	public boolean handleMessage(final MessageLite message) {
		if (!(message instanceof Search)) {
			throw new IllegalArgumentException();
		}

		final Search search = (Search) message;
		Request request = search.getRequest();
		String hostname = request.getOrigin();
		if (hostname.equals(Cloud.getInstance().getHostName())) {
			return false; //a request that originated from this system, ignore it
		}
		
		final String query = search.getQuery();
		System.out.println("Handling search request for " + query + " ID " + request.getId());
		final Playlist results = search(query, request);

		if (results.getMediaList().size() > 0) {
			System.out.println("Search found stuff ");
			Cloud.getInstance().send(results, request.getId());
		}// else {
		//	System.out.println("No search results ");
		//}

		return true;
	}

	public Playlist search(final String query, Request request) {
		final Playlist.Builder builder = Playlist.newBuilder();
		builder.setType(builder.getType());
		builder.setHost(host);
		builder.setRequest(request);

		for (final Playlist playlist : localPlaylists) {
			if (playlist.equals(getPlaylist(PlaylistPanel.getInstance().getCurrentPlaylistIndex()))){
				continue;
			}
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
		for (final Media media : library.getMediaList()) {
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
		return builder.build();
	}

	public Playlist open(final File file) {
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
			return playlist;
		}
		
		System.out.println("Empty playlist");
		
		return null;
	}

	public Playlist createEmptyPlaylist() {
		Playlist.Builder builder = Playlist.newBuilder().setHost(host).setType(Type.PLAYLIST);
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

	public Playlist getSearchPlaylist() {
		return searchList;
	}

	public void setSearchPlaylist(Playlist list) {
		searchList = list;
		PlaylistEvent e = PlaylistEvent.create(searchList, PlaylistEvent.Type.REPLACE, 0, 0);
		firePlaylistEvent(searchListener, e);
	}

	public Playlist getPlaylist(int index) {
		return localPlaylists.get(index);
	}

	public void setPlaylist(int index, Playlist list) {
		localPlaylists.set(index, list);
		PlaylistEvent e = PlaylistEvent.create(localPlaylists.get(index), PlaylistEvent.Type.REPLACE, 0, 0);
		firePlaylistEvent(localPlaylistListeners.get(index), e);
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

	public void addSearchListener(PlaylistListener l) {
		searchListener.add(PlaylistListener.class, l);
	}

	public void removeSearchListener(PlaylistListener l) {
		searchListener.remove(PlaylistListener.class, l);
	}

	public void addPlaylistListener(PlaylistListener l, int playlistIndex) {
		localPlaylistListeners.get(playlistIndex).add(PlaylistListener.class, l);
	}

	public void removePlaylistListener(PlaylistListener l, int playlistIndex) {
		localPlaylistListeners.get(playlistIndex).remove(PlaylistListener.class, l);
	}

	protected void firePlaylistEvent(final EventListenerList list, final PlaylistEvent event) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				Object[] listeners = list.getListenerList();
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == PlaylistListener.class) {
						((PlaylistListener) listeners[i + 1]).playlistChanged(event);
					}
				}
			}
		});
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
		PlaylistEvent e = PlaylistEvent.create(list, PlaylistEvent.Type.ADD, startIndex, endIndex);
		firePlaylistEvent(localPlaylistListeners.get(playlistIndex), e);
	}

	public void addMediaToSearch(List<Media> m) {
		Playlist list = searchList;
		int startIndex = list.getMediaCount();

		Playlist.Builder builder = Playlist.newBuilder(list);
		list = builder.addAllMedia(m).build();
		searchList = list;

		int endIndex = list.getMediaCount() - 1;
		PlaylistEvent e = PlaylistEvent.create(list, PlaylistEvent.Type.ADD, startIndex, endIndex);
		firePlaylistEvent(searchListener, e);
	}
}
