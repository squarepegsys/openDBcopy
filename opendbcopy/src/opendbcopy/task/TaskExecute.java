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
package opendbcopy.task;

import opendbcopy.config.XMLTags;

import opendbcopy.model.ProjectManager;
import opendbcopy.model.ProjectModel;

import org.apache.log4j.Logger;

import org.jdom.Document;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.reflect.Constructor;

import java.util.Observable;

import javax.swing.Timer;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class TaskExecute extends Observable {
    private static Logger  logger = Logger.getLogger(TaskExecute.class.getName());
    public final int       interrupted = -1;
    public final int       init = 0;
    public final int       started = 1;
    public final int       done = 2;
    private ProjectManager projectManager;
    private ProjectModel   projectModel;
    private int            lengthOfTaskTable;
    private int            lengthOfTaskRecord;
    private int            currentTable;
    private int            currentRecord;
    private String         statMessage;
    private int            taskStatus;
    private SwingWorker    worker = null;
    private Timer          timer;

    /**
     * Creates a new TaskExecute object.
     *
     * @param projectManager DOCUMENT ME!
     */
    public TaskExecute(ProjectManager projectManager) {
        this.projectManager     = projectManager;
        this.projectModel       = projectManager.getProjectModel();

        lengthOfTaskTable      = 0;
        lengthOfTaskRecord     = 0;
        taskStatus             = init;
    }

    /**
     * DOCUMENT ME!
     */
    private void broadcast() {
        setChanged();
        notifyObservers();
    }

    /**
     * Decide which Execute class to use
     *
     * @throws Exception DOCUMENT ME!
     */
    public final void go() throws Exception {
        currentTable      = 0;
        currentRecord     = 0;

        worker = new SwingWorker() {
                    public Object construct() {
                        // plugin
                        try {
                            Class         dynClass = Class.forName(projectModel.getPlugin().getChild(XMLTags.CLASS).getAttributeValue(XMLTags.NAME));

                            Constructor[] constructors = dynClass.getConstructors();

                            Object[]      params = new Object[2];
                            params[0]     = TaskExecute.this;
                            params[1]     = projectModel;

                            // works as long there is only one constructor
                            return constructors[0].newInstance(params);
                        } catch (Exception e) {
                            logger.error(e);

                            return null;
                        }
                    }
                };

        try {
            worker.start();
            taskStatus = started;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final Document getTypeMapping() {
        return projectManager.getTypeMapping();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getLengthOfTaskTable() {
        return lengthOfTaskTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getLengthOfTaskRecord() {
        return lengthOfTaskRecord;
    }

    /**
     * DOCUMENT ME!
     *
     * @param lengthOfTaskTable DOCUMENT ME!
     */
    public final void setLengthOfTaskTable(int lengthOfTaskTable) {
        this.lengthOfTaskTable     = lengthOfTaskTable;
        statMessage                = lengthOfTaskTable + " tables to process";
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @param lengthOfTaskRecord DOCUMENT ME!
     */
    public final void setLengthOfTaskRecord(int lengthOfTaskRecord) {
        this.lengthOfTaskRecord     = lengthOfTaskRecord;
        statMessage                 = lengthOfTaskRecord + " records to process";
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getCurrentTable() {
        return currentTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getCurrentRecord() {
        return currentRecord;
    }

    /**
     * DOCUMENT ME!
     *
     * @param currentTable DOCUMENT ME!
     */
    public final void setCurrentTable(int currentTable) {
        this.currentTable = currentTable;
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @param currentRecord DOCUMENT ME!
     */
    public final void setCurrentRecord(int currentRecord) {
        this.currentRecord = currentRecord;
        broadcast();
    }

    /**
     * DOCUMENT ME!
     */
    public final void interrupt() {
        worker.interrupt();
        taskStatus = interrupted;
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final boolean isInterrupted() {
        return worker.isInterrupted();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getTaskStatus() {
        return taskStatus;
    }

    /**
     * DOCUMENT ME!
     *
     * @param status DOCUMENT ME!
     */
    public final void setTaskStatus(int status) {
        taskStatus = status;
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @param millis DOCUMENT ME!
     */
    public final void have_a_break_and_relaunch_process(int millis) {
        timer = new Timer(millis, new TimerListener());
        timer.start();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final String getMessage() {
        return statMessage;
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     */
    public final void setMessage(String message) {
        this.statMessage = message;
        broadcast();
    }

    /**
     * class description
     *
     * @author smi
     * @version $Revision$
     */
    class TimerListener implements ActionListener {
        /**
         * DOCUMENT ME!
         *
         * @param evt DOCUMENT ME!
         */
        public final void actionPerformed(ActionEvent evt) {
            timer.stop();

            try {
                go();
                logger.info("process relaunched. continuing ...");
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }
    }
}
