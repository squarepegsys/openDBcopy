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
package opendbcopy.plugin.model;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.PluginException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.jdom.Document;


/**
 * Individual plugins shall extend DynamicPluginThread. Plugin instances are launched, monitored and eventually synchronised using a
 * PluginThreadManager.
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class DynamicPluginThread extends Thread implements IExecute {
    protected static Logger  logger;
    protected MainController controller;
    protected Document       typeMapping;
    protected Model          baseModel;

    /**
     * Creates a new DynamicPlugin object.
     *
     * @param controller DOCUMENT ME!
     * @param baseModel DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public DynamicPluginThread(MainController controller,
                               Model          baseModel) throws PluginException {
        this.controller      = controller;
        this.baseModel       = baseModel;
        this.typeMapping     = controller.getProjectManager().getTypeMapping();

        try {
            logger = Logger.getLogger(baseModel.getThreadClassName());
        } catch (MissingAttributeException e) {
            throw new PluginException(e);
        }
    }

    /**
     * Implement this method in your subclass to do whatever action is required BEFORE the thread is started. This method is called before the thread
     * is started
     *
     * @throws PluginException DOCUMENT ME!
     */
    protected void setUp() throws PluginException {
    }

    /**
     * Implement this method in your subclass to do whatever action is required AFTER the thread is finished. This method is called after the
     * execute() method has finished
     *
     * @throws PluginException DOCUMENT ME!
     */
    protected void tearDown() throws PluginException {
    }

    /**
     * A subclass must implement the execute() method to do whatever required Before execute setUp() is called. When execute() is finished,
     * tearDown()  is called to finish model's execution
     *
     * @throws PluginException DOCUMENT ME!
     */
    public void execute() throws PluginException {
        String threadClassName = null;

        try {
            threadClassName = baseModel.getThreadClassName();
        } catch (MissingAttributeException e) {
            throw new PluginException(e);
        }

        throw new PluginException("Plugin " + baseModel.getIdentifier() + " (" + threadClassName + ") does not implement execute() method!");
    }

    /**
     * Called by PluginManager via Thread.start()
     */
    public final void run() {
        try {
            setUp();

            baseModel.setStarted();

            execute();

            tearDown();

            baseModel.setDone();
        } catch (PluginException e) {
            postException(e);
        }
    }

    /**
     * Use this method to post an exception and an appropriate Log Level (log4j) to the next higher level
     *
     * @param e DOCUMENT ME!
     * @param level DOCUMENT ME!
     */
    protected final void postException(Exception e,
                                       Level     level) {
        baseModel.setExceptionOccured();
        controller.postException(e, level);
    }

    /**
     * Use this method to post an exception to the next higher level
     *
     * @param e DOCUMENT ME!
     */
    protected final void postException(Exception e) {
        baseModel.setExceptionOccured();
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
     * @return Returns the interrupted.
     */
    public boolean isInterrupted() {
        return baseModel.isInterrupted();
    }

    /**
     * DOCUMENT ME!
     *
     * @param interrupted The interrupted to set.
     */
    public void setInterrupted(boolean interrupted) {
        if (interrupted) {
            baseModel.setInterrupted();
            interrupt();
        }
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
     * @return Returns the model.
     */
    public Model getPlugin() {
        return baseModel;
    }
}
