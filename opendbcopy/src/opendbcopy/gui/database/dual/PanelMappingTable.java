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

import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.gui.DynamicPanel;
import opendbcopy.gui.PluginGui;

import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.exception.MissingElementException;

import opendbcopy.swing.JTableX;
import opendbcopy.swing.RowEditorModel;

import org.jdom.Element;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PanelMappingTable extends DynamicPanel {
	private DatabaseModel model;
    private Object[][]        dataMapping;
    private Object[][]        dataProcess;
    private RowEditorModel    rowModel;
    private DefaultCellEditor dce;
    private boolean           select_all = false;
    private BorderLayout      borderLayout = new BorderLayout();
    private BorderLayout      borderLayoutPanelMain = new BorderLayout();
    private JPanel            panelOptions = new JPanel();
    private JPanel            panelControl = new JPanel();
    private JPanel            panelMain = new JPanel();
    private JPanel            panelTables = null;
    private JPanel            panelMappingTable = null;
    private JPanel            panelProcessTable = null;
    private JButton           buttonSelect = new JButton();
    private JScrollPane       scrollPane = null;
    private JTableX           tableMapping = null;
    private JTableX           tableProcess = null;
    private MappingTableModel mappingTableModel = null;
    private ProcessTableModel processTableModel = null;
    private Vector            destinationTablesComboBoxValues = new Vector();

    /**
     * Creates a new PanelMappingTable object.
     *
     * @param controller DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public PanelMappingTable(MainController controller, PluginGui workingMode, Boolean registerAsObserver) throws MissingElementException {
        super(controller, workingMode, registerAsObserver);
        
        model = (DatabaseModel) super.model;
        
        rowModel = new RowEditorModel();
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
    public final void onSelect() {
        try {
            initTable();
        } catch (MissingElementException e) {
            postException(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void initTable() throws MissingElementException {
        if (model.isMappingSetup()) {
        	
        	// means tables have already been loaded once
        	if (panelTables != null) {
        		scrollPane.removeAll();
        		panelMain.remove(scrollPane);
        	}
        	
            mappingTableModel     = new MappingTableModel();
            processTableModel     = new ProcessTableModel();

            initMappingTableData();

            tableMapping     = new JTableX(mappingTableModel);
            tableProcess     = new JTableX(processTableModel);

            // Set up column sizes for two columns
            initColumnSizesMappingTable(tableMapping);

            // tell the JTableX which RowEditorModel we are using
            tableMapping.setRowEditorModel(rowModel);

            panelMappingTable = new JPanel();
            panelMappingTable.setLayout(new BorderLayout());
            panelMappingTable.add(tableMapping.getTableHeader(), BorderLayout.PAGE_START);
            panelMappingTable.add(tableMapping, BorderLayout.CENTER);

            panelProcessTable = new JPanel();
            panelProcessTable.setLayout(new BorderLayout());
            panelProcessTable.add(tableProcess.getTableHeader(), BorderLayout.PAGE_START);
            panelProcessTable.add(tableProcess, BorderLayout.CENTER);

            panelTables = new JPanel(new BorderLayout());
            panelTables.add(panelMappingTable, BorderLayout.CENTER);
            panelTables.add(panelProcessTable, BorderLayout.EAST);

            scrollPane = new JScrollPane(panelTables);
            
            panelMain.add(scrollPane, BorderLayout.CENTER);
            
            buttonSelect.setEnabled(true);
            
            panelMain.updateUI();
        } else {
        	buttonSelect.setEnabled(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    private void initMappingTableData() throws MissingElementException {
        int nbrSourceTables = model.getNbrSourceTables();

        dataMapping     = new Object[nbrSourceTables][2];
        dataProcess     = new Object[nbrSourceTables][1];

        for (int row = 0; row < mappingTableModel.getRowCount(); row++) {
            dataMapping[row][0]     = new String("");
            dataMapping[row][1]     = new String("");
        }

        for (int row = 0; row < processTableModel.getRowCount(); row++) {
            dataProcess[row][0] = new Boolean(false);
        }

        Iterator itMappingTables = model.getMappingTables().iterator();

        int      row = 0;

        while (itMappingTables.hasNext()) {
            Element tableMapping = (Element) itMappingTables.next();
            dataMapping[row][0] = tableMapping.getAttributeValue(XMLTags.SOURCE_DB);

            // create a new JComboBox and editor for this row
            JComboBox combo = getDestinationTableComboBox(tableMapping.getAttributeValue(XMLTags.DESTINATION_DB));
            dce = new DefaultCellEditor(combo);
            rowModel.addEditorForRow(row, dce);

            // set default selected item
            dataMapping[row][1]     = combo.getSelectedItem();

            dataProcess[row][0] = new Boolean(tableMapping.getAttributeValue(XMLTags.PROCESS));
            row++;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param destinationTableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    private JComboBox getDestinationTableComboBox(String destinationTableName) throws MissingElementException {
        JComboBox combo = null;

        // initializes vector with destination tables if not already done
        createComboBoxVector(model.getDestinationTables());

        combo = new JComboBox(destinationTablesComboBoxValues);

        // if destinationTableName is empty selects empty element as selected element
        combo.setSelectedItem(destinationTableName);

        return combo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tables DOCUMENT ME!
     */
    private void createComboBoxVector(List tables) {
        if (destinationTablesComboBoxValues.size() == 0) {
            // add empty element
            destinationTablesComboBoxValues.add("");

            if (tables.size() > 0) {
                Iterator itTables = tables.iterator();

                while (itTables.hasNext()) {
                    Element table = (Element) itTables.next();
                    destinationTablesComboBoxValues.add(table.getAttributeValue(XMLTags.NAME));
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    private void guiInit() throws MissingElementException {
        borderLayout.setHgap(10);
        borderLayout.setVgap(10);
        this.setLayout(borderLayout);

        panelMain.setLayout(borderLayoutPanelMain);

        borderLayoutPanelMain.setHgap(10);
        borderLayoutPanelMain.setVgap(10);

        buttonSelect.setPreferredSize(new Dimension(160, 25));

        panelControl.setMinimumSize(new Dimension(300, 30));
        panelControl.setPreferredSize(new Dimension(300, 30));
        panelControl.setLayout(new BorderLayout(20, 20));
        panelControl.add(buttonSelect, BorderLayout.EAST);

        if (select_all) {
            buttonSelect.setText(rm.getString("button.selectAll"));
        } else {
            buttonSelect.setText(rm.getString("button.deselectAll"));
        }

        buttonSelect.addActionListener(new PanelMappingTable_buttonSelect_actionAdapter(this));
        buttonSelect.setEnabled(false);

        scrollPane = new JScrollPane(panelTables);

        panelMain.add(panelControl, BorderLayout.NORTH);

        initTable();

        this.add(panelOptions, BorderLayout.SOUTH);
        this.add(panelMain, BorderLayout.CENTER);
    }

    /*
             * This method picks good column sizes.
             * If all column heads are wider than the column's cells'
             * contents, then you can just use column.sizeWidthToFit().
             */
    private void initColumnSizesMappingTable(JTable table) {
        MappingTableModel model = (MappingTableModel) table.getModel();
        TableColumn       column = null;
        Component         comp = null;
        int               headerWidth = 0;
        int               cellWidth = 0;
        Object[]          longValues = model.longValues;
        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < 2; i++) {
            column     = table.getColumnModel().getColumn(i);

            comp            = headerRenderer.getTableCellRendererComponent(null, column.getHeaderValue(), false, false, 0, 0);
            headerWidth     = comp.getPreferredSize().width;

            comp          = table.getDefaultRenderer(model.getColumnClass(i)).getTableCellRendererComponent(table, longValues[i], false, false, 0, i);
            cellWidth     = comp.getPreferredSize().width;

            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }

    /*
                     * This method picks good column sizes.
                     * If all column heads are wider than the column's cells'
                     * contents, then you can just use column.sizeWidthToFit().
                     */
    private void initColumnSizesProcessTable(JTable table) {
        ProcessTableModel model = (ProcessTableModel) table.getModel();
        TableColumn       column = null;
        Component         comp = null;
        int               headerWidth = 0;
        int               cellWidth = 0;
        Object[]          longValues = model.longValues;
        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < 2; i++) {
            column     = table.getColumnModel().getColumn(i);

            comp            = headerRenderer.getTableCellRendererComponent(null, column.getHeaderValue(), false, false, 0, 0);
            headerWidth     = comp.getPreferredSize().width;

            comp          = table.getDefaultRenderer(model.getColumnClass(i)).getTableCellRendererComponent(table, longValues[i], false, false, 0, i);
            cellWidth     = comp.getPreferredSize().width;

            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonSelect_actionPerformed(ActionEvent e) {
        try {
            if (!select_all) {
                for (int row = 0; row < processTableModel.getRowCount(); row++) {
                    processTableModel.setValueAt(new Boolean(false), row, 0);
                }

                buttonSelect.setText(rm.getString("button.selectAll"));
                select_all = true;
            } else {
                for (int row = 0; row < processTableModel.getRowCount(); row++) {
                    processTableModel.setValueAt(new Boolean(true), row, 0);
                }

                buttonSelect.setText(rm.getString("button.deselectAll"));
                select_all = false;
            }
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
    class MappingTableModel extends AbstractTableModel {
        private String[]      columnNames = { rm.getString("text.table.sourceTableView"), rm.getString("text.table.destinationTableView") };
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

            Element mapping_table = null;

            // check if value is not empty
            if (((String) value).length() > 0) {
                try {
                    mapping_table = model.getMappingSourceTable((String) dataMapping[row][col - 1]);

                    if (mapping_table != null) {
                        mapping_table.setAttribute(XMLTags.DESTINATION_DB, (String) dataMapping[row][col]);
                        mapping_table.setAttribute(XMLTags.MAPPED, "true");

                        // Set the process option automatically
                        processTableModel.setValueAt(new Boolean(true), row, 0);

                        // check for column mappings
                        model.checkForMappings((String) dataMapping[row][col - 1]);
                    }
                } catch (Exception e) {
                    postException(e);
                }
            } else {
                // Set the process option automatically
                processTableModel.setValueAt(new Boolean(false), row, 0);

                try {
                    mapping_table = model.getMappingSourceTable((String) dataMapping[row][col - 1]);
                    mapping_table.setAttribute(XMLTags.MAPPED, "false");
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
    class ProcessTableModel extends AbstractTableModel {
        private String[]      columnNames = { rm.getString("text.table.process") };
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
                Element mapping_table = model.getMappingSourceTable((String) dataMapping[row][col]);

                if (mapping_table != null) {
                    if (value.equals(new Boolean(true))) {
                        mapping_table.setAttribute(XMLTags.PROCESS, "true");
                    } else {
                        mapping_table.setAttribute(XMLTags.PROCESS, "false");
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
class PanelMappingTable_buttonSelect_actionAdapter implements java.awt.event.ActionListener {
    PanelMappingTable adaptee;

    /**
     * Creates a new PanelMappingTable_buttonSelect_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelMappingTable_buttonSelect_actionAdapter(PanelMappingTable adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonSelect_actionPerformed(e);
    }
}
