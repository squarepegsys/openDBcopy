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

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import opendbcopy.config.APM;
import opendbcopy.config.GUI;
import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;
import opendbcopy.controller.MainController;
import opendbcopy.plugin.JobManager;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.UnsupportedAttributeValueException;
import opendbcopy.resource.ResourceManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class FrameMain extends JFrame implements Observer {
    private static Logger          logger = Logger.getLogger(FrameMain.class.getName());
    private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private Menu                   menu;
    private MainController         controller;
    private ResourceManager        rm;
    private JobManager             jm;
    private String                 frameTitle;
    private String                 newLine;
    private JPanel                 contentPane;
    private JPanel                 panelCurrentPluginGui;
    private TitledBorder           titledBorderPluginGui;
    private DialogFile             dialogFile;
    private DialogConfig           dialogConfig;
    private PanelPluginChain       panelPluginChain;
    private FrameShowFile          frameExecutionLog;
    private FrameShowURL           frameShowUserManual;
    private int                    frameWidth;
    private int                    frameHeight;

    /**
     * Creates a new FrameMain object.
     *
     * @param controller DOCUMENT ME!
     * @param frameWidth DOCUMENT ME!
     * @param frameHeight DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public FrameMain(MainController controller,
                     int            frameWidth,
                     int            frameHeight) throws MissingAttributeException {
        this.controller      = controller;
        this.rm              = controller.getResourceManager();
        this.jm              = controller.getJobManager();
        this.frameWidth      = frameWidth;
        this.frameHeight     = frameHeight;

        // dialogFile must be setup before menu so that actions using dialogFile have a valid reference
        this.dialogFile     = new DialogFile(this);
        this.menu           = new Menu(this, controller, jm);

        try {
            guiInit();

            frameExecutionLog = new FrameShowFile(controller, 600, 300, controller.getExecutionLogFile(), rm.getString("menu.show.executionLog"));
            locateDialogLowerRight(frameExecutionLog);

            // i like to be informed about changes
            controller.getPluginGuiManager().registerObserver(this);
            controller.getJobManager().getPluginManager().registerObserver(this);
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
        // update plugin gui shown
        if ((controller.getJobManager().getPluginManager().getCurrentModel() != null) && (controller.getPluginGuiManager().getCurrentPluginGui() != null)) {
            panelCurrentPluginGui.removeAll();
            panelCurrentPluginGui.add(controller.getPluginGuiManager().getCurrentPluginGui().getPanelPluginGui());
            titledBorderPluginGui.setTitle(" " + controller.getPluginGuiManager().getCurrentPluginGui().getTitle() + " ");
            panelCurrentPluginGui.updateUI();
        } else {
            panelCurrentPluginGui.removeAll();
            titledBorderPluginGui.setTitle(" " + rm.getString("text.pluginChain.noPluginLoadedHelp") + " ");
            panelCurrentPluginGui.updateUI();
        }
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
            logger.info(messageSuccessful);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Oooooops!", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     */
    public final void postMessage(String message) {
        logger.info(message);
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     * @param level DOCUMENT ME!
     */
    public final void postException(Exception e,
                                    Level     level) {
        e.printStackTrace();

        if (level.isGreaterOrEqual(Level.ERROR)) {
            logger.error(e);
            JOptionPane.showMessageDialog(this, e.getMessage(), "Oooooops!", JOptionPane.ERROR_MESSAGE);
        } else if (level.isGreaterOrEqual(Level.WARN)) {
            logger.warn(e);
            JOptionPane.showMessageDialog(this, e.getMessage(), "Oooooops!", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void guiInit() throws Exception {
        double[][] size = {
                              { GUI.B, GUI.F, GUI.B }, // Columns
        { GUI.B, GUI.P, GUI.VS, GUI.F, GUI.B }
        }; // Rows

        TableLayout layout = new TableLayout(size);

        contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(layout);

        panelPluginChain     = new PanelPluginChain(this, controller);

        panelCurrentPluginGui = new JPanel(new GridLayout(1, 1));

        if (controller.getPluginGuiManager().getCurrentPluginGui() != null) {
            panelCurrentPluginGui.add(controller.getPluginGuiManager().getCurrentPluginGui().getPanelPluginGui());
        } else {
            titledBorderPluginGui = new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.pluginChain.noPluginLoadedHelp") + " ");
            panelCurrentPluginGui.setBorder(BorderFactory.createCompoundBorder(titledBorderPluginGui, BorderFactory.createRaisedBevelBorder()));
        }

        Properties p = this.controller.getApplicationProperties();

        this.frameTitle = p.getProperty(APM.APPLICATION_NAME) + " " + p.getProperty(APM.APPLICATION_VERSION) + " - " + p.getProperty(APM.APPLICATION_COPYRIGHT);
        super.setTitle(this.frameTitle);

        this.setJMenuBar(this.menu);
        this.menu.setVisible(true);
        this.setSize(new Dimension(frameWidth, frameHeight));

        // first line
        contentPane.add(panelPluginChain, "1, 1");

        // third line
        contentPane.add(panelCurrentPluginGui, "1, 3");

        locateDialogCentreScreen(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param parent DOCUMENT ME!
     */
    public final void locateDialogCentreScreen(Component parent) {
        //Center the window
        Dimension frameSize = parent.getSize();

        if (frameSize.height > SCREEN_SIZE.height) {
            frameSize.height = SCREEN_SIZE.height;
        }

        if (frameSize.width > SCREEN_SIZE.width) {
            frameSize.width = SCREEN_SIZE.width;
        }

        parent.setLocation(((SCREEN_SIZE.width - frameSize.width) / 2), (SCREEN_SIZE.height - frameSize.height) / 2);
    }

    /**
     * DOCUMENT ME!
     *
     * @param parent DOCUMENT ME!
     */
    public final void locateDialogUpperRight(Component parent) {
        Dimension frameSize = parent.getSize();

        if (frameSize.height > SCREEN_SIZE.height) {
            frameSize.height = SCREEN_SIZE.height;
        }

        if (frameSize.width > SCREEN_SIZE.width) {
            frameSize.width = SCREEN_SIZE.width;
        }

        parent.setLocation(SCREEN_SIZE.width - frameSize.width, 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param parent DOCUMENT ME!
     */
    public final void locateDialogLowerRight(Component parent) {
        Dimension frameSize = parent.getSize();

        if (frameSize.height > SCREEN_SIZE.height) {
            frameSize.height = SCREEN_SIZE.height;
        }

        if (frameSize.width > SCREEN_SIZE.width) {
            frameSize.width = SCREEN_SIZE.width;
        }

        parent.setLocation(SCREEN_SIZE.width - frameSize.width, SCREEN_SIZE.height - frameSize.height);
    }

    // Project | Exit action performed
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
    void tab_mouseClicked(MouseEvent e) {
        //controller.getWorkingModeManager().setSelectedTabIndex(tabPluginModel.getSelectedIndex());
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the dialogFile.
     */
    public DialogFile getDialogFile() {
        return dialogFile;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the panelPluginChain.
     */
    public final PanelPluginChain getPanelPluginChain() {
        return panelPluginChain;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the frameExecutionLog.
     */
    public final FrameShowFile getFrameExecutionLog() {
        return frameExecutionLog;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the frameShowUserManual.
     */

    //    public final FrameShowRTF getFrameShowUserManual() {
    //        return frameShowUserManual;
    //    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the dialogConfig.
     */
    public DialogConfig getDialogConfig() {
        if (dialogConfig == null) {
            dialogConfig = new DialogConfig(this, controller.getConfigManager(), rm, rm.getString("text.config.title"), true);
            dialogConfig.pack();
            locateDialogCentreScreen(dialogConfig);
        }

        return dialogConfig;
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class FrameMain_tab_mouseAdapter extends java.awt.event.MouseAdapter {
    FrameMain adaptee;

    /**
     * Creates a new FrameMain_tab_mouseAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    FrameMain_tab_mouseAdapter(FrameMain adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void mouseClicked(MouseEvent e) {
        adaptee.tab_mouseClicked(e);
    }
}
