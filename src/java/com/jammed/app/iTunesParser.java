
package com.jammed.app;

import com.jammed.gen.MediaProtos.Media;

import java.io.File;
import java.io.IOException;

import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import static com.jammed.app.iTunesHandler.FINISHED;

public class iTunesParser {

    private final SAXParser parser;
    private final iTunesHandler handler;
    private Map<Integer, Media.Builder> mediaList;

    public iTunesParser() {
        this.handler = new iTunesHandler();
		
        final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
        
        try {
            this.parser = factory.newSAXParser();
        } catch (final ParserConfigurationException pce) {
            throw new RuntimeException("Failed to create SAX Parser");
        } catch (final SAXException saxe) {
            throw new RuntimeException("Failed to create SAX Parser");
        }
		
    }

    public void parse (final File file) {
        try {
            parser.parse(file, handler);
        } catch (final SAXException saxe) {
            if (saxe.getMessage().equals(FINISHED)) {
                // No error occured
            }
        } catch (final IOException ioe) {
            
        }

        mediaList = handler.getMediaList();
    }

    public Map<Integer, Media.Builder> getMediaList() {
        return mediaList;
    }

}

