/*
 * Copyright (C) 2004 Anthony Smith
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * ----------------------------------------------------------------------------
 * TITLE $Id$
 * ---------------------------------------------------------------------------
 *
 * --------------------------------------------------------------------------*/
package opendbcopy.gui;

import opendbcopy.controller.MainController;

import opendbcopy.resource.ResourceManager;

import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class FrameShowURL extends JFrame {
    private MainController  controller;
    private ResourceManager rm;
    private JPanel          panelMain;
    private JPanel          panelControl;
    private JScrollPane     scrollPane;
    private JEditorPane     editorPaneURL;
    private String          urlString;
    private URL             url;
    private String          title;

    /**
     * Creates a new FrameLaunchingProgress object.
     *
     * @param controller DOCUMENT ME!
     * @param frameWidth DOCUMENT ME!
     * @param frameHeight DOCUMENT ME!
     * @param urlString DOCUMENT ME!
     * @param title DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public FrameShowURL(MainController controller,
                        int            frameWidth,
                        int            frameHeight,
                        String         urlString,
                        String         title) throws Exception {
        this.controller     = controller;
        this.rm             = controller.getResourceManager();
        this.urlString      = urlString;
        this.title          = title;
        this.setSize(frameWidth, frameHeight);
    }

    /**
     * DOCUMENT ME!
     */
    public void showMe() {
        if (url == null) {
            this.setTitle(title);

            try {
            	File urlFile = new File(urlString);
                url = urlFile.toURL();
            } catch (MalformedURLException e) {
                // see message below
            }

            try {
                if (url != null) {
                    editorPaneURL = new JEditorPane(url);
                }
            } catch (IOException e) {
                editorPaneURL     = null; // giving up
                url               = null; // see message below
            }

            if (editorPaneURL != null) {
                editorPaneURL.setEditable(false);
                editorPaneURL.setBackground(SystemColor.WHITE);
                scrollPane = new JScrollPane(editorPaneURL);
            } else {
                scrollPane = new JScrollPane(new JLabel("cannot access " + urlString));
            }

            this.getContentPane().setLayout(new GridLayout(1, 1));
            this.getContentPane().add(scrollPane);
        }

        show();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            hide();
        }
    }
}
