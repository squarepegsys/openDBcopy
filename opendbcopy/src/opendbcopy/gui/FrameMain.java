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

import opendbcopy.config.APM;

import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.ProjectManager;

import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.UnsupportedAttributeValueException;

import opendbcopy.resource.ResourceManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.jdom.Element;
import org.jdom.JDOMException;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.IOException;

import java.sql.SQLException;

import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class FrameMain extends JFrame implements Observer {
    private static final ImageIcon       openIcon = new ImageIcon("resource/images/Open16.gif");
    private static final ImageIcon       saveAsIcon = new ImageIcon("resource/images/SaveAs16.gif");
    private static final ImageIcon       newIcon = new ImageIcon("resource/images/New16.gif");
    private static final ImageIcon       historyIcon = new ImageIcon("resource/images/History16.gif");
    private static Logger                logger = Logger.getLogger(FrameMain.class.getName());
    private static final Dimension       SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private Menu                         menu;
    private MainController               controller;
    private ResourceManager              rm;
    private ProjectManager               pm;
    private String                       frameTitle;
    private String                       newLine;
    private JPanel                       contentPane;
    private JPanel                       panelCurrentPluginGui;
    private TitledBorder                 titledBorderPluginGui;
    private DialogFile                   dialogFile;
    private PanelPluginChain             panelPluginChain;
    private PanelShutdownAfterCompletion panelShutdown;
    private FrameShowFile                frameExecutionLog;
    private FrameShowRTF                 frameShowUserManual;
    private int                          frameWidth;
    private int                          frameHeight;

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
        this.pm              = controller.getProjectManager();
        this.frameWidth      = frameWidth;
        this.frameHeight     = frameHeight;

        // dialogFile must be setup before menu so that actions using dialogFile have a valid reference
        this.dialogFile     = new DialogFile(this);
        this.menu           = new Menu(this, controller, pm);

        contentPane = (JPanel) this.getContentPane();

        try {
            guiInit();

            frameExecutionLog = new FrameShowFile(controller, 600, 300, controller.getExecutionLogFile(), rm.getString("menu.show.executionLog"));
            locateDialogLowerRight(frameExecutionLog);

            //frameShowUserManual = new FrameShowRTF(controller, 800, 800, new File(controller.getApplicationProperties().getProperty(APM.APPLICATION_WEBSITE_USER_MANUAL)), rm.getString("menu.help.userManual"));

            // i like to be informed about changes
            controller.getPluginGuiManager().registerObserver(this);
            controller.getProjectManager().getPluginManager().registerObserver(this);
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
        if ((controller.getProjectManager().getPluginManager().getCurrentModel() != null) && (controller.getPluginGuiManager().getCurrentPluginGui() != null)) {
            panelCurrentPluginGui.removeAll();
            panelCurrentPluginGui.add(new JScrollPane(controller.getPluginGuiManager().getCurrentPluginGui().getPanelPluginGui(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
            titledBorderPluginGui.setTitle(" " + controller.getPluginGuiManager().getCurrentPluginGui().getTitle() + " ");
            panelCurrentPluginGui.updateUI();
        } else {
            panelCurrentPluginGui.removeAll();
            titledBorderPluginGui.setTitle(" " + rm.getString("text.pluginChain.noPluginLoadedHelp") + " ");
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
        JPanel panelMain = new JPanel();
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));
        panelMain.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panelShutdown = new PanelShutdownAfterCompletion(pm, rm);
        panelShutdown.setPreferredSize(new Dimension(frameWidth, 50));
        pm.addObserver(panelShutdown);

        panelPluginChain = new PanelPluginChain(this, controller);
        panelPluginChain.setPreferredSize(new Dimension(frameWidth, 150));

        panelCurrentPluginGui = new JPanel(new GridLayout(1, 1));
        panelCurrentPluginGui.setPreferredSize(new Dimension(frameWidth, frameHeight));

        if (controller.getPluginGuiManager().getCurrentPluginGui() != null) {
            titledBorderPluginGui = new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + controller.getPluginGuiManager().getCurrentPluginGui().getTitle() + " ");
            panelCurrentPluginGui.setBorder(BorderFactory.createCompoundBorder(titledBorderPluginGui, BorderFactory.createRaisedBevelBorder()));
            panelCurrentPluginGui.add(new JScrollPane(controller.getPluginGuiManager().getCurrentPluginGui().getPanelPluginGui(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        } else {
            titledBorderPluginGui = new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.pluginChain.noPluginLoadedHelp") + " ");
            panelCurrentPluginGui.setBorder(BorderFactory.createCompoundBorder(titledBorderPluginGui, BorderFactory.createRaisedBevelBorder()));
        }

        panelMain.add(panelShutdown);
        panelMain.add(Box.createRigidArea(new Dimension(0, 10)));
        panelMain.add(panelPluginChain);
        panelMain.add(Box.createRigidArea(new Dimension(0, 10)));
        panelMain.add(panelCurrentPluginGui);

        JPanel contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(new GridLayout(1, 1));
        contentPane.add(panelMain);

        // finally
        Properties p = this.controller.getApplicationProperties();

        this.frameTitle = p.getProperty(APM.APPLICATION_NAME) + " " + p.getProperty(APM.APPLICATION_VERSION) + " - " + p.getProperty(APM.APPLICATION_COPYRIGHT);
        super.setTitle(this.frameTitle);

        this.setJMenuBar(this.menu);
        this.menu.setVisible(true);
        this.setSize(new Dimension(frameWidth, frameHeight));

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

        parent.setLocation(((SCREEN_SIZE.width - frameSize.width) / 2) + 40, (SCREEN_SIZE.height - frameSize.height) / 2);
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
     * @return Returns the historyIcon.
     */
    public final ImageIcon getHistoryIcon() {
        return historyIcon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the newIcon.
     */
    public final ImageIcon getNewIcon() {
        return newIcon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the openIcon.
     */
    public final ImageIcon getOpenIcon() {
        return openIcon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the saveAsIcon.
     */
    public final ImageIcon getSaveAsIcon() {
        return saveAsIcon;
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
    public final FrameShowRTF getFrameShowUserManual() {
        return frameShowUserManual;
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
