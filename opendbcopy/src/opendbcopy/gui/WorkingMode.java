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

import opendbcopy.controller.MainController;

import java.lang.reflect.Constructor;

import java.util.HashMap;
import java.util.Vector;

import javax.swing.JTabbedPane;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class WorkingMode {
    private MainController controller;
    private String         identifier;
    private String         title;
    private HashMap        availablePlugins;
    private Vector         panelsMetadata;
    private Vector         panels;
    private JTabbedPane    tab;

    /**
     * Creates a new Mode object.
     *
     * @param controller DOCUMENT ME!
     * @param identifier DOCUMENT ME!
     * @param title DOCUMENT ME!
     */
    WorkingMode(MainController controller,
                String         identifier,
                String         title) {
        this.controller     = controller;
        this.identifier     = identifier;
        this.title          = title;

        availablePlugins = new HashMap();
    }

    /**
     * DOCUMENT ME!
     *
     * @param width DOCUMENT ME!
     * @param height DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final JTabbedPane loadDynamically(int width,
                                             int height) throws Exception {
        tab = new JTabbedPane();

        for (int i = 0; i < getNbrDynamicPanelsMetadata(); i++) {
            DynamicPanelMetadata dynPanelMetadata = getDynamicPanelMetadata(i);

            DynamicPanel         dynPanel = (DynamicPanel) dynamicallyLoadPanel(dynPanelMetadata);
            dynPanel.setSize(width, height);

            if (dynPanelMetadata.isRegisterObserver()) {
                controller.registerObserver(dynPanel);
            }

            tab.add(dynPanel, dynPanelMetadata.getTitle());
        }

        return tab;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final void destroyDynamicPanels() throws Exception {
        if (tab != null) {
            for (int i = 0; i < tab.getTabCount(); i++) {
                DynamicPanel dynPanel = (DynamicPanel) tab.getComponent(i);
                tab.remove(dynPanel);

                if (getDynamicPanelMetadata(i).isRegisterObserver()) {
                    try {
                        controller.deleteObserver(dynPanel);
                    } catch (Exception e) {
                        // who cares ...
                    }
                }

                dynPanel = null;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     */
    public final void setSelectedTab(int index) {
        tab.setSelectedIndex(index);

        DynamicPanel dynPanel = (DynamicPanel) tab.getComponentAt(index);

        // give the panel the chance to do some action on selection
        dynPanel.onSelect();
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the panels.
     */
    public final Vector getPanels() {
        return panels;
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private DynamicPanel getDynamicPanel(int index) {
        if (index < panels.size()) {
            return (DynamicPanel) panels.elementAt(index);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private DynamicPanelMetadata getDynamicPanelMetadata(int index) {
        if (index < panelsMetadata.size()) {
            return (DynamicPanelMetadata) panelsMetadata.elementAt(index);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getNbrDynamicPanels() {
        return panels.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getNbrDynamicPanelsMetadata() {
        return panelsMetadata.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param dynamicPanelMetadata DOCUMENT ME!
     */
    public final void addDynamicPanelMetadata(DynamicPanelMetadata dynamicPanelMetadata) {
        if (panelsMetadata == null) {
            panelsMetadata = new Vector();
        }

        panelsMetadata.add(dynamicPanelMetadata);
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the title.
     */
    public final String getTitle() {
        return title;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the identifier.
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pluginIdentifier DOCUMENT ME!
     * @param pluginDescription DOCUMENT ME!
     */
    public final void addPlugin(String pluginIdentifier,
                                String pluginDescription) {
        availablePlugins.put(pluginIdentifier, pluginDescription);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final HashMap getAvailablePlugins() {
        return availablePlugins;
    }

    /**
     * DOCUMENT ME!
     *
     * @param dynamicPanelMetadata DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private Object dynamicallyLoadPanel(DynamicPanelMetadata dynamicPanelMetadata) throws Exception {
        Class         dynClass = Class.forName(dynamicPanelMetadata.getClassName());

        Constructor[] constructors = dynClass.getConstructors();

        Object[]      params = new Object[1];
        params[0] = controller;

        // works as long there is only one constructor
        return constructors[0].newInstance(params);
    }
}
