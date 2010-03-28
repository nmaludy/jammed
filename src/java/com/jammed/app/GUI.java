package com.jammed.app;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.table.TableColumn;

/*
 * TODO: Convert to completely custom controls and remove JMF controls.
 * TODO: Abstract more media handling utilities to MediaUtils
 *
 * @author Nicholas Maludy
 */
public class GUI extends JFrame implements ActionListener, KeyListener, MouseListener {

	private static final long serialVersionUID = 0;
	private java.net.URL previousURL = GUI.class.getResource("images/previous_small.png");
	private java.net.URL playPauseURL = GUI.class.getResource("images/play_small.png");
	private java.net.URL nextURL = GUI.class.getResource("images/next_small.png");
	private JButton previousButton = new JButton(new ImageIcon(previousURL));
	private JButton playPauseButton = new JButton(new ImageIcon(playPauseURL));
	private JButton nextButton = new JButton(new ImageIcon(nextURL));
	private JCheckBox showPlaylistBox = new JCheckBox("Show Playlist");
	private PlayerPanel playerPanel = new PlayerPanel();
	private String[] tableColumns = {"Title", "Duration"};
	private Object[][] tableFormat = new Object[5][2];
	private JTable table = new JTable(tableFormat, tableColumns);
	private JScrollPane tablePanel = new JScrollPane(table);
	private MediaController controller = MediaController.getInstance();
	
	/*
	 * Initialze and layout all GUI components.
	 *
	 * TODO: Refactor
	 */
	public GUI() {
		super();
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		layout.setAutoCreateGaps(true);
		//layout.setAutoCreateContainerGaps(true);
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
				.addComponent(tablePanel)));

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
				.addComponent(tablePanel)));


		//layout.linkSize(SwingConstants.VERTICAL, previousButton, playPauseButton, nextButton, showPlaylistBox);

		previousButton.setBackground(Color.white);
		playPauseButton.setBackground(Color.white);
		nextButton.setBackground(Color.white);

		previousButton.addActionListener(this);
		playPauseButton.addActionListener(this);
		nextButton.addActionListener(this);
		showPlaylistBox.addActionListener(this);

		controller.setPlayerPanel(playerPanel);

		table.setValueAt("Song1.mp3", 0, 0);
		table.setValueAt("1:30", 0, 1);
		table.setValueAt("Movie1.mov", 1, 0);
		table.setValueAt("1:25:02", 1, 1);
		table.setValueAt("Song_xyx.mp3", 2, 0);
		table.setValueAt("8:56", 2, 1);
		table.setValueAt("Movie_abc.mov", 3, 0);
		table.setValueAt("2:30:01", 3, 1);
		table.setValueAt("abc.mov", 4, 0);
		table.setValueAt("99:30:01", 4, 1);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		TableColumn column = null;
		for (int i = 0; i < table.getColumnCount(); i++) {
			column = table.getColumnModel().getColumn(i);
			if (i == 1) {
				column.setPreferredWidth(80);
			} else {
				column.setPreferredWidth(280);
			}
		}
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setAutoCreateRowSorter(true);
		table.getRowSorter().toggleSortOrder(0);
		tablePanel.setVisible(false);
		tablePanel.doLayout();

		initializeButtonsRollover();

		setTitle("Jammed!");
		pack();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setVisible(true);
	}

	//TODO: Implement icon switching when Allison gets done
	private void initializeButtonsRollover() {
		//previousButton.setRolloverIcon(new ImageIcon("previous.png"));
		//previousButton.setRolloverEnabled(true);
		previousButton.addMouseListener(this);
		//playPauseButton.setRolloverIcon(new ImageIcon("play.png"));
		//playPauseButton.setRolloverEnabled(true);
		playPauseButton.addMouseListener(this);
		//nextButton.setRolloverIcon(new ImageIcon("next.png"));
		//nextButton.setRolloverEnabled(true);
		nextButton.addMouseListener(this);
	}

	/*
	 * Handle events for the tableCheckBox and selectComboBox components
	 *
	 * TODO: Handling song switching (next, previous)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.showPlaylistBox)) {
			if (showPlaylistBox.isSelected()) {
				tablePanel.setVisible(true);
			} else {
				tablePanel.setVisible(false);
			}
			pack();
		} else if (e.getSource().equals(previousButton)) {
		} else if (e.getSource().equals(playPauseButton)) {
			try {
				final JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fc.getSelectedFile();
					String selectedUrl = selectedFile.toURI().toURL().toString();
					controller.playLocalMedia(selectedUrl);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (e.getSource().equals(nextButton)) {
		}
	}

	//TODO: Implement hotkeys
	public void keyTyped(KeyEvent e) {
	}
	public void keyPressed(KeyEvent e) {
	}
	public void keyReleased(KeyEvent e) {
	}

	
	public void mouseClicked(MouseEvent e) {
	}
	public void mousePressed(MouseEvent e) {
	}
	public void mouseReleased(MouseEvent e) {
	}

	/* Highlights the currently moused-over button */
	public void mouseEntered(MouseEvent e) {
		JButton button = (JButton) e.getSource();
		button.setBackground(Color.RED);
	}
	/* Removes highlight when mouse goes else where */
	public void mouseExited(MouseEvent e) {
		JButton button = (JButton) e.getSource();
		button.setBackground(Color.WHITE);
	}

	

	/*
	 * Initializes a new instance of GUI and schedules it for launch.
	 */
	public static void main(String[] args) throws Exception {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				GUI g = new GUI();
			}
		});
	}
}
