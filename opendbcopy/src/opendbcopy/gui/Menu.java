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

import opendbcopy.action.Actions;
import opendbcopy.action.NewPluginAction;

import opendbcopy.config.OperationType;
import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.PluginManager;
import opendbcopy.plugin.ProjectManager;

import opendbcopy.plugin.model.exception.MissingAttributeException;

import opendbcopy.resource.ResourceManager;

import org.jdom.Element;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

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
    private ProjectManager       pm;
    private PluginGuiManager     wmm;
    private ResourceManager      rm;
    private PluginManager        pluginManager;
    private Actions              actions;
    private Vector               availableWorkingModes;
    private JMenu                projectMenu;
    private JMenu                pluginMenu;
    private JMenu                showMenu;
    private JMenu                helpMenu;
    private JMenu                newPluginMenu;
    private JMenuItem            projectNewItem;
    private JMenuItem            projectExportItem;
    private JMenuItem            projectImportItem;
    private JMenuItem            pluginExportItem;
    private JMenuItem            pluginImportItem;
    private JMenuItem            showConsoleLog;
    private JMenuItem            showExecutionLog;
    private JMenuItem            helpUserManual;
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
                ProjectManager projectManager) throws MissingAttributeException {
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
        projectMenu       = new JMenu(rm.getString("menu.project"));
        pluginMenu        = new JMenu(rm.getString("menu.plugin"));
        showMenu          = new JMenu(rm.getString("menu.show"));
        helpMenu          = new JMenu(rm.getString("menu.help"));
        newPluginMenu     = new JMenu(rm.getString("menu.plugin.new"));

        /*
         * Project Menu
         */
        projectNewItem        = new JMenuItem(actions.projectNewAction);
        projectExportItem     = new JMenuItem(actions.projectExportAction);
        projectImportItem     = new JMenuItem(actions.projectImportAction);

        // menuItems which are fully implemented in here
        exitItem = new JMenuItem(rm.getString("menu.project.exit"));
        exitItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    exitItem_actionPerformed(e);
                }
            });

        // put together menu structure
        projectMenu.add(projectNewItem);
        projectMenu.addSeparator();
        projectMenu.add(projectExportItem);
        projectMenu.add(projectImportItem);
        projectMenu.addSeparator();
        projectMenu.add(exitItem);

        /*
         * Plugin Menu
         */
        pluginExportItem      = new JMenuItem(actions.pluginExportAction);
        pluginImportItem      = new JMenuItem(actions.pluginImportAction);

        // create a JMenuItem for each available plugin
        for (int i = 0; i < availableWorkingModes.size(); i++) {
            Element         wm = (Element) availableWorkingModes.get(i);
            NewPluginAction newPluginAction = new NewPluginAction(OperationType.NEW_PLUGIN, wm.getAttributeValue(XMLTags.IDENTIFIER), rm.getString(wm.getChild(XMLTags.TITLE).getAttributeValue(XMLTags.VALUE)), parentFrame.getNewIcon(), parentFrame, controller);
            newPluginMenu.add(new JMenuItem(newPluginAction));
        }

        pluginMenu.add(newPluginMenu);
        pluginMenu.addSeparator();
        pluginMenu.add(pluginExportItem);
        pluginMenu.add(pluginImportItem);

        /*
         * Show Menu
         */
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

        showMenu.add(showExecutionLog);
        showMenu.add(showConsoleLog);

        /*
         * Help Menu
         */
        helpUserManual = new JMenuItem(rm.getString("menu.help.userManual"));
        helpUserManual.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    helpUserManual_actionPerformed(e);
                }
            });

        helpMenu.add(helpUserManual);

        this.add(projectMenu);
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
     */
    void helpUserManual_actionPerformed(ActionEvent e) {
        if (parentFrame.getFrameShowUserManual() != null) {
            parentFrame.getFrameShowUserManual().show();
        }
    }
}
