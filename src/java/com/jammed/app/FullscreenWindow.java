
package com.jammed.app;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FullscreenWindow extends FullscreenCanvas {

    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
	private static final long serialVersionUID   = 0x454D53;
	
    private static final String[] songInfo = new String[] {
        "Title:  Hello World",
        "Artist: David Sunshine",
        "Album:  Back in Town"
    };

    private String[] timeInfo = new String[] { "" };

    private MessageBox songBox = new MessageBox(songInfo);
    private MessageBox timeBox = new MessageBox(timeInfo);

    private final List<Drawable> drawable = new ArrayList<Drawable>();

    public FullscreenWindow() {
        super();

        drawable.add(songBox);
        drawable.add(timeBox);

        timeBox.setBackground(Color.BLACK);
    }
    
    @Override
    public void paint(final Graphics g) {
        final Rectangle bounds = this.getBounds();

        // Fill the backround
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, bounds.width, bounds.height);

        // Message area
        for (final Drawable d : drawable) {
            d.setScreenSize(bounds);
            d.draw(g);
            d.move();
        }

        // Update the time box
        final Calendar now = Calendar.getInstance();
        final String time  = format.format(now.getTime());
        timeInfo[0] = time;
        timeBox.setMessage(timeInfo);

        // Top layer
        g.setColor(Color.WHITE);
        g.drawString("Click anywhere to exit", 5, bounds.height - 15);
    }

    @Override
    public void mouseClicked(final MouseEvent me) {
        super.exit();
    }
    
}
