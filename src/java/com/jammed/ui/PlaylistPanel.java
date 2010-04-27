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
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 *
 * @author nmaludy
 */
public class PlaylistPanel extends JPanel implements ActionListener, MouseListener, TableModelListener {

	private static final long serialVersionUID = 1L;
	private static PlaylistPanel INSTANCE;
	private final JTable table;
	private final JScrollPane scrollPane;
	private final MediaTableModel model;
	private int playlistIndex;
	private final JButton sendButton;

	public PlaylistPanel() {
		super();
		playlistIndex = Librarian.getInstance().addEmptyPlaylist();
		model = MediaTableModel.createModel(Librarian.getInstance().getPlaylist(playlistIndex));
		Librarian.getInstance().addPlaylistListener(model, playlistIndex);
		model.addTableModelListener(this);
		table = new JTable(model);
		scrollPane = new JScrollPane(table);
		//setColumnHeaderView(table.getTableHeader());
		//setViewportView(table);
		//table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setAutoCreateRowSorter(true);
		table.addMouseListener(this);
		
		sendButton = new JButton("Send Playlist");
		sendButton.addActionListener(this);
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
			.addGroup(layout.createSequentialGroup()
				.addComponent(scrollPane))
			.addGroup(layout.createSequentialGroup()
				.addComponent(sendButton)));

		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(scrollPane))
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(sendButton)));

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
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final Object source = e.getSource();
		if (source == sendButton) {
			send();
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
