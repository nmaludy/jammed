package com.jammed.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;
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
	private final URL imageURL;
	private final ImageIcon backgroundImage;
	private final JLabel background;
	private Player player = null;
	private Player audioPlayer = null;
	private Player videoPlayer = null;

	private PlayerPanel() {
		super();
		imageURL = this.getClass().getResource("/images/jammed_logo.png");
		backgroundImage = new ImageIcon(imageURL);
		background = new JLabel(backgroundImage);
		BorderLayout layout = new BorderLayout();
		setLayout(layout);
		add(background, BorderLayout.CENTER);
	}

	public static PlayerPanel create() {
		return new PlayerPanel();
	}

	public Player getPlayer() {
		return this.player;
	}

	public void setPlayer(Player p) {
		if (player != null) {
			player.close();
		}
		if (audioPlayer != null) {
			audioPlayer.close();
			audioPlayer = null;
		}
		if (videoPlayer != null) {
			videoPlayer.close();
			videoPlayer = null;
		}
		player = p;
		update();
	}

	public void setAudioPlayer(Player p) {
		if (player != null) {
			player.close();
			player = null;
		}
		if (audioPlayer != null) {
			audioPlayer.close();
		}
		audioPlayer = p;
		update();
	}

	public void setVideoPlayer(Player p) {
		if (player != null) {
			player.close();
			player = null;
		}
		if (videoPlayer != null) {
			videoPlayer.close();
		}
		videoPlayer = p;
		update();
	}

	private void update() {
		removeAll();
		invalidate();
		BorderLayout layout = new BorderLayout();
		setLayout(layout);
		if (player != null) {
			Component visualComponent = player.getVisualComponent();
			if (visualComponent != null) {
				System.out.println("Adding Video");
				add(visualComponent, BorderLayout.CENTER);
			} else {
				add(background, BorderLayout.CENTER);
			}
			Component controlComponent = player.getControlPanelComponent();
			if (controlComponent != null) {
				add(controlComponent, BorderLayout.SOUTH);
			}
			revalidate();
			return;
		}

		if (audioPlayer != null) {
			add(background, BorderLayout.CENTER);
			Component controlComponent = player.getControlPanelComponent();
			if (controlComponent != null) {
				add(controlComponent, BorderLayout.SOUTH);
			}
		}

		if (videoPlayer != null) {
			Component visualComponent = videoPlayer.getVisualComponent();
			if (visualComponent != null) {
				System.out.println("Adding Video");
				add(visualComponent, BorderLayout.CENTER);
			} else {
				add(background, BorderLayout.CENTER);
			}
			Component controlComponent = player.getControlPanelComponent();
			if (controlComponent != null) {
				add(controlComponent, BorderLayout.SOUTH);
			}
		}

		revalidate();
	}
}
