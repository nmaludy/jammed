package com.jammed.ui;

import com.jammed.app.GUI;
import com.jammed.app.Librarian;
import com.jammed.gen.MediaProtos.Media;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 *
 * @author nmaludy
 */
public class PlaylistPanel extends JScrollPane implements MouseListener {

	private static final long serialVersionUID = 1L;
	private static PlaylistPanel INSTANCE;
	private final JTable table;
	private final MediaTableModel model;
	private int playlistIndex;

	public PlaylistPanel() {
		super();
		playlistIndex = Librarian.getInstance().addEmptyPlaylist();
		model = MediaTableModel.createModel(Librarian.getInstance().getPlaylist(playlistIndex));
		Librarian.getInstance().addPlaylistListener(model, playlistIndex);
		table = new JTable(model);
		setColumnHeaderView(table.getTableHeader());
		setViewportView(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setAutoCreateRowSorter(true);
		table.addMouseListener(this);
	}

	public static PlaylistPanel getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PlaylistPanel();
		}
		return INSTANCE;
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
}
