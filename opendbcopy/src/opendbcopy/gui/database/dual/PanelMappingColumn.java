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
package opendbcopy.gui.database.dual;

import opendbcopy.config.OperationType;
import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.gui.DynamicPanel;
import opendbcopy.gui.WorkingMode;

import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.exception.MissingElementException;

import opendbcopy.swing.JTableX;
import opendbcopy.swing.RowEditorModel;

import org.jdom.Element;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PanelMappingColumn extends DynamicPanel {
    private static final String    SQL_START = "SELECT <RECORDS> FROM ";
    private static final String    WHERE = " WHERE";
    private DatabaseModel          model;
    private Object[][]             dataMapping;
    private Object[][]             dataProcess;
    private String                 currentTable;
    private DefaultMutableTreeNode top;
    private MappingColumnModel     mappingColumnModel;
    private ProcessColumnModel     processColumnModel;
    private RowEditorModel         rowModel;
    private DefaultCellEditor      dce;
    private boolean                select_all = false;
    private Border                 borderRightPanel;
    private BorderLayout           borderLayout = new BorderLayout();
    private BorderLayout           borderLayoutRight = new BorderLayout();
    private BorderLayout           borderLayoutTextfield = new BorderLayout();
    private GridLayout             gridLayoutFilter = new GridLayout();
    private JPanel                 panelOptions = new JPanel();
    private JPanel                 panelRight = new JPanel();
    private JPanel                 panelTableFilters = new JPanel();
    private JPanel                 panelMappingColumns = null;
    private JPanel                 panelProcessColumns = null;
    private JPanel                 panelSelection = new JPanel();
    private JPanel                 panelControlFilter = new JPanel();
    private JSplitPane             splitPane = new JSplitPane();
    private JScrollPane            scrollPaneTree = null;
    private JScrollPane            scrollPaneTables = null;
    private JTree                  treeSourceTables = new JTree();
    private JTableX                tableMappingColumn = new JTableX();
    private JTableX                tableProcessColumn = new JTableX();
    private JTextField             tfFilter = new JTextField();
    private JButton                buttonApplyTestFilter = new JButton();
    private JButton                buttonDeleteFilter = new JButton();
    private JCheckBox              checkBoxProcess = new JCheckBox();
    private JLabel                 labelSelect = new JLabel();
    private JLabel                 labelInfo = new JLabel();

    /**
     * Creates a new PanelMappingColumn object.
     *
     * @param controller DOCUMENT ME!
     * @param workingMode DOCUMENT ME!
     * @param registerAsObserver DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public PanelMappingColumn(MainController controller,
                              WorkingMode    workingMode,
                              Boolean        registerAsObserver) throws Exception {
        super(controller, workingMode, registerAsObserver);
        model = (DatabaseModel) super.model;
        guiInit();
        loadMappingTables();
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param obj DOCUMENT ME!
     */
    public final void update(Observable o,
                             Object     obj) {
        if ((currentTable != null) && (currentTable.length() > 0)) {
            try {
                Element tableFilter = model.getTableFilter(currentTable);

                if (tableFilter != null) {
                    tfFilter.setText(tableFilter.getAttributeValue(XMLTags.VALUE));
                }
            } catch (Exception e) {
                postException(e);
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public final void onSelect() {
        try {
            loadMappingTables();
        } catch (Exception e) {
            postException(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void loadMappingTables() throws MissingElementException {
        if (model.isMappingSetup()) {
            // remove component
            splitPane.remove(scrollPaneTree);

            top = new DefaultMutableTreeNode(rm.getString("text.column.mappedSourceTableView"));
            createMappingNodes(top);
            treeSourceTables = new JTree(top);

            // Listen for when the selection changes.
            treeSourceTables.addTreeSelectionListener(new TreeSelectionListener() {
                    public void valueChanged(TreeSelectionEvent e) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeSourceTables.getLastSelectedPathComponent();

                        if (node == null) {
                            return;
                        }

                        if (node.getLevel() == 1) {
                            try {
                                loadMappingColumnsAndFilter(node.toString());
                            } catch (Exception ex) {
                                postException(ex);
                            }
                        }
                    }
                });

            // add component		
            scrollPaneTree = new JScrollPane(treeSourceTables);

            splitPane.add(scrollPaneTree);
            splitPane.updateUI();

            if ((currentTable != null) && (currentTable.length() > 0)) {
                Element tableFilter = model.getTableFilter(currentTable);

                if (tableFilter != null) {
                    tfFilter.setText(tableFilter.getAttributeValue(XMLTags.VALUE));
                }
            }

            // if visible remove panels on right hand side
            if ((panelRight != null) && (panelMappingColumns != null) && (panelProcessColumns != null)) {
                panelRight.remove(panelMappingColumns);
                panelRight.remove(panelProcessColumns);
            }

            enableFilterPanels(false);
        } else {
            enableFilterPanels(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableName DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    private void loadMappingColumnsAndFilter(String tableName) throws MissingElementException {
        currentTable = tableName;

        // remove components if existing
        if ((panelRight != null) && (panelMappingColumns != null) && (panelProcessColumns != null)) {
            panelMappingColumns.removeAll();
            panelProcessColumns.removeAll();

            try {
                panelRight.remove(panelMappingColumns);
                panelRight.remove(panelProcessColumns);
            } catch (Exception e) {
                // who cares
            }
        }

        mappingColumnModel     = new MappingColumnModel();
        processColumnModel     = new ProcessColumnModel();

        rowModel = new RowEditorModel();

        // init table data
        initTableData(tableName);

        // load filter for table
        labelSelect.setText(SQL_START + tableName + WHERE);

        Element tableFilter = model.getTableFilter(tableName);

        if (tableFilter != null) {
            tfFilter.setText(tableFilter.getAttributeValue(XMLTags.VALUE));
            buttonDeleteFilter.setEnabled(true);
            checkBoxProcess.setSelected(Boolean.valueOf(tableFilter.getAttributeValue(XMLTags.PROCESS)).booleanValue());

            // display number of records for this filter if available
            String nbrRecordsString = tableFilter.getAttributeValue(XMLTags.RECORDS);

            if (nbrRecordsString != null) {
                String[] param = { nbrRecordsString };
                labelInfo.setText(rm.getString("text.column.records", param));
            }
        } else {
            buttonDeleteFilter.setEnabled(false);
            checkBoxProcess.setSelected(false);
            tfFilter.setText("");
            labelInfo.setText("");
        }

        tableMappingColumn     = new JTableX(mappingColumnModel);
        tableProcessColumn     = new JTableX(processColumnModel);

        // tell the JTableX which RowEditorModel we are using
        tableMappingColumn.setRowEditorModel(rowModel);

        panelMappingColumns.add(tableMappingColumn.getTableHeader(), BorderLayout.PAGE_START);
        panelMappingColumns.add(tableMappingColumn, BorderLayout.CENTER);

        panelProcessColumns.add(tableProcessColumn.getTableHeader(), BorderLayout.PAGE_START);
        panelProcessColumns.add(tableProcessColumn, BorderLayout.CENTER);

        enableFilterPanels(true);

        panelRight.add(panelMappingColumns, BorderLayout.CENTER);
        panelRight.add(panelProcessColumns, BorderLayout.EAST);
        panelRight.updateUI();
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableName DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    private void initTableData(String tableName) throws MissingElementException {
        Element sourceTable = model.getMappingSourceTable(currentTable);

        int     nbrSourceColumns = sourceTable.getChildren(XMLTags.COLUMN).size();

        dataMapping     = new Object[nbrSourceColumns][2];
        dataProcess     = new Object[nbrSourceColumns][1];

        for (int row = 0; row < mappingColumnModel.getRowCount(); row++) {
            dataMapping[row][0]     = new String("");
            dataMapping[row][1]     = new String("");
            dataProcess[row][0]     = new Boolean(false);
        }

        Iterator itMappingColumns = sourceTable.getChildren(XMLTags.COLUMN).iterator();

        int      row = 0;

        while (itMappingColumns.hasNext()) {
            Element columnMapping = (Element) itMappingColumns.next();
            dataMapping[row][0] = columnMapping.getAttributeValue(XMLTags.SOURCE_DB);

            // create a new JComboBox and editor for this row
            JComboBox combo = getDestinationColumnsComboBox(sourceTable.getAttributeValue(XMLTags.DESTINATION_DB), columnMapping.getAttributeValue(XMLTags.DESTINATION_DB));
            dce = new DefaultCellEditor(combo);
            rowModel.addEditorForRow(row, dce);

            // set default selected item
            dataMapping[row][1]     = combo.getSelectedItem();

            dataProcess[row][0] = new Boolean(columnMapping.getAttributeValue(XMLTags.MAPPED));

            row++;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param enable DOCUMENT ME!
     */
    private void enableFilterPanels(boolean enable) {
        for (int i = 0; i < panelTableFilters.getComponentCount(); i++) {
            panelTableFilters.getComponent(i).setEnabled(enable);
        }

        for (int i = 0; i < panelControlFilter.getComponentCount(); i++) {
            panelControlFilter.getComponent(i).setEnabled(enable);
        }

        for (int i = 0; i < panelSelection.getComponentCount(); i++) {
            panelSelection.getComponent(i).setEnabled(enable);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param destinationTableName DOCUMENT ME!
     * @param destinationColumnName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    private JComboBox getDestinationColumnsComboBox(String destinationTableName,
                                                    String destinationColumnName) throws MissingElementException {
        JComboBox combo = null;

        combo = new JComboBox(createComboBoxVector(model.getDestinationColumns(destinationTableName)));

        // if destinationTableName is empty selects empty element as selected element
        combo.setSelectedItem(destinationColumnName);

        return combo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param columns DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Vector createComboBoxVector(List columns) {
        Vector destinationColumnsComboBoxValues = new Vector();

        // add empty element
        destinationColumnsComboBoxValues.add("");

        if (columns.size() > 0) {
            Iterator itColumns = columns.iterator();

            while (itColumns.hasNext()) {
                Element column = (Element) itColumns.next();
                destinationColumnsComboBoxValues.add(column.getAttributeValue(XMLTags.NAME));
            }
        }

        return destinationColumnsComboBoxValues;
    }

    /**
     * DOCUMENT ME!
     *
     * @param top DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    private void createMappingNodes(DefaultMutableTreeNode top) throws MissingElementException {
        DefaultMutableTreeNode table = null;

        Iterator               itMappingTables = model.getMappingTables().iterator();

        while (itMappingTables.hasNext()) {
            Element tableElement = (Element) itMappingTables.next();

            if ((tableElement.getAttributeValue(XMLTags.MAPPED).compareTo("true") == 0) && (tableElement.getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0)) {
                table = new DefaultMutableTreeNode(tableElement.getAttributeValue(XMLTags.SOURCE_DB));
                top.add(table);
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void guiInit() {
        borderLayout.setHgap(10);
        borderLayout.setVgap(10);

        borderRightPanel = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        panelOptions.setLayout(null);

        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOpaque(false);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.3);
        splitPane.setLastDividerLocation(splitPane.getDividerLocation());

        borderLayoutRight.setHgap(0);
        borderLayoutRight.setVgap(0);
        panelRight.setLayout(borderLayoutRight);

        gridLayoutFilter.setColumns(1);
        gridLayoutFilter.setHgap(0);
        gridLayoutFilter.setRows(3);
        gridLayoutFilter.setVgap(5);

        borderLayoutTextfield.setHgap(5);
        panelControlFilter.setLayout(null);
        panelSelection.setLayout(borderLayoutTextfield);

        panelTableFilters.setMinimumSize(new Dimension(300, 105));
        panelTableFilters.setPreferredSize(new Dimension(300, 105));
        panelTableFilters.setLayout(gridLayoutFilter);
        panelTableFilters.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " Specify Table Filters "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        buttonApplyTestFilter.setBounds(new Rectangle(1, 1, 115, 19));
        buttonApplyTestFilter.setText(rm.getString("button.applyAndTest"));
        buttonApplyTestFilter.addActionListener(new PanelMappingColumn_buttonApplyTestFilter_actionAdapter(this));

        buttonDeleteFilter.setBounds(new Rectangle(125, 1, 115, 19));
        buttonDeleteFilter.setText(rm.getString("button.delete"));
        buttonDeleteFilter.addActionListener(new PanelMappingColumn_buttonDeleteFilter_actionAdapter(this));
        buttonDeleteFilter.setEnabled(false);

        checkBoxProcess.setText(rm.getString("text.column.process"));
        checkBoxProcess.addActionListener(new PanelMappingColumn_checkBoxProcess_actionAdapter(this));

        tfFilter.setToolTipText(rm.getString("text.column.tableFilter.toolTip"));

        labelSelect.setText(SQL_START + "<TABLE>" + WHERE);
        labelInfo.setText("");
        labelInfo.setBounds(new Rectangle(255, 2, 150, 15));

        panelSelection.add(tfFilter, BorderLayout.CENTER);
        panelSelection.add(checkBoxProcess, BorderLayout.EAST);

        panelControlFilter.add(buttonApplyTestFilter, null);
        panelControlFilter.add(buttonDeleteFilter, null);
        panelControlFilter.add(labelInfo, null);

        panelTableFilters.add(labelSelect, null);
        panelTableFilters.add(panelSelection, null);
        panelTableFilters.add(panelControlFilter, null);

        panelMappingColumns     = new JPanel(new BorderLayout());
        panelProcessColumns     = new JPanel(new BorderLayout());

        panelRight.add(panelMappingColumns, BorderLayout.CENTER);
        panelRight.add(panelProcessColumns, BorderLayout.EAST);
        panelRight.add(panelTableFilters, BorderLayout.SOUTH);
        panelRight.setBorder(borderRightPanel);

        splitPane.setDividerLocation(splitPane.getLastDividerLocation());

        scrollPaneTree       = new JScrollPane();
        scrollPaneTables     = new JScrollPane(panelRight);

        splitPane.add(scrollPaneTree, JSplitPane.LEFT);
        splitPane.add(scrollPaneTables, JSplitPane.RIGHT);

        this.setLayout(borderLayout);
        this.add(panelOptions, BorderLayout.NORTH);
        this.add(splitPane, BorderLayout.CENTER);

        enableFilterPanels(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonApplyTestFilter_actionPerformed(ActionEvent e) {
        if (tfFilter.getText().length() > 0) {
            if (currentTable != null) {
                try {
                    Element tableFilter = model.getTableFilter(currentTable);

                    if (tableFilter != null) {
                        tableFilter.setAttribute(XMLTags.VALUE, tfFilter.getText());
                        tableFilter.setAttribute(XMLTags.PROCESS, "true");
                    } else {
                        tableFilter = new Element(XMLTags.TABLE);
                        tableFilter.setAttribute(XMLTags.NAME, currentTable);
                        tableFilter.setAttribute(XMLTags.VALUE, tfFilter.getText());
                        tableFilter.setAttribute(XMLTags.PROCESS, "true");
                        model.getFilter().addContent(tableFilter);
                    }

                    // now run the test execute with table filter
                    Element operation = new Element(XMLTags.OPERATION);
                    operation.setAttribute(XMLTags.NAME, OperationType.TEST_TABLE_FILTER);
                    operation.setAttribute(XMLTags.TABLE, currentTable);

                    execute(operation, rm.getString("message.column.filter.successful"));

                    // now update the gui
                    loadMappingColumnsAndFilter(currentTable);
                } catch (Exception ex) {
                    postException(ex);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void checkBoxProcess_actionPerformed(ActionEvent e) {
        try {
            Element tableFilter = model.getTableFilter(currentTable);

            if (tableFilter != null) {
                if (checkBoxProcess.isSelected()) {
                    tableFilter.setAttribute(XMLTags.PROCESS, "true");
                } else {
                    tableFilter.setAttribute(XMLTags.PROCESS, "false");
                }
            }
        } catch (Exception ex) {
            postException(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonDeleteFilter_actionPerformed(ActionEvent e) {
        // delete old table filter if exists 
        try {
            model.deleteTableFilter(currentTable);

            buttonDeleteFilter.setEnabled(false);
            checkBoxProcess.setSelected(false);
            tfFilter.setText("");
            labelInfo.setText("");
        } catch (Exception ex) {
            postException(ex);
        }
    }

    /**
     * class description
     *
     * @author Anthony Smith
     * @version $Revision$
     */
    class MappingColumnModel extends AbstractTableModel {
        private String[]      columnNames = { rm.getString("text.column.sourceColumn"), rm.getString("text.column.destinationColumn") };
        public final Object[] longValues = { "abcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyz" };

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public final int getColumnCount() {
            return columnNames.length;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public final int getRowCount() {
            return dataMapping.length;
        }

        /**
         * DOCUMENT ME!
         *
         * @param col DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public final String getColumnName(int col) {
            return columnNames[col];
        }

        /**
         * DOCUMENT ME!
         *
         * @param row DOCUMENT ME!
         * @param col DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public final Object getValueAt(int row,
                                       int col) {
            return dataMapping[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public final Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public final boolean isCellEditable(int row,
                                            int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 1) {
                return false;
            } else {
                return true;
            }
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public final void setValueAt(Object value,
                                     int    row,
                                     int    col) {
            dataMapping[row][col] = value;

            Element mapping_column = null;

            // check if value is not empty
            if (((String) value).length() > 0) {
                try {
                    mapping_column = model.getMappingSourceColumn(currentTable, (String) dataMapping[row][col - 1]);

                    if (mapping_column != null) {
                        mapping_column.setAttribute(XMLTags.DESTINATION_DB, (String) dataMapping[row][col]);
                        mapping_column.setAttribute(XMLTags.MAPPED, "true");
                        processColumnModel.setValueAt(new Boolean(true), row, 0);
                    }
                } catch (Exception e) {
                    postException(e);
                }
            } else {
                try {
                    mapping_column = model.getMappingSourceColumn(currentTable, (String) dataMapping[row][col - 1]);
                    mapping_column.setAttribute(XMLTags.MAPPED, "false");
                    processColumnModel.setValueAt(new Boolean(false), row, 0);
                } catch (Exception e) {
                    postException(e);
                }
            }

            fireTableCellUpdated(row, col);
        }
    }

    /**
     * class description
     *
     * @author Anthony Smith
     * @version $Revision$
     */
    class ProcessColumnModel extends AbstractTableModel {
        private String[]      columnNames = { rm.getString("text.column.process") };
        public final Object[] longValues = { new Boolean(false) };

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public final int getColumnCount() {
            return columnNames.length;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public final int getRowCount() {
            return dataProcess.length;
        }

        /**
         * DOCUMENT ME!
         *
         * @param col DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public final String getColumnName(int col) {
            return columnNames[col];
        }

        /**
         * DOCUMENT ME!
         *
         * @param row DOCUMENT ME!
         * @param col DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public final Object getValueAt(int row,
                                       int col) {
            return dataProcess[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public final Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public final void setValueAt(Object value,
                                     int    row,
                                     int    col) {
            dataProcess[row][col] = value;

            try {
                Element mapping_column = model.getMappingSourceColumn(currentTable, (String) dataMapping[row][col]);

                if (mapping_column != null) {
                    if (value.equals(new Boolean(true))) {
                        mapping_column.setAttribute(XMLTags.PROCESS, "true");
                    } else {
                        mapping_column.setAttribute(XMLTags.PROCESS, "false");
                    }
                }
            } catch (Exception e) {
                postException(e);
            }

            fireTableCellUpdated(row, col);
        }

        /**
         * DOCUMENT ME!
         *
         * @param row DOCUMENT ME!
         * @param col DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public final boolean isCellEditable(int row,
                                            int col) {
            return true;
        }
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelMappingColumn_buttonApplyTestFilter_actionAdapter implements java.awt.event.ActionListener {
    PanelMappingColumn adaptee;

    /**
     * Creates a new PanelMappingColumn_buttonApplyTestFilter_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelMappingColumn_buttonApplyTestFilter_actionAdapter(PanelMappingColumn adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonApplyTestFilter_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelMappingColumn_checkBoxProcess_actionAdapter implements java.awt.event.ActionListener {
    PanelMappingColumn adaptee;

    /**
     * Creates a new PanelMappingColumn_checkBoxProcess_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelMappingColumn_checkBoxProcess_actionAdapter(PanelMappingColumn adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.checkBoxProcess_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelMappingColumn_buttonDeleteFilter_actionAdapter implements java.awt.event.ActionListener {
    PanelMappingColumn adaptee;

    /**
     * Creates a new PanelMappingColumn_buttonDeleteFilter_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelMappingColumn_buttonDeleteFilter_actionAdapter(PanelMappingColumn adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonDeleteFilter_actionPerformed(e);
    }
}
