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

import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.gui.DynamicPanel;

import opendbcopy.swing.JTableX;
import opendbcopy.swing.RowEditorModel;

import org.jdom.Element;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import java.util.Iterator;
import java.util.Observable;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
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
    private JTableX                   tableProcessSingleDb = null;
    private ProcessTableSingleDbModel processTableSingleDbModel = null;

    /**
     * Creates a new PanelMappingTable object.
     *
     * @param controller DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public PanelMappingTable(MainController controller) throws Exception {
        super(controller);

        // create a RowEditorModel... this is used to hold the extra
        // information that is needed to deal with row specific editors
        rm = new RowEditorModel();

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
                    postException(e);
                }
            }
        } catch (Exception e) {
            postException(e);
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

    /**
     * DOCUMENT ME!
     *
     * @param table DOCUMENT ME!
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
                for (int row = 0; row < processTableSingleDbModel.getRowCount(); row++) {
                    processTableSingleDbModel.setValueAt(new Boolean(false), row, 1);
                }

                buttonSelect.setText("Select All");
                select_all = true;
            } else {
                for (int row = 0; row < processTableSingleDbModel.getRowCount(); row++) {
                    processTableSingleDbModel.setValueAt(new Boolean(true), row, 1);
                }

                buttonSelect.setText("Deselect All");
                select_all = false;
            }
        } catch (Exception ex) {
            postException(ex);
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
