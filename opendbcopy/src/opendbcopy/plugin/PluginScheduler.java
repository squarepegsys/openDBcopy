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
package opendbcopy.plugin;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.model.*;
import opendbcopy.plugin.model.exception.MissingAttributeException;

import org.apache.log4j.Level;

import org.jdom.Element;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PluginScheduler implements Observer {
    private static PluginScheduler     instance = null;
    private static DynamicPluginThread currentPluginThread;
    private static Model               currentModel;
    private MainController             controller;
    private PluginManager              pluginManager;
    private LinkedList                 pluginsToExecute;
    private boolean                    executeSinglePlugin;

    /**
     * Creates a new PluginExecutor object.
     *
     * @param pluginManager DOCUMENT ME!
     * @param controller DOCUMENT ME!
     */
    private PluginScheduler(PluginManager  pluginManager,
                            MainController controller) {
        this.pluginManager     = pluginManager;
        this.controller        = controller;
    }

    /**
     * Return singleton instance
     *
     * @param pluginManager DOCUMENT ME!
     * @param controller DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static PluginScheduler getInstance(PluginManager  pluginManager,
                                              MainController controller) {
        if (instance == null) {
            instance = new PluginScheduler(pluginManager, controller);
        }

        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param obj DOCUMENT ME!
     */
    public final void update(Observable o,
                             Object     obj) {
        // check if current plugin had errors, then do not continue
        if (!currentModel.isExceptionOccured()) {
            if (!currentModel.isInterrupted() && !currentModel.isSuspended()) {
                // only execute the next plugin if the previous isDone() and in the meantime pluginManager has not been interrupted
                if (currentModel.isDone() && !pluginManager.isInterrupted()) {
                    // plugin chain
                    if (!executeSinglePlugin) {
                        Model plugin = getNextPlugin();

                        if (plugin != null) {
                            // check if next plugin requires input and if so, get it from previous plugin
                            if (plugin.isInputRequired()) {
                                if (currentModel.getOutput() != null) {
                                    plugin.setInput((Element) currentModel.getOutput().clone());
                                } else {
                                    plugin.setProgressMessage("Missing input from previous plugin!");
                                    pluginManager.setExceptionOccured(true);
                                }
                            } else {
                                // if output of previous plugin is not null, pass it to next plugin
                                if (currentModel.getOutput() != null) {
                                    plugin.setInput((Element) currentModel.getOutput().clone());
                                }
                            }

                            currentModel = plugin;
                            pluginManager.incrementCurrentExecuteIndex();

                            try {
                                executePlugin(currentModel);
                            } catch (Exception e) {
                                controller.postException(e, Level.ERROR);
                            }
                        } else {
                            // means all plugins have been executed "successfully"
                            pluginManager.setDone(true);
                        }
                    }
                    // single plugin
                    else {
                        if (currentModel.isDone()) {
                            pluginManager.setDone(true);
                        }
                    }
                }
            } else {
                if (currentModel.isInterrupted()) {
                    pluginManager.setInterrupted(true);
                }

                if (currentModel.isSuspended()) {
                    pluginManager.setSuspended(true);
                }
            }
        } else {
            pluginManager.setExceptionOccured(true);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param pluginsToExecute DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     */
    public final void executePlugins(LinkedList pluginsToExecute) throws MissingAttributeException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
        this.pluginsToExecute     = pluginsToExecute;
        executeSinglePlugin       = false;

        // set the first plugin to execute
        currentModel = getNextPlugin();

        // requires that first first plugin has been set
        executePlugin(currentModel);
    }

    /**
     * DOCUMENT ME!
     */
    public final void interruptCurrentPlugin() {
        if (currentPluginThread != null) {
            currentPluginThread.interrupt();
        }

        pluginManager.setInterrupted(true);
    }

    /**
     * Returns the next PluginListModel that wants to be executed. Removes returned PluginListModel from list of plugins to execute Registers
     * observer and sets it as default in PluginManager 2!
     *
     * @return DOCUMENT ME!
     */
    private Model getNextPlugin() {
        if ((pluginsToExecute != null) && (pluginsToExecute.size() > 0)) {
            Model plugin = (Model) pluginsToExecute.getFirst();
            pluginsToExecute.removeFirst();

            plugin.registerObserver(this);
            pluginManager.setCurrentModel(plugin);

            return plugin;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param model DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     */
    protected void executeSinglePlugin(Model model) throws MissingAttributeException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
        executeSinglePlugin     = true;

        currentModel = model;
        currentModel.registerObserver(this);

        currentPluginThread = (DynamicPluginThread) dynamicallyLoadPlugin();
        currentPluginThread.start();
    }

    /**
     * DOCUMENT ME!
     *
     * @param model DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     */
    private void executePlugin(Model model) throws MissingAttributeException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
        currentModel     = model;

        currentPluginThread = (DynamicPluginThread) dynamicallyLoadPlugin();
        currentPluginThread.start();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     */
    private Object dynamicallyLoadPlugin() throws MissingAttributeException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Class         dynClass = Class.forName(currentModel.getThreadClassName());

        Constructor[] constructors = dynClass.getConstructors();

        Object[]      params = new Object[2];
        params[0]     = controller;
        params[1]     = currentModel;

        // works as long there is only one constructor
        return constructors[0].newInstance(params);
    }
}
