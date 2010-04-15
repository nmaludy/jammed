package com.jammed.app;

import com.jammed.gen.MediaProtos.Media;
import com.jammed.ui.PlayerPanel;
import com.jammed.ui.PlaylistPanel;
import com.jammed.ui.TabbedPanel;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import javax.media.MediaLocator;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/*
 * TODO: Convert to completely custom controls and remove JMF controls.
 * TODO: Abstract more media handling utilities to MediaUtils
 *
 * @author Nicholas Maludy
 */
public class GUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 0;
	private static GUI INSTANCE;
	private URL previousURL = this.getClass().getResource("/images/rew_gray.png");
	private URL previousColorURL = this.getClass().getResource("/images/rew_color.png");
	private URL playPauseURL = this.getClass().getResource("/images/play_gray.png");
	private URL playPauseColorURL =this.getClass().getResource("/images/play_color.png");
	private URL nextURL = this.getClass().getResource("/images/ff_gray.png");
	private URL nextColorURL = this.getClass().getResource("/images/ff_color.png");
	private JButton previousButton = new JButton(new ImageIcon(previousURL));
	private JButton playPauseButton = new JButton(new ImageIcon(playPauseURL));
	private JButton nextButton = new JButton(new ImageIcon(nextURL));
	private JCheckBox showPlaylistBox = new JCheckBox("Show Playlist");
	private PlayerPanel playerPanel = PlayerPanel.create();
	private JPanel tabsPanel = TabbedPanel.create();
	private MediaController controller = MediaController.getInstance();

	/*
	 * Initialze and layout all GUI components.
	 *
	 * TODO: Refactor
	 */
	private GUI() {
		super();
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		layout.setAutoCreateGaps(true);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addGroup(layout.createSequentialGroup()
						.addComponent(playerPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(previousButton)
						.addComponent(playPauseButton)
						.addComponent(nextButton)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(showPlaylistBox)))
				.addGroup(layout.createSequentialGroup()
					.addComponent(tabsPanel)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(playerPanel))
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(previousButton)
						.addComponent(playPauseButton)
						.addComponent(nextButton)
						.addComponent(showPlaylistBox)))
				.addGroup(layout.createSequentialGroup()
					.addComponent(tabsPanel)));


		layout.linkSize(SwingConstants.VERTICAL, previousButton, playPauseButton, nextButton, showPlaylistBox);

		previousButton.setBackground(Color.white);
		playPauseButton.setBackground(Color.white);
		nextButton.setBackground(Color.white);

		previousButton.addActionListener(this);
		playPauseButton.addActionListener(this);
		nextButton.addActionListener(this);
		showPlaylistBox.addActionListener(this);

		controller.setPlayerPanel(playerPanel);

		tabsPanel.setVisible(false);

		initializeButtonIcons();

		setTitle("Jammed!");
		pack();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public static GUI getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new GUI();
		}
		return INSTANCE;
	}

	//TODO: Implement icon switching when Allison gets done
	private void initializeButtonIcons() {
		previousButton.setRolloverIcon(new ImageIcon(previousColorURL));
		previousButton.setPressedIcon(new ImageIcon(previousColorURL));
		previousButton.setRolloverEnabled(true);
		playPauseButton.setRolloverIcon(new ImageIcon(playPauseColorURL));
		playPauseButton.setPressedIcon(new ImageIcon(playPauseColorURL));
		playPauseButton.setRolloverEnabled(true);
		nextButton.setRolloverIcon(new ImageIcon(nextColorURL));
		nextButton.setPressedIcon(new ImageIcon(nextColorURL));
		nextButton.setRolloverEnabled(true);
	}

	/*
	 * Handle events for the tableCheckBox and selectComboBox components
	 *
	 * TODO: Handling song switching (next, previous)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(showPlaylistBox)) {
			tabsPanel.setVisible(showPlaylistBox.isSelected());
			pack();
		} else if (e.getSource().equals(previousButton)) {
		} else if (e.getSource().equals(playPauseButton)) {
			Media m = PlaylistPanel.getInstance().getSelectedMedia();
			play(m);
		} else if (e.getSource().equals(nextButton)) {
			try {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fc.getSelectedFile();
					MediaLocator l = new MediaLocator(selectedFile.toURI().toURL());
					RTPTransmitter t = new RTPTransmitter(l, "224.111.111.111", 5004);
					t.start();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void play(Media toPlay) {
		if (toPlay == null) { //No media to play
			return;
		}
		try {
			File f = new File(toPlay.getLocation());
			String selectedUrl = f.toURI().toURL().toString();
			controller.playLocalMedia(selectedUrl);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * Initializes a new instance of GUI and schedules it for launch.
	 */
	public static void main(String[] args) throws Exception {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				GUI.getInstance();
			}
		});
	}
}
