
package com.jammed.app;

import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.awt.image.BufferStrategy;

public abstract class FullscreenCanvas extends Frame
    implements MouseListener, Runnable {

    public static final DisplayMode[] DISPLAY_MODES = new DisplayMode[] {
        new DisplayMode(640, 480, 32, 0),
        new DisplayMode(640, 480, 16, 0),
        new DisplayMode(640, 480, 8,  0)
    };

    private static final int numBuffers = 4;
	private static final int fps = 75;

    private final GraphicsDevice device;
    private volatile boolean done; // This will be modified by multiple threads

    public FullscreenCanvas() {
        this(GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice());
    }

    protected FullscreenCanvas (final GraphicsDevice device) {
        super(device.getDefaultConfiguration());
        this.device = device;
        this.done = false;
        
        addMouseListener(this);
        (new Thread(this)).start();
    }

    public void run () {

        Graphics g = null;
        
        try {
            this.setUndecorated(true);
            this.setIgnoreRepaint(true);
            device.setFullScreenWindow(this);

            if (device.isDisplayChangeSupported()) {
                chooseBestDisplayMode(device);
            }

            this.createBufferStrategy(numBuffers);
            final BufferStrategy strategy = this.getBufferStrategy();
            
            while (!done) {
				long startTime = System.currentTimeMillis();
				
                for (int i = 0; i < numBuffers; i++) {
                    g = strategy.getDrawGraphics();
                    if (!strategy.contentsLost()) {
                        paint(g);
                        strategy.show();
                        g.dispose();
                    }
                }
                try {
					startTime += fps;
                    Thread.sleep(Math.max(15, startTime - System.currentTimeMillis()));
                } catch (final InterruptedException ie) { }
            }   
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            device.setFullScreenWindow(null);
            if (g != null) g.dispose();
            this.dispose();
        }
    }

    public synchronized void exit() {
        done = true;
    }

    @Override
    public abstract void paint       (final Graphics g);
    public abstract void mouseClicked(final MouseEvent me);

    public void mousePressed (final MouseEvent me) { }
    public void mouseReleased(final MouseEvent me) { }
    public void mouseEntered (final MouseEvent me) { }
    public void mouseExited  (final MouseEvent me) { }

    protected DisplayMode getBestDisplayMode (final GraphicsDevice device) {
        for (int x = 0; x < DISPLAY_MODES.length; x++) {
            final DisplayMode[] modes = device.getDisplayModes();
            for (int i = 0; i < modes.length; i++) {
                if (modes[i].getWidth()    == DISPLAY_MODES[x].getWidth()   &&
                    modes[i].getHeight()   == DISPLAY_MODES[x].getHeight()  &&
                    modes[i].getBitDepth() == DISPLAY_MODES[x].getBitDepth())
                {
                    return DISPLAY_MODES[x];
                }
            }
        }

        return null;
    }

    protected void chooseBestDisplayMode (final GraphicsDevice device) {
        final DisplayMode best = getBestDisplayMode(device);
        if (best != null) {
            device.setDisplayMode(best);
        }
    }
}
