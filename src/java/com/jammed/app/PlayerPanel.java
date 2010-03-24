package com.jammed.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.media.Player;
import javax.swing.JPanel;

/**
 * TODO: Make background a jammed image instead of black
 * TODO: Set preferred size to 0, 0 when audio only
 *
 * @author nmaludy
 */
public class PlayerPanel extends JPanel {
	private static final long serialVersionUID = 2394329L;

	private Player currentPlayer = null;

	public PlayerPanel() {
		super();
		this.setBackground(Color.black);
	}

	public Player getPlayer() {
		return this.currentPlayer;
	}

	public void setPlayer(Player player) {
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
			add(visualComponent, BorderLayout.CENTER);
		}
		Component controlComponent = currentPlayer.getControlPanelComponent();
		if (controlComponent != null) {
			add(controlComponent, BorderLayout.SOUTH);
		}
		revalidate();
	}
}
