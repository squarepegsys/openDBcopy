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
package opendbcopy.plugin.schemageneration.gui;

import opendbcopy.config.OperationType;
import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.gui.DynamicPanel;
import opendbcopy.gui.PluginGui;

import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;

import org.jdom.Element;

import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PanelModel extends DynamicPanel implements ItemListener {
    private DatabaseModel model;
    private boolean       databaseMetadataRead;
    private GridLayout    gridLayout = new GridLayout();
    private JPanel        panelSource = new JPanel();
    private JLabel        labelSchemaS = new JLabel();
    private JLabel        labelCatalogS = new JLabel();
    private JLabel        labelTablePatternS = new JLabel();
    private JComboBox     comboSchemaS = new JComboBox();
    private JComboBox     comboCatalogS = new JComboBox();
    private JTextField    tfTablePatternS = new JTextField();
    private JButton       buttonReadModelS = new JButton();
    private JCheckBox     checkBoxReadSourcePrimaryKeys = new JCheckBox();
    private JCheckBox     checkBoxReadSourceForeignKeys = new JCheckBox();
    private JCheckBox     checkBoxReadSourceIndexes = new JCheckBox();

    /**
     * Creates a new PanelModel object.
     *
     * @param controller DOCUMENT ME!
     * @param workingMode DOCUMENT ME!
     * @param registerAsObserver DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public PanelModel(MainController controller,
                      PluginGui      workingMode,
                      Boolean        registerAsObserver) throws Exception {
        super(controller, workingMode, registerAsObserver);

        model = (DatabaseModel) super.model;

        guiInit();
        enablePanels(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param obj DOCUMENT ME!
     */
    public final void update(Observable o,
                             Object     obj) {
        onSelect();
    }

    /**
     * DOCUMENT ME!
     */
    public void onSelect() {
        try {
            // setup comboBox values
            if ((model.getSourceConnection().getAttributes().size() > 0) && (model.getDestinationConnection().getAttributes().size() > 0)) {
                if ((model.getSourceMetadata().getChildren().size() > 0) && (model.getDestinationMetadata().getChildren().size() > 0)) {
                    // enable this panel
                    enablePanels(true);
                    setValuesSource();
                }
            }
        } catch (Exception e) {
            postException(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void itemStateChanged(ItemEvent e) {
        Object  source = e.getItemSelectable();
        boolean enable = false;

        if (e.getStateChange() == ItemEvent.SELECTED) {
            enable = true;
        }

        try {
            if (source == checkBoxReadSourcePrimaryKeys) {
                model.getSourceModel().setAttribute(XMLTags.READ_PRIMARY_KEYS, Boolean.toString(enable));
            } else if (source == checkBoxReadSourceForeignKeys) {
                model.getSourceModel().setAttribute(XMLTags.READ_FOREIGN_KEYS, Boolean.toString(enable));
            } else if (source == checkBoxReadSourceIndexes) {
                model.getSourceModel().setAttribute(XMLTags.READ_INDEXES, Boolean.toString(enable));
            }
        } catch (MissingElementException ex) {
            postException(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    private void setValuesSource() throws MissingAttributeException, MissingElementException {
        // source catalogs
        fillComboBoxes(comboCatalogS, model.getSourceMetadata().getChild(XMLTags.CATALOG).getChildren(XMLTags.ELEMENT));

        // source schemas
        fillComboBoxes(comboSchemaS, model.getSourceMetadata().getChild(XMLTags.SCHEMA).getChildren(XMLTags.ELEMENT));

        if (model.getSourceSchema().length() > 0) {
            comboSchemaS.setSelectedItem(model.getSourceSchema());
        }

        if (model.getSourceCatalog().length() > 0) {
            comboCatalogS.setSelectedItem(model.getSourceCatalog());
        }

        if (model.getSourceTablePattern().length() > 0) {
            tfTablePatternS.setText(model.getSourceTablePattern());
        }

        try {
            if (Boolean.valueOf(model.getSourceModel().getAttributeValue(XMLTags.READ_PRIMARY_KEYS)).booleanValue()) {
                checkBoxReadSourcePrimaryKeys.setSelected(true);
            } else {
                checkBoxReadSourcePrimaryKeys.setSelected(false);
            }

            if (Boolean.valueOf(model.getSourceModel().getAttributeValue(XMLTags.READ_FOREIGN_KEYS)).booleanValue()) {
                checkBoxReadSourceForeignKeys.setSelected(true);
            } else {
                checkBoxReadSourceForeignKeys.setSelected(false);
            }

            if (Boolean.valueOf(model.getSourceModel().getAttributeValue(XMLTags.READ_INDEXES)).booleanValue()) {
                checkBoxReadSourceIndexes.setSelected(true);
            } else {
                checkBoxReadSourceIndexes.setSelected(false);
            }
        } catch (Exception e) {
            // ignore it as those attributes are not mandatory
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param box DOCUMENT ME!
     * @param list DOCUMENT ME!
     */
    private void fillComboBoxes(JComboBox box,
                                List      list) {
        if ((box != null) && (list != null) && (list.size() > 0)) {
            box.removeAllItems();

            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                box.addItem(((Element) iterator.next()).getAttributeValue(XMLTags.NAME));
            }
        } else if ((box != null) && (list.size() == 0)) {
            box.setEnabled(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param enable DOCUMENT ME!
     */
    private void enablePanels(boolean enable) {
        for (int i = 0; i < panelSource.getComponentCount(); i++) {
            panelSource.getComponent(i).setEnabled(enable);
        }

        this.setEnabled(enable);
    }

    /**
     * DOCUMENT ME!
     */
    private void guiInit() {
        gridLayout.setColumns(1);
        gridLayout.setHgap(10);
        gridLayout.setRows(2);
        gridLayout.setVgap(10);
        this.setLayout(gridLayout);
        panelSource.setLayout(null);

        panelSource.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " Source Data Model "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // source
        labelCatalogS.setText(rm.getString("text.model.catalog"));
        labelCatalogS.setBounds(new Rectangle(12, 26, 100, 20));

        comboCatalogS.setToolTipText(rm.getString("text.model.catalog.toolTip"));
        comboCatalogS.setBounds(new Rectangle(130, 22, 183, 20));

        labelSchemaS.setText(rm.getString("text.model.schema"));
        labelSchemaS.setBounds(new Rectangle(12, 58, 100, 20));

        comboSchemaS.setToolTipText(rm.getString("text.model.schema.toolTip"));
        comboSchemaS.setBounds(new Rectangle(130, 56, 183, 20));

        labelTablePatternS.setText(rm.getString("text.model.tablePattern"));
        labelTablePatternS.setBounds(new Rectangle(12, 90, 100, 20));

        tfTablePatternS.setToolTipText(rm.getString("text.model.tablePattern.toolTip"));
        tfTablePatternS.setText("%");
        tfTablePatternS.setBounds(new Rectangle(130, 90, 183, 20));

        buttonReadModelS.setBounds(new Rectangle(370, 22, 230, 24));
        buttonReadModelS.setText(rm.getString("button.captureSourceModel"));
        buttonReadModelS.addActionListener(new PanelModel_buttonReadModelS_actionAdapter(this));
        buttonReadModelS.setToolTipText(rm.getString("text.model.bePatient"));

        checkBoxReadSourcePrimaryKeys.setSelected(true);
        checkBoxReadSourcePrimaryKeys.setBounds(new Rectangle(130, 116, 400, 20));
        checkBoxReadSourcePrimaryKeys.setText(" " + rm.getString("text.model.readPrimaryKey"));
        checkBoxReadSourcePrimaryKeys.addItemListener(this);

        checkBoxReadSourceForeignKeys.setSelected(true);
        checkBoxReadSourceForeignKeys.setBounds(new Rectangle(130, 136, 400, 20));
        checkBoxReadSourceForeignKeys.setText(" " + rm.getString("text.model.readForeignKey"));
        checkBoxReadSourceForeignKeys.addItemListener(this);

        checkBoxReadSourceIndexes.setSelected(false);
        checkBoxReadSourceIndexes.setBounds(new Rectangle(130, 156, 400, 20));
        checkBoxReadSourceIndexes.setText(" " + rm.getString("text.model.readIndex"));
        checkBoxReadSourceIndexes.addItemListener(this);

        this.add(panelSource, null);
        panelSource.add(labelCatalogS, null);
        panelSource.add(labelSchemaS, null);
        panelSource.add(labelTablePatternS, null);
        panelSource.add(comboCatalogS, null);
        panelSource.add(comboSchemaS, null);
        panelSource.add(tfTablePatternS, null);
        panelSource.add(buttonReadModelS, null);
        panelSource.add(checkBoxReadSourcePrimaryKeys, null);
        panelSource.add(checkBoxReadSourceForeignKeys, null);
        panelSource.add(checkBoxReadSourceIndexes, null);

        // enable checkboxes by default
        checkBoxReadSourcePrimaryKeys.setSelected(true);
        checkBoxReadSourceForeignKeys.setSelected(true);
        checkBoxReadSourceIndexes.setSelected(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonReadModelS_actionPerformed(ActionEvent e) {
        try {
            model.setSourceCatalog((String) this.comboCatalogS.getSelectedItem());
            model.setSourceSchema((String) this.comboSchemaS.getSelectedItem());
            model.setSourceTablePattern(this.tfTablePatternS.getText());

            Element operation = new Element(XMLTags.OPERATION);
            operation.setAttribute(XMLTags.NAME, OperationType.CAPTURE_SOURCE_MODEL);
            operation.setAttribute(XMLTags.READ_PRIMARY_KEYS, Boolean.toString(checkBoxReadSourcePrimaryKeys.isSelected()));
            operation.setAttribute(XMLTags.READ_FOREIGN_KEYS, Boolean.toString(checkBoxReadSourceForeignKeys.isSelected()));
            operation.setAttribute(XMLTags.READ_INDEXES, Boolean.toString(checkBoxReadSourceIndexes.isSelected()));

            String[] param = { rm.getString("text.model.sourceModel") };

            execute(operation, rm.getString("message.model.successful", param));
        } catch (Exception ex) {
            postException(ex);
        }
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelModel_buttonReadModelS_actionAdapter implements java.awt.event.ActionListener {
    PanelModel adaptee;

    /**
     * Creates a new PanelModel_buttonReadModelS_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelModel_buttonReadModelS_actionAdapter(PanelModel adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonReadModelS_actionPerformed(e);
    }
}
