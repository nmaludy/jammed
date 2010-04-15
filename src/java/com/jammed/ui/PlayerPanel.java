package com.jammed.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.media.Player;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * TODO: Make background a jammed image instead of black
 * TODO: Set preferred size to 0, 0 when audio only
 *
 * @author nmaludy
 */
public class PlayerPanel extends JPanel {

	private static final long serialVersionUID = 2394329L;
	private final ImageIcon backgroundImage;
	private Player currentPlayer = null;

	private PlayerPanel() {
		super();
		backgroundImage = new ImageIcon("images/jammed_logo.png");
		JLabel backgroundLabel = new JLabel(backgroundImage);
		BorderLayout layout = new BorderLayout();
		setLayout(layout);
		add(backgroundLabel, BorderLayout.CENTER);
	}

	public static PlayerPanel create() {
		return new PlayerPanel();
	}

	public Player getPlayer() {
		return this.currentPlayer;
	}

	public void setPlayer(Player player) {
		if (currentPlayer != null) {
			currentPlayer.close();
		}
		currentPlayer = player;
		update();
	}

	private void update() {
		removeAll();
		invalidate();
		BorderLayout layout = new BorderLayout();
		setLayout(layout);
		Component visualComponent = currentPlayer.getVisualComponent();
		if (visualComponent != null) {
			System.out.println("Adding Video");
			add(visualComponent, BorderLayout.CENTER);
		}
		Component controlComponent = currentPlayer.getControlPanelComponent();
		if (controlComponent != null) {
			add(controlComponent, BorderLayout.SOUTH);
		}
		revalidate();
	}
}