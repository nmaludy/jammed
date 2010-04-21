package com.jammed.ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author nmaludy
 */
public class TabbedPanel extends JPanel {

	private static final long serialVersionUID = 2394329L;
	private final JTabbedPane tabs;

	private TabbedPanel() {
		super();
		tabs = new JTabbedPane();
		BorderLayout layout = new BorderLayout();
		setLayout(layout);
		tabs.addTab("Playlist", PlaylistPanel.getInstance());
		//tabs.addTab("Library", LibraryPanel.create());
		tabs.addTab("Library", LibraryMediaPanel.create());
		tabs.addTab("Search", LibrarySearchPanel.create());
		add(tabs, BorderLayout.CENTER);
	}

	public static TabbedPanel create() {
		return new TabbedPanel();
	}

}
