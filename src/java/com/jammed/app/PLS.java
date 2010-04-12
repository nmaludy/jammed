
package com.jammed.app;

import com.jammed.gen.MediaProtos.Media;
import com.jammed.gen.MediaProtos.Playlist;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PLS extends AbstractPlaylist {
    
    private final static Pattern entryPattern =
		Pattern.compile("([a-zA-Z]+)(\\d+)=(.*)");
	
	private final static String CURRENT_ENTRY = "(.*)([num])=(.*)";
	private final static String HEADER        = "[playlist]";
	private final static String FILE          = "File";
	private final static String TITLE         = "Title";
	private final static String LENGTH        = "Length";
	
	protected Playlist.Builder playlist;
	
	public PLS (final String host) {
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
		final Scanner scanner;

        try {
            scanner = new Scanner(file);
        } catch (final FileNotFoundException fnfe) {
            return false;
        }

        while (scanner.hasNextLine()) {
            if (scanner.nextLine().equalsIgnoreCase(HEADER)) {
                break;
            }
        }
        
        String peek;
        while (scanner.hasNext(entryPattern)) {
            peek = scanner.nextLine();
            final Matcher entryMatcher = entryPattern.matcher(peek);
			
            if (entryMatcher.matches()) {
				final Media.Builder media = Media.newBuilder();
                final Pattern currentEntryPattern 
					= Pattern.compile(CURRENT_ENTRY.replace("[num]",
						entryMatcher.group(2)));
					
				boolean add = true;
				
				add = add && setValue(peek, media);
                while (scanner.hasNext(currentEntryPattern)) {
					add = add && setValue(scanner.nextLine(), media);
                }
				
				if (add) {
					media.setType(media.getType());
					playlist.addMedia(media);
				}
            }
        }

        return true;
	}
	
	protected boolean setValue (final String line, final Media.Builder media) {
		final String[] parts = line.split("(\\d+)=");
		
		if (parts.length != 2) {
			return false;
		}
		
		final String name  = parts[0];
		final String value = parts[1];
		
		if (name.equalsIgnoreCase(FILE)) {
			media.setLocation(value);
		} else if (name.equalsIgnoreCase(TITLE)) {
			media.setTitle(value);
		} else if (name.equalsIgnoreCase(LENGTH)) {
			media.setLength(Integer.valueOf(value));
		} else {
			return false;
		}
		
		return true;
	}
	
}
