package com.jammed.ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author nmaludy
 */
public class LibraryPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final JTabbedPane tabs;

	private LibraryPanel() {
		super();
		tabs = new JTabbedPane();
		BorderLayout layout = new BorderLayout();
		setLayout(layout);
		tabs.addTab("Media", LibraryMediaPanel.create());
		tabs.addTab("Search", LibrarySearchPanel.create());
		add(tabs, BorderLayout.CENTER);
	}

	public static LibraryPanel create() {
		return new LibraryPanel();
	}

}
