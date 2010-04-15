package com.jammed.ui;

import com.jammed.app.Librarian;
import com.jammed.gen.MediaProtos.Media;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 *
 * @author nmaludy
 */
public class LibraryMediaPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	private final JTable table;
	private final MediaTableModel model;
	private final JScrollPane scrollPane;
	private final JButton selectButton;
	private final JButton addButton;

	public LibraryMediaPanel() {
		super();
		model = MediaTableModel.createModel(Librarian.getInstance().getLibrary());
		Librarian.getInstance().addLibraryListener(model);
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
	}

	public static LibraryMediaPanel create() {
		return new LibraryMediaPanel();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(addButton)) {
			List<Media> selected = new ArrayList<Media>();
			int[] selectedIndexes = table.getSelectedRows();
			for(int i : selectedIndexes) {
				selected.add(model.getMedia(i));
			}
			PlaylistPanel.getInstance().addAll(selected);
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
}