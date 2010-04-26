
package com.jammed.app;

import com.jammed.handlers.SearchHandler;
import com.jammed.handlers.PlayResponseHandler;
import com.jammed.handlers.PlayRequestHandler;
import com.jammed.handlers.PlaylistHandler;

import java.io.File;

public class AndroidServer {
	
	private static final Cloud     cloud   = Cloud.getInstance();
	private static final Librarian library = Librarian.getInstance();
	
	public static void main (final String[] args) {
		final File root = new File("/home/david/jmusic");
		
		library.setLibraryRoot(root);
		registerHandlers();
	}
	
	protected static void registerHandlers() {
		cloud.addMessageHandler(new PlayResponseHandler());
		cloud.addMessageHandler(new SearchHandler());
		cloud.addMessageHandler(new PlayRequestHandler());
		cloud.addMessageHandler(new PlaylistHandler());
	}
}
