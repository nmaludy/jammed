package com.jammed.ui;

import com.jammed.app.Librarian;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 *
 * @author nmaludy
 */
public class LibraryMediaPanel extends JPanel implements ActionListener, TableModelListener{
	private static final long serialVersionUID = 1L;
	private final JTable table;
	private final MediaTableModel model;
	private final JScrollPane scrollPane;
	private final JButton selectButton;
	private final JButton addButton;

	private LibraryMediaPanel() {
		super();
		model = MediaTableModel.createModel(Librarian.getInstance().getLibrary());
		Librarian.getInstance().addLibraryListener(model);
		model.addTableModelListener(this);
		table = new JTable(model);
		scrollPane = new JScrollPane(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setAutoCreateRowSorter(true);
		table.getRowSorter().toggleSortOrder(0);

		selectButton = new JButton("Set Folder");
		selectButton.addActionListener(this);
		addButton = new JButton("Add to Playlist");
		addButton.addActionListener(this);
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
			.addGroup(layout.createSequentialGroup()
				.addComponent(scrollPane))
			.addGroup(layout.createSequentialGroup()
				.addComponent(addButton)
				.addComponent(selectButton)));

		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(scrollPane))
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(addButton)
				.addComponent(selectButton)));

		normalizeTable();
	}

	public static LibraryMediaPanel create() {
		return new LibraryMediaPanel();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(addButton)) {
			int[] selectedIndexes = table.getSelectedRows();
			int index = PlaylistPanel.getInstance().getCurrentPlaylistIndex();
			for(int i : selectedIndexes) {
				Librarian.getInstance().addMediaToPlaylist(model.getMedia(i), index);
			}
		} else if (e.getSource().equals(selectButton)) {
			try {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fc.getSelectedFile();
					Librarian.getInstance().setLibraryRoot(selectedFile);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
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
}
