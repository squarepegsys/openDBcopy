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
 * $Log$
 * Revision 1.1  2004/01/09 18:10:51  iloveopensource
 * first release
 *
 * --------------------------------------------------------------------------*/
package opendbcopy.gui;

import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.model.ProjectManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.jdom.Element;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PanelFilter extends JPanel implements Observer {
    private static Logger  logger = Logger.getLogger(PanelFilter.class.getName());
    private FrameMain      parentFrame;
    private MainController controller;
    private ProjectManager pm;
    private GridLayout     gridLayout = new GridLayout();
    private JPanel         panelStringFilter = new JPanel();
    private JCheckBox      checkBoxTrim = new JCheckBox();
    private JCheckBox      checkBoxRemoveMultipleIntermediateSpaces = new JCheckBox();
    private JCheckBox      checkBoxSetNull = new JCheckBox();

    /**
     * Creates a new PanelFilter object.
     *
     * @param parentFrame DOCUMENT ME!
     * @param controller DOCUMENT ME!
     * @param projectManager DOCUMENT ME!
     */
    public PanelFilter(FrameMain      parentFrame,
                       MainController controller,
                       ProjectManager projectManager) {
        this.parentFrame     = parentFrame;
        this.controller      = controller;
        this.pm              = projectManager;

        try {
            guiInit();
        } catch (Exception e) {
            logger.error(e.toString());
            this.parentFrame.setStatusBar(e.toString(), Level.ERROR);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param obj DOCUMENT ME!
     */
    public final void update(Observable o,
                             Object     obj) {
        try {
            // check for string filters
            Iterator itStringFilters = pm.getProjectModel().getStringFilters().iterator();

            while (itStringFilters.hasNext()) {
                Element stringFilter = (Element) itStringFilters.next();

                if ((stringFilter.getAttributeValue(XMLTags.NAME).compareTo(XMLTags.TRIM) == 0) && (stringFilter.getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0)) {
                    checkBoxTrim.setSelected(true);
                }

                if ((stringFilter.getAttributeValue(XMLTags.NAME).compareTo(XMLTags.REMOVE_INTERMEDIATE_WHITESPACES) == 0) && (stringFilter.getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0)) {
                    checkBoxRemoveMultipleIntermediateSpaces.setSelected(true);
                }

                if ((stringFilter.getAttributeValue(XMLTags.NAME).compareTo(XMLTags.SET_NULL) == 0) && (stringFilter.getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0)) {
                    checkBoxSetNull.setSelected(true);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
            parentFrame.setStatusBar(e.toString(), Level.ERROR);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param model_captured DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final boolean canBeShown(boolean model_captured) throws Exception {
        if (model_captured) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void guiInit() throws Exception {
        gridLayout.setColumns(1);
        gridLayout.setHgap(10);
        gridLayout.setRows(1);
        gridLayout.setVgap(10);
        this.setLayout(gridLayout);
        panelStringFilter.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(Color.black, 1), " String Filters "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panelStringFilter.setLayout(null);

        checkBoxTrim.setText("Trim Strings (remove spaces at begin and end of string)");
        checkBoxTrim.setBounds(new Rectangle(14, 22, 360, 23));
        checkBoxTrim.addActionListener(new PanelFilter_checkBoxTrim_actionAdapter(this));

        checkBoxRemoveMultipleIntermediateSpaces.setBounds(new Rectangle(14, 45, 360, 23));
        checkBoxRemoveMultipleIntermediateSpaces.setText("Remove multiple intermediate whitespaces");
        checkBoxRemoveMultipleIntermediateSpaces.addActionListener(new PanelFilter_checkBoxRemoveMultipleIntermediateSpaces_actionAdapter(this));

        checkBoxSetNull.setBounds(new Rectangle(14, 68, 360, 23));
        checkBoxSetNull.setText("Set NULL if String is empty (if column is NULLABLE)");
        checkBoxSetNull.addActionListener(new PanelFilter_checkBoxSetNull_actionAdapter(this));

        this.add(panelStringFilter, null);

        panelStringFilter.add(checkBoxTrim, null);
        panelStringFilter.add(checkBoxRemoveMultipleIntermediateSpaces, null);
        panelStringFilter.add(checkBoxSetNull, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void checkBoxTrim_actionPerformed(ActionEvent e) {
        try {
            Element filter = pm.getProjectModel().getStringFilter(XMLTags.TRIM);

            if (filter != null) {
                if (checkBoxTrim.isSelected()) {
                    filter.setAttribute(XMLTags.PROCESS, "true");
                } else {
                    filter.setAttribute(XMLTags.PROCESS, "false");
                }
            }
        } catch (Exception ex) {
            logger.error(ex.toString());
            parentFrame.setStatusBar(ex.toString(), Level.ERROR);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void checkBoxRemoveMultipleIntermediateSpaces_actionPerformed(ActionEvent e) {
        try {
            Element filter = pm.getProjectModel().getStringFilter(XMLTags.REMOVE_INTERMEDIATE_WHITESPACES);

            if (filter != null) {
                if (checkBoxRemoveMultipleIntermediateSpaces.isSelected()) {
                    filter.setAttribute(XMLTags.PROCESS, "true");
                } else {
                    filter.setAttribute(XMLTags.PROCESS, "false");
                }
            }
        } catch (Exception ex) {
            logger.error(ex.toString());
            parentFrame.setStatusBar(ex.toString(), Level.ERROR);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void checkBoxSetNull_actionPerformed(ActionEvent e) {
        try {
            Element filter = pm.getProjectModel().getStringFilter(XMLTags.SET_NULL);

            if (filter != null) {
                if (checkBoxSetNull.isSelected()) {
                    filter.setAttribute(XMLTags.PROCESS, "true");
                } else {
                    filter.setAttribute(XMLTags.PROCESS, "false");
                }
            }
        } catch (Exception ex) {
            logger.error(ex.toString());
            parentFrame.setStatusBar(ex.toString(), Level.ERROR);
        }
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelFilter_checkBoxTrim_actionAdapter implements java.awt.event.ActionListener {
    PanelFilter adaptee;

    /**
     * Creates a new PanelFilter_checkBoxTrim_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelFilter_checkBoxTrim_actionAdapter(PanelFilter adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.checkBoxTrim_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelFilter_checkBoxRemoveMultipleIntermediateSpaces_actionAdapter implements java.awt.event.ActionListener {
    PanelFilter adaptee;

    /**
     * Creates a new PanelFilter_checkBoxTrim_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelFilter_checkBoxRemoveMultipleIntermediateSpaces_actionAdapter(PanelFilter adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.checkBoxRemoveMultipleIntermediateSpaces_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelFilter_checkBoxSetNull_actionAdapter implements java.awt.event.ActionListener {
    PanelFilter adaptee;

    /**
     * Creates a new PanelFilter_checkBoxSetNull_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelFilter_checkBoxSetNull_actionAdapter(PanelFilter adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.checkBoxSetNull_actionPerformed(e);
    }
}
