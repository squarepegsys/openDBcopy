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
package opendbcopy.plugin;

import opendbcopy.controller.MainController;
import opendbcopy.model.ProjectModel;
import opendbcopy.plugin.exception.PluginException;

import org.apache.log4j.Level;
import org.jdom.Document;


/**
 * Individual plugins shall extend DynamicPluginThread. Plugin instances are launched, monitored and eventually synchronised using a
 * PluginThreadManager.
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class DynamicPluginThread extends Thread {
    protected MainController controller;
    protected ProjectModel   projectModel;
    private Document         typeMapping;
    private PluginMetadata   plugin;
    private boolean          paused = false;
    private boolean          interrupted = false;
    private boolean          done = false;
    private boolean          progressTableEnabled = false;
    private boolean          progressRecordEnabled = false;
    private int              lengthProgressTable = 0;
    private int              lengthProgressRecord = 0;
    private int              currentProgressTable = 0;
    private int              currentProgressRecord = 0;
    private String           description;

    /**
     * Creates a new DynamicPlugin object.
     *
     * @param controller DOCUMENT ME!
     * @param plugin DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public DynamicPluginThread(MainController controller,
                               PluginMetadata plugin) throws PluginException {
        this.controller       = controller;
        this.plugin           = plugin;
        this.projectModel     = controller.getProjectManager().getProjectModel();
        this.typeMapping      = controller.getProjectManager().getTypeMapping();

        setUp();
    }

    /**
     * Implement this method in your subclass to do whatever action is required before  the thread is started. setUp() is then automatically called
     * after using the  constructor super().
     *
     * @throws PluginException DOCUMENT ME!
     */
    protected void setUp() throws PluginException {
    }

    /**
     * A subclass must implement the run() method to do whatever required
     */
    public void run() {
        postException(new Exception("run() method not implemented by plugin!"));
    }

    /**
     * Use this method to post an exception and an appropriate Log Level (log4j) to the next higher level
     *
     * @param e DOCUMENT ME!
     * @param level DOCUMENT ME!
     */
    protected final void postException(Exception e,
                                       Level     level) {
        controller.postException(e, level);
    }

    /**
     * Use this method to post an exception to the next higher level
     *
     * @param e DOCUMENT ME!
     */
    protected final void postException(Exception e) {
        controller.postException(e, Level.ERROR);
    }

    /**
     * Use this method to a post a message
     *
     * @param message DOCUMENT ME!
     */
    protected final void postMessage(String message) {
        controller.postMessage(message);
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the currentProgressColumn.
     */
    public int getCurrentProgressRecord() {
        return currentProgressRecord;
    }

    /**
     * DOCUMENT ME!
     *
     * @param currentProgressRecord The currentProgressRecord to set.
     */
    protected void setCurrentProgressRecord(int currentProgressRecord) {
        this.currentProgressRecord = currentProgressRecord;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the currentProgressTable.
     */
    public int getCurrentProgressTable() {
        return currentProgressTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param currentProgressTable The currentProgressTable to set.
     */
    protected void setCurrentProgressTable(int currentProgressTable) {
        this.currentProgressTable = currentProgressTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the lengthProgressRecord.
     */
    public int getLengthProgressRecord() {
        return lengthProgressRecord;
    }

    /**
     * Set the total number of records to process. If progress tracking is not yet initialised, it is automatically enabled
     *
     * @param lengthProgressRecord The lengthProgressRecord to set.
     */
    protected void setLengthProgressRecord(int lengthProgressRecord) {
        if (!isProgressRecordEnabled()) {
            setProgressRecordEnabled(true);
        }

        this.lengthProgressRecord = lengthProgressRecord;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the lengthProgressTable.
     */
    public int getLengthProgressTable() {
        return lengthProgressTable;
    }

    /**
     * Set the total number of tables / views to process. If progress tracking is not yet initialised, it is automatically enabled
     *
     * @param lengthProgressTable The lengthProgressTable to set.
     */
    protected void setLengthProgressTable(int lengthProgressTable) {
        if (!isProgressTableEnabled()) {
            setProgressTableEnabled(true);
        }

        this.lengthProgressTable = lengthProgressTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the progressRecordEnabled.
     */
    public boolean isProgressRecordEnabled() {
        return progressRecordEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param progressRecordEnabled The progressRecordEnabled to set.
     */
    protected void setProgressRecordEnabled(boolean progressRecordEnabled) {
        this.progressRecordEnabled = progressRecordEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the progressTableEnabled.
     */
    public boolean isProgressTableEnabled() {
        return progressTableEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param progressTableEnabled The progressTableEnabled to set.
     */
    protected void setProgressTableEnabled(boolean progressTableEnabled) {
        this.progressTableEnabled = progressTableEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the done.
     */
    public boolean isDone() {
        return done;
    }

    /**
     * DOCUMENT ME!
     *
     * @param done The done to set.
     */
    protected void setDone(boolean done) {
        this.done = done;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the interrupted.
     */
    public boolean isInterrupted() {
        return interrupted;
    }

    /**
     * DOCUMENT ME!
     *
     * @param interrupted The interrupted to set.
     */
    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the paused.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * DOCUMENT ME!
     *
     * @param paused The paused to set.
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the typeMapping.
     */
    public Document getTypeMapping() {
        return typeMapping;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the plugin.
     */
    public PluginMetadata getPlugin() {
        return plugin;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the title.
     */
    public String getTitle() {
        return description;
    }
}
