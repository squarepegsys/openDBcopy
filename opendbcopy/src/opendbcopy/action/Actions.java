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

import opendbcopy.config.FileType;
import opendbcopy.config.GUI;
import opendbcopy.config.OperationType;

import opendbcopy.controller.MainController;

import opendbcopy.gui.FrameMain;
import opendbcopy.gui.PluginGuiManager;

import opendbcopy.plugin.JobManager;

import java.util.Observable;
import java.util.Observer;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class Actions implements Observer {
    private FrameMain        parentFrame;
    private MainController   controller;
    private PluginGuiManager wmm;
    private JobManager       pm;

    // project actions
    public SimpleAction   jobNewAction;
    public SaveFileAction jobExportAction;
    public OpenFileAction jobImportAction;

    // plugin actions
    public SaveFileAction pluginExportAction;
    public OpenFileAction pluginImportAction;

    /**
     * Constructor
     *
     * @param parentFrame DOCUMENT ME!
     * @param controller DOCUMENT ME!
     * @param projectManager DOCUMENT ME!
     */
    public Actions(FrameMain      parentFrame,
                   MainController controller,
                   JobManager     projectManager) {
        this.parentFrame     = parentFrame;
        this.controller      = controller;
        this.wmm             = controller.getPluginGuiManager();
        this.pm              = projectManager;

        // register itself as observer
        controller.registerObserver(this);
        pm.getPluginManager().registerObserver(this);
        wmm.registerObserver(this);

        // job actions
        jobNewAction        = new SimpleAction(OperationType.NEW_JOB, controller.getResourceManager().getString("menu.job.new"), GUI.getNewIcon(), parentFrame, controller);
        jobExportAction     = new SaveFileAction(OperationType.EXPORT_JOB, controller.getResourceManager().getString("menu.job.export"), GUI.getSaveAsIcon(), FileType.XML_FILE, controller.getPersonalJobsDir(), parentFrame, controller);
        jobImportAction     = new OpenFileAction(OperationType.IMPORT_JOB, controller.getResourceManager().getString("menu.job.import"), GUI.getOpenIcon(), FileType.XML_FILE, controller.getPersonalJobsDir(), parentFrame, controller);

        // plugin actions
        pluginExportAction     = new SaveFileAction(OperationType.EXPORT_PLUGIN, controller.getResourceManager().getString("menu.plugin.export"), GUI.getSaveAsIcon(), FileType.XML_FILE, controller.getPersonalPluginsDir(), parentFrame, controller);
        pluginImportAction     = new OpenFileAction(OperationType.IMPORT_PLUGIN, controller.getResourceManager().getString("menu.plugin.import"), GUI.getOpenIcon(), FileType.XML_FILE, controller.getPersonalPluginsDir(), parentFrame, controller);

        // enable / disable actions
        jobNewAction.setEnabled(true);
        jobImportAction.setEnabled(true);
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
        if (wmm.getNbrPluginGuisLoaded() > 0) {
            pluginExportAction.setEnabled(true);
        } else {
            pluginExportAction.setEnabled(false);
        }

        if (wmm.getNbrPluginGuisToExecute() > 0) {
            jobExportAction.setEnabled(true);
        } else {
            jobExportAction.setEnabled(false);
        }
    }
}
