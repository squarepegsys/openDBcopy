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
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class WorkingModeManager extends Observable {
    private MainController  controller;
    private ResourceManager rm;
    private HashMap         availableModes;
    private HashMap         availablePlugins;
    private HashMap         pluginThreads;
    private WorkingMode     currentWorkingMode;
    private JTabbedPane     tabModelsLoaded;
    private JTabbedPane     tabModelsToExecute;
    private int             frameWidth;
    private int             frameHeight;

    /**
     * Creates a new WorkingModeManager object.  Please be aware that WorkingModeManager contains two JTabbedPanes. One to hold WorkingModes which
     * are in LinkedList of models to be executed, one to hold WorkingModes which are in LinkedList of modes only loaded. Because of the MVC pattern
     * implemented it is up to the controller to decide whether a GUI is available or not. Therefore models, which are contained within a project
     * and are loaded at startup, are passed to WorkingModeManager if and only if Gui is enabled. The Gui is able to display both LinkedList of
     * WorkingModes.
     *
     * @param controller DOCUMENT ME!
     * @param frameWidth DOCUMENT ME!
     * @param frameHeight DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public WorkingModeManager(MainController controller,
                              int            frameWidth,
                              int            frameHeight) throws MissingAttributeException, MissingElementException {
        this.frameWidth      = frameWidth;
        this.frameHeight     = frameHeight;
        this.controller      = controller;
        this.rm              = controller.getResourceManager();

        tabModelsLoaded        = new JTabbedPane();
        tabModelsToExecute     = new JTabbedPane();

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
     * Given a working mode identifier or description load it
     *
     * @param workingModeIdentifier can be a working mode identifier or its description
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
    public final void loadWorkingMode(String  workingModeIdentifier,
                                      boolean isExecutableModel) throws MissingAttributeException, MissingElementException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException, PluginException {
        WorkingMode wm = null;
        Model       model = null;
        JPanel      panel = null;

        if (!availableModes.containsKey(workingModeIdentifier)) {
            throw new RuntimeException("Unknown workingModeIdentifier");
        } else {
            wm = createNewWorkingMode(workingModeIdentifier);
        }

        // now load the model with appropriate model
        if (isExecutableModel) {
            // takes the last model that is within the linked list of executable models
            model = controller.getProjectManager().getPluginManager().getModelToExecute(tabModelsToExecute.getTabCount());
            model.setTitle(rm.getString(wm.getTitle()));
            panel = wm.load(model, frameWidth, frameHeight);
            tabModelsToExecute.addTab(rm.getString(wm.getTitle()), panel);
            tabModelsToExecute.setSelectedIndex(tabModelsToExecute.getTabCount() - 1);
        } else {
            // creates a new model for linked list of models loaded
            controller.getProjectManager().getPluginManager().loadModel(wm.getPluginElement(), rm.getString(wm.getTitle()));
            model     = controller.getProjectManager().getPluginManager().getModelLoaded(tabModelsLoaded.getTabCount());
            panel     = wm.load(model, frameWidth, frameHeight);
            tabModelsLoaded.addTab(rm.getString(wm.getTitle()), panel);
            tabModelsLoaded.setSelectedIndex(tabModelsLoaded.getTabCount() - 1);
        }

        // set current working mode
        currentWorkingMode = wm;

        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     */
    public final void removeWorkingMode(int index) {
        tabModelsLoaded.remove(index);
    }

    /**
     * Returns a vector of available working modes ordered using display_order tag If a working mode does not provide a display order it is appended
     * at the end
     *
     * @return DOCUMENT ME!
     */
    public final Vector getAvailableWorkingModesOrdered() {
        Iterator itWorkingModes = availableModes.values().iterator();
        TreeMap  modesDisplayOrdered = new TreeMap();
        Vector   modesDisplayUnordered = new Vector();
        Vector   modesOrdered = new Vector();

        while (itWorkingModes.hasNext()) {
            Element wm = (Element) itWorkingModes.next();

            if (wm.getAttributeValue(XMLTags.DISPLAY_ORDER) != null) {
                modesDisplayOrdered.put(wm.getAttributeValue(XMLTags.DISPLAY_ORDER), wm);
            } else {
                modesDisplayUnordered.add(wm);
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

        // now put ordered and unordered working modes together
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
     * @param workingModeElement DOCUMENT ME!
     * @param pluginElement DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void addWorkingMode(Element workingModeElement,
                                     Element pluginElement) throws MissingAttributeException, MissingElementException {
        availableModes.put(workingModeElement.getAttributeValue(XMLTags.IDENTIFIER), workingModeElement);
        availablePlugins.put(workingModeElement.getAttributeValue(XMLTags.IDENTIFIER), pluginElement);

        String resourceName = null;

        if (workingModeElement.getAttributeValue(XMLTags.RESOURCE_NAME) != null) {
            resourceName = workingModeElement.getAttributeValue(XMLTags.RESOURCE_NAME);
        }

        // load resources for Working Mode if such exist
        if (resourceName != null) {
            rm.addResourceBundle(resourceName);
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
    private WorkingMode createNewWorkingMode(String identifier) throws MissingAttributeException, MissingElementException {
        if (identifier == null) {
            throw new IllegalArgumentException("Missing identifier");
        }

        if (availableModes.containsKey(identifier) && availablePlugins.containsKey(identifier)) {
            // create cloned elements
            Element workingModeElementClone = (Element) ((Element) availableModes.get(identifier)).clone();
            Element pluginElementClone = (Element) ((Element) availablePlugins.get(identifier)).clone();

            return new WorkingMode(controller, workingModeElementClone, pluginElementClone);
        } else {
            throw new RuntimeException("Working Mode with identifier " + identifier + " does not exist.");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the tabModelsLoaded.
     */
    public final JTabbedPane getTabModelsLoaded() {
        return tabModelsLoaded;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final JTabbedPane getTabModelsToExecute() {
        return tabModelsToExecute;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the currentWorkingMode.
     */
    public final WorkingMode getCurrentWorkingMode() {
        return currentWorkingMode;
    }
}
