package com.jammed.ui;

import com.jammed.app.Cloud;
import com.jammed.app.RequestPool;
import com.jammed.app.GUI;
import com.jammed.app.Librarian;
import com.jammed.gen.MediaProtos.Media;
import com.jammed.gen.MediaProtos.Playlist;
import com.jammed.gen.MessageProtos.Directive;
import com.jammed.gen.ProtoBuffer.Request;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *
 * @author nmaludy
 */
public class PlaylistPanel extends JPanel implements ActionListener, KeyListener, MouseListener, TableModelListener {

	private static final long serialVersionUID = 1L;
	private static PlaylistPanel INSTANCE;
	private final JTable table;
	private final JScrollPane scrollPane;
	private final MediaTableModel model;
	private int playlistIndex;
	
	private final JButton sendButton;
	private final JButton importButton;

	public PlaylistPanel() {
		super();
		playlistIndex = Librarian.getInstance().addEmptyPlaylist();
		model = MediaTableModel.createModel(Librarian.getInstance().getPlaylist(playlistIndex));
		Librarian.getInstance().addPlaylistListener(model, playlistIndex);
		model.addTableModelListener(this);
		table = new JTable(model);
		table.addKeyListener(this);
		scrollPane = new JScrollPane(table);
		//setColumnHeaderView(table.getTableHeader());
		//setViewportView(table);
		//table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setAutoCreateRowSorter(true);
		table.addMouseListener(this);
		
		sendButton   = new JButton("Send Playlist");
		importButton = new JButton("Import Playlist");
		
		sendButton.addActionListener(this);
		importButton.addActionListener(this);
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
			.addGroup(layout.createSequentialGroup()
				.addComponent(scrollPane))
			.addGroup(layout.createSequentialGroup()
				.addComponent(sendButton)
				.addComponent(importButton)));

		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(scrollPane))
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(sendButton)
				.addComponent(importButton)));

		normalizeTable();
	}

	public static PlaylistPanel getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PlaylistPanel();
		}
		return INSTANCE;
	}
	
	protected void send() {
		final Playlist playlist = model.getPlaylist();
		final String host = (String)JOptionPane.showInputDialog(
			this,
			"Enter a destination hostname:",
			"Send Playlist",
			JOptionPane.PLAIN_MESSAGE,
			null,
			null, // Allow user to type text
			""); // Initial text
		
		if (host == null) return;
		
		System.out.println("HOST: " + host);
		final Directive.Builder builder = Directive.newBuilder();
		final Request request = RequestPool.getInstance().lease();
		
		builder.setType(builder.getType());
		builder.setDestination(host);
		builder.setRequest(request);
		builder.setPlaylist(playlist);
		
		Cloud.getInstance().send(builder.build(), request.getId());
	}
	
	protected void importPlaylist() {
		final JFileChooser fc = new JFileChooser();
		fc.showOpenDialog(this);
		
		final File selection = fc.getSelectedFile();
		if (selection == null) return;
		
		System.out.println("Import");
		
		final Librarian library = Librarian.getInstance();
		final Playlist  p       = library.open(selection);
		final int       index   = getCurrentPlaylistIndex();
		
		library.addMediaToPlaylist(p.getMediaList(), index);
		normalizeTable();
		
		System.out.println(p.getMediaCount());
		
	}

	private void normalizeTable() {
		TableUtils.normalizeColumnWidths(table);
		revalidate();
	}

	public int getCurrentPlaylistIndex() {
		return playlistIndex;
	}

	public Media getSelectedMedia() {
		int row = table.getSelectedRow();
		if (row == -1) {
			return null;
		}
		row = table.convertRowIndexToModel(row);
		return model.getMedia(row);
	}

	public Media getNextSelectedMedia() {
		int row = table.getSelectedRow();
		if (row == -1) {
			return null;
		}
		int rowCount = model.getRowCount();
		if ((row + 1) < rowCount){
			row = table.convertRowIndexToModel(row + 1);
			table.getSelectionModel().setSelectionInterval(row, row);
			return model.getMedia(row);
		} else {
			return null;
		}
	}

	public Media getPreviousSelectedMedia() {
		int row = table.getSelectedRow();
		if (row == -1) {
			return null;
		}
		if ((row - 1) >= 0){
			row = table.convertRowIndexToModel(row - 1);
			table.getSelectionModel().setSelectionInterval(row, row);
			return model.getMedia(row);
		} else {
			return null;
		}
	}
	
	public void keyReleased(final KeyEvent ke) {
	}
	
	public void keyPressed(final KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_DELETE) {
			final int row = table.getSelectedRow();
			
			if (row > 0) {
				model.deleteRow(row);
			}
		}
	}
	
	public void keyTyped(final KeyEvent ke) {
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final Object source = e.getSource();
		if (source == sendButton) {
			send();
		} else if (source == importButton) {
			importPlaylist();
		} else {
			assert false : "A button was pressed that does not exist";
		}
	}

	public void mouseClicked(MouseEvent me) {
		if (me.getClickCount() > 1) {
			Media m = getSelectedMedia();
			GUI.getInstance().play(m);
		}
	}

	public void mousePressed(MouseEvent me) {
	}

	public void mouseReleased(MouseEvent me) {
	}

	public void mouseEntered(MouseEvent me) {
	}

	public void mouseExited(MouseEvent me) {
	}

	public void tableChanged(TableModelEvent tme) {
		normalizeTable();
	}

}
