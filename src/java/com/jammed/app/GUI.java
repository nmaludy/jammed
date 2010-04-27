package com.jammed.app;

import com.jammed.gen.MediaProtos.Media;
import com.jammed.ui.PlayerPanel;
import com.jammed.ui.PlaylistPanel;
import com.jammed.ui.TabbedPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.DecimalFormat;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel;

/*
 * TODO: Convert to completely custom controls and remove JMF controls.
 * TODO: Abstract more media handling utilities to MediaUtils
 *
 * @author Nicholas Maludy
 */
public class GUI extends JFrame implements ActionListener, ChangeListener, ControllerListener {

	private static final long serialVersionUID = 0;
	private static GUI INSTANCE;
   private DecimalFormat twoDigits    = new DecimalFormat("00");
	private URL previousURL = this.getClass().getResource("/images/rew_gray.png");
	private URL previousColorURL = this.getClass().getResource("/images/rew_color.png");
	private URL playURL = this.getClass().getResource("/images/play_gray.png");
	private URL playColorURL =this.getClass().getResource("/images/play_color.png");
	private URL pauseURL = this.getClass().getResource("/images/pause_gray.png");
	private URL pauseColorURL = this.getClass().getResource("/images/pause_color.png");
	private URL nextURL = this.getClass().getResource("/images/ff_gray.png");
	private URL nextColorURL = this.getClass().getResource("/images/ff_color.png");
	private JButton previousButton = new JButton(new ImageIcon(previousURL));
	private JButton playPauseButton = new JButton(new ImageIcon(playURL));
	private JButton nextButton = new JButton(new ImageIcon(nextURL));
	private JCheckBox showPlaylistBox = new JCheckBox("Show Playlist");
	private PlayerPanel playerPanel = PlayerPanel.create();
	private JPanel tabsPanel = TabbedPanel.create();
	private MediaController controller = MediaController.getInstance();
	private JSlider volumeSlider = new JSlider(SwingConstants.VERTICAL,0, 100, 50);
	private JLabel timeLabel = new JLabel("0:00/0:00");
	private boolean paused = false;

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
						.addComponent(volumeSlider)
						.addComponent(timeLabel)
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
						.addComponent(volumeSlider, GroupLayout.Alignment.LEADING)
						.addComponent(timeLabel)
						.addComponent(showPlaylistBox)))
				.addGroup(layout.createSequentialGroup()
					.addComponent(tabsPanel)));


		layout.linkSize(SwingConstants.VERTICAL, previousButton, playPauseButton, nextButton
				  , volumeSlider, timeLabel, showPlaylistBox);

		previousButton.addActionListener(this);
		playPauseButton.addActionListener(this);
		nextButton.addActionListener(this);
		showPlaylistBox.addActionListener(this);

		controller.setPlayerPanel(playerPanel);
		controller.addControllerListener(this);
		volumeSlider.addChangeListener(this);
		volumeSlider.setPreferredSize(nextButton.getPreferredSize());
		tabsPanel.setVisible(false);
		initializeButtonIcons();

		TimerThread t = new TimerThread();
		t.start();

		setTitle("jammed");
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
		playPauseButton.setRolloverIcon(new ImageIcon(playColorURL));
		playPauseButton.setPressedIcon(new ImageIcon(playColorURL));
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
			previous();
		} else if (e.getSource().equals(playPauseButton)) {
			playPauseResume();
		} else if (e.getSource().equals(nextButton)) {
			next();
		}
	}

	public void play(Media toPlay) {
		if (toPlay == null) { //No media to play
			return;
		}
		paused = false;
		controller.playMedia(toPlay);
		
		playPauseButton.setIcon(new ImageIcon(pauseURL));
		playPauseButton.setRolloverIcon(new ImageIcon(pauseColorURL));
		playPauseButton.setPressedIcon(new ImageIcon(pauseColorURL));
	}

	public void resume() {
		paused = false;
		controller.play();
		
		playPauseButton.setIcon(new ImageIcon(pauseURL));
		playPauseButton.setRolloverIcon(new ImageIcon(pauseColorURL));
		playPauseButton.setPressedIcon(new ImageIcon(pauseColorURL));
	}

	public void pause() {
		paused = true;
		
		controller.pause();

		playPauseButton.setIcon(new ImageIcon(playURL));
		playPauseButton.setRolloverIcon(new ImageIcon(playColorURL));
		playPauseButton.setPressedIcon(new ImageIcon(playColorURL));
	}

	public void playPauseResume() {
		if (paused) {
			resume();
		} else if (controller.isSessionInProgress()) {
			pause();
		} else {
			Media m = PlaylistPanel.getInstance().getSelectedMedia();
			play(m);
		}
	}

	public void next() {
		Media m = PlaylistPanel.getInstance().getNextSelectedMedia();
		if (m != null) {
			play(m);
		}
	}

	public void previous() {
		Media m = PlaylistPanel.getInstance().getPreviousSelectedMedia();
		if (m != null) {
			play(m);
		}
	}


	public void stateChanged(ChangeEvent ce) {
		if (!volumeSlider.getValueIsAdjusting()) {
			int value = volumeSlider.getValue();
			float volume = (float)value;
			volume /= 100;
			controller.setGain(volume);
		}
	}

	public void controllerUpdate(ControllerEvent ce) {
		System.out.println("GUI " +ce.getClass().toString());
		if (ce instanceof EndOfMediaEvent) {
			System.out.println("Goto next!");
			next();
		}
	}
	
	private class TimerThread extends Thread {

		@Override
		public void run() {
			while (!isInterrupted()) {
				if (controller.isSessionInProgress()) {
					final int time = (int) controller.getMediaTime();
					final int duration = (int) controller.getMediaDuration();

					SwingUtilities.invokeLater(new Runnable() {

						public void run() {
							StringBuilder b = new StringBuilder();
							int minutes = time / 60;
							b.append(minutes);
							b.append(':');
							int seconds = time % 60;
							b.append(twoDigits.format((double) seconds));
							b.append('/');

							int dminutes = duration / 60;
							b.append(dminutes);
							b.append(':');
							int dseconds = duration % 60;
							b.append(twoDigits.format((double) dseconds));

							timeLabel.setText(b.toString());
						}
					});
				}

				try {
					sleep(250);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}
	/*
	 * Initializes a new instance of GUI and schedules it for launch.
	 */
	public static void main(final String[] args) throws Exception {
		JFrame.setDefaultLookAndFeelDecorated(true);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				//com.sun.media.codec.audio.mp3.JavaDecoder.main(args);
				Cloud.getInstance(); //Start the cloud
				TransmissionAddressManager.getInstance(); //Start transmission address manager
				try {
					UIManager.setLookAndFeel(new SubstanceBusinessLookAndFeel());
				} catch (Exception e) {
					System.out.println("Substance Graphite failed to initialize");
				}
				URL imageURL = this.getClass().getResource("/images/jammed_text.png");
				JFrame f = GUI.getInstance();
				f.setIconImage(new ImageIcon(imageURL).getImage());
			}
		});
	}
}
