
package com.jammed.app;

import com.jammed.app.Protos.Playlist;
import com.jammed.app.Protos.Search;

import com.google.protobuf.MessageLite;

public class SearchHandler implements PacketHandler<Search> {
	
	public SearchHandler() {
	}
	
	public boolean isMessageSupported (final MessageLite message) {
		return (message instanceof Search);
	}
	
	public int type (final Search search) {
		return search.getType().ordinal();
	}
	
	public boolean handleMessage (final Search search) {
		final int request  = search.getRequest();
		final String query = search.getQuery();
		
		System.out.println("Received search for: " + query);
		
		final Playlist results = Librarian.getInstance().search(query);
		
		if (results.getMediaList().size() > 0) {
			Cloud.getInstance().send(results, request);
		}
		
		return true;
	}
	
}
