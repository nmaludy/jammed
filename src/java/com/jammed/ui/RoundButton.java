package com.jammed.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 *
 * @author nmaludy
 */
public class RoundButton extends JButton implements MouseListener {

    private static final long serialVersionUID = 1L;
    protected Shape shape;
    boolean mouseIn = false;
    private Color pressedColor = Color.LIGHT_GRAY;
    private Color borderColor = Color.GRAY;
    private Color mouseOverBorder = Color.BLACK.brighter().brighter().brighter().brighter();
    private float borderWidth = 1.8f;

    public RoundButton(Icon icon) {
	super(icon);
	Dimension size = new Dimension(icon.getIconWidth(), icon.getIconHeight());
	setPreferredSize(size);
	addMouseListener(this);
	mouseOverBorder = mouseOverBorder.brighter().brighter();
	setContentAreaFilled(false);
    }

    public float getBorderWidth() {
	return borderWidth;
    }

    public void setBorderWidth(float width) {
	borderWidth = width;
    }

    @Override
    public void paintComponent(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	if (getModel().isPressed()) {
	    g.setColor(pressedColor);
	    g2.fillRect(3, 3, getWidth() - 6, getHeight() - 6);
	}
	super.paintComponent(g);

	if (mouseIn) {
	    g2.setColor(mouseOverBorder);
	} else {
	    g2.setColor(borderColor);
	}

	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	g2.setStroke(new BasicStroke(borderWidth));
	g2.draw(new RoundRectangle2D.Double(1, 1, (getWidth() - 3), (getHeight() - 3), 15, 15));
	g2.dispose();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
	mouseIn = true;
    }

    public void mouseExited(MouseEvent e) {
	mouseIn = false;
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
}
