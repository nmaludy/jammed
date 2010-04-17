package com.jammed.ui;

import com.jammed.app.PlaylistEvent;
import com.jammed.app.PlaylistListener;
import com.jammed.gen.MediaProtos.Media;
import com.jammed.gen.MediaProtos.Playlist;
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
		int start = media.size() - 1;
		media.add(m);
		fireTableRowsInserted(start, start);
	}

	public void addAll(List<Media> m) {
		if (m.isEmpty()) {
			return;
		}
		int start = media.size() - 1;
		media.addAll(m);
		int end = media.size() - 1;
		fireTableRowsInserted(start, end);
	}

	public void deleteRow(int i) {
		media.remove(i);
		fireTableRowsDeleted(i, i);
	}

	public void deleteRows(int start, int end) {
		for (int i = start; i <= end; i++) {
			media.remove(i);
		}
		fireTableRowsDeleted(start, end);
	}

	public Media getMedia(int row) {
		return media.get(row);
	}

	public void clear() {
		if (media.isEmpty()) {
			return;
		}
		int end = media.size() - 1;
		media.clear();
		fireTableRowsDeleted(0, end);
	}

	public void playlistChanged(PlaylistEvent event) {
		if (event.getType() == PlaylistEvent.Type.ADD) {
			Playlist list = (Playlist)event.getSource();
			List<Media> toAdd = new ArrayList<Media>(event.getEndIndex() - event.getStartIndex());
			for (int i = event.getStartIndex(); i <= event.getEndIndex(); i++){
				toAdd.add(list.getMedia(i));
			}
			addAll(toAdd);
		} else if(event.getType() == PlaylistEvent.Type.DELETE) {
			deleteRows(event.getStartIndex(), event.getEndIndex());
		} else if(event.getType() == PlaylistEvent.Type.REPLACE) {
			clear();
			Playlist list = (Playlist)event.getSource();
			addAll(list.getMediaList());
			System.out.println("Replacing " + list.getMediaCount());
		}
	}
}
