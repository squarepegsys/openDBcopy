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
package opendbcopy.gui.database.single;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import opendbcopy.config.OperationType;
import opendbcopy.config.XMLTags;
import opendbcopy.controller.MainController;
import opendbcopy.gui.DynamicPanel;
import opendbcopy.gui.PluginGui;
import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.exception.MissingElementException;

import org.jdom.Element;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PanelProcessColumn extends DynamicPanel {
    private static final String    SQL_START = "SELECT <RECORDS> FROM ";
    private static final String    WHERE = " WHERE";
    private DatabaseModel          model;
    private String                 currentTable;
    private DefaultMutableTreeNode top;
    private ProcessColumnModel     processColumnModel;
    private boolean                select_all = false;
    private Border                 borderRightPanel;
    private BorderLayout           borderLayout = new BorderLayout();
    private BorderLayout           borderLayoutRight = new BorderLayout();
    private BorderLayout           borderLayoutTextfield = new BorderLayout();
    private GridLayout             gridLayoutFilter = new GridLayout();
    private JPanel                 panelOptions = new JPanel();
    private JPanel                 panelRight = new JPanel();
    private JPanel                 panelTableFilters = new JPanel();
    private JPanel                 panelProcessColumns = null;
    private JPanel                 panelSelection = new JPanel();
    private JPanel                 panelControlFilter = new JPanel();
    private JSplitPane             splitPane = new JSplitPane();
    private JScrollPane            scrollPaneTree = null;
    private JScrollPane            scrollPaneTables = null;
    private JTree                  treeSourceTables = new JTree();
    private JTable                tableProcessColumn = new JTable();
    private JTextField             tfFilter = new JTextField();
    private JButton                buttonApplyTestFilter = new JButton();
    private JButton                buttonDeleteFilter = new JButton();
    private JCheckBox              checkBoxProcess = new JCheckBox();
    private JLabel                 labelSelect = new JLabel();
    private JLabel                 labelInfo = new JLabel();

    /**
     * Creates a new PanelProcessColumn object.
     *
     * @param controller DOCUMENT ME!
     * @param pluginGui DOCUMENT ME!
     * @param registerAsObserver DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public PanelProcessColumn(MainController controller,
                              PluginGui    workingMode,
                              Boolean        registerAsObserver) throws Exception {
        super(controller, workingMode, registerAsObserver);
        model = (DatabaseModel) super.model;
        guiInit();
        loadTablesToProcess();
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
            loadTablesToProcess();
        } catch (Exception e) {
            postException(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void loadTablesToProcess() throws MissingElementException {
        if (model.getNbrSourceTables() > 0) {
            // remove component
            splitPane.remove(scrollPaneTree);

            top = new DefaultMutableTreeNode(rm.getString("text.column.processSourceTableView"));
            createNodes(top);
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
            if ((panelRight != null) && (panelProcessColumns != null)) {
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
    private void loadColumnsAndFilter(String tableName) throws MissingElementException {
        currentTable = tableName;

        // remove components if existing
        if ((panelRight != null) && (panelProcessColumns != null)) {
            panelProcessColumns.removeAll();

            try {
                panelRight.remove(panelProcessColumns);
            } catch (Exception e) {
                // who cares
            }
        }

        processColumnModel     = new ProcessColumnModel(model.getSourceColumns(tableName));

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
                labelInfo.setText(rm.getString("text.column.selectedRecords", param));
            }
        } else {
            buttonDeleteFilter.setEnabled(false);
            checkBoxProcess.setSelected(false);
            tfFilter.setText("");
            labelInfo.setText("");
        }

        tableProcessColumn     = new JTable(processColumnModel);

        panelProcessColumns.add(tableProcessColumn.getTableHeader(), BorderLayout.PAGE_START);
        panelProcessColumns.add(tableProcessColumn, BorderLayout.CENTER);

        enableFilterPanels(true);

        panelRight.add(panelProcessColumns, BorderLayout.CENTER);
        panelRight.updateUI();
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
     * @param top DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    private void createNodes(DefaultMutableTreeNode top) throws MissingElementException {
        DefaultMutableTreeNode table = null;

        Iterator               itSourceTables = model.getSourceTables().iterator();

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
        buttonApplyTestFilter.addActionListener(new PanelProcessColumn_buttonApplyTestFilter_actionAdapter(this));

        buttonDeleteFilter.setBounds(new Rectangle(125, 1, 115, 19));
        buttonDeleteFilter.setText(rm.getString("button.delete"));
        buttonDeleteFilter.addActionListener(new PanelProcessColumn_buttonDeleteFilter_actionAdapter(this));
        buttonDeleteFilter.setEnabled(false);

        checkBoxProcess.setText(rm.getString("text.column.process"));
        checkBoxProcess.addActionListener(new PanelProcessColumn_checkBoxProcess_actionAdapter(this));

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

        panelProcessColumns     = new JPanel(new BorderLayout());

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
    class ProcessColumnModel extends AbstractTableModel {
        private String[]      columnNames = { rm.getString("text.column.type"), rm.getString("text.column.sourceColumn"), rm.getString("text.column.process") };
        public final Object[] longValues = { "TIMESTAMP(6)", "abcdefghijklmnopqrstuvwxyz", new Boolean(false) };
        private List columnsList;
        
        public ProcessColumnModel(List columnsList) {
    		this.columnsList = columnsList;
    	}    	

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
            return columnsList.size();
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
        	Element rowElement = (Element) columnsList.get(row);
            
        	switch (col) {
        		case 0: return rowElement.getAttributeValue(XMLTags.TYPE_NAME);
        		case 1: return rowElement.getAttributeValue(XMLTags.NAME);
        		case 2: return Boolean.valueOf(rowElement.getAttributeValue(XMLTags.PROCESS));
        	}
        	return "you should'nt be here";
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
            if (col < 2) {
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
        	
        	if (col == 2) {
            	Element rowElement = (Element) columnsList.get(row);
            	rowElement.setAttribute(XMLTags.PROCESS, ((Boolean) value).toString());
                fireTableCellUpdated(row, col);
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
class PanelProcessColumn_buttonApplyTestFilter_actionAdapter implements java.awt.event.ActionListener {
    PanelProcessColumn adaptee;

    /**
     * Creates a new PanelProcessColumn_buttonApplyTestFilter_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelProcessColumn_buttonApplyTestFilter_actionAdapter(PanelProcessColumn adaptee) {
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
class PanelProcessColumn_checkBoxProcess_actionAdapter implements java.awt.event.ActionListener {
    PanelProcessColumn adaptee;

    /**
     * Creates a new PanelProcessColumn_checkBoxProcess_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelProcessColumn_checkBoxProcess_actionAdapter(PanelProcessColumn adaptee) {
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
class PanelProcessColumn_buttonDeleteFilter_actionAdapter implements java.awt.event.ActionListener {
    PanelProcessColumn adaptee;

    /**
     * Creates a new PanelProcessColumn_buttonDeleteFilter_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelProcessColumn_buttonDeleteFilter_actionAdapter(PanelProcessColumn adaptee) {
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
