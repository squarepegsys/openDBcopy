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
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
    private Object[][]             dataProcess;
    private String                 currentTable;
    private DefaultMutableTreeNode top;
    private ProcessColumnModel     processColumnModel;
    private RowEditorModel         rm;
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
    private JPanel                 panelProcessColumns = new JPanel();
    private JPanel                 panelSelection = new JPanel();
    private JPanel                 panelControlFilter = new JPanel();
    private JSplitPane             splitPane = new JSplitPane();
    private JScrollPane            scrollPaneTree = null;
    private JScrollPane            scrollPaneTables = null;
    private JTree                  treeSourceTables = new JTree();
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
     *
     * @throws Exception DOCUMENT ME!
     */
    public PanelMappingColumn(MainController controller) throws Exception {
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
        if ((currentTable != null) && (currentTable.length() > 0)) {
            try {
                Element tableFilter = pm.getProjectModel().getTableFilter(currentTable);

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
     *
     * @throws Exception DOCUMENT ME!
     */
    public final void loadSourceTables() throws Exception {
        if (pm.getProjectModel().isSourceModelCaptured()) {
            // remove component
            scrollPaneTree.remove(treeSourceTables);

            top = new DefaultMutableTreeNode("Source Tables / Views (to be processed)");
            createSourceNodes(top);
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
                                loadColumnsAndFilter(node.toString());
                            } catch (Exception ex) {
                                postException(ex);
                            }
                        }
                    }
                });

            // add components		
            scrollPaneTree.add(treeSourceTables);
            scrollPaneTree.updateUI();

            if ((currentTable != null) && (currentTable.length() > 0)) {
                Element tableFilter = pm.getProjectModel().getTableFilter(currentTable);

                if (tableFilter != null) {
                    tfFilter.setText(tableFilter.getAttributeValue(XMLTags.VALUE));
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableName DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void loadColumnsAndFilter(String tableName) throws Exception {
        currentTable     = tableName;

        processColumnModel = new ProcessColumnModel();

        // init table data
        initTableData(tableName);

        // load filter for table
        labelSelect.setText(SQL_START + tableName + WHERE);

        Element tableFilter = pm.getProjectModel().getTableFilter(tableName);

        if (tableFilter != null) {
            tfFilter.setText(tableFilter.getAttributeValue(XMLTags.VALUE));
            buttonDeleteFilter.setEnabled(true);
            checkBoxProcess.setSelected(Boolean.valueOf(tableFilter.getAttributeValue(XMLTags.PROCESS)).booleanValue());

            // display number of records for this filter if available
            String nbrRecordsString = tableFilter.getAttributeValue(XMLTags.RECORDS);

            if (nbrRecordsString != null) {
                labelInfo.setText(nbrRecordsString + " record(s)");
            }
        } else {
            buttonDeleteFilter.setEnabled(false);
            checkBoxProcess.setSelected(false);
            tfFilter.setText("");
            labelInfo.setText("");
        }

        tableProcessColumn = new JTableX(processColumnModel);

        panelProcessColumns.removeAll();

        panelProcessColumns.add(tableProcessColumn.getTableHeader(), BorderLayout.PAGE_START);
        panelProcessColumns.add(tableProcessColumn, BorderLayout.CENTER);
        panelProcessColumns.updateUI();
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableName DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void initTableData(String tableName) throws Exception {
        Element sourceTable = pm.getProjectModel().getSourceTable(currentTable);

        int     nbrSourceColumns = sourceTable.getChildren(XMLTags.COLUMN).size();

        dataProcess = new Object[nbrSourceColumns][2];

        for (int row = 0; row < processColumnModel.getRowCount(); row++) {
            dataProcess[row][0]     = new String("");
            dataProcess[row][1]     = new Boolean(false);
        }

        Iterator itColumns = sourceTable.getChildren(XMLTags.COLUMN).iterator();

        int      row = 0;

        while (itColumns.hasNext()) {
            Element column = (Element) itColumns.next();
            dataProcess[row][0] = column.getAttributeValue(XMLTags.NAME);

            if (column.getAttributeValue(XMLTags.PROCESS) != null) {
                dataProcess[row][1] = new Boolean(column.getAttributeValue(XMLTags.PROCESS));
            } else {
                column.setAttribute(XMLTags.PROCESS, "true");
                dataProcess[row][1] = new Boolean(true);
            }

            row++;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param top DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void createSourceNodes(DefaultMutableTreeNode top) throws Exception {
        DefaultMutableTreeNode table = null;

        Iterator               itSourceTables = pm.getProjectModel().getSourceTables().iterator();

        while (itSourceTables.hasNext()) {
            Element tableElement = (Element) itSourceTables.next();

            if (tableElement.getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0) {
                table = new DefaultMutableTreeNode(tableElement.getAttributeValue(XMLTags.NAME));
                top.add(table);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void guiInit() throws Exception {
        borderLayout.setHgap(10);
        borderLayout.setVgap(10);

        borderRightPanel = BorderFactory.createEmptyBorder(0, 5, 5, 5);
        panelOptions.setLayout(null);

        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOpaque(false);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.3);
        splitPane.setLastDividerLocation(splitPane.getDividerLocation());

        panelRight.setLayout(borderLayoutRight);
        borderLayoutRight.setHgap(0);
        borderLayoutRight.setVgap(0);

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
        buttonApplyTestFilter.setText("Apply & Test");
        buttonApplyTestFilter.addActionListener(new PanelMappingColumn_buttonApplyTestFilter_actionAdapter(this));

        buttonDeleteFilter.setBounds(new Rectangle(125, 1, 115, 19));
        buttonDeleteFilter.setText("Delete");
        buttonDeleteFilter.addActionListener(new PanelMappingColumn_buttonDeleteFilter_actionAdapter(this));
        buttonDeleteFilter.setEnabled(false);

        checkBoxProcess.setText("Process");
        checkBoxProcess.addActionListener(new PanelMappingColumn_checkBoxProcess_actionAdapter(this));

        tfFilter.setToolTipText("enter SQL (you may use wildcards)");

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

        panelRight.add(panelProcessColumns, BorderLayout.CENTER);
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
                    Element tableFilter = pm.getProjectModel().getTableFilter(currentTable);

                    if (tableFilter != null) {
                        tableFilter.setAttribute(XMLTags.VALUE, tfFilter.getText());
                        tableFilter.setAttribute(XMLTags.PROCESS, "true");
                    } else {
                        tableFilter = new Element(XMLTags.TABLE);
                        tableFilter.setAttribute(XMLTags.NAME, currentTable);
                        tableFilter.setAttribute(XMLTags.VALUE, tfFilter.getText());
                        tableFilter.setAttribute(XMLTags.PROCESS, "true");
                        pm.getProjectModel().getFilter().addContent(tableFilter);
                    }

                    // now run the test execute with table filter
                    Element operation = new Element(XMLTags.OPERATION);
                    operation.setAttribute(XMLTags.NAME, OperationType.TEST_TABLE_FILTER);
                    operation.setAttribute(XMLTags.TABLE, currentTable);

                    execute(operation, "filter successfully tested");

                    // now update the gui
                    loadColumnsAndFilter(currentTable);
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
            Element tableFilter = pm.getProjectModel().getTableFilter(currentTable);

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
            pm.getProjectModel().deleteTableFilter(currentTable);

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
    class ProcessColumnModel extends AbstractTableModel {
        private String[]      columnNames = { "Column", "Process" };
        public final Object[] longValues = { "abcdefghijklmnrstuvwxyz", new Boolean(false) };

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
                Element column = pm.getProjectModel().getSourceColumn(currentTable, (String) dataProcess[row][0]);

                if (column != null) {
                    if (value.equals(new Boolean(true))) {
                        column.setAttribute(XMLTags.PROCESS, "true");
                    } else {
                        column.setAttribute(XMLTags.PROCESS, "false");
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
            if (col == 1) {
                return true;
            } else {
                return false;
            }
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
