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
package opendbcopy.gui.database;

import info.clearthought.layout.TableLayout;

import opendbcopy.config.GUI;
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
    private JPanel        panelDestination = new JPanel();
    private JLabel        labelSchemaS = new JLabel();
    private JLabel        labelCatalogS = new JLabel();
    private JLabel        labelTablePatternS = new JLabel();
    private JLabel        labelSchemaD = new JLabel();
    private JLabel        labelTablePatternD = new JLabel();
    private JLabel        labelCatalogD = new JLabel();
    private JComboBox     comboSchemaS = new JComboBox();
    private JComboBox     comboCatalogS = new JComboBox();
    private JComboBox     comboSchemaD = new JComboBox();
    private JComboBox     comboCatalogD = new JComboBox();
    private JTextField    tfTablePatternS = new JTextField();
    private JTextField    tfTablePatternD = new JTextField();
    private JButton       buttonReadModelD = new JButton();
    private JButton       buttonReadModelS = new JButton();
    private JCheckBox     checkBoxReadSourcePrimaryKeys = new JCheckBox();
    private JCheckBox     checkBoxReadSourceForeignKeys = new JCheckBox();
    private JCheckBox     checkBoxReadSourceIndexes = new JCheckBox();
    private JCheckBox     checkBoxUseQualifiedSourceTableName = new JCheckBox();
    private JCheckBox     checkBoxReadDestinationPrimaryKeys = new JCheckBox();
    private JCheckBox     checkBoxReadDestinationForeignKeys = new JCheckBox();
    private JCheckBox     checkBoxReadDestinationIndexes = new JCheckBox();
    private JCheckBox     checkBoxUseQualifiedDestinationTableName = new JCheckBox();

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
            if (model.getDbMode() == model.DUAL_MODE) {
                panelDestination.setVisible(true);

                // setup comboBox values
                if ((model.getSourceConnection().getAttributes().size() > 0) && (model.getDestinationConnection().getAttributes().size() > 0)) {
                    if ((model.getSourceMetadata().getChildren().size() > 0) && (model.getDestinationMetadata().getChildren().size() > 0)) {
                        // enable this panel
                        enablePanels(true);
                        setValuesSource();
                        setValuesDestination();
                    }
                }
                // disable panels
                else {
                    enablePanels(false);
                }
            } else {
                panelDestination.setVisible(false);

                // setup comboBox values
                if (model.getSourceConnection().getAttributes().size() > 0) {
                    if (model.getSourceMetadata().getChildren().size() > 0) {
                        setValuesSource();

                        // enable this panel
                        enablePanels(true);
                    }
                }
                // disable panels
                else {
                    enablePanels(false);
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
            } else if (source == checkBoxReadDestinationPrimaryKeys) {
                model.getDestinationModel().setAttribute(XMLTags.READ_PRIMARY_KEYS, Boolean.toString(enable));
            } else if (source == checkBoxReadSourceForeignKeys) {
                model.getSourceModel().setAttribute(XMLTags.READ_FOREIGN_KEYS, Boolean.toString(enable));
            } else if (source == checkBoxReadDestinationForeignKeys) {
                model.getDestinationModel().setAttribute(XMLTags.READ_FOREIGN_KEYS, Boolean.toString(enable));
            } else if (source == checkBoxReadSourceIndexes) {
                model.getSourceModel().setAttribute(XMLTags.READ_INDEXES, Boolean.toString(enable));
            } else if (source == checkBoxReadDestinationIndexes) {
                model.getDestinationModel().setAttribute(XMLTags.READ_INDEXES, Boolean.toString(enable));
            } else if (source == checkBoxUseQualifiedSourceTableName) {
                model.getSourceModel().setAttribute(XMLTags.USE_QUALIFIED_TABLE_NAME, Boolean.toString(enable));
            } else if (source == checkBoxUseQualifiedDestinationTableName) {
                model.getDestinationModel().setAttribute(XMLTags.USE_QUALIFIED_TABLE_NAME, Boolean.toString(enable));
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

            if (Boolean.valueOf(model.getSourceModel().getAttributeValue(XMLTags.USE_QUALIFIED_TABLE_NAME)).booleanValue()) {
                checkBoxUseQualifiedSourceTableName.setSelected(true);
            } else {
                checkBoxUseQualifiedSourceTableName.setSelected(false);
            }
        } catch (Exception e) {
            // ignore it as those attributes are not mandatory
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    private void setValuesDestination() throws MissingAttributeException, MissingElementException {
        // destination catalogs
        fillComboBoxes(comboCatalogD, model.getDestinationMetadata().getChild(XMLTags.CATALOG).getChildren(XMLTags.ELEMENT));

        // destination schemas
        fillComboBoxes(comboSchemaD, model.getDestinationMetadata().getChild(XMLTags.SCHEMA).getChildren(XMLTags.ELEMENT));

        if (model.getDestinationSchema().length() > 0) {
            comboSchemaD.setSelectedItem(model.getDestinationSchema());
        }

        if (model.getDestinationCatalog().length() > 0) {
            comboCatalogD.setSelectedItem(model.getDestinationCatalog());
        }

        if (model.getDestinationTablePattern().length() > 0) {
            tfTablePatternD.setText(model.getDestinationTablePattern());
        }

        try {
            if (Boolean.valueOf(model.getDestinationModel().getAttributeValue(XMLTags.READ_PRIMARY_KEYS)).booleanValue()) {
                checkBoxReadDestinationPrimaryKeys.setSelected(true);
            } else {
                checkBoxReadDestinationPrimaryKeys.setSelected(false);
            }

            if (Boolean.valueOf(model.getDestinationModel().getAttributeValue(XMLTags.READ_FOREIGN_KEYS)).booleanValue()) {
                checkBoxReadDestinationForeignKeys.setSelected(true);
            } else {
                checkBoxReadDestinationForeignKeys.setSelected(false);
            }

            if (Boolean.valueOf(model.getDestinationModel().getAttributeValue(XMLTags.READ_INDEXES)).booleanValue()) {
                checkBoxReadDestinationIndexes.setSelected(true);
            } else {
                checkBoxReadDestinationIndexes.setSelected(false);
            }

            if (Boolean.valueOf(model.getDestinationModel().getAttributeValue(XMLTags.USE_QUALIFIED_TABLE_NAME)).booleanValue()) {
                checkBoxUseQualifiedDestinationTableName.setSelected(true);
            } else {
                checkBoxUseQualifiedDestinationTableName.setSelected(false);
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

        for (int i = 0; i < panelDestination.getComponentCount(); i++) {
            panelDestination.getComponent(i).setEnabled(enable);
        }

        this.setEnabled(enable);
    }

    /**
     * DOCUMENT ME!
     */
    private void guiInit() {
        double[][] size = {
                              { GUI.B, GUI.P, GUI.HG, GUI.F, 40, GUI.P, GUI.HG, GUI.P, GUI.B }, // Columns
        { GUI.B, GUI.P, GUI.VS, GUI.P, GUI.VS, GUI.P, GUI.VS, GUI.P, GUI.B }
        }; // Rows

        TableLayout layout = new TableLayout(size);

        panelSource.setLayout(layout);
        panelDestination.setLayout(layout);

        panelSource.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.model.sourceModel") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panelDestination.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1),  " " + rm.getString("text.model.destinationModel") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // source
        labelCatalogS.setText(rm.getString("text.model.catalog"));
        comboCatalogS.setToolTipText(rm.getString("text.model.catalog.toolTip"));

        labelSchemaS.setText(rm.getString("text.model.schema"));
        comboSchemaS.setToolTipText(rm.getString("text.model.schema.toolTip"));

        labelTablePatternS.setText(rm.getString("text.model.tablePattern"));
        tfTablePatternS.setToolTipText(rm.getString("text.model.tablePattern.toolTip"));
        tfTablePatternS.setText("%");

        buttonReadModelS.setText(rm.getString("button.captureSourceModel"));
        buttonReadModelS.addActionListener(new PanelModel_buttonReadModelS_actionAdapter(this));
        buttonReadModelS.setToolTipText(rm.getString("text.model.bePatient"));

        checkBoxReadSourcePrimaryKeys.setText(" " + rm.getString("text.model.readPrimaryKey"));
        checkBoxReadSourcePrimaryKeys.addItemListener(this);

        checkBoxReadSourceForeignKeys.setText(" " + rm.getString("text.model.readForeignKey"));
        checkBoxReadSourceForeignKeys.addItemListener(this);

        checkBoxReadSourceIndexes.setText(" " + rm.getString("text.model.readIndex"));
        checkBoxReadSourceIndexes.addItemListener(this);

        checkBoxUseQualifiedSourceTableName.setText(" " + rm.getString("text.model.qualifiedTableName"));
        checkBoxUseQualifiedSourceTableName.addItemListener(this);

        // source layout
        // first line
        panelSource.add(labelCatalogS, "1, 1");
        panelSource.add(comboCatalogS, "3, 1");
        panelSource.add(checkBoxReadSourcePrimaryKeys, "5, 1");
        panelSource.add(buttonReadModelS, "7, 1");

        // second line
        panelSource.add(labelSchemaS, "1, 3");
        panelSource.add(comboSchemaS, "3, 3");
        panelSource.add(checkBoxReadSourceForeignKeys, "5, 3");

        // third line
        panelSource.add(labelTablePatternS, "1, 5");
        panelSource.add(tfTablePatternS, "3, 5");
        panelSource.add(checkBoxReadSourceIndexes, "5, 5");

        // fourth line
        panelSource.add(checkBoxUseQualifiedSourceTableName, "5, 7");

        // destination
        labelCatalogD.setText(rm.getString("text.model.catalog"));
        comboCatalogD.setToolTipText(rm.getString("text.model.catalog.toolTip"));

        labelSchemaD.setText(rm.getString("text.model.schema"));
        comboSchemaD.setToolTipText(rm.getString("text.model.schema.toolTip"));

        labelTablePatternD.setText(rm.getString("text.model.tablePattern"));
        tfTablePatternD.setText("%");
        tfTablePatternD.setToolTipText(rm.getString("text.model.tablePattern.toolTip"));

        buttonReadModelD.setText(rm.getString("button.captureDestinationModel"));
        buttonReadModelD.addActionListener(new PanelModel_buttonReadModelD_actionAdapter(this));
        buttonReadModelD.setToolTipText(rm.getString("text.model.bePatient"));

        checkBoxReadDestinationPrimaryKeys.setText(" " + rm.getString("text.model.readPrimaryKey"));
        checkBoxReadDestinationPrimaryKeys.addItemListener(this);

        checkBoxReadDestinationForeignKeys.setText(" " + rm.getString("text.model.readForeignKey"));
        checkBoxReadDestinationForeignKeys.addItemListener(this);

        checkBoxReadDestinationIndexes.setText(" " + rm.getString("text.model.readIndex"));
        checkBoxReadDestinationIndexes.addItemListener(this);

        checkBoxUseQualifiedDestinationTableName.setText(" " + rm.getString("text.model.qualifiedTableName"));
        checkBoxUseQualifiedDestinationTableName.addItemListener(this);

        // destination layout
        // first line
        panelDestination.add(labelCatalogD, "1, 1");
        panelDestination.add(comboCatalogD, "3, 1");
        panelDestination.add(checkBoxReadDestinationPrimaryKeys, "5, 1");
        panelDestination.add(buttonReadModelD, "7, 1");

        // second line
        panelDestination.add(labelSchemaD, "1, 3");
        panelDestination.add(comboSchemaD, "3, 3");
        panelDestination.add(checkBoxReadDestinationForeignKeys, "5, 3");

        // third line
        panelDestination.add(labelTablePatternD, "1, 5");
        panelDestination.add(tfTablePatternD, "3, 5");
        panelDestination.add(checkBoxReadDestinationIndexes, "5, 5");

        // fourth line
        panelDestination.add(checkBoxUseQualifiedDestinationTableName, "5, 7");

        // tempororay
        this.setLayout(new GridLayout(2, 1, 10, 10));

        this.add(panelSource, null);
        this.add(panelDestination, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonReadModelS_actionPerformed(ActionEvent e) {
        try {
            model.setSourceCatalog((String) comboCatalogS.getSelectedItem());
            model.setSourceSchema((String) comboSchemaS.getSelectedItem());
            model.setSourceTablePattern(tfTablePatternS.getText());            

            Element operation = new Element(XMLTags.OPERATION);
            operation.setAttribute(XMLTags.NAME, OperationType.CAPTURE_SOURCE_MODEL);

            String[] param = { rm.getString("text.model.sourceModel") };

            execute(operation, rm.getString("message.model.successful", param));
        } catch (Exception ex) {
            postException(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonReadModelD_actionPerformed(ActionEvent e) {
        try {
            model.setDestinationCatalog((String) this.comboCatalogD.getSelectedItem());
            model.setDestinationSchema((String) this.comboSchemaD.getSelectedItem());
            model.setDestinationTablePattern(this.tfTablePatternD.getText());

            Element operation = new Element(XMLTags.OPERATION);
            operation.setAttribute(XMLTags.NAME, OperationType.CAPTURE_DESTINATION_MODEL);

            String[] param = { rm.getString("text.model.destinationModel") };

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


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelModel_buttonReadModelD_actionAdapter implements java.awt.event.ActionListener {
    PanelModel adaptee;

    /**
     * Creates a new PanelModel_buttonReadModelD_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelModel_buttonReadModelD_actionAdapter(PanelModel adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonReadModelD_actionPerformed(e);
    }
}
