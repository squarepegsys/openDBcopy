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
package opendbcopy.plugin.model.database.beans;

import opendbcopy.config.XMLTags;

import opendbcopy.plugin.model.exception.MissingAttributeException;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;


/**
 * class description
 *
 * @author  Anthony Smith
 * @version $Revision$
 */
public class Table {
    private Element   table;
    private String    name;
    private ArrayList columns;
    private ArrayList primaryKeys;
    private ArrayList exportedKeys;
    private ArrayList importedKeys;
    private ArrayList indexes;

    /**
     * Creates a new Table object.
     */
    public Table() {
    }

    /**
     * Creates a new Table object.
     *
     * @param name DOCUMENT ME!
     */
    public Table(String name) {
        this.name = name;
    }

    /**
     * Creates a new Table object.
     *
     * @param table DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public Table(Element table) throws MissingAttributeException {
        this.table     = table;
        columns        = new ArrayList();

        name = table.getAttributeValue(XMLTags.NAME);

        // columns
        if ((table.getChildren(XMLTags.COLUMN) != null) && (table.getChildren(XMLTags.COLUMN).size() > 0)) {
            parseColumns(table.getChildren(XMLTags.COLUMN));
        }

        // primary key elements
        if ((table.getChildren(XMLTags.PRIMARY_KEY) != null) && (table.getChildren(XMLTags.PRIMARY_KEY).size() > 0)) {
            primaryKeys = new ArrayList();
            parsePrimaryKeys(table.getChildren(XMLTags.PRIMARY_KEY));
        }

        // imported key elements
        if ((table.getChildren(XMLTags.IMPORTED_KEY) != null) && (table.getChildren(XMLTags.IMPORTED_KEY).size() > 0)) {
            importedKeys = new ArrayList();
            parseImportedKeys(table.getChildren(XMLTags.IMPORTED_KEY));
        }

        // exported key elements
        if ((table.getChildren(XMLTags.EXPORTED_KEY) != null) && (table.getChildren(XMLTags.EXPORTED_KEY).size() > 0)) {
            exportedKeys = new ArrayList();
            parseExportedKeys(table.getChildren(XMLTags.EXPORTED_KEY));
        }

        // index elements
        if ((table.getChildren(XMLTags.INDEX) != null) && (table.getChildren(XMLTags.INDEX).size() > 0)) {
            indexes = new ArrayList();
            parseIndexes(table.getChildren(XMLTags.INDEX));
        }
    }
    
    public void addColumn(int index, Column column) {
    	if (columns.contains(column)) {
    		throw new RuntimeException("table " + getName() + " already contains column " + column.getName());
    	} else {
        	columns.add(index, column);
    	}
    }

    public void removeColumn(Column column) {
    	if (columns.contains(column)) {
    		columns.remove(column);
    	} else {
    		throw new RuntimeException("table " + getName() + " does not contain column " + column.getName());
    	}
    }

    /**
     * DOCUMENT ME!
     *
     * @param columnList DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    private void parseColumns(List columnList) throws MissingAttributeException {
        for (int i = 0; i < columnList.size(); i++) {
            Column column = new Column((Element) columnList.get(i));
            columns.add(column);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param primaryKeyList DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    private void parsePrimaryKeys(List primaryKeyList) throws MissingAttributeException {
        for (int i = 0; i < primaryKeyList.size(); i++) {
            PrimaryKey primaryKey = new PrimaryKey((Element) primaryKeyList.get(i));
            primaryKeys.add(primaryKey);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param importedKeyList DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    private void parseImportedKeys(List importedKeyList) throws MissingAttributeException {
        for (int i = 0; i < importedKeyList.size(); i++) {
            Key key = new Key((Element) importedKeyList.get(i));
            importedKeys.add(key);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param exportedKeyList DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    private void parseExportedKeys(List exportedKeyList) throws MissingAttributeException {
        for (int i = 0; i < exportedKeyList.size(); i++) {
            Key key = new Key((Element) exportedKeyList.get(i));
            exportedKeys.add(key);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param indexList DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    private void parseIndexes(List indexList) throws MissingAttributeException {
        for (int i = 0; i < indexList.size(); i++) {
            Index index = new Index((Element) indexList.get(i));
            indexes.add(index);
        }
    }
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
}
