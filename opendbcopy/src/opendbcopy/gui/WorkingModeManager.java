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

import org.jdom.Document;
import org.jdom.Element;

import java.awt.event.MouseEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JTabbedPane;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class WorkingModeManager {
    private FrameMain      frame;
    private MainController controller;
    private HashMap        modes;
    private Vector         modesNames;
    private WorkingMode    currentWorkingMode;
    private JTabbedPane    tab;

    /**
     * Creates a new WorkingModeManager object.
     *
     * @param frame DOCUMENT ME!
     * @param controller DOCUMENT ME!
     * @param workingModeDocument DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public WorkingModeManager(FrameMain      frame,
                              MainController controller,
                              Document       workingModeDocument) throws Exception {
        this.frame          = frame;
        this.controller     = controller;

        // init containers
        modes          = new HashMap();
        modesNames     = new Vector();

        // now read the available working modes
        readWorkingModes(workingModeDocument);
    }

    /**
     * DOCUMENT ME!
     *
     * @param workingModeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final JTabbedPane loadWorkingMode(String workingModeName) throws Exception {
        currentWorkingMode     = getWorkingMode(workingModeName);
        tab                    = currentWorkingMode.loadDynamically(frame.getWidth(), frame.getHeight());
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
     * @param workingModeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private final WorkingMode getWorkingMode(String workingModeName) {
        Iterator itWorkingModes = modes.values().iterator();

        while (itWorkingModes.hasNext()) {
            WorkingMode workingMode = (WorkingMode) itWorkingModes.next();

            if (workingMode.getTitle().compareTo(workingModeName) == 0) {
                return workingMode;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param workingModeDocument DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void readWorkingModes(Document workingModeDocument) throws Exception {
        Iterator itWorkingModes = workingModeDocument.getRootElement().getChildren(XMLTags.WORKING_MODE).iterator();

        while (itWorkingModes.hasNext()) {
            Element workingModeElement = (Element) itWorkingModes.next();

            if ((workingModeElement.getAttributeValue(XMLTags.IDENTIFIER) != null) && (workingModeElement.getAttributeValue(XMLTags.IDENTIFIER).length() > 0)) {
                String identifier = workingModeElement.getAttributeValue(XMLTags.IDENTIFIER);
                String titleWorkingMode = workingModeElement.getChild(XMLTags.TITLE).getAttributeValue(XMLTags.VALUE);

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
                throw new Exception("Missing Identifier tag in working_mode element");
            }
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
