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

import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.model.exception.MissingAttributeException;
import opendbcopy.model.exception.MissingElementException;
import opendbcopy.model.exception.UnsupportedAttributeValueException;

import org.jdom.Document;
import org.jdom.Element;

import java.awt.event.MouseEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.JTabbedPane;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class WorkingModeManager extends Observable {
    private MainController controller;
    private HashMap        modes;
    private HashMap        plugins;
    private Vector         modesNames;
    private WorkingMode    currentWorkingMode;
    private JTabbedPane    tab;
    private int            frameWidth;
    private int            frameHeight;

    /**
     * Creates a new WorkingModeManager object.
     *
     * @param controller DOCUMENT ME!
     * @param workingModeDocument DOCUMENT ME!
     * @param frameWidth DOCUMENT ME!
     * @param frameHeight DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public WorkingModeManager(MainController controller,
                              Document       workingModeDocument,
                              int            frameWidth,
                              int            frameHeight) throws Exception {
        this.frameWidth      = frameWidth;
        this.frameHeight     = frameHeight;
        this.controller      = controller;

        // init containers
        modes          = new HashMap();
        plugins        = new HashMap();
        modesNames     = new Vector();

        // now read the available working modes
        readStandardWorkingModes(workingModeDocument);

        broadcast();
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
     * @param workingModeTitle DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final JTabbedPane loadWorkingMode(String workingModeTitle) throws Exception {
        currentWorkingMode     = findWorkingMode(workingModeTitle);

        tab = currentWorkingMode.loadDynamically(frameWidth, frameHeight);
        tab.addMouseListener(new TabManager_tab_mouseAdapter(this));

        return tab;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final void destroyCurrentWorkingMode() throws Exception {
        if (currentWorkingMode != null) {
            currentWorkingMode.destroyDynamicPanels();
        }

        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final Vector getAvailableWorkingModes() {
        return modesNames;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final HashMap getPluginsForCurrentWorkingMode() {
        return currentWorkingMode.getAvailablePlugins();
    }

    /**
     * DOCUMENT ME!
     *
     * @param workingModeElement DOCUMENT ME!
     * @param pluginIdentifier DOCUMENT ME!
     * @param pluginDescription DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void addWorkingModeForPlugin(Element workingModeElement,
                                              String  pluginIdentifier,
                                              String  pluginDescription) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException {
        if ((workingModeElement == null) || (pluginIdentifier == null) || (pluginDescription == null)) {
            throw new IllegalArgumentException("Missing arguments values: workingModeElement=" + workingModeElement + " pluginIdentifier=" + pluginIdentifier + " pluginDescription=" + pluginDescription);
        }

        if ((workingModeElement.getAttributeValue(XMLTags.IDENTIFIER) == null) || (workingModeElement.getAttributeValue(XMLTags.IDENTIFIER).length() == 0)) {
            throw new MissingAttributeException(workingModeElement, XMLTags.IDENTIFIER);
        }

        String identifier = workingModeElement.getAttributeValue(XMLTags.IDENTIFIER);

        // if plugin references a standard working mode, check availability
        if (identifier.compareToIgnoreCase(XMLTags.STANDARD_WORKING_MODE_REFERENCE) == 0) {
            List standardWorkingModes = workingModeElement.getChildren(XMLTags.STANDARD_WORKING_MODE);

            if (standardWorkingModes.size() == 0) {
                throw new MissingElementException(workingModeElement, XMLTags.STANDARD_WORKING_MODE);
            }

            Iterator itStandardWorkingModes = standardWorkingModes.iterator();

            while (itStandardWorkingModes.hasNext()) {
                Element standardWorkingMode = (Element) itStandardWorkingModes.next();

                if ((standardWorkingMode.getAttributeValue(XMLTags.IDENTIFIER) == null) || (standardWorkingMode.getAttributeValue(XMLTags.IDENTIFIER).length() == 0)) {
                    throw new MissingAttributeException(standardWorkingMode, XMLTags.IDENTIFIER);
                }

                if (!modes.containsKey(standardWorkingMode.getAttributeValue(XMLTags.IDENTIFIER))) {
                    throw new UnsupportedAttributeValueException(standardWorkingMode, XMLTags.IDENTIFIER);
                }

                // add plugin information to available plugins of selected working mode
                WorkingMode workingMode = (WorkingMode) modes.get(standardWorkingMode.getAttributeValue(XMLTags.IDENTIFIER));
                workingMode.addPlugin(pluginIdentifier, pluginDescription);
            }
        }
        // a new working mode must be registered
        else {
            addWorkingMode(workingModeElement);

            WorkingMode workingMode = (WorkingMode) modes.get(workingModeElement.getAttributeValue(XMLTags.IDENTIFIER));
            workingMode.addPlugin(pluginIdentifier, pluginDescription);
        }

        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @param observer DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void registerObserver(Observer observer) throws IllegalArgumentException {
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
    public final void deleteObserver(Observer observer) throws IllegalArgumentException {
        if (observer == null) {
            throw new IllegalArgumentException("Missing observer");
        }

        this.deleteObserver(observer);
    }

    /**
     * DOCUMENT ME!
     *
     * @param workingModeElement DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private void addWorkingMode(Element workingModeElement) throws MissingAttributeException, MissingElementException {
        if (workingModeElement == null) {
            throw new IllegalArgumentException("Missing workingModeElement");
        }

        if ((workingModeElement.getAttributeValue(XMLTags.IDENTIFIER) != null) && (workingModeElement.getAttributeValue(XMLTags.IDENTIFIER).length() > 0)) {
            String identifier = null;
            String titleWorkingMode = null;

            identifier = workingModeElement.getAttributeValue(XMLTags.IDENTIFIER);

            if (workingModeElement.getChild(XMLTags.TITLE) == null) {
                throw new MissingElementException(workingModeElement, XMLTags.TITLE);
            }

            titleWorkingMode = workingModeElement.getChild(XMLTags.TITLE).getAttributeValue(XMLTags.VALUE);

            if (titleWorkingMode == null) {
                throw new MissingAttributeException(workingModeElement.getChild(XMLTags.TITLE), XMLTags.VALUE);
            }

            // add name to vector of available working modes
            modesNames.add(titleWorkingMode);

            WorkingMode workingMode = new WorkingMode(controller, identifier, titleWorkingMode);

            // now retrieve requested panels
            Iterator itPanels = workingModeElement.getChild(XMLTags.PANELS).getChildren(XMLTags.PANEL).iterator();

            while (itPanels.hasNext()) {
                Element dynamicPanelElement = (Element) itPanels.next();
                String  titlePanel = dynamicPanelElement.getAttributeValue(XMLTags.TITLE);
                String  className = dynamicPanelElement.getChild(XMLTags.CLASS).getAttributeValue(XMLTags.NAME);

                boolean registerObserver = false;

                if ((dynamicPanelElement.getChild(XMLTags.CLASS).getAttributeValue(XMLTags.REGISTER_AS_OBSERVER) != null) && (dynamicPanelElement.getChild(XMLTags.CLASS).getAttributeValue(XMLTags.REGISTER_AS_OBSERVER).compareToIgnoreCase("true") == 0)) {
                    registerObserver = true;
                } else {
                    registerObserver = false;
                }

                workingMode.addDynamicPanelMetadata(new DynamicPanelMetadata(titlePanel, className, registerObserver));
            }

            modes.put(identifier, workingMode);
        } else {
            throw new MissingAttributeException(workingModeElement, XMLTags.IDENTIFIER);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param workingModeIdentifier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    private WorkingMode getWorkingMode(String workingModeIdentifier) throws IllegalArgumentException {
        if (workingModeIdentifier == null) {
            throw new IllegalArgumentException("Missing workingModeIdentifier");
        }

        if (modes.containsKey(workingModeIdentifier)) {
            return (WorkingMode) modes.get(workingModeIdentifier);
        } else {
            throw new RuntimeException("Working Mode with identifier " + workingModeIdentifier + " does not exist.");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param workingModeTitle DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    private WorkingMode findWorkingMode(String workingModeTitle) throws Exception {
        WorkingMode workingMode = null;

        if (modes.size() > 0) {
            Iterator itWorkingModes = modes.values().iterator();

            while (itWorkingModes.hasNext()) {
                workingMode = (WorkingMode) itWorkingModes.next();

                if (workingMode.getTitle().compareTo(workingModeTitle) == 0) {
                    return workingMode;
                }
            }
        }

        throw new RuntimeException("Working Mode with title " + workingModeTitle + " does not exist.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param workingModeDocument DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    private void readStandardWorkingModes(Document workingModeDocument) throws MissingAttributeException, MissingElementException {
        Iterator itWorkingModes = workingModeDocument.getRootElement().getChildren(XMLTags.WORKING_MODE).iterator();

        while (itWorkingModes.hasNext()) {
            Element workingModeElement = (Element) itWorkingModes.next();
            addWorkingMode(workingModeElement);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void tab_mouseClicked(MouseEvent e) {
        System.out.println(tab.getSelectedIndex());
        currentWorkingMode.setSelectedTab(tab.getSelectedIndex());
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class TabManager_tab_mouseAdapter extends java.awt.event.MouseAdapter {
    WorkingModeManager adaptee;

    /**
     * Creates a new FrameMain_tab_mouseAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    TabManager_tab_mouseAdapter(WorkingModeManager adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void mouseClicked(MouseEvent e) {
        adaptee.tab_mouseClicked(e);
    }
}
