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

import opendbcopy.io.Reader;

import opendbcopy.resource.ResourceManager;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class FrameShowFile extends JFrame {
    private MainController  controller;
    private ResourceManager rm;
    private JPanel          panelMain;
    private JPanel          panelControl;
    private JButton         buttonRefresh;
    private JScrollPane     scrollPane;
    private JTextArea       textArea;
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
    public FrameShowFile(MainController controller,
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
     */
    public final void refreshFile() {
        if ((file != null) && file.exists()) {
            if (textArea != null) {
                try {
                    textArea.setText(Reader.read(file).toString());
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void guiInit() throws Exception {
        this.setTitle(title);

        textArea = new JTextArea();
        textArea.setLineWrap(false);

        String content = null;

        try {
            content = Reader.read(file).toString();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        if (content != null) {
            textArea.setText(content);
        } else {
            textArea.setText("");
        }

        scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.black, 1), " " + title + " (" + file.getAbsolutePath() + ") "));
        scrollPane.setBackground(SystemColor.WHITE);

        buttonRefresh = new JButton(" " + rm.getString("button.refresh"), new ImageIcon("resource/images/Refresh24.gif"));
        buttonRefresh.addActionListener(new FrameShowFile_buttonRefresh_actionAdapter(this));

        panelControl = new JPanel(new GridLayout(1, 1, 20, 20));
        panelControl.add(buttonRefresh);

        panelMain = new JPanel(new BorderLayout(20, 20));
        panelMain.setBackground(SystemColor.WHITE);
        panelMain.setBorder(new EmptyBorder(5, 5, 5, 5));

        panelMain.add(panelControl, BorderLayout.NORTH);
        panelMain.add(scrollPane, BorderLayout.CENTER);

        this.getContentPane().add(panelMain);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonRefresh_actionPerformed(ActionEvent e) {
        try {
            textArea.setText(Reader.read(file).toString());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
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


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class FrameShowFile_buttonRefresh_actionAdapter implements java.awt.event.ActionListener {
    FrameShowFile adaptee;

    /**
     * Creates a new WorkingMode_buttonNext_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    FrameShowFile_buttonRefresh_actionAdapter(FrameShowFile adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonRefresh_actionPerformed(e);
    }
}
