
package com.jammed.app;

import com.jammed.gen.Protos.Playlist;
import com.jammed.gen.Protos.Search;

import com.google.protobuf.MessageLite;

public class SearchHandler extends PacketHandler<Search> {
	
	public SearchHandler() {
	}
	
	public boolean isMessageSupported (final MessageLite message) {
		return (message instanceof Search);
	}
	
	public int type (final MessageLite message) {
		if (!(message instanceof Search)) {
			throw new IllegalArgumentException();
		}
		
		final Search search = (Search)message;
		
		return search.getType().ordinal();
	}
	
	public boolean handleMessage (final MessageLite message) {
		if (!(message instanceof Search)) {
			throw new IllegalArgumentException();
		}
		
		final Search search = (Search)message;
		
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
