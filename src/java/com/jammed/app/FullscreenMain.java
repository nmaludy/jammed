
package com.jammed.app;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class FullscreenMain {

    private static final JFrame frame   = new JFrame("Fullscreen Test");
    private static final JButton button = new JButton("Enter Fullscreen");

    private static final ActionListener buttonListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
            new FullscreenWindow();
        }
    };

    public static void addComponentsToPane(final Container pane) {
        
        final GridBagConstraints c = new GridBagConstraints();
        pane.setLayout(new GridBagLayout());

        button.addActionListener(buttonListener);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
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
}
