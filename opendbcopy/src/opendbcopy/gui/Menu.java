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

import opendbcopy.action.Actions;

import opendbcopy.config.MenuName;

import opendbcopy.controller.MainController;

import opendbcopy.model.ProjectManager;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class Menu extends JMenuBar implements Observer {
    private static Logger  logger = Logger.getLogger(Menu.class.getName());
    private FrameMain      parentFrame;
    private MainController controller;
    private ProjectManager projectManager;
    private Actions        actions;
    private JMenu          projectMenu = new JMenu();
    private JMenuItem      new_Item;
    private JMenuItem      export_Project_As_XML_File_Item;
    private JMenuItem      import_Project_As_XML_File_Item;
    private JMenuItem      exitItem = new JMenuItem();

    /**
     * Constructor
     *
     * @param parentFrame DOCUMENT ME!
     * @param controller DOCUMENT ME!
     * @param projectManager DOCUMENT ME!
     */
    public Menu(FrameMain      parentFrame,
                MainController controller,
                ProjectManager projectManager) {
        this.parentFrame        = parentFrame;
        this.controller         = controller;
        this.projectManager     = projectManager;
        this.actions            = new Actions(this.parentFrame, this.controller, this.projectManager);

        // register itself as observer
        this.controller.registerObserver(this);

        /**
         * initialise Menu Items with their individual Action
         */
        this.new_Item     = new JMenuItem(this.actions.project_NewAction);

        this.export_Project_As_XML_File_Item     = new JMenuItem(this.actions.project_ExportProjectAsXMLAction);

        this.import_Project_As_XML_File_Item = new JMenuItem(this.actions.project_ImportProjectAsXMLAction);

        projectMenu.setText(MenuName.PROJECT);

        exitItem.setText(MenuName.EXIT);
        exitItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    exitItem_actionPerformed(e);
                }
            });

        this.add(projectMenu);

        projectMenu.add(this.new_Item);
        projectMenu.addSeparator();

        projectMenu.add(this.export_Project_As_XML_File_Item);
        projectMenu.add(this.import_Project_As_XML_File_Item);

        projectMenu.addSeparator();
        projectMenu.add(exitItem);

        // enable / disable menu
        this.actions.project_NewAction.setEnabled(true);
        this.actions.project_ExportProjectAsXMLAction.setEnabled(true);
        this.actions.project_ImportProjectAsXMLAction.setEnabled(true);
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
}
