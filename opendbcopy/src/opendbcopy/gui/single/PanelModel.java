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
package opendbcopy.gui.single;

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
import java.util.StringTokenizer;

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
    private JLabel     labelSchemaS = new JLabel();
    private JLabel     labelCatalogS = new JLabel();
    private JLabel     labelTablePatternS = new JLabel();
    private JComboBox  comboSchemaS = new JComboBox();
    private JComboBox  comboCatalogS = new JComboBox();
    private JTextField tfTablePatternS = new JTextField();
    private JButton    buttonReadModelS = new JButton();
    private JCheckBox  checkBoxReadSourcePrimaryKeys = new JCheckBox();
    private JCheckBox  checkBoxReadSourceForeignKeys = new JCheckBox();
    private JCheckBox  checkBoxReadSourceIndexes = new JCheckBox();

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
        try {
            // setup comboBox values
            if (pm.getProjectModel().getDbMode() == pm.getProjectModel().DUAL_MODE) {
                if ((pm.getProjectModel().getSourceConnection().getAttributes().size() > 0) && (pm.getProjectModel().getDestinationConnection().getAttributes().size() > 0)) {
                    if ((pm.getProjectModel().getSourceMetadata().getChildren().size() > 0) && (pm.getProjectModel().getDestinationMetadata().getChildren().size() > 0)) {
                        // source catalogs
                        List sourceCatalogs = pm.getProjectModel().getSourceMetadata().getChild(XMLTags.CATALOG).getChildren();
                        fillComboBoxes(comboCatalogS, sourceCatalogs.iterator());

                        // try to retrieve catalog from url
                        String catalog = findCatalog(sourceCatalogs.iterator(), pm.getProjectModel().getSourceConnection().getAttributeValue(XMLTags.URL));

                        if (catalog != null) {
                            pm.getProjectModel().setSourceCatalog(catalog);
                        }

                        // source schemas
                        fillComboBoxes(comboSchemaS, pm.getProjectModel().getSourceMetadata().getChild(XMLTags.SCHEMA).getChildren().iterator());

                        // try to retrieve source schema from user
                        String schema = findSchema(pm.getProjectModel().getSourceMetadata().getChild(XMLTags.SCHEMA).getChildren().iterator(), pm.getProjectModel().getSourceConnection().getAttributeValue(XMLTags.USERNAME));

                        if (schema != null) {
                            pm.getProjectModel().setSourceSchema(schema);
                        }

                        if (pm.getProjectModel().getSourceSchema().length() > 0) {
                            comboSchemaS.setSelectedItem(pm.getProjectModel().getSourceSchema());
                        }

                        if (pm.getProjectModel().getSourceCatalog().length() > 0) {
                            comboCatalogS.setSelectedItem(pm.getProjectModel().getSourceCatalog());
                        }

                        if (pm.getProjectModel().getSourceTablePattern().length() > 0) {
                            tfTablePatternS.setText(pm.getProjectModel().getSourceTablePattern());
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
                }
            } else {
                if (pm.getProjectModel().getSourceMetadata().getChildren().size() > 0) {
                    // source catalogs
                    List sourceCatalogs = pm.getProjectModel().getSourceMetadata().getChild(XMLTags.CATALOG).getChildren();
                    fillComboBoxes(comboCatalogS, sourceCatalogs.iterator());

                    // try to retrieve catalog from url
                    String catalog = findCatalog(sourceCatalogs.iterator(), pm.getProjectModel().getSourceConnection().getAttributeValue(XMLTags.URL));

                    if (catalog != null) {
                        pm.getProjectModel().setSourceCatalog(catalog);
                    }

                    // source schemas
                    fillComboBoxes(comboSchemaS, pm.getProjectModel().getSourceMetadata().getChild(XMLTags.SCHEMA).getChildren().iterator());

                    // try to retrieve source schema from user
                    String schema = findSchema(pm.getProjectModel().getSourceMetadata().getChild(XMLTags.SCHEMA).getChildren().iterator(), pm.getProjectModel().getSourceConnection().getAttributeValue(XMLTags.USERNAME));

                    if (schema != null) {
                        pm.getProjectModel().setSourceSchema(schema);
                    }

                    if (pm.getProjectModel().getSourceSchema().length() > 0) {
                        comboSchemaS.setSelectedItem(pm.getProjectModel().getSourceSchema());
                    }

                    if (pm.getProjectModel().getSourceCatalog().length() > 0) {
                        comboCatalogS.setSelectedItem(pm.getProjectModel().getSourceCatalog());
                    }

                    if (pm.getProjectModel().getSourceTablePattern().length() > 0) {
                        tfTablePatternS.setText(pm.getProjectModel().getSourceTablePattern());
                    }
                }

                // set values of checkboxes if available
                if (pm.getProjectModel().getSourceModel().getAttributes().size() > 0) {
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
                }
            }
        } catch (Exception e) {
            postException(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final boolean canContinue() throws Exception {
        if (pm.getProjectModel().getDbMode() == pm.getProjectModel().DUAL_MODE) {
            if (pm.getProjectModel().isSourceModelCaptured() && pm.getProjectModel().isDestinationModelCaptured() && pm.getProjectModel().isMappingSetup()) {
                return true;
            } else {
                return false;
            }
        } else {
            if (pm.getProjectModel().isSourceModelCaptured()) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param box DOCUMENT ME!
     * @param iterator DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void fillComboBoxes(JComboBox box,
                                Iterator  iterator) throws Exception {
        box.removeAllItems();

        while (iterator.hasNext()) {
            box.addItem(((Element) iterator.next()).getAttributeValue(XMLTags.NAME));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param itCatalogs DOCUMENT ME!
     * @param url DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private String findCatalog(Iterator itCatalogs,
                               String   url) throws Exception {
        StringTokenizer st = new StringTokenizer(url, "/");
        String          lastElement = "";

        // get last element
        while (st.hasMoreElements()) {
            lastElement = (String) st.nextElement();
        }

        while (itCatalogs.hasNext()) {
            Element catalogElement = (Element) itCatalogs.next();

            if (catalogElement.getAttributeValue(XMLTags.NAME).compareToIgnoreCase(lastElement) == 0) {
                return catalogElement.getAttributeValue(XMLTags.NAME);
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param itSchemas DOCUMENT ME!
     * @param user DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private String findSchema(Iterator itSchemas,
                              String   user) throws Exception {
        while (itSchemas.hasNext()) {
            Element schemaElement = (Element) itSchemas.next();

            if (schemaElement.getAttributeValue(XMLTags.NAME).compareToIgnoreCase(user) == 0) {
                return schemaElement.getAttributeValue(XMLTags.NAME);
            }
        }

        return null;
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

        panelSource.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " Source Data Model "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

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
