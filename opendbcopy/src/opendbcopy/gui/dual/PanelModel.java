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
package opendbcopy.gui.dual;

import opendbcopy.config.OperationType;
import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.gui.DynamicPanel;

import org.jdom.Element;

import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;

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
public class PanelModel extends DynamicPanel {
    private boolean    databaseMetadataRead;
    private GridLayout gridLayout = new GridLayout();
    private JPanel     panelSource = new JPanel();
    private JPanel     panelDestination = new JPanel();
    private JLabel     labelSchemaS = new JLabel();
    private JLabel     labelCatalogS = new JLabel();
    private JLabel     labelTablePatternS = new JLabel();
    private JLabel     labelSchemaD = new JLabel();
    private JLabel     labelTablePatternD = new JLabel();
    private JLabel     labelCatalogD = new JLabel();
    private JComboBox  comboSchemaS = new JComboBox();
    private JComboBox  comboCatalogS = new JComboBox();
    private JComboBox  comboSchemaD = new JComboBox();
    private JComboBox  comboCatalogD = new JComboBox();
    private JTextField tfTablePatternS = new JTextField();
    private JTextField tfTablePatternD = new JTextField();
    private JButton    buttonReadModelD = new JButton();
    private JButton    buttonReadModelS = new JButton();
    private JCheckBox  checkBoxReadSourcePrimaryKeys = new JCheckBox();
    private JCheckBox  checkBoxReadSourceForeignKeys = new JCheckBox();
    private JCheckBox  checkBoxReadSourceIndexes = new JCheckBox();
    private JCheckBox  checkBoxReadDestinationPrimaryKeys = new JCheckBox();
    private JCheckBox  checkBoxReadDestinationForeignKeys = new JCheckBox();
    private JCheckBox  checkBoxReadDestinationIndexes = new JCheckBox();

    /**
     * Creates a new PanelModel object.
     *
     * @param controller DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public PanelModel(MainController controller) throws Exception {
        super(controller);

        guiInit();
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param obj DOCUMENT ME!
     */
    public final void update(Observable o,
                             Object     obj) {
    }

    /**
     * DOCUMENT ME!
     */
    public void onSelect() {
        System.out.println("say hello from PanelModel");

        try {
            // setup comboBox values
            if ((pm.getProjectModel().getSourceConnection().getAttributes().size() > 0) && (pm.getProjectModel().getDestinationConnection().getAttributes().size() > 0)) {
                if ((pm.getProjectModel().getSourceMetadata().getChildren().size() > 0) && (pm.getProjectModel().getDestinationMetadata().getChildren().size() > 0)) {
                    // enable this panel
                    setEnabled(true);

                    // source catalogs
                    fillComboBoxes(comboCatalogS, pm.getProjectModel().getSourceMetadata().getChild(XMLTags.CATALOG).getChildren(XMLTags.ELEMENT));

                    // destination catalogs
                    fillComboBoxes(comboCatalogD, pm.getProjectModel().getDestinationMetadata().getChild(XMLTags.CATALOG).getChildren(XMLTags.ELEMENT));

                    // source schemas
                    fillComboBoxes(comboSchemaS, pm.getProjectModel().getSourceMetadata().getChild(XMLTags.SCHEMA).getChildren(XMLTags.ELEMENT));

                    // destination schemas
                    fillComboBoxes(comboSchemaD, pm.getProjectModel().getDestinationMetadata().getChild(XMLTags.SCHEMA).getChildren(XMLTags.ELEMENT));

                    if (pm.getProjectModel().getSourceSchema().length() > 0) {
                        comboSchemaS.setSelectedItem(pm.getProjectModel().getSourceSchema());
                    }

                    if (pm.getProjectModel().getSourceCatalog().length() > 0) {
                        comboCatalogS.setSelectedItem(pm.getProjectModel().getSourceCatalog());
                    }

                    if (pm.getProjectModel().getSourceTablePattern().length() > 0) {
                        tfTablePatternS.setText(pm.getProjectModel().getSourceTablePattern());
                    }

                    if (pm.getProjectModel().getDestinationSchema().length() > 0) {
                        comboSchemaD.setSelectedItem(pm.getProjectModel().getDestinationSchema());
                    }

                    if (pm.getProjectModel().getDestinationCatalog().length() > 0) {
                        comboCatalogD.setSelectedItem(pm.getProjectModel().getDestinationCatalog());
                    }

                    if (pm.getProjectModel().getDestinationTablePattern().length() > 0) {
                        tfTablePatternD.setText(pm.getProjectModel().getDestinationTablePattern());
                    }
                }
            }

            // set values of checkboxes if available
            if ((pm.getProjectModel().getSourceModel().getAttributes().size() > 0) && (pm.getProjectModel().getDestinationModel().getAttributes().size() > 0)) {
                if (Boolean.valueOf(pm.getProjectModel().getSourceModel().getAttributeValue(XMLTags.READ_PRIMARY_KEYS)).booleanValue()) {
                    checkBoxReadSourcePrimaryKeys.setSelected(true);
                } else {
                    checkBoxReadSourcePrimaryKeys.setSelected(false);
                }

                if (Boolean.valueOf(pm.getProjectModel().getSourceModel().getAttributeValue(XMLTags.READ_FOREIGN_KEYS)).booleanValue()) {
                    checkBoxReadSourceForeignKeys.setSelected(true);
                } else {
                    checkBoxReadSourceForeignKeys.setSelected(false);
                }

                if (Boolean.valueOf(pm.getProjectModel().getSourceModel().getAttributeValue(XMLTags.READ_INDEXES)).booleanValue()) {
                    checkBoxReadSourceIndexes.setSelected(true);
                } else {
                    checkBoxReadSourceIndexes.setSelected(false);
                }

                if (Boolean.valueOf(pm.getProjectModel().getDestinationModel().getAttributeValue(XMLTags.READ_PRIMARY_KEYS)).booleanValue()) {
                    checkBoxReadDestinationPrimaryKeys.setSelected(true);
                } else {
                    checkBoxReadDestinationPrimaryKeys.setSelected(false);
                }

                if (Boolean.valueOf(pm.getProjectModel().getDestinationModel().getAttributeValue(XMLTags.READ_FOREIGN_KEYS)).booleanValue()) {
                    checkBoxReadDestinationForeignKeys.setSelected(true);
                } else {
                    checkBoxReadDestinationForeignKeys.setSelected(false);
                }

                if (Boolean.valueOf(pm.getProjectModel().getDestinationModel().getAttributeValue(XMLTags.READ_INDEXES)).booleanValue()) {
                    checkBoxReadDestinationIndexes.setSelected(true);
                } else {
                    checkBoxReadDestinationIndexes.setSelected(false);
                }

                updateUI();
            } else {
                setEnabled(false);
            }
        } catch (Exception e) {
            postException(e);
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
        gridLayout.setRows(2);
        gridLayout.setVgap(10);
        this.setLayout(gridLayout);
        panelSource.setLayout(null);
        panelDestination.setLayout(null);

        panelSource.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " Source Data Model "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panelDestination.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " Destination Data Model "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // source
        labelCatalogS.setText("Catalog");
        labelCatalogS.setBounds(new Rectangle(12, 32, 100, 20));

        comboCatalogS.setToolTipText("Select a catalog");
        comboCatalogS.setBounds(new Rectangle(130, 28, 183, 20));

        labelSchemaS.setText("Schema Pattern");
        labelSchemaS.setBounds(new Rectangle(12, 64, 100, 20));

        comboSchemaS.setToolTipText("Select a schema");
        comboSchemaS.setBounds(new Rectangle(130, 62, 183, 20));

        labelTablePatternS.setText("Table Pattern");
        labelTablePatternS.setBounds(new Rectangle(12, 96, 100, 20));

        tfTablePatternS.setToolTipText("Enter a table pattern (may use SQL Wildcards)");
        tfTablePatternS.setText("%");
        tfTablePatternS.setBounds(new Rectangle(130, 96, 183, 20));

        buttonReadModelS.setBounds(new Rectangle(370, 28, 190, 24));
        buttonReadModelS.setText(OperationType.CAPTURE_SOURCE_MODEL);
        buttonReadModelS.addActionListener(new PanelModel_buttonReadModelS_actionAdapter(this));
        buttonReadModelS.setToolTipText("Please be patient while capturing ...");

        checkBoxReadSourcePrimaryKeys.setSelected(true);
        checkBoxReadSourcePrimaryKeys.setBounds(new Rectangle(130, 128, 200, 20));
        checkBoxReadSourcePrimaryKeys.setText(" Read Primary Keys");

        checkBoxReadSourceForeignKeys.setSelected(true);
        checkBoxReadSourceForeignKeys.setBounds(new Rectangle(130, 160, 200, 20));
        checkBoxReadSourceForeignKeys.setText(" Read Foreign Keys");

        checkBoxReadSourceIndexes.setSelected(false);
        checkBoxReadSourceIndexes.setBounds(new Rectangle(130, 192, 200, 20));
        checkBoxReadSourceIndexes.setText(" Read Indexes");

        // destination
        labelCatalogD.setText("Catalog");
        labelCatalogD.setBounds(new Rectangle(12, 32, 100, 20));

        comboCatalogD.setToolTipText("Select a catalog");
        comboCatalogD.setBounds(new Rectangle(130, 28, 183, 20));

        labelSchemaD.setText("Schema Pattern");
        labelSchemaD.setBounds(new Rectangle(12, 64, 100, 20));

        comboSchemaD.setBounds(new Rectangle(130, 62, 183, 20));
        comboSchemaD.setToolTipText("Select a schema");

        labelTablePatternD.setText("Table Pattern");
        labelTablePatternD.setBounds(new Rectangle(12, 96, 100, 20));

        tfTablePatternD.setText("%");
        tfTablePatternD.setBounds(new Rectangle(130, 96, 183, 20));
        tfTablePatternD.setToolTipText("Enter a table pattern (may use SQL Wildcards)");

        buttonReadModelD.setBounds(new Rectangle(370, 27, 190, 24));
        buttonReadModelD.setText(OperationType.CAPTURE_DESTINATION_MODEL);
        buttonReadModelD.addActionListener(new PanelModel_buttonReadModelD_actionAdapter(this));
        buttonReadModelD.setToolTipText("Please be patient while capturing ...");

        checkBoxReadDestinationPrimaryKeys.setSelected(true);
        checkBoxReadDestinationPrimaryKeys.setBounds(new Rectangle(130, 128, 200, 20));
        checkBoxReadDestinationPrimaryKeys.setText(" Read Primary Keys");

        checkBoxReadDestinationForeignKeys.setSelected(true);
        checkBoxReadDestinationForeignKeys.setBounds(new Rectangle(130, 160, 200, 20));
        checkBoxReadDestinationForeignKeys.setText(" Read Foreign Keys");

        checkBoxReadDestinationIndexes.setSelected(true);
        checkBoxReadDestinationIndexes.setBounds(new Rectangle(130, 192, 200, 20));
        checkBoxReadDestinationIndexes.setText(" Read Indexes");

        checkBoxReadDestinationPrimaryKeys.setSelected(true);
        checkBoxReadDestinationForeignKeys.setSelected(true);
        checkBoxReadDestinationIndexes.setSelected(false);

        this.add(panelSource, null);
        this.add(panelDestination, null);
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

        panelDestination.add(labelCatalogD, null);
        panelDestination.add(labelSchemaD, null);
        panelDestination.add(labelTablePatternD, null);
        panelDestination.add(comboCatalogD, null);
        panelDestination.add(comboSchemaD, null);
        panelDestination.add(tfTablePatternD, null);
        panelDestination.add(buttonReadModelD, null);
        panelDestination.add(checkBoxReadDestinationPrimaryKeys, null);
        panelDestination.add(checkBoxReadDestinationForeignKeys, null);
        panelDestination.add(checkBoxReadDestinationIndexes, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonReadModelS_actionPerformed(ActionEvent e) {
        try {
            pm.getProjectModel().setSourceCatalog((String) this.comboCatalogS.getSelectedItem());
            pm.getProjectModel().setSourceSchema((String) this.comboSchemaS.getSelectedItem());
            pm.getProjectModel().setSourceTablePattern(this.tfTablePatternS.getText());

            Element operation = new Element(XMLTags.OPERATION);
            operation.setAttribute(XMLTags.NAME, OperationType.CAPTURE_SOURCE_MODEL);
            operation.setAttribute(XMLTags.READ_PRIMARY_KEYS, Boolean.toString(checkBoxReadSourcePrimaryKeys.isSelected()));
            operation.setAttribute(XMLTags.READ_FOREIGN_KEYS, Boolean.toString(checkBoxReadSourceForeignKeys.isSelected()));
            operation.setAttribute(XMLTags.READ_INDEXES, Boolean.toString(checkBoxReadSourceIndexes.isSelected()));

            execute(operation, "capturing source model done");
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
            pm.getProjectModel().setDestinationCatalog((String) this.comboCatalogD.getSelectedItem());
            pm.getProjectModel().setDestinationSchema((String) this.comboSchemaD.getSelectedItem());
            pm.getProjectModel().setDestinationTablePattern(this.tfTablePatternD.getText());

            Element operation = new Element(XMLTags.OPERATION);
            operation.setAttribute(XMLTags.NAME, OperationType.CAPTURE_DESTINATION_MODEL);
            operation.setAttribute(XMLTags.READ_PRIMARY_KEYS, Boolean.toString(checkBoxReadDestinationPrimaryKeys.isSelected()));
            operation.setAttribute(XMLTags.READ_FOREIGN_KEYS, Boolean.toString(checkBoxReadDestinationForeignKeys.isSelected()));
            operation.setAttribute(XMLTags.READ_INDEXES, Boolean.toString(checkBoxReadDestinationIndexes.isSelected()));

            execute(operation, "capturing destination model done");
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
