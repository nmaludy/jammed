
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

    private final List<Drawable> drawable = new ArrayList<Drawable>();
	
	private Runnable exitAction;

    public FullscreenWindow() {
        super();
    }
	
	public void addDrawable (final Drawable d) {
		this.drawable.add(d);
	}
	
	public void setExitAction (final Runnable exitAction) {
		this.exitAction = exitAction;
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

        // Top layer
		final String hostname = Cloud.getInstance().getHostName();
        g.setColor(Color.WHITE);
        g.drawString("Click anywhere to exit", 5, bounds.height - 15);
		g.drawString("Host: " + hostname, 5, 15);
    }

    @Override
    public void mouseClicked(final MouseEvent me) {
        super.exit();
		exitAction.run();
    }
    
}
