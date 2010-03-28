
package com.jammed.app;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Random;

public class MessageBox implements Drawable {

    private static final Insets insets = new Insets(10, 10, 10, 10);

    private Rectangle screenSize = new Rectangle(0, 0);
    private final Random random  = new Random();

    private Color foreground, background;

    private double x, y, deltaX, deltaY;
    private int width, height;
    private String[] message;

    public MessageBox (final String[] message) {
        this.deltaX = random.nextFloat() + 0.1f;
        this.deltaY = random.nextFloat() + 0.1f;

        this.background = new Color(0x00, 0x00, 0xFF, 125);
        this.foreground = Color.WHITE;

        this.x = random.nextInt(FullscreenCanvas.DISPLAY_MODES[0].getWidth());
        this.y = random.nextInt(FullscreenCanvas.DISPLAY_MODES[0].getHeight());
        
        if (random.nextBoolean()) { this.deltaX *= -1; }
        if (random.nextBoolean()) { this.deltaY *= -1; }

        this.message = message;
    }

    public void setMessage (final String[] message) {
        this.message = message;
    }

    public void setForeground (final Color c) {
        this.foreground = c;
    }

    public void setBackground (final Color c) {
        this.background = c;
    }

    public void move() {
        x += deltaX;
        y += deltaY;
        
        if (x > screenSize.width -
           (width + insets.right + insets.left))  { deltaX *= -1; }
        if (x < 0)                                { deltaX *= -1; }
        if (y > screenSize.height - 
           (height + insets.top + insets.bottom)) { deltaY *= -1; }
        if (y < 0)                                { deltaY *= -1; }
    }

    public void setScreenSize(final Rectangle bounds) {
        this.screenSize = bounds;
    }

    public void draw(final Graphics g) {
        this.width  = this.calculateWidth(g);
        this.height = this.calculateHeight(g);
        
        g.setColor(background);
        g.fillRoundRect(
                (int)x,
                (int)y,
                width  + insets.left + insets.right,
                height + insets.top  + insets.bottom,
                25, 25);

        g.setColor(foreground);
        final int lineHeight = height / (message.length);
        for (int i = 1; i <= message.length; i++) {
            g.drawString(message[i-1],
                    (int)(x + insets.left),
                    (int)(y + insets.bottom + lineHeight * i));
        }
    }

    protected int calculateWidth (final Graphics g) {
        String longestLine = message[0];
        
        for (int i = 1; i < message.length; i++) {
            if (message[i].length() > longestLine.length()) {
                longestLine = message[i];
            }
        }

        final FontMetrics metrics = g.getFontMetrics();
        final Rectangle2D bounds  = metrics.getStringBounds(longestLine, g);

        return (int)bounds.getWidth();
    }

    protected int calculateHeight (final Graphics g) {
        final FontMetrics metrics = g.getFontMetrics();
        final Rectangle2D bounds  = metrics.getStringBounds(message[0], g);

        return (int)bounds.getHeight() * (message.length);
    }

}
