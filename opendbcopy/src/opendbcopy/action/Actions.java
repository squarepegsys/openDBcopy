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
package opendbcopy.action;

import java.util.Observable;
import java.util.Observer;

import opendbcopy.config.FileType;
import opendbcopy.config.OperationType;
import opendbcopy.controller.MainController;
import opendbcopy.gui.FrameMain;
import opendbcopy.gui.WorkingModeManager;
import opendbcopy.plugin.ProjectManager;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class Actions implements Observer {
    private FrameMain          parentFrame;
    private MainController     controller;
    private WorkingModeManager wmm;
    private ProjectManager     pm;

    // project actions
    public SimpleAction   projectNewAction;
    public SaveFileAction projectExportAction;
    public OpenFileAction projectImportAction;

    // plugin actions
    public SaveFileAction pluginExportAction;
    public OpenFileAction pluginImportAction;
    public SimpleAction   pluginExecuteAction;

    /**
     * Constructor
     *
     * @param parentFrame DOCUMENT ME!
     * @param controller DOCUMENT ME!
     * @param projectManager DOCUMENT ME!
     */
    public Actions(FrameMain      parentFrame,
                   MainController controller,
                   ProjectManager projectManager) {
        this.parentFrame     = parentFrame;
        this.controller      = controller;
        this.wmm             = controller.getWorkingModeManager();
        this.pm              = projectManager;

        // register itself as observer
        controller.registerObserver(this);
        pm.getPluginManager().registerObserver(this);
        wmm.registerObserver(this);

        // project actions
        projectNewAction        = new SimpleAction(OperationType.NEW_PROJECT, controller.getResourceManager().getString("menu.project.new"), parentFrame.getNewIcon(), parentFrame, controller);
        projectExportAction     = new SaveFileAction(OperationType.EXPORT_PROJECT, controller.getResourceManager().getString("menu.project.export"), parentFrame.getSaveAsIcon(), FileType.XML_FILE, controller.getPersonalProjectsDir(), parentFrame, controller);
        projectImportAction     = new OpenFileAction(OperationType.IMPORT_PROJECT, controller.getResourceManager().getString("menu.project.import"), parentFrame.getOpenIcon(), FileType.XML_FILE, controller.getPersonalProjectsDir(), parentFrame, controller);

        // plugin actions
        pluginExportAction      = new SaveFileAction(OperationType.EXPORT_PLUGIN, controller.getResourceManager().getString("menu.plugin.export"), parentFrame.getSaveAsIcon(), FileType.XML_FILE, controller.getPersonalPluginsDir(), parentFrame, controller);
        pluginImportAction      = new OpenFileAction(OperationType.IMPORT_PLUGIN, controller.getResourceManager().getString("menu.plugin.import"), parentFrame.getOpenIcon(), FileType.XML_FILE, controller.getPersonalPluginsDir(), parentFrame, controller);
        pluginExecuteAction     = new SimpleAction(OperationType.EXECUTE, controller.getResourceManager().getString("menu.plugin.executePluginChain"), null, parentFrame, controller);

        // enable / disable actions
        projectNewAction.setEnabled(true);
        projectExportAction.setEnabled(true);
        projectImportAction.setEnabled(true);

        pluginImportAction.setEnabled(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param obj DOCUMENT ME!
     */
    public void update(Observable o,
                       Object     obj) {
        if (wmm.getCurrentWorkingMode() != null) {
            pluginExportAction.setEnabled(true);
        } else {
            pluginExportAction.setEnabled(false);
        }

        if (pm.getPluginManager().getNbrPluginsToExecute() > 0) {
            pluginExecuteAction.setEnabled(true);
        } else {
            pluginExecuteAction.setEnabled(false);
        }
    }
}
