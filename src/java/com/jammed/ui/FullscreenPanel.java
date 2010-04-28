
package com.jammed.ui;

import com.jammed.app.Cloud;
import com.jammed.app.DirectedPlaylist;
import com.jammed.app.MessageBox;
import com.jammed.app.FullscreenWindow;
import com.jammed.app.RTPSessionManager;
import com.jammed.handlers.DirectiveHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class FullscreenPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private final JButton startButton;
	private final MessageBox message;
	private final DirectiveHandler directive;
	private final DirectedPlaylist playlist;
	
	private Runnable exitAction = new Runnable() {
		public void run() {
			// Stop listening for directives
			Cloud.getInstance().removeMessageHandler(directive);
		}
	};
	
	private FullscreenPanel () {
		startButton = new JButton("Enter Fullscreen");
		startButton.addActionListener(this);
		
		message   = new MessageBox();
		playlist  = new DirectedPlaylist(message);
		directive = new DirectiveHandler(playlist);
		RTPSessionManager.getInstance().addReceiverListener(playlist);
		
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
		// Start listening for directives
		Cloud.getInstance().addMessageHandler(directive);
		
		// Open the fullscreen window
		final FullscreenWindow fw = new FullscreenWindow();
		fw.setExitAction(exitAction);
		message.clear();
		message.resetPosition();
		fw.addDrawable(message);
	}
	
}
