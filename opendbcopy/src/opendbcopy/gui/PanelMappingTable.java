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
 * --------------------------------------------------------------------------*/
package opendbcopy.gui;

import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.model.ProjectManager;

import opendbcopy.swing.JTableX;
import opendbcopy.swing.RowEditorModel;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.jdom.Element;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
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
public class PanelMappingTable extends JPanel implements Observer {
    private static Logger             logger = Logger.getLogger(PanelMappingTable.class.getName());
    private FrameMain                 parentFrame;
    private MainController            controller;
    private ProjectManager            pm;
    private Object[][]                dataMapping;
    private Object[][]                dataProcess;
    private RowEditorModel            rm;
    private DefaultCellEditor         dce;
    private boolean                   select_all = false;
    private BorderLayout              borderLayout = new BorderLayout();
    private BorderLayout              borderLayoutPanelMain = new BorderLayout();
    private JPanel                    panelOptions = new JPanel();
    private JPanel                    panelControl = new JPanel();
    private JPanel                    panelMain = new JPanel();
    private JPanel                    panelTables = null;
    private JPanel                    panelMappingTable = null;
    private JPanel                    panelProcessTable = null;
    private JButton                   buttonSelect = new JButton();
    private JScrollPane               scrollPane = new JScrollPane();
    private JTableX                   tableMapping = null;
    private JTableX                   tableProcessDualDb = null;
    private JTableX                   tableProcessSingleDb = null;
    private MappingTableModel         mappingTableModel = null;
    private ProcessTableDualDbModel   processTableDualDbModel = null;
    private ProcessTableSingleDbModel processTableSingleDbModel = null;

    /**
     * Creates a new PanelMappingTable object.
     *
     * @param parentFrame DOCUMENT ME!
     * @param controller DOCUMENT ME!
     * @param projectManager DOCUMENT ME!
     */
    public PanelMappingTable(FrameMain      parentFrame,
                             MainController controller,
                             ProjectManager projectManager) {
        this.parentFrame     = parentFrame;
        this.controller      = controller;
        this.pm              = projectManager;

        // create a RowEditorModel... this is used to hold the extra
        // information that is needed to deal with row specific editors
        rm = new RowEditorModel();

        try {
            guiInit();
        } catch (Exception e) {
            logger.error(e.toString());
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
        initTable();
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
     */
    public final void initTable() {
        try {
            // single db_mode
            if (pm.getProjectModel().getDbMode() == pm.getProjectModel().SINGLE_MODE) {
                if (pm.getProjectModel().isSourceModelCaptured()) {
                    try {
                        panelMain.remove(scrollPane);

                        processTableSingleDbModel = new ProcessTableSingleDbModel();

                        initSingleDbTableData();

                        tableProcessSingleDb = new JTableX(processTableSingleDbModel);

                        // Set up column sizes for two columns
                        initColumnSizesProcessTable(tableProcessSingleDb);

                        panelProcessTable = new JPanel();
                        panelProcessTable.setLayout(new BorderLayout());
                        panelProcessTable.add(tableProcessSingleDb.getTableHeader(), BorderLayout.PAGE_START);
                        panelProcessTable.add(tableProcessSingleDb, BorderLayout.CENTER);

                        panelTables = new JPanel();
                        panelTables.setLayout(new BorderLayout());

                        panelTables.add(panelProcessTable, BorderLayout.CENTER);

                        scrollPane = new JScrollPane(panelTables);

                        panelMain.add(scrollPane, BorderLayout.CENTER);
                    } catch (Exception e) {
                        logger.error(e.toString());
                        this.parentFrame.setStatusBar(e.toString(), Level.ERROR);
                    }
                }
            }
            // dual db_mode
            else {
                if (pm.getProjectModel().isMappingSetup()) {
                    try {
                        panelMain.remove(scrollPane);

                        mappingTableModel           = new MappingTableModel();
                        processTableDualDbModel     = new ProcessTableDualDbModel();

                        initMappingTableData();

                        tableMapping           = new JTableX(mappingTableModel);
                        tableProcessDualDb     = new JTableX(processTableDualDbModel);

                        // Set up column sizes for two columns
                        initColumnSizesMappingTable(tableMapping);

                        // tell the JTableX which RowEditorModel we are using
                        tableMapping.setRowEditorModel(rm);

                        panelMappingTable = new JPanel();
                        panelMappingTable.setLayout(new BorderLayout());
                        panelMappingTable.add(tableMapping.getTableHeader(), BorderLayout.PAGE_START);
                        panelMappingTable.add(tableMapping, BorderLayout.CENTER);

                        panelProcessTable = new JPanel();
                        panelProcessTable.setLayout(new BorderLayout());
                        panelProcessTable.add(tableProcessDualDb.getTableHeader(), BorderLayout.PAGE_START);
                        panelProcessTable.add(tableProcessDualDb, BorderLayout.CENTER);

                        panelTables = new JPanel();
                        panelTables.setLayout(new BorderLayout());

                        panelTables.add(panelMappingTable, BorderLayout.CENTER);
                        panelTables.add(panelProcessTable, BorderLayout.EAST);

                        scrollPane = new JScrollPane(panelTables);

                        panelMain.add(scrollPane, BorderLayout.CENTER);
                    } catch (Exception e) {
                        logger.error(e.toString());
                        this.parentFrame.setStatusBar(e.toString(), Level.ERROR);
                    }
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
     * @throws Exception DOCUMENT ME!
     */
    private void initMappingTableData() throws Exception {
        int nbrSourceTables = pm.getProjectModel().getNbrSourceTables();

        dataMapping     = new Object[nbrSourceTables][2];
        dataProcess     = new Object[nbrSourceTables][1];

        for (int row = 0; row < mappingTableModel.getRowCount(); row++) {
            dataMapping[row][0]     = new String("");
            dataMapping[row][1]     = new String("");
        }

        for (int row = 0; row < processTableDualDbModel.getRowCount(); row++) {
            dataProcess[row][0] = new Boolean(false);
        }

        Iterator itMappingTables = pm.getProjectModel().getMappingTables().iterator();

        int      row = 0;

        while (itMappingTables.hasNext()) {
            Element tableMapping = (Element) itMappingTables.next();
            dataMapping[row][0] = tableMapping.getAttributeValue(XMLTags.SOURCE_DB);

            if (tableMapping.getAttributeValue(XMLTags.DESTINATION_DB).length() == 0) {
                // create a new JComboBox and editor for this row
                dce = new DefaultCellEditor(new JComboBox(createComboBoxVector(pm.getProjectModel().getUnmappedDestinationTables())));
                rm.addEditorForRow(row, dce);
            } else {
                dataMapping[row][1] = tableMapping.getAttributeValue(XMLTags.DESTINATION_DB);
            }

            dataProcess[row][0] = new Boolean(tableMapping.getAttributeValue(XMLTags.PROCESS));
            row++;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void initSingleDbTableData() throws Exception {
        int nbrSourceTables = 0;

        nbrSourceTables     = pm.getProjectModel().getNbrSourceTables();

        dataProcess = new Object[nbrSourceTables][2];

        for (int row = 0; row < processTableSingleDbModel.getRowCount(); row++) {
            dataProcess[row][0]     = new String("");
            dataProcess[row][1]     = new Boolean(true);
        }

        Iterator itSourceTables = pm.getProjectModel().getSourceTables().iterator();

        int      row = 0;

        while (itSourceTables.hasNext()) {
            Element table = (Element) itSourceTables.next();
            dataProcess[row][0] = table.getAttributeValue(XMLTags.NAME);

            if (table.getAttributeValue(XMLTags.PROCESS) != null) {
                dataProcess[row][1] = new Boolean(table.getAttributeValue(XMLTags.PROCESS));
            } else {
                pm.getProjectModel().setElementProcess(table, true);
                dataProcess[row][1] = new Boolean(true);
            }

            row++;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param unmappedTables DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Vector createComboBoxVector(List unmappedTables) {
        Vector unmappedTablesComboBoxValues = new Vector();

        // add empty element
        unmappedTablesComboBoxValues.add("");

        if (unmappedTables.size() > 0) {
            Iterator itUnmappedTables = unmappedTables.iterator();

            while (itUnmappedTables.hasNext()) {
                Element unmappedTable = (Element) itUnmappedTables.next();
                unmappedTablesComboBoxValues.add(unmappedTable.getAttributeValue(XMLTags.NAME));
            }
        }

        return unmappedTablesComboBoxValues;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void guiInit() throws Exception {
        borderLayout.setHgap(10);
        borderLayout.setVgap(10);
        this.setLayout(borderLayout);

        panelMain.setLayout(borderLayoutPanelMain);

        borderLayoutPanelMain.setHgap(10);
        borderLayoutPanelMain.setVgap(10);

        buttonSelect.setPreferredSize(new Dimension(120, 25));

        panelControl.setMinimumSize(new Dimension(300, 30));
        panelControl.setPreferredSize(new Dimension(300, 30));
        panelControl.setLayout(new BorderLayout(20, 20));
        panelControl.add(buttonSelect, BorderLayout.EAST);

        if (select_all) {
            buttonSelect.setText("Select All");
        } else {
            buttonSelect.setText("Deselect All");
        }

        buttonSelect.addActionListener(new PanelMappingTable_buttonSelect_actionAdapter(this));

        panelMain.add(panelControl, BorderLayout.NORTH);
        panelMain.add(scrollPane);

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
        ProcessTableSingleDbModel model = (ProcessTableSingleDbModel) table.getModel();
        TableColumn               column = null;
        Component                 comp = null;
        int                       headerWidth = 0;
        int                       cellWidth = 0;
        Object[]                  longValues = model.longValues;
        TableCellRenderer         headerRenderer = table.getTableHeader().getDefaultRenderer();

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
                if (pm.getProjectModel().getDbMode() == pm.getProjectModel().SINGLE_MODE) {
                    for (int row = 0; row < processTableSingleDbModel.getRowCount(); row++) {
                        processTableSingleDbModel.setValueAt(new Boolean(false), row, 1);
                    }
                } else {
                    for (int row = 0; row < processTableDualDbModel.getRowCount(); row++) {
                        processTableDualDbModel.setValueAt(new Boolean(false), row, 0);
                    }
                }

                buttonSelect.setText("Select All");
                select_all = true;
            } else {
                if (pm.getProjectModel().getDbMode() == pm.getProjectModel().SINGLE_MODE) {
                    for (int row = 0; row < processTableSingleDbModel.getRowCount(); row++) {
                        processTableSingleDbModel.setValueAt(new Boolean(true), row, 1);
                    }
                } else {
                    for (int row = 0; row < processTableDualDbModel.getRowCount(); row++) {
                        processTableDualDbModel.setValueAt(new Boolean(true), row, 0);
                    }
                }

                buttonSelect.setText("Deselect All");
                select_all = false;
            }
        } catch (Exception ex) {
            logger.error(ex.toString());
            parentFrame.setStatusBar(ex.toString(), Level.ERROR);
        }
    }

    /**
     * class description
     *
     * @author Anthony Smith
     * @version $Revision$
     */
    class MappingTableModel extends AbstractTableModel {
        private String[]      columnNames = { "Source Table / View", "Destination Table / View" };
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
                    mapping_table = pm.getProjectModel().getMappingSourceTable((String) dataMapping[row][col - 1]);

                    if (mapping_table != null) {
                        mapping_table.setAttribute(XMLTags.DESTINATION_DB, (String) dataMapping[row][col]);
                        mapping_table.setAttribute(XMLTags.MAPPED, "true");

                        // Set the process option automatically
                        processTableDualDbModel.setValueAt(new Boolean(true), row, 0);

                        // check for column mappings
                        pm.getProjectModel().checkForMappings((String) dataMapping[row][col - 1]);
                    }
                } catch (Exception e) {
                    logger.error(e.toString());
                    parentFrame.setStatusBar(e.toString(), Level.ERROR);
                }
            } else {
                // Set the process option automatically
                processTableDualDbModel.setValueAt(new Boolean(false), row, 0);

                try {
                    mapping_table = pm.getProjectModel().getMappingSourceTable((String) dataMapping[row][col - 1]);
                    mapping_table.setAttribute(XMLTags.MAPPED, "false");
                } catch (Exception e) {
                    logger.error(e.toString());
                    parentFrame.setStatusBar(e.toString(), Level.ERROR);
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
    class ProcessTableDualDbModel extends AbstractTableModel {
        private String[]      columnNames = { "Process" };
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
                Element mapping_table = pm.getProjectModel().getMappingSourceTable((String) dataMapping[row][col]);

                if (mapping_table != null) {
                    if (value.equals(new Boolean(true))) {
                        mapping_table.setAttribute(XMLTags.PROCESS, "true");
                    } else {
                        mapping_table.setAttribute(XMLTags.PROCESS, "false");
                    }
                }
            } catch (Exception e) {
                logger.error(e.toString());
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

    /**
     * class description
     *
     * @author smi
     * @version $Revision$
     */
    class ProcessTableSingleDbModel extends AbstractTableModel {
        private String[]      columnNames = { "Tables / Views", "Process" };
        public final Object[] longValues = { "abcdefghijklmnopqrstuvwxyz", new Boolean(false) };

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
                Element table = pm.getProjectModel().getSourceTable((String) dataProcess[row][0]);

                if (table != null) {
                    if (value.equals(new Boolean(true))) {
                        table.setAttribute(XMLTags.PROCESS, "true");
                    } else {
                        table.setAttribute(XMLTags.PROCESS, "false");
                    }
                }
            } catch (Exception e) {
                logger.error(e.toString());
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
 * @author rhu
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
