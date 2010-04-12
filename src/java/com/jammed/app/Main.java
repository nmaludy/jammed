package com.jammed.app;

import com.jammed.gen.MediaProtos.*;
import com.jammed.gen.MessageProtos.*;
import com.jammed.gen.ProtoBuffer.*;

import com.google.protobuf.MessageLite;

import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Main {
	
	private static final JFrame frame = new JFrame("jammed");
	
	private static final JButton searchButton = new JButton("Search");
	private static final JButton loadButton   = new JButton("Load Playlist");
		
	private static final JTextField searchField = new JTextField("");
	
	private static final ActionListener buttonListener = new ActionListener() {
		@Override
		public void actionPerformed( final ActionEvent e ) {
			final Object source = e.getSource();
			
			if (source == searchButton) {
				search(searchField.getText());
			} else if (source == loadButton) {
				load();
			}
		}
	};
	
	public static void addComponentsToPane (final Container pane) {
		pane.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		searchButton.addActionListener(buttonListener);
		loadButton.addActionListener(buttonListener);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		pane.add(searchField, c);
		
		c.gridy++;
		pane.add(searchButton, c);
		
		c.gridy++;
		pane.add(loadButton, c);

    }
	
	private static void createAndShowGUI() {
        //Create and set up the window.
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set up the content pane.
        addComponentsToPane(frame.getContentPane());
		
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
	
	public static void main (final String[] args) throws Exception {
		//Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
		
	}
	
	private static void search (final String query) {
		System.out.println("Query: " + query);
		
		if (query.trim().equals("")) {
			return;
		}
		
		final int request = 69;
		
		Request.Builder requestBuilder = Request.newBuilder();
		requestBuilder.setId(request);
		requestBuilder.setOrigin("me");
		requestBuilder.setRelease(false);
		
		Search.Builder builder = Search.newBuilder();
		
		builder.setQuery(query);
		builder.setRequest(requestBuilder.build());
		builder.setType(builder.getType());
		
		Cloud.getInstance().send(builder.build(), request);
		
	}
	
	private static void load() {
		final JFileChooser fc = new JFileChooser();
		fc.showOpenDialog(frame);
		Librarian.getInstance().open(fc.getSelectedFile()); 
	}
	
}                                                                                    
