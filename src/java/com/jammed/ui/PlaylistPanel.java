package com.jammed.ui;

import com.jammed.gen.MediaProtos.Media;
import com.jammed.gen.MediaProtos.Playlist;
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

	public PlaylistPanel() {
		super();
		model = MediaTableModel.createBlankModel();
		table = new JTable(model);
		setColumnHeaderView(table.getTableHeader());
		setViewportView(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setAutoCreateRowSorter(true);
		table.getRowSorter().toggleSortOrder(0);
	}

	public static PlaylistPanel getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PlaylistPanel();
		}
		return INSTANCE;
	}

	private void getPlaylist(){

	}

	private void normalizeTable() {
		TableUtils.normalizeColumnWidths(table);
		revalidate();
	}

	void add(Media m) {
		model.addRow(m);
		normalizeTable();
	}

	void addAll(List<Media> list) {
		model.addAll(list);
		normalizeTable();
	}
}
