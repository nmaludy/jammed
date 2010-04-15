package com.jammed.ui;

import com.jammed.app.PlaylistEvent;
import com.jammed.app.PlaylistListener;
import com.jammed.gen.MediaProtos.Media;
import com.jammed.gen.MediaProtos.Playlist;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author nmaludy
 */
public class MediaTableModel extends AbstractTableModel implements PlaylistListener {
	private static final long serialVersionUID = 1L;

	private String[] columns = {"Title", "Duration"};
	private List<Media> media;

	private MediaTableModel(List<Media> m) {
		super();
		media = m;
	}
	
	public static MediaTableModel createModel(Playlist playlist) {
		List<Media> l = new ArrayList<Media>(playlist.getMediaCount());
		for (Media m: playlist.getMediaList()) {
			l.add(m);
		}
		return new MediaTableModel(l);
	}

	public static MediaTableModel createBlankModel() {
		List<Media> m = new ArrayList<Media>();
		return new MediaTableModel(m);
	}

	@Override
	public int getRowCount() {
		return media.size();
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public String getColumnName(int column){
		return columns[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Media value = media.get(rowIndex);
		switch(columnIndex) {
			case 0:
				return value.getTitle();
			case 1:
				return value.getLength();
			default:
				return "BAD COLUMN INDEX";
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}


	public void addRow(Media m) {
		media.add(m);
		fireTableDataChanged();
	}

	public void addAll(List<Media> m) {
		if (m.isEmpty()) {
			return;
		}
		int start = media.size();
		media.addAll(m);
		int end = media.size();
		fireTableDataChanged();
	}

	public void deleteRow(int i) {
		media.remove(i);
		fireTableDataChanged();
	}

	public void deleteRows(int start, int end) {
		for (int i = start; i < end; i++) {
			media.remove(i);
		}
		fireTableDataChanged();
	}

	public Media getMedia(int row) {
		return media.get(row);
	}

	public void clear() {
		if (media.isEmpty()) {
			return;
		}
		int end = media.size();
		media.clear();
		fireTableDataChanged();
	}

	public void playlistChanged(PlaylistEvent event) {
		if(event.getType() == PlaylistEvent.Type.REPLACE) {
			System.out.println("PlaylistChanged!");
			clear();
			Playlist list = (Playlist)event.getSource();
			addAll(list.getMediaList());
		}
	}
}
