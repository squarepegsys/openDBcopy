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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class FrameShowRTF extends JFrame {
    private MainController  controller;
    private ResourceManager rm;
    private JPanel          panelMain;
    private JPanel          panelControl;
    private JScrollPane     scrollPane;
    private JEditorPane     editorPane;
    private File            file;
    private String          title;

    /**
     * Creates a new FrameLaunchingProgress object.
     *
     * @param controller DOCUMENT ME!
     * @param frameWidth DOCUMENT ME!
     * @param frameHeight DOCUMENT ME!
     * @param file DOCUMENT ME!
     * @param title DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public FrameShowRTF(MainController controller,
                        int            frameWidth,
                        int            frameHeight,
                        File           file,
                        String         title) throws Exception {
        this.controller     = controller;
        this.rm             = controller.getResourceManager();
        this.file           = file;
        this.title          = title;
        this.setSize(frameWidth, frameHeight);

        guiInit();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws BadLocationException DOCUMENT ME!
     */
    private void guiInit() throws FileNotFoundException, IOException, BadLocationException {
        RTFEditorKit rtf = new RTFEditorKit();
        editorPane = new JEditorPane();
        editorPane.setEditorKit(rtf);
        editorPane.setBackground(SystemColor.WHITE);

        FileInputStream fi = new FileInputStream(file);
        rtf.read(fi, editorPane.getDocument(), 0);

        scrollPane = new JScrollPane(editorPane);

        this.getContentPane().setLayout(new GridLayout(1, 1));
        this.getContentPane().add(scrollPane);
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
