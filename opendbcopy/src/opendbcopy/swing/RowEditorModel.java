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
package opendbcopy.swing;

import java.util.Hashtable;

import javax.swing.table.TableCellEditor;


/**
 * Special implemenetation of JTable to allow individual RowEditors see http://www.javaworld.com/javatips/jw-javatip102_p.html for details
 *
 * @author Tony Culston
 * @version $Revision$
 */
public class RowEditorModel {
    private Hashtable data;

    /**
     * Creates a new RowEditorModel object.
     */
    public RowEditorModel() {
        data = new Hashtable();
    }

    /**
     * DOCUMENT ME!
     *
     * @param row DOCUMENT ME!
     * @param e DOCUMENT ME!
     */
    public void addEditorForRow(int             row,
                                TableCellEditor e) {
        data.put(new Integer(row), e);
    }

    /**
     * DOCUMENT ME!
     *
     * @param row DOCUMENT ME!
     */
    public void removeEditorForRow(int row) {
        data.remove(new Integer(row));
    }

    /**
     * DOCUMENT ME!
     *
     * @param row DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public TableCellEditor getEditor(int row) {
        return (TableCellEditor) data.get(new Integer(row));
    }
}
