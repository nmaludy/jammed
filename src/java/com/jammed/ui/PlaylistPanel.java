package com.jammed.ui;

import com.jammed.app.Librarian;
import com.jammed.gen.MediaProtos.Media;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 *
 * @author nmaludy
 */
public class PlaylistPanel extends JScrollPane {

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
		System.out.println(playlistIndex);
		table = new JTable(model);
		setColumnHeaderView(table.getTableHeader());
		setViewportView(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
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
}
