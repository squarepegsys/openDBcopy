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
package opendbcopy.controller;

import opendbcopy.swing.SwingWorker;

import java.lang.reflect.Constructor;

import java.util.Observable;
import java.util.Observer;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class TaskLauncher extends Observable {
    private MainController controller;
    private int            lengthOfTask;
    private int            currentTask;
    private String         statMessage;
    private SwingWorker    worker = null;
    private Integer        frameConsoleWidth;
    private Integer        frameConsoleHeight;
    private String         pathFilenameConsoleOut;
    private String         pathFilenameLogo;

    /**
     * Creates a new TaskLauncher object.
     *
     * @param controller DOCUMENT ME!
     * @param lengthOfTask DOCUMENT ME!
     * @param frameConsoleWidth DOCUMENT ME!
     * @param frameConsoleHeight DOCUMENT ME!
     * @param pathFilenameConsoleOut DOCUMENT ME!
     * @param pathFilenameLogo DOCUMENT ME!
     */
    public TaskLauncher(MainController controller,
                        int            lengthOfTask,
                        int            frameConsoleWidth,
                        int            frameConsoleHeight,
                        String         pathFilenameConsoleOut,
                        String         pathFilenameLogo) {
        this.controller                 = controller;
        this.lengthOfTask               = lengthOfTask;
        this.frameConsoleWidth          = new Integer(frameConsoleWidth);
        this.frameConsoleHeight         = new Integer(frameConsoleHeight);
        this.pathFilenameConsoleOut     = pathFilenameConsoleOut;
        this.pathFilenameLogo           = pathFilenameLogo;
        currentTask                     = 0;
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
        worker = new SwingWorker() {
                    public Object construct() {
                        try {
                            Class         dynClass = Class.forName("opendbcopy.gui.FrameConsole");

                            Constructor[] constructors = dynClass.getConstructors();

                            Object[]      params = new Object[6];
                            params[0]     = TaskLauncher.this;
                            params[1]     = controller;
                            params[2]     = frameConsoleWidth;
                            params[3]     = frameConsoleHeight;
                            params[4]     = pathFilenameConsoleOut;
                            params[5]     = pathFilenameLogo;

                            // works as long there is only one constructor
                            return constructors[0].newInstance(params);
                        } catch (Exception e) {
                            System.err.println(e.fillInStackTrace());

                            return null;
                        }
                    }
                };

        try {
            worker.start();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param observer DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void registerObserver(Observer observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Missing observer");
        }

        this.addObserver(observer);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getLengthOfTask() {
        return lengthOfTask;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getCurrentTask() {
        return currentTask;
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
     * Sets the message and increments current task
     *
     * @param message DOCUMENT ME!
     */
    public final void setMessage(String message) {
        this.statMessage = message;
        this.currentTask++;
        broadcast();
    }
}
