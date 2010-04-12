
package com.jammed.app;

import com.jammed.gen.MediaProtos.Media;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

public class iTunesHandler extends DefaultHandler2 {

    private static final String KEY, LOCATION, STRING, INT, NAME, ARTIST,
            ALBUM, ID, PLAYLIST;
			
    public static final String FINISHED;
	
    private String currentKey;
    private int currentID;
    private Map<Integer, Media.Builder> mediaList;
	private StringBuilder element;

    static {
        KEY      = "key";
        LOCATION = "Location";
        STRING   = "string";
        INT      = "integer";
        NAME     = "Name";
        ARTIST   = "Artist";
        ALBUM    = "Album";
        ID       = "Track ID";
        PLAYLIST = "Playlists";
        FINISHED = "Done";
    }

    public iTunesHandler () {
        this.mediaList  = new HashMap<Integer, Media.Builder>();
        this.element    = new StringBuilder();
        this.currentKey = new String();
        this.currentID  = -1;
    }

    @Override
    public void characters(final char[] chars, final int start,
            final int length) throws SAXException {
        element.append(chars, start, length);
    }

    @Override
    public void endDocument() throws SAXException {

    }

    @Override
    public void endElement(final String uri, final String localName,
            final String qName) throws SAXException {

        if (currentKey.equals(ID) && qName.equals(INT)) {
            currentID = Integer.valueOf(element.toString().trim());
			
            if (!mediaList.containsKey(currentID)) {
				final Media.Builder media = Media.newBuilder();
				media.setType(media.getType());
				
                mediaList.put(currentID, media);
            }
        }
		
		if (qName.equals(STRING)) {
			final Media.Builder media = mediaList.remove(currentID);
			final String value        = element.toString().trim();
			
			if (currentKey.equals(LOCATION) && !media.hasLocation()) {
				media.setLocation(value);
			} else if (currentKey.equals(NAME) && !media.hasName()) {
				media.setName(value);
			} else if (currentKey.equals(ARTIST) && !media.hasArtist()) {
				media.setArtist(value);
			} else if (currentKey.equals(ALBUM) && !media.hasAlbum()) {
				media.setAlbum(value);
			}
			
			mediaList.put(currentID, media);
		}
        
        if (qName.equals(KEY)) {
            currentKey = element.toString().trim();
            
            if (currentKey.equals(PLAYLIST)) {
                throw new SAXException(FINISHED);
            }
        }
    }

    @Override
    public void startDocument() throws SAXException {
        
    }

    @Override
    public void startElement(final String uri, final String localName,
            final String qName, final Attributes attributes)
		throws SAXException {
			
        element.setLength(0);
    }

    public Map<Integer, Media.Builder> getMediaList() {
        return mediaList;
    }
}

