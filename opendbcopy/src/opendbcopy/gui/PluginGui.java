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

import info.clearthought.layout.TableLayout;

import opendbcopy.config.GUI;
import opendbcopy.config.XMLTags;

import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.PluginManager;

import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.PluginException;
import opendbcopy.plugin.model.exception.UnsupportedAttributeValueException;

import opendbcopy.resource.ResourceManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.jdom.Element;
import org.jdom.JDOMException;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import java.io.IOException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.sql.SQLException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PluginGui {
    private static Logger   logger = Logger.getLogger(PluginGui.class.getName());
    private MainController  controller;
    private ResourceManager rm;
    private PluginManager   pluginManager;
    private Model           model;
    private Element         pluginElement;
    private String          identifier;
    private String          displayOrder;
    private String          title;
    private String          modelClassName;
    private HashMap         availablePluginThreads;
    private Vector          panelsMetadata;
    private Vector          panels;
    private JTabbedPane     tab;
    private JPanel          panelPluginGui;
    private JPanel          panelControl;
    private JTextArea       statusBar;
    private JButton         buttonNext;

    /**
     * Creates a new Mode object.
     *
     * @param controller DOCUMENT ME!
     * @param pluginGuiElement DOCUMENT ME!
     * @param pluginElement DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    PluginGui(MainController controller,
              Element        pluginGuiElement,
              Element        pluginElement) throws MissingAttributeException, MissingElementException {
        if (controller == null) {
            throw new IllegalArgumentException("Missing controller");
        }

        if (pluginGuiElement == null) {
            throw new IllegalArgumentException("Missing pluginGuiElement");
        }

        if (pluginElement == null) {
            throw new IllegalArgumentException("Missing pluginElement");
        }

        if ((pluginGuiElement.getAttributeValue(XMLTags.IDENTIFIER) != null) && (pluginGuiElement.getAttributeValue(XMLTags.IDENTIFIER).length() == 0)) {
            throw new MissingAttributeException(pluginGuiElement, XMLTags.IDENTIFIER);
        }

        this.controller        = controller;
        this.pluginElement     = pluginElement;
        rm                     = controller.getResourceManager();

        availablePluginThreads     = new HashMap();

        identifier = pluginGuiElement.getAttributeValue(XMLTags.IDENTIFIER);

        if (pluginGuiElement.getChild(XMLTags.TITLE) == null) {
            throw new MissingElementException(pluginGuiElement, XMLTags.TITLE);
        }

        title = pluginGuiElement.getChild(XMLTags.TITLE).getAttributeValue(XMLTags.VALUE);

        if (title == null) {
            throw new MissingAttributeException(pluginGuiElement.getChild(XMLTags.TITLE), XMLTags.VALUE);
        } else {
            title = rm.getString(title);
        }

        loadPlugins(pluginElement);
        loadPanels(pluginGuiElement);
    }

    /**
     * DOCUMENT ME!
     *
     * @param model DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws PluginException DOCUMENT ME!
     */
    public final JPanel load(Model model) throws MissingAttributeException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException, PluginException {
        this.model = model;

        double[][] sizeMain = {
                                  { GUI.B, GUI.F, GUI.B }, // Columns
        { GUI.B, GUI.F, GUI.VS, GUI.P, GUI.B }
        }; // Rows

        double[][] sizeControl = {
                                     { GUI.B, GUI.F, GUI.HG, GUI.P, GUI.B }, // Columns
        { GUI.VS, GUI.P, GUI.VS }
        }; // Rows

        panelPluginGui     = new JPanel(new TableLayout(sizeMain));
        panelControl       = new JPanel(new TableLayout(sizeControl));

        statusBar = new JTextArea();
        statusBar.setEditable(false);
        statusBar.setBackground(null);

        buttonNext = new JButton(rm.getString("button.next"));
        buttonNext.addActionListener(new WorkingMode_buttonNext_actionAdapter(this));

        panelControl.add(statusBar, "1, 1, l, t");
        panelControl.add(buttonNext, "3, 1, r, t");

        JTabbedPane tab = loadDynamically();
        tab.addMouseListener(new WorkingMode_tab_mouseAdapter(this));

        panelPluginGui.add(tab, "1, 1");
        panelPluginGui.add(panelControl, "1, 3");

        return panelPluginGui;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     */
    private JTabbedPane loadDynamically() throws ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException {
        tab = new JTabbedPane();

        for (int i = 0; i < getNbrDynamicPanelsMetadata(); i++) {
            DynamicPanelMetadata dynPanelMetadata = getDynamicPanelMetadata(i);

            DynamicPanel         dynPanel = (DynamicPanel) dynamicallyLoadPanel(dynPanelMetadata);

            tab.add(dynPanel, controller.getResourceManager().getString(dynPanelMetadata.getTitle()));
        }

        return tab;
    }

    /**
     * DOCUMENT ME!
     *
     * @param operation DOCUMENT ME!
     * @param messageSuccessful DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws DriverNotFoundException DOCUMENT ME!
     * @throws OpenConnectionException DOCUMENT ME!
     * @throws CloseConnectionException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public final void execute(Element operation,
                              String  messageSuccessful) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, DriverNotFoundException, OpenConnectionException, CloseConnectionException, JDOMException, SQLException, IOException, Exception {
        try {
            model.execute(operation);
            logger.info(messageSuccessful);
            postMessage(messageSuccessful);
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            JOptionPane.showMessageDialog(controller.getFrame(), e.getMessage(), "Oooooops!", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     */
    public final void postMessage(String message) {
        statusBar.setText(message);
        logger.info(message);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     * @param level DOCUMENT ME!
     */
    public final void postException(Exception e,
                                    Level     level) {
        e.printStackTrace();

        if (level.isGreaterOrEqual(Level.ERROR)) {
            logger.error(e);
            JOptionPane.showMessageDialog(controller.getFrame(), e.getMessage(), "Oooooops!", JOptionPane.ERROR_MESSAGE);
        } else if (level.isGreaterOrEqual(Level.WARN)) {
            logger.warn(e);
            JOptionPane.showMessageDialog(controller.getFrame(), e.getMessage(), "Oooooops!", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public final void enableNextButton() {
        if (tab.getSelectedIndex() == (tab.getTabCount() - 1)) {
            buttonNext.setVisible(false);
        } else {
            buttonNext.setVisible(true);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public final void destroyDynamicPanels() {
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
     * @return DOCUMENT ME!
     */
    public final HashMap getAvailablePluginThreads() {
        return availablePluginThreads;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the displayOrder.
     */
    public final String getDisplayOrder() {
        return displayOrder;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the modelClassName.
     */
    public final String getModelClassName() {
        return modelClassName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the pluginElement.
     */
    public final Element getPluginElement() {
        return pluginElement;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the model.
     */
    public final Model getModel() {
        return model;
    }

    /**
     * DOCUMENT ME!
     *
     * @param model The model to set.
     */
    public final void setModel(Model model) {
        this.model = model;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the panelPluginGui.
     */
    public JPanel getPanelPluginGui() {
        return panelPluginGui;
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
     * @param dynamicPanelMetadata DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     */
    private Object dynamicallyLoadPanel(DynamicPanelMetadata dynamicPanelMetadata) throws ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException {
        Class         dynClass = Class.forName(dynamicPanelMetadata.getClassName());

        Constructor[] constructors = dynClass.getConstructors();

        Object[]      params = new Object[3];
        params[0]     = controller;
        params[1]     = this;

        if (dynamicPanelMetadata.isRegisterObserver()) {
            params[2] = new Boolean(true);
        } else {
            params[2] = new Boolean(false);
        }

        // works as long there is only one constructor
        return constructors[0].newInstance(params);
    }

    /**
     * DOCUMENT ME!
     *
     * @param pluginElement DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private void loadPlugins(Element pluginElement) throws MissingAttributeException {
        if (pluginElement == null) {
            throw new IllegalArgumentException("Missing pluginElement");
        }

        if ((pluginElement.getAttributeValue(XMLTags.MODEL_CLASS) == null) || (pluginElement.getAttributeValue(XMLTags.MODEL_CLASS).length() == 0)) {
            throw new MissingAttributeException(pluginElement, XMLTags.MODEL_CLASS);
        } else {
            modelClassName = pluginElement.getAttributeValue(XMLTags.MODEL_CLASS);
        }

        Iterator itThreads = pluginElement.getChild(XMLTags.THREADS).getChildren(XMLTags.THREAD).iterator();

        while (itThreads.hasNext()) {
            Element threadElement = (Element) itThreads.next();

            if (threadElement.getAttributeValue(XMLTags.THREAD_CLASS) == null) {
                throw new MissingAttributeException(threadElement, XMLTags.THREAD);
            }

            if (threadElement.getAttributeValue(XMLTags.DESCRIPTION) == null) {
                throw new MissingAttributeException(threadElement, XMLTags.DESCRIPTION);
            }

            availablePluginThreads.put(threadElement.getAttributeValue(XMLTags.THREAD_CLASS), rm.getString(threadElement.getAttributeValue(XMLTags.DESCRIPTION)));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param workingModeElement DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    private void loadPanels(Element workingModeElement) throws MissingAttributeException, MissingElementException {
        // now retrieve requested panels
        if (workingModeElement.getChild(XMLTags.PANELS) == null) {
            throw new MissingElementException(workingModeElement, XMLTags.PANELS);
        }

        if (workingModeElement.getChild(XMLTags.PANELS).getChildren(XMLTags.PANEL).size() == 0) {
            throw new MissingElementException(workingModeElement.getChild(XMLTags.PANELS), XMLTags.PANEL);
        }

        Iterator itPanels = workingModeElement.getChild(XMLTags.PANELS).getChildren(XMLTags.PANEL).iterator();

        while (itPanels.hasNext()) {
            Element dynamicPanelElement = (Element) itPanels.next();
            String  titlePanel = dynamicPanelElement.getAttributeValue(XMLTags.TITLE);

            if (titlePanel == null) {
                throw new MissingAttributeException(dynamicPanelElement, XMLTags.TITLE);
            }

            if (dynamicPanelElement.getChild(XMLTags.CLASS) == null) {
                throw new MissingElementException(dynamicPanelElement, XMLTags.CLASS);
            }

            if (dynamicPanelElement.getChild(XMLTags.CLASS).getAttributeValue(XMLTags.NAME) == null) {
                throw new MissingAttributeException(dynamicPanelElement, XMLTags.NAME);
            }

            String  className = dynamicPanelElement.getChild(XMLTags.CLASS).getAttributeValue(XMLTags.NAME);

            boolean registerObserver = false;

            if ((dynamicPanelElement.getChild(XMLTags.CLASS).getAttributeValue(XMLTags.REGISTER_AS_OBSERVER) != null) && (dynamicPanelElement.getChild(XMLTags.CLASS).getAttributeValue(XMLTags.REGISTER_AS_OBSERVER).compareToIgnoreCase("true") == 0)) {
                registerObserver = true;
            } else {
                registerObserver = false;
            }

            addDynamicPanelMetadata(new DynamicPanelMetadata(titlePanel, className, registerObserver));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonNext_actionPerformed(ActionEvent e) {
        // selection of working mode
        if (tab.getSelectedIndex() < (tab.getTabCount() - 1)) {
            tab.setSelectedIndex(tab.getSelectedIndex() + 1);
        }

        setSelectedTab(tab.getSelectedIndex());
        enableNextButton();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void tab_mouseClicked(MouseEvent e) {
        setSelectedTab(tab.getSelectedIndex());
        enableNextButton();
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class WorkingMode_buttonNext_actionAdapter implements java.awt.event.ActionListener {
    PluginGui adaptee;

    /**
     * Creates a new WorkingMode_buttonNext_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    WorkingMode_buttonNext_actionAdapter(PluginGui adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonNext_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class WorkingMode_tab_mouseAdapter extends java.awt.event.MouseAdapter {
    PluginGui adaptee;

    /**
     * Creates a new WorkingMode_tab_mouseAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    WorkingMode_tab_mouseAdapter(PluginGui adaptee) {
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
