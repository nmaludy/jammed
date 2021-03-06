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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 *
 * @author nmaludy
 */
public class LibrarySearchPanel extends JPanel implements ActionListener, MouseListener, TableModelListener {
	private static final long serialVersionUID = 1L;
	private final JTable table;
	private final MediaTableModel model;
	private final JScrollPane scrollPane;
	private final JTextField searchBox;
	private final JButton searchButton;
	private final JButton clearButton;
	private final JButton addButton;
	private final SearchResponder responder;
	private final SortedMap<Integer, Request> searchRequests;

	private LibrarySearchPanel() {
		super();
		model = MediaTableModel.createModel(Librarian.getInstance().getSearchPlaylist());
		Librarian.getInstance().addSearchListener(model);
		model.addTableModelListener(this);
		table = new JTable(model);
		scrollPane = new JScrollPane(table);
		//table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setAutoCreateRowSorter(true);
		table.getRowSorter().toggleSortOrder(0);
		table.addMouseListener(this);

		searchRequests = new TreeMap<Integer, Request>();

		searchButton = new JButton("Search");
		searchButton.addActionListener(this);
		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);
		addButton = new JButton("Add to Playlist");
		addButton.addActionListener(this);
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
				.addComponent(addButton)
				.addComponent(clearButton)));

		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				 .addComponent(searchBox)
				 .addComponent(searchButton))
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(scrollPane))
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(addButton)
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
			Librarian.getInstance().setSearchPlaylist(Librarian.getInstance().createEmptyPlaylist());
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
			Librarian.getInstance().setSearchPlaylist(empty);
		} else if (e.getSource().equals(addButton)) {
			int[] selectedIndexes = table.getSelectedRows();
			int index = PlaylistPanel.getInstance().getCurrentPlaylistIndex();
			for(int i : selectedIndexes) {
				int modelIndex = table.convertRowIndexToModel(i);
				Librarian.getInstance().addMediaToPlaylist(model.getMedia(modelIndex), index);
			}
		}
	}

	private void normalizeTable() {
		TableUtils.normalizeColumnWidths(table);
		revalidate();
	}

	public void tableChanged(TableModelEvent tme) {
		normalizeTable();
	}

	public void mouseClicked(MouseEvent me) {
		if (me.getClickCount() > 1) {
			addButton.doClick();
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

	private class SearchResponder extends PlaylistHandler {

		@Override
		public boolean handleMessage(final MessageLite message) {
			if (!(message instanceof Playlist)) {
				throw new IllegalArgumentException();
			}

			final Playlist playlist = (Playlist) message;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					handle(playlist);
				}
			});
			
			return true;
		}

		public void handle(Playlist playlist) {
			Request request = playlist.getRequest();
			String hostname = request.getOrigin();
			if (!hostname.equals(Cloud.getInstance().getHostName())) {
				return; //a request that originated from this system, ignore it
			}

			Librarian.getInstance().addMediaToSearch(playlist.getMediaList());
			normalizeTable();

			Integer requestId = Integer.valueOf(playlist.getRequest().getId());
			System.out.println("Got search response for ID " + requestId);
			if (searchRequests.containsKey(requestId)) {
				//System.out.println("Got Playlist! ");
				searchRequests.remove(requestId);
				Request.Builder builder = Request.newBuilder(request);
				builder.setRelease(true);
				RequestPool.getInstance().release(builder.build()); //Release request ID
			}
		}
	}
}
