/*
 * Copyright (C) 2003 Anthony Smith
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

import opendbcopy.config.APM;

import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;

import opendbcopy.controller.MainController;

import opendbcopy.model.ProjectManager;

import opendbcopy.model.exception.MissingAttributeException;
import opendbcopy.model.exception.MissingElementException;
import opendbcopy.model.exception.UnsupportedAttributeValueException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import java.io.IOException;

import java.sql.SQLException;

import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class FrameMain extends JFrame implements Observer {
    private static Logger          logger = Logger.getLogger(FrameMain.class.getName());
    public static final int        FRAME_WIDTH = 800;
    public static final int        FRAME_HEIGHT = 650;
    private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private Menu                   menu;
    private MainController         controller;
    private ProjectManager         pm;
    private WorkingModeManager     wmm;
    private PanelWorkingMode       panelWorkingMode;
    private String                 frameTitle;
    private String                 newLine;
    private JPanel                 contentPane;
    private JTextPane              statusBar = new JTextPane();
    private JScrollPane            scrollPane = null;
    private BorderLayout           borderLayout = new BorderLayout();
    private JPanel                 panelMain = new JPanel();
    private JPanel                 panelControl = new JPanel();
    private GridLayout             gridLayout = new GridLayout();
    private JButton                buttonNext = new JButton();
    private JTabbedPane            tab;
    private DialogFile             dialogFile;

    //Construct the frame
    public FrameMain(MainController controller,
                     Document       workingMode,
                     ProjectManager projectManager) {
        this.controller     = controller;
        this.pm             = projectManager;

        // dialogFile must be setup before menu so that actions using dialogFile have a valid reference
        this.dialogFile     = new DialogFile(this);
        this.menu           = new Menu(this, this.controller, projectManager);

        contentPane     = (JPanel) this.getContentPane();
        newLine         = System.getProperty("line.separator");

        try {
            wmm = new WorkingModeManager(this, this.controller, workingMode);
            guiInit();
        } catch (Exception e) {
            postException(e, Level.ERROR);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param obj DOCUMENT ME!
     */
    public final void update(Observable o,
                             Object     obj) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param operation DOCUMENT ME!
     * @param messageSuccessful DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws DriverNotFoundException DOCUMENT ME!
     * @throws OpenConnectionException DOCUMENT ME!
     * @throws CloseConnectionException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public final void execute(Element operation,
                              String  messageSuccessful) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, DriverNotFoundException, OpenConnectionException, CloseConnectionException, JDOMException, SQLException, IOException, Exception {
        try {
            controller.execute(operation);
            postMessage(messageSuccessful);
        } catch (Exception e) {
            statusBar.setForeground(Color.RED);
            statusBar.setText(e.getMessage() + "\n" + "-> see log file (opendbcopy/log) for further details.");
            logger.error(e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     */
    public final void setSelectedTabIndex(int index) {
        tab.setSelectedIndex(index);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void guiInit() throws Exception {
        JPanel contentPane = (JPanel) this.getContentPane();

        panelWorkingMode = new PanelWorkingMode(this, wmm);
        panelMain.add(panelWorkingMode);

        borderLayout.setHgap(10);
        borderLayout.setVgap(10);

        Properties p = this.controller.getApplicationProperties();

        this.frameTitle = p.getProperty(APM.APPLICATION_NAME) + " " + p.getProperty(APM.APPLICATION_VERSION) + " - " + p.getProperty(APM.APPLICATION_COPYRIGHT);
        super.setTitle(this.frameTitle);

        statusBar.setText(" ");
        statusBar.setPreferredSize(new Dimension(300, 40));
        statusBar.setBackground(null);
        scrollPane = new JScrollPane(statusBar);

        panelControl.setLayout(new BorderLayout(20, 20));
        panelMain.setLayout(gridLayout);
        gridLayout.setColumns(1);
        gridLayout.setHgap(0);
        panelMain.setBorder(BorderFactory.createEmptyBorder());
        buttonNext.setPreferredSize(new Dimension(120, 40));
        buttonNext.setText("Next >");
        buttonNext.addActionListener(new FrameMain_buttonNext_actionAdapter(this));
        panelControl.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        panelControl.setMinimumSize(new Dimension(600, 60));
        panelControl.setPreferredSize(new Dimension(600, 60));

        panelControl.add(scrollPane, BorderLayout.CENTER);
        panelControl.add(buttonNext, BorderLayout.EAST);

        contentPane.setLayout(borderLayout);
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentPane.add(panelMain, BorderLayout.CENTER);
        contentPane.add(panelControl, BorderLayout.SOUTH);

        this.setJMenuBar(this.menu);
        this.menu.setVisible(true);
        this.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));

        centerDialog(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param parent DOCUMENT ME!
     */
    public final void centerDialog(Component parent) {
        //Center the window
        Dimension frameSize = parent.getSize();

        if (frameSize.height > SCREEN_SIZE.height) {
            frameSize.height = SCREEN_SIZE.height;
        }

        if (frameSize.width > SCREEN_SIZE.width) {
            frameSize.width = SCREEN_SIZE.width;
        }

        parent.setLocation(((SCREEN_SIZE.width - frameSize.width) / 2) + 40, (SCREEN_SIZE.height - frameSize.height) / 2);
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     */
    public final void postMessage(String message) {
        statusBar.setForeground(Color.BLACK);
        statusBar.setText(message);
        logger.info(message);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     * @param level DOCUMENT ME!
     */
    public final void postException(Exception e,
                                    Level     level) {
        statusBar.setForeground(Color.RED);

        if (level.isGreaterOrEqual(Level.ERROR)) {
            logger.error(e.getMessage());
            statusBar.setText("ERROR: " + e.getMessage() + newLine + "-> see log files in log directory for further details");
        } else if (level.isGreaterOrEqual(Level.WARN)) {
            logger.warn(e.getMessage());
            statusBar.setForeground(Color.ORANGE);
            statusBar.setText("WARNING: " + e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getSelectedTabIndex() {
        return tab.getSelectedIndex();
    }

    /**
     * DOCUMENT ME!
     *
     * @param workingMode DOCUMENT ME!
     */
    public final void loadWorkingMode(String workingMode) {
        try {
            if (workingMode == null) {
                throw new Exception("Missing workingMode");
            }

            // destroy currentWorkingMode if available
            wmm.destroyCurrentWorkingMode();

            tab = wmm.loadWorkingMode(workingMode);

            // set working mode in Project Model
            pm.getProjectModel().setWorkingMode(workingMode);

            // removes panelWorkingMode and possible tab which could already have been loaded
            panelMain.removeAll();

            // add tab containing panels loaded dynamically
            panelMain.add(tab);

            panelMain.updateUI();

            if (logger.isDebugEnabled()) {
                logger.debug("index = " + tab.getSelectedIndex() + " / " + tab.getTitleAt(0));
            }
        } catch (Exception ex) {
            postException(ex, Level.ERROR);
        }
    }

    //File | Exit action performed
    public final void jMenuFileExit_actionPerformed(ActionEvent e) {
        System.exit(0);
    }

    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            jMenuFileExit_actionPerformed(null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonNext_actionPerformed(ActionEvent e) {
        // selection of working mode
        if (tab == null) {
            loadWorkingMode(panelWorkingMode.getSelectedWorkingMode());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the dialogFile.
     */
    public DialogFile getDialogFile() {
        return dialogFile;
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class FrameMain_buttonNext_actionAdapter implements java.awt.event.ActionListener {
    FrameMain adaptee;

    /**
     * Creates a new FrameMain_buttonNext_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    FrameMain_buttonNext_actionAdapter(FrameMain adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonNext_actionPerformed(e);
    }
}
