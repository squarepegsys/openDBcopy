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

import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.PluginException;

import opendbcopy.resource.ResourceManager;

import org.jdom.Element;

import java.lang.reflect.InvocationTargetException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;
import java.util.Vector;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PluginGuiManager extends Observable implements Observer {
    private MainController  controller;
    private ResourceManager rm;
    private HashMap         availableModes;
    private HashMap         availablePlugins;
    private HashMap         pluginThreads;
    private PluginGui       currentPluginGui;
    private LinkedList      pluginGuisLoaded;
    private LinkedList      pluginGuisToExecute;
    private boolean         amRegisteredAsObserverToPluginManager = false;

    /**
     * Creates a new PluginGuiManager object. Please be aware that PluginGuiManager contains two LinkedLists. One to hold plugin guis which are in
     * LinkedList of models to be executed, one to hold plugin guis which are in LinkedList of modes only loaded. Because of the MVC pattern
     * implemented it is up to the controller to decide whether a GUI is available or not. Therefore models, which are contained within a project
     * and are loaded at startup, are passed to PluginGuiManager if and only if Gui is enabled. The Gui is able to display both LinkedList of Plugin
     * Guis.
     *
     * @param controller DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public PluginGuiManager(MainController controller) throws MissingAttributeException, MissingElementException {
        this.controller     = controller;
        this.rm             = controller.getResourceManager();

        pluginGuisLoaded        = new LinkedList();
        pluginGuisToExecute     = new LinkedList();

        // init containers
        availableModes       = new HashMap();
        availablePlugins     = new HashMap();
    }

    /**
     * DOCUMENT ME!
     */
    public final void broadcast() {
        setChanged();
        notifyObservers();
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param arg DOCUMENT ME!
     */
    public void update(Observable o,
                       Object     arg) {
        if (controller.getJobManager().getPluginManager().getModelsLoaded().size() == 0) {
            pluginGuisLoaded.clear();
        }

        if (controller.getJobManager().getPluginManager().getModelsToExecute().size() == 0) {
            pluginGuisToExecute.clear();
        }
    }

    /**
     * Given a working mode identifier or description load it
     *
     * @param pluginGuiIdentifier can be a working mode identifier or its description
     * @param isExecutableModel DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws PluginException DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    public final void loadPluginGui(String  pluginGuiIdentifier,
                                    boolean isExecutableModel) throws MissingAttributeException, MissingElementException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException, PluginException {
        PluginGui pluginGui = null;
        Model     model = null;

        if (!availableModes.containsKey(pluginGuiIdentifier)) {
            throw new RuntimeException("Unknown pluginGuiIdentifier");
        } else {
            pluginGui = createNewPluginGui(pluginGuiIdentifier);
        }

        // now load the model with appropriate model
        if (isExecutableModel) {
            // takes the last model that is within the linked list of executable models
            model = controller.getJobManager().getPluginManager().getModelToExecute(pluginGuisToExecute.size());
            model.setTitle(pluginGui.getTitle());
            pluginGui.load(model);
            pluginGuisToExecute.add(pluginGui);
        } else {
            // creates a new model for linked list of models loaded
            model = controller.getJobManager().getPluginManager().loadModel(pluginGui.getPluginElement(), pluginGui.getTitle());
            pluginGui.load(model);
            pluginGuisLoaded.add(pluginGui);
        }

        // set current plugin gui
        setCurrentPluginGui(pluginGui);

        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @param newModel DOCUMENT ME!
     * @param modelToExecute DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws PluginException DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    public final void loadPluginGuiFromModel(Model   newModel,
                                             boolean modelToExecute) throws MissingAttributeException, MissingElementException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException, PluginException {
        PluginGui pluginGui = null;
        Model     model = newModel;

        if (!availableModes.containsKey(model.getIdentifier())) {
            throw new RuntimeException("Unknown " + model.getIdentifier());
        } else {
            pluginGui = createNewPluginGui(model.getIdentifier());
        }

        model.setTitle(pluginGui.getTitle());
        pluginGui.load(model);

        if (modelToExecute) {
            pluginGuisToExecute.add(pluginGui);
        } else {
            pluginGuisLoaded.add(pluginGui);
        }

        // set current plugin gui
        setCurrentPluginGui(pluginGui);

        broadcast();
    }

    /**
     * Returns a vector of available plugin guis ordered using display_order tag If a plugin gui does not provide a display order it is appended at
     * the end
     *
     * @return DOCUMENT ME!
     */
    public final Vector getAvailablePluginGuisOrdered() {
        // register myself to pluginManager
        if (!amRegisteredAsObserverToPluginManager) {
            controller.getJobManager().getPluginManager().registerObserver(this);
            amRegisteredAsObserverToPluginManager = true;
        }

        Iterator itPluginGuis = availableModes.values().iterator();
        TreeMap  modesDisplayOrdered = new TreeMap();
        Vector   modesDisplayUnordered = new Vector();
        Vector   modesOrdered = new Vector();

        while (itPluginGuis.hasNext()) {
            Element pluginGui = (Element) itPluginGuis.next();

            if (pluginGui.getAttributeValue(XMLTags.DISPLAY_ORDER) != null) {
                modesDisplayOrdered.put(pluginGui.getAttributeValue(XMLTags.DISPLAY_ORDER), pluginGui);
            } else {
                modesDisplayUnordered.add(pluginGui);
            }
        }

        if (modesDisplayOrdered.size() > 0) {
            Iterator itModesOrdered = modesDisplayOrdered.values().iterator();

            while (itModesOrdered.hasNext()) {
                modesOrdered.add((Element) itModesOrdered.next());
            }
        }

        if (modesDisplayUnordered.size() > 0) {
            for (int i = 0; i < modesDisplayUnordered.size(); i++) {
                modesOrdered.add((Element) modesDisplayUnordered.elementAt(i));
            }
        }

        // now put ordered and unordered plugin guis together
        return modesOrdered;
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
     * @param observer DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void deleteObserver(Observer observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Missing observer");
        }

        this.deleteObserver(observer);
    }

    /**
     * DOCUMENT ME!
     *
     * @param pluginGuiElement DOCUMENT ME!
     * @param pluginElement DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void addPluginGui(Element pluginGuiElement,
                                   Element pluginElement) throws MissingAttributeException, MissingElementException {
        availableModes.put(pluginGuiElement.getAttributeValue(XMLTags.IDENTIFIER), pluginGuiElement);
        availablePlugins.put(pluginGuiElement.getAttributeValue(XMLTags.IDENTIFIER), pluginElement);

        String resourceName = null;

        if (pluginGuiElement.getAttributeValue(XMLTags.RESOURCE_NAME) != null) {
            resourceName = pluginGuiElement.getAttributeValue(XMLTags.RESOURCE_NAME);
        }

        // load resources for plugin gui if such exist
        if (resourceName != null) {
            rm.addResourceBundle(resourceName);
        }

        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final PluginGui getPluginGuiLoaded(int index) {
        if (pluginGuisLoaded.size() > 0) {
            return (PluginGui) pluginGuisLoaded.get(index);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getNbrPluginGuisLoaded() {
        return pluginGuisLoaded.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final PluginGui getPluginGuiToExecute(int index) {
        if (pluginGuisToExecute.size() > 0) {
            return (PluginGui) pluginGuisToExecute.get(index);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getNbrPluginGuisToExecute() {
        return pluginGuisToExecute.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     */
    public final void removePluginGuiToExecute(int index) {
        pluginGuisToExecute.remove(index);

        if (pluginGuisToExecute.size() == 0) {
            setCurrentPluginGui(null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param indexLoaded DOCUMENT ME!
     */
    public final void addPluginGuiToExecuteLast(int indexLoaded) {
        pluginGuisToExecute.add(pluginGuisLoaded.get(indexLoaded));
        pluginGuisLoaded.remove(indexLoaded);
    }

    /**
     * DOCUMENT ME!
     *
     * @param indexToExecute DOCUMENT ME!
     */
    public final void addPluginGuiLoadedLast(int indexToExecute) {
        pluginGuisLoaded.add(pluginGuisToExecute.get(indexToExecute));
        pluginGuisToExecute.remove(indexToExecute);
    }

    /**
     * DOCUMENT ME!
     *
     * @param sourceIndex DOCUMENT ME!
     * @param destinationIndex DOCUMENT ME!
     */
    public final void changeOrderPluginToExecute(int sourceIndex,
                                                 int destinationIndex) {
        if (pluginGuisToExecute.size() > sourceIndex) {
            PluginGui pluginGuiRemoved = (PluginGui) pluginGuisToExecute.remove(sourceIndex);
            pluginGuisToExecute.add(destinationIndex, pluginGuiRemoved);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the currentPluginGui.
     */
    public final PluginGui getCurrentPluginGui() {
        return currentPluginGui;
    }

    /**
     * DOCUMENT ME!
     *
     * @param currentPluginGui The currentPluginGui to set.
     */
    public final void setCurrentPluginGui(PluginGui currentPluginGui) {
        this.currentPluginGui = currentPluginGui;

        if (currentPluginGui != null) {
            try {
                controller.getJobManager().getPluginManager().setCurrentModel(currentPluginGui.getModel());
            } catch (RuntimeException e) {
                // ignore as plugin manager is not yet reachable and does not worry program flow
            }
        }

        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @param identifier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    private PluginGui createNewPluginGui(String identifier) throws MissingAttributeException, MissingElementException {
        if (identifier == null) {
            throw new IllegalArgumentException("Missing identifier");
        }

        if (availableModes.containsKey(identifier) && availablePlugins.containsKey(identifier)) {
            // create cloned elements
            Element pluginGuiElementClone = (Element) ((Element) availableModes.get(identifier)).clone();
            Element pluginElementClone = (Element) ((Element) availablePlugins.get(identifier)).clone();

            return new PluginGui(controller, pluginGuiElementClone, pluginElementClone);
        } else {
            throw new RuntimeException("Plugin Gui with identifier " + identifier + " does not exist.");
        }
    }
}
