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

import java.awt.BorderLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import opendbcopy.controller.MainController;
import opendbcopy.controller.TaskLauncher;
import opendbcopy.io.FileHandling;
import opendbcopy.io.Reader;
import opendbcopy.resource.ResourceManager;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class FrameConsole extends JFrame implements Observer {
    private final static int TIMER_SLOT = 100;
    private FrameConsole     frameConsole;
    private TaskLauncher     task;
    private MainController   controller;
    private ResourceManager  rm;
    private Timer            timer;
    private JPanel           panelMain;
    private JPanel           panelInfo;
    private JProgressBar     progressBar;
    private JScrollPane      scrollPaneConsoleOut;
    private JTextArea        textAreaConsoleOutLog;
    private long             lastModifiedConsoleOut;
    private long             lengthConsoleOut;
    private File             consoleOut;
    private String           pathFilenameLogo;

    /**
     * Creates a new FrameLaunchingProgress object.
     *
     * @param task DOCUMENT ME!
     * @param controller DOCUMENT ME!
     * @param frameConsoleWidth DOCUMENT ME!
     * @param frameConsoleHeight DOCUMENT ME!
     * @param pathFilenameConsoleOut DOCUMENT ME!
     * @param pathFilenameLogo DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public FrameConsole(TaskLauncher   task,
                        MainController controller,
                        Integer        frameConsoleWidth,
                        Integer        frameConsoleHeight,
                        String         pathFilenameConsoleOut,
                        String         pathFilenameLogo) throws Exception {
        this.frameConsole         = this;
        this.task                 = task;
        this.controller           = controller;
        this.rm                   = controller.getResourceManager();
        this.pathFilenameLogo     = pathFilenameLogo;
        this.setSize(frameConsoleWidth.intValue(), frameConsoleHeight.intValue());

        consoleOut                 = FileHandling.getFile(pathFilenameConsoleOut);
        lastModifiedConsoleOut     = 0;
        lengthConsoleOut           = 0;
        guiInit();
        task.registerObserver(this);
        updateProgressBar();

        // register me at the controller
        controller.setFrameConsole(this);
    }

    /**
     * DOCUMENT ME!
     */
    private void updateProgressBar() {
        if (progressBar.getMaximum() == task.getLengthOfTask()) {
            if (task.getCurrentTask() == task.getLengthOfTask()) {
                progressBar.setValue(task.getCurrentTask());
                progressBar.setString(rm.getString("text.controller.console.done"));
            } else {
                progressBar.setValue(task.getCurrentTask());
                progressBar.setString(task.getMessage());
            }
        } else {
            progressBar.setMaximum(task.getLengthOfTask());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param obj DOCUMENT ME!
     */
    public void update(Observable o,
                       Object     obj) {
        updateProgressBar();
    }

    /**
     * DOCUMENT ME!
     */
    public void showMe() {
        this.show();

        if (timer != null) {
            timer.restart();
        } else {
            // initialize timer for updating execution log
            timer = new Timer(TIMER_SLOT, new TimerUpdateListener());
            timer.start();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void hideMe() {
        this.hide();

        timer.stop();

        // only shutdown opendbcopy if FrameMain was not able to be initialised
        if (controller.getFrame() == null) {
            System.exit(0);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void guiInit() throws Exception {
        this.setTitle(rm.getString("title.controller.console"));
        panelMain = new JPanel(new BorderLayout(20, 20));
        panelMain.setBackground(SystemColor.WHITE);

        // panel info
        panelInfo = new JPanel(new BorderLayout(20, 20));
        panelInfo.setBackground(SystemColor.WHITE);

        ImageIcon icon = new ImageIcon(pathFilenameLogo);

        panelInfo.add(new JLabel(icon), BorderLayout.CENTER);

        progressBar = new JProgressBar(0, task.getLengthOfTask());
        progressBar.setStringPainted(true);

        panelInfo.add(progressBar, BorderLayout.SOUTH);

        textAreaConsoleOutLog = new JTextArea();
        textAreaConsoleOutLog.setLineWrap(false);
        textAreaConsoleOutLog.setText(Reader.read(consoleOut).toString());

        scrollPaneConsoleOut = new JScrollPane(textAreaConsoleOutLog);
        scrollPaneConsoleOut.setBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.black, 1), " " + rm.getString("text.controller.console.log") + " (" + consoleOut.getAbsolutePath() + ") "));
        scrollPaneConsoleOut.setBackground(SystemColor.WHITE);

        panelMain.setBorder(new EmptyBorder(5, 5, 5, 5));
        panelMain.add(panelInfo, BorderLayout.NORTH);
        panelMain.add(scrollPaneConsoleOut, BorderLayout.CENTER);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(panelMain, BorderLayout.CENTER);

        showMe();
    }

    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            hideMe();
        }
    }

    /**
     * class description
     *
     * @author Anthony Smith
     * @version $Revision$
     */
    class TimerUpdateListener implements ActionListener {
        /**
         * DOCUMENT ME!
         *
         * @param evt DOCUMENT ME!
         */
        public void actionPerformed(ActionEvent evt) {
            // read log file and show in log pane
            try {
                if ((lastModifiedConsoleOut < consoleOut.lastModified()) || (lengthConsoleOut < consoleOut.length())) {
                    lastModifiedConsoleOut     = consoleOut.lastModified();
                    lengthConsoleOut           = consoleOut.length();
                    textAreaConsoleOutLog.setText(Reader.read(consoleOut).toString());
                    textAreaConsoleOutLog.setBackground(SystemColor.WHITE);
                }
            } catch (IOException e) {
                // do nothing as it is possible that user deleted log or whatever ...
            }
        }
    }
}
