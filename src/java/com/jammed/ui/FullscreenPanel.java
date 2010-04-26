
package com.jammed.ui;

import com.jammed.app.MessageBox;
import com.jammed.app.FullscreenWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class FullscreenPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private final JButton startButton;
	private final MessageBox message;
	
	private FullscreenPanel () {
		startButton = new JButton("Enter Fullscreen");
		startButton.addActionListener(this);
		
		message = new MessageBox();
		
		add(startButton);
		
	}
	
	public static FullscreenPanel create() {
		return new FullscreenPanel();
	}
	
	@Override
	public void actionPerformed(final ActionEvent ae) {
		final Object source = ae.getSource();
		
		if (source == startButton) {
			enterFullscreen();
		}
	}
	
	protected void enterFullscreen() {
		final FullscreenWindow fw = new FullscreenWindow();
		message.clear();
		message.resetPosition();
		fw.addDrawable(message);
	}
	
}
