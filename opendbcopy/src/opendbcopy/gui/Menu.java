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

import com.Ostermiller.util.Browser;

import opendbcopy.action.Actions;
import opendbcopy.action.NewPluginAction;

import opendbcopy.config.APM;
import opendbcopy.config.GUI;
import opendbcopy.config.OperationType;
import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.JobManager;
import opendbcopy.plugin.PluginManager;

import opendbcopy.plugin.model.exception.MissingAttributeException;

import opendbcopy.resource.ResourceManager;

import org.jdom.Element;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import java.io.IOException;

import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class Menu extends JMenuBar {
    private FrameMain            parentFrame;
    private final MainController controller;
    private JobManager           pm;
    private PluginGuiManager     wmm;
    private ResourceManager      rm;
    private PluginManager        pluginManager;
    private Actions              actions;
    private Vector               availableWorkingModes;
    private JMenu                jobMenu;
    private JMenu                pluginMenu;
    private JMenu                showMenu;
    private JMenu                helpMenu;
    private JMenu                newPluginMenu;
    private JMenuItem            jobNewItem;
    private JMenuItem            jobExportItem;
    private JMenuItem            jobImportItem;
    private JMenuItem            pluginExportItem;
    private JMenuItem            pluginImportItem;
    private JMenuItem            showConfig;
    private JMenuItem            showConsoleLog;
    private JMenuItem            showExecutionLog;
    private JMenuItem            helpUserManual;
    private JMenuItem            helpForum;
    private JMenuItem            helpWebsite;
    private JMenuItem            helpSourceforge;
    private JMenuItem            exitItem;

    /**
     * Constructor
     *
     * @param parentFrame DOCUMENT ME!
     * @param controller DOCUMENT ME!
     * @param projectManager DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public Menu(FrameMain      parentFrame,
                MainController controller,
                JobManager     projectManager) throws MissingAttributeException {
        this.parentFrame     = parentFrame;
        this.controller      = controller;
        this.pm              = projectManager;
        this.wmm             = controller.getPluginGuiManager();
        pluginManager        = pm.getPluginManager();
        rm                   = controller.getResourceManager();

        actions     = new Actions(parentFrame, controller, projectManager);

        // initialise the list of available working modes
        availableWorkingModes     = controller.getPluginGuiManager().getAvailablePluginGuisOrdered();

        // menus
        jobMenu           = new JMenu(rm.getString("menu.job"));
        pluginMenu        = new JMenu(rm.getString("menu.plugin"));
        showMenu          = new JMenu(rm.getString("menu.show"));
        helpMenu          = new JMenu(rm.getString("menu.help"));
        newPluginMenu     = new JMenu(rm.getString("menu.plugin.new"));

        /*
         * job Menu
         */
        jobNewItem        = new JMenuItem(actions.jobNewAction);
        jobExportItem     = new JMenuItem(actions.jobExportAction);
        jobImportItem     = new JMenuItem(actions.jobImportAction);

        // menuItems which are fully implemented in here
        exitItem = new JMenuItem(rm.getString("menu.job.exit"));
        exitItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    exitItem_actionPerformed(e);
                }
            });

        // put together menu structure
        jobMenu.add(jobNewItem);
        jobMenu.addSeparator();
        jobMenu.add(jobExportItem);
        jobMenu.add(jobImportItem);
        jobMenu.addSeparator();
        jobMenu.add(exitItem);

        /*
         * Plugin Menu
         */
        pluginExportItem     = new JMenuItem(actions.pluginExportAction);
        pluginImportItem     = new JMenuItem(actions.pluginImportAction);

        // create a JMenuItem for each available plugin
        for (int i = 0; i < availableWorkingModes.size(); i++) {
            Element         wm = (Element) availableWorkingModes.get(i);
            NewPluginAction newPluginAction = new NewPluginAction(OperationType.NEW_PLUGIN, wm.getAttributeValue(XMLTags.IDENTIFIER), rm.getString(wm.getChild(XMLTags.TITLE).getAttributeValue(XMLTags.VALUE)), GUI.getNewIcon(), parentFrame, controller);
            newPluginMenu.add(new JMenuItem(newPluginAction));
        }

        pluginMenu.add(newPluginMenu);
        pluginMenu.addSeparator();
        pluginMenu.add(pluginExportItem);
        pluginMenu.add(pluginImportItem);

        /*
         * Show Menu
         */
        showConfig = new JMenuItem(rm.getString("menu.show.config"));
        showConfig.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showConfig_actionPerformed(e);
                }
            });

        showConsoleLog = new JMenuItem(rm.getString("menu.show.consoleLog"));
        showConsoleLog.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showConsoleLog_actionPerformed(e);
                }
            });

        showExecutionLog = new JMenuItem(rm.getString("menu.show.executionLog"));
        showExecutionLog.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showExecutionLog_actionPerformed(e);
                }
            });

        showMenu.add(showConfig);
        showMenu.add(showExecutionLog);
        showMenu.add(showConsoleLog);

        /*
         * Help Menu
         */
        helpUserManual = new JMenuItem(rm.getString("menu.help.userManual"));
        helpUserManual.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        helpUserManual_actionPerformed(e);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

        helpForum = new JMenuItem(rm.getString("menu.help.forum"));
        helpForum.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        helpForum_actionPerformed(e);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

        helpWebsite = new JMenuItem(rm.getString("menu.help.website"));
        helpWebsite.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        helpWebsite_actionPerformed(e);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

        helpSourceforge = new JMenuItem(rm.getString("menu.help.sourceforge"));
        helpSourceforge.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        helpSourceforge_actionPerformed(e);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

        helpMenu.add(helpUserManual);
        helpMenu.add(helpForum);
        helpMenu.add(helpWebsite);
        helpMenu.add(helpSourceforge);

        this.add(jobMenu);
        this.add(pluginMenu);
        this.add(showMenu);
        this.add(helpMenu);
    }

    /**
     * Action Listeners
     *
     * @param e DOCUMENT ME!
     */
    void this_windowClosing(WindowEvent e) {
        System.exit(0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void exitItem_actionPerformed(ActionEvent e) {
        System.exit(0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void showConfig_actionPerformed(ActionEvent e) {
        controller.getFrame().getDialogConfig().show();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void showConsoleLog_actionPerformed(ActionEvent e) {
        if (controller.getFrameConsole() != null) {
            controller.getFrameConsole().showMe();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void showExecutionLog_actionPerformed(ActionEvent e) {
        if (parentFrame.getFrameExecutionLog() != null) {
            parentFrame.getFrameExecutionLog().show();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    void helpUserManual_actionPerformed(ActionEvent e) throws IOException {
        Browser.displayURL(controller.getConfigManager().getApplicationProperty(APM.APPLICATION_USER_MANUAL));
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    void helpForum_actionPerformed(ActionEvent e) throws IOException {
        Browser.displayURL(controller.getConfigManager().getApplicationProperty(APM.APPLICATION_FORUM));
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    void helpWebsite_actionPerformed(ActionEvent e) throws IOException {
        Browser.displayURL(controller.getConfigManager().getApplicationProperty(APM.APPLICATION_WEBSITE));
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    void helpSourceforge_actionPerformed(ActionEvent e) throws IOException {
        Browser.displayURL(controller.getConfigManager().getApplicationProperty(APM.APPLICATION_SOURCEFORGE));
    }
}
