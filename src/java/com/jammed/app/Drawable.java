
package com.jammed.app;

import java.awt.Graphics;
import java.awt.Rectangle;

public interface Drawable {
    public void move();
    public void setScreenSize(final Rectangle bounds);
    public void draw(final Graphics g);
}
