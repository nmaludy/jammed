package com.jammed.app;

import com.jammed.gen.MediaProtos.Media;
import com.jammed.gen.MediaProtos.Playlist;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;

/**
 *
 * @author nmaludy
 */
public class FileSystemScanner {

	private static FileSystemScanner INSTANCE;
	private static MediaFilter mediaFilter = new MediaFilter();
	private final ExecutorService executor;

	private static FileFilter directores = new FileFilter() {
		public boolean accept(File f) {
			return f.isDirectory();
		}
	};

	private FileSystemScanner() {
		executor = Executors.newSingleThreadExecutor();
	}

	public static final void start(File root, ScannerListener listener) {
		if (INSTANCE == null) {
			INSTANCE = new FileSystemScanner();
		}
		INSTANCE.scan(root, listener);
	}

	private void scan(File root, ScannerListener listener) {
		executor.execute(new Scanner(root, listener));
	}

	private class Scanner implements Runnable {

		private File root;
		private ScannerListener listener;
		private Playlist.Builder result;
		private Queue<File> dirs = new LinkedList<File>();

		public Scanner(File r, ScannerListener l) {
			root = r;
			listener = l;
			result = Playlist.newBuilder();
			result.setType(result.getType());
			result.setHost("127.0.0.1");
		}

		@Override
		public void run() {
			processDir(root);
			while(!dirs.isEmpty()) {
				processDir(dirs.poll());
			}
			fireScanComplete();
		}

		private void processDir(File dir){
			dirs.addAll(Arrays.asList(dir.listFiles(directores)));
			List<File> media = new ArrayList<File>(Arrays.asList(dir.listFiles(mediaFilter)));
			processFiles(media);
		}

		private void processFiles(List<File> files) {
			for (File f: files) {
				Media.Builder media = Media.newBuilder();
				media.setLocation(f.getAbsolutePath());
				media.setTitle(f.getName());
				media.setLength(0);
				media.setType(media.getType());
				result.addMedia(media.build());
			}
		}

		private void fireScanComplete() {
			final Playlist playlist = result.build();
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					listener.scanCompleted(playlist);
				}
			});
		}
	}
}
