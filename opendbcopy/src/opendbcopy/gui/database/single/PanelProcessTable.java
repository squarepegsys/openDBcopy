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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Observable;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

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
public class PanelProcessTable extends DynamicPanel {
    private DatabaseModel     model;
    private boolean           select_all = false;
    private BorderLayout      borderLayout = new BorderLayout();
    private BorderLayout      borderLayoutPanelMain = new BorderLayout();
    private JPanel            panelOptions = new JPanel();
    private JPanel            panelControl = new JPanel();
    private JPanel            panelMain = new JPanel();
    private JPanel            panelProcessTable = null;
    private JButton           buttonSelect = new JButton();
    private JScrollPane       scrollPane = null;
    private JTable           tableProcess = null;
    private ProcessTableModel processTableModel = null;

    /**
     * Creates a new PanelProcessTable object.
     *
     * @param controller DOCUMENT ME!
     * @param pluginGui DOCUMENT ME!
     * @param registerAsObserver DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public PanelProcessTable(MainController controller,
                             PluginGui    workingMode,
                             Boolean        registerAsObserver) throws MissingElementException {
        super(controller, workingMode, registerAsObserver);

        model     = (DatabaseModel) super.model;
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
        if (model.getNbrSourceTables() > 0) {
            // means tables have already been loaded once
            if (scrollPane != null) {
                panelMain.remove(scrollPane);
            }

            processTableModel     = new ProcessTableModel(model.getSourceTables());

            tableProcess     = new JTable(processTableModel);

            // Set up column sizes
            initColumnSizesTable(tableProcess);

            panelProcessTable = new JPanel();
            panelProcessTable.setLayout(new BorderLayout());
            panelProcessTable.add(tableProcess.getTableHeader(), BorderLayout.PAGE_START);
            panelProcessTable.add(tableProcess, BorderLayout.CENTER);

            scrollPane = new JScrollPane(panelProcessTable);

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

        buttonSelect.addActionListener(new PanelProcessTable_buttonSelect_actionAdapter(this));
        buttonSelect.setEnabled(false);

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
    private void initColumnSizesTable(JTable table) {
        ProcessTableModel modelTable = (ProcessTableModel) table.getModel();
        TableColumn       column = null;
        Component         comp = null;
        int               headerWidth = 0;
        int               cellWidth = 0;
        Object[]          longValues = modelTable.longValues;
        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < modelTable.getColumnCount(); i++) {
            column     = table.getColumnModel().getColumn(i);

            comp            = headerRenderer.getTableCellRendererComponent(null, column.getHeaderValue(), false, false, 0, 0);
            headerWidth     = comp.getPreferredSize().width;

            comp          = table.getDefaultRenderer(modelTable.getColumnClass(i)).getTableCellRendererComponent(table, longValues[i], false, false, 0, i);
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
                    processTableModel.setValueAt(new Boolean(false), row, 2);
                }

                buttonSelect.setText(rm.getString("button.selectAll"));
                select_all = true;
            } else {
                for (int row = 0; row < processTableModel.getRowCount(); row++) {
                    processTableModel.setValueAt(new Boolean(true), row, 2);
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
    class ProcessTableModel extends AbstractTableModel {
        private String[]      columnNames = { rm.getString("text.table.type"), rm.getString("text.table.sourceTableView"), rm.getString("text.table.process") };
        public final Object[] longValues = { "table", "abcdefghijklmnopqrstuvwxyz", new Boolean(false) };
        private List tablesList;
        
        public ProcessTableModel(List tablesList) {
    		this.tablesList = tablesList;
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
            return tablesList.size();
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
        	Element rowElement = (Element) tablesList.get(row);
            
        	switch (col) {
        		case 0: return rowElement.getAttributeValue(XMLTags.TABLE_TYPE);
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
            	Element rowElement = (Element) tablesList.get(row);
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
class PanelProcessTable_buttonSelect_actionAdapter implements java.awt.event.ActionListener {
    PanelProcessTable adaptee;

    /**
     * Creates a new PanelMappingTable_buttonSelect_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelProcessTable_buttonSelect_actionAdapter(PanelProcessTable adaptee) {
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
