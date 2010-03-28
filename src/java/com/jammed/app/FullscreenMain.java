
package com.jammed.app;

import com.jammed.app.Protos.*;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class FullscreenMain {

    private static final JFrame frame   = new JFrame("Fullscreen Test");
	
    private static final JButton button = new JButton("Enter Fullscreen");
	private static final JButton send   = new JButton("Send");
	
	private static final JTextField searchField = new JTextField("");
	private static final JTextField hostField   = new JTextField("");
	
	private static final Cloud cloud         = Cloud.getInstance();
	private static final MessageBox message  = new MessageBox();
	private static final DirectiveHandler dh = new DirectiveHandler(message);
	
	static {
		cloud.addMessageHandler(dh);
		System.out.println("Host: " + cloud.getHostName());
	}

    private static final ActionListener buttonListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
			final Object source = e.getSource();
			
			if (source == button) {
				FullscreenWindow fw = new FullscreenWindow();
				message.reset();
				fw.addDrawable(message);
			} else if (source == send) {
				send(searchField.getText());
			}
        }
    };

    public static void addComponentsToPane(final Container pane) {
        
        final GridBagConstraints c = new GridBagConstraints();
        pane.setLayout(new GridBagLayout());

        button.addActionListener(buttonListener);
		send.addActionListener(buttonListener);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        
		c.gridy = 0;
		pane.add(searchField, c);
		
		c.gridy++;
		pane.add(hostField, c);
		
		c.gridy++;
		pane.add(send, c);
		
		c.gridy++;
        pane.add(button, c);

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

    public static void main(final String[] args) throws Exception {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
	
	private static void send (final String message) {
		
		Directive.Builder builder = Directive.newBuilder();
		
		builder.setDestination(hostField.getText());
		builder.setType(builder.getType());
		
		for (final String line : message.split(",")) {
			builder.addMessage(line);
		}
		
		cloud.send(builder.build());
	}
}
