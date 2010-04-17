package com.jammed.ui;

import com.google.protobuf.MessageLite;
import com.jammed.gen.MessageProtos.Search;
import com.jammed.app.Cloud;
import com.jammed.app.Librarian;
import com.jammed.app.RequestPool;
import com.jammed.gen.MediaProtos.Playlist;
import com.jammed.gen.ProtoBuffer.Request;
import com.jammed.handlers.PlaylistHandler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 *
 * @author nmaludy
 */
public class LibrarySearchPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	private final JTable table;
	private final MediaTableModel model;
	private final JScrollPane scrollPane;
	private final JTextField searchBox;
	private final JButton searchButton;
	private final JButton clearButton;
	private final int searchPlaylistIndex;
	private final SearchResponder responder;
	private final SortedMap<Integer, Request> searchRequests;

	private LibrarySearchPanel() {
		super();
		searchPlaylistIndex = Librarian.getInstance().addEmptyPlaylist();
		model = MediaTableModel.createModel(Librarian.getInstance().getPlaylist(searchPlaylistIndex));
		Librarian.getInstance().addPlaylistListener(model, searchPlaylistIndex);
		table = new JTable(model);
		scrollPane = new JScrollPane(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setAutoCreateRowSorter(true);
		table.getRowSorter().toggleSortOrder(0);

		searchRequests = new TreeMap<Integer, Request>();

		searchButton = new JButton("Search");
		searchButton.addActionListener(this);
		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);
		searchBox = new JTextField();
		searchBox.addActionListener(this);

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
			.addGroup(layout.createSequentialGroup()
				  .addComponent(searchBox)
				  .addComponent(searchButton))
			.addGroup(layout.createSequentialGroup()
				.addComponent(scrollPane))
			.addGroup(layout.createSequentialGroup()
				.addComponent(clearButton)));

		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				 .addComponent(searchBox)
				 .addComponent(searchButton))
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(scrollPane))
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(clearButton)));
		
		normalizeTable();
		responder = new SearchResponder();
		Cloud.getInstance().addMessageHandler(responder);
	}

	public static LibrarySearchPanel create() {
		return new LibrarySearchPanel();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(searchButton)) {
			String query = searchBox.getText();
			Request request = RequestPool.getInstance().lease();
			Search.Builder builder = Search.newBuilder();
			builder.setType(builder.getType());
			builder.setQuery(query);
			builder.setRequest(request);
			Integer requestId = Integer.valueOf(request.getId());
			searchRequests.put(requestId, request);
			System.out.println("Searching for " + query + " ID: " + requestId);
			Cloud.getInstance().send(builder.build(), request.getId());
		} else if (e.getSource().equals(clearButton)) {
			Playlist empty = Librarian.getInstance().createEmptyPlaylist();
			Librarian.getInstance().setPlaylist(searchPlaylistIndex, empty);
		}
	}

	private void normalizeTable() {
		TableUtils.normalizeColumnWidths(table);
		revalidate();
	}

	private class SearchResponder extends PlaylistHandler {

		@Override
		public boolean handleMessage(final MessageLite message) {
			if (!(message instanceof Playlist)) {
				throw new IllegalArgumentException();
			}

			final Playlist playlist = (Playlist) message;
			Request request = playlist.getRequest();
			Integer requestId = Integer.valueOf(playlist.getRequest().getId());
			System.out.println("Got search response for ID " + requestId);
			if (searchRequests.containsKey(requestId)) {
				System.out.println("Got Playlist! ");
				searchRequests.remove(requestId);
				Request.Builder builder = Request.newBuilder(request);
				builder.setRelease(true);
				RequestPool.getInstance().release(builder.build());
				Librarian.getInstance().setPlaylist(searchPlaylistIndex, playlist);
			}

			return true;
		}
	}
}
