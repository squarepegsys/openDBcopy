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
package opendbcopy.swing;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


/**
 * Special implemenetation of JTable to allow individual RowEditors see http://www.javaworld.com/javatips/jw-javatip102_p.html for details
 *
 * @author Tony Culston
 * @version $Revision$
 */
public class JTableX extends JTable {
    protected RowEditorModel rm;

    /**
     * Creates a new JTableX object.
     */
    public JTableX() {
        super();
        rm = null;
    }

    /**
     * Creates a new JTableX object.
     *
     * @param tm DOCUMENT ME!
     */
    public JTableX(TableModel tm) {
        super(tm);
        rm = null;
    }

    /**
     * Creates a new JTableX object.
     *
     * @param tm DOCUMENT ME!
     * @param cm DOCUMENT ME!
     */
    public JTableX(TableModel       tm,
                   TableColumnModel cm) {
        super(tm, cm);
        rm = null;
    }

    /**
     * Creates a new JTableX object.
     *
     * @param tm DOCUMENT ME!
     * @param cm DOCUMENT ME!
     * @param sm DOCUMENT ME!
     */
    public JTableX(TableModel         tm,
                   TableColumnModel   cm,
                   ListSelectionModel sm) {
        super(tm, cm, sm);
        rm = null;
    }

    /**
     * Creates a new JTableX object.
     *
     * @param rows DOCUMENT ME!
     * @param cols DOCUMENT ME!
     */
    public JTableX(int rows,
                   int cols) {
        super(rows, cols);
        rm = null;
    }

    /**
     * Creates a new JTableX object.
     *
     * @param rowData DOCUMENT ME!
     * @param columnNames DOCUMENT ME!
     */
    public JTableX(final Vector rowData,
                   final Vector columnNames) {
        super(rowData, columnNames);
        rm = null;
    }

    /**
     * Creates a new JTableX object.
     *
     * @param rowData DOCUMENT ME!
     * @param colNames DOCUMENT ME!
     */
    public JTableX(final Object[][] rowData,
                   final Object[]   colNames) {
        super(rowData, colNames);
        rm = null;
    }

    // new constructor
    public JTableX(TableModel     tm,
                   RowEditorModel rm) {
        super(tm, null, null);
        this.rm = rm;
    }

    /**
     * DOCUMENT ME!
     *
     * @param rm DOCUMENT ME!
     */
    public void setRowEditorModel(RowEditorModel rm) {
        this.rm = rm;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public RowEditorModel getRowEditorModel() {
        return rm;
    }

    /**
     * DOCUMENT ME!
     *
     * @param row DOCUMENT ME!
     * @param col DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public TableCellEditor getCellEditor(int row,
                                         int col) {
        TableCellEditor tmpEditor = null;

        if (rm != null) {
            tmpEditor = rm.getEditor(row);
        }

        if (tmpEditor != null) {
            return tmpEditor;
        }

        return super.getCellEditor(row, col);
    }
}
