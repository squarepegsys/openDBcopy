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

import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.UnsupportedAttributeValueException;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class BeanManager {
    private DatabaseModel dbProject;
    private Element       modelElement;
    private ArrayList     tables;

    /**
     * Creates a new BeanManager object.
     *
     * @param dbProject DOCUMENT ME!
     * @param modelElement DOCUMENT ME!
     */
    public BeanManager(DatabaseModel dbProject,
                       Element       modelElement) {
        this.dbProject        = dbProject;
        this.modelElement     = modelElement;
    }

    /**
     * DOCUMENT ME!
     */
    public void createEmptyModel() {
        if (tables != null) {
            tables.clear();
        } else {
            tables = new ArrayList();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Table findTable(String tableName) {
        for (int i = 0; i < tables.size(); i++) {
            Table table = (Table) tables.get(i);

            if (table.getName().compareTo(tableName) == 0) {
                return table;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param table DOCUMENT ME!
     */
    public void removeTable(Table table) {
        if (tables.contains(table)) {
            tables.remove(table);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     */
    public void parseTables() throws MissingAttributeException, UnsupportedAttributeValueException {
        if (tables != null) {
            tables.clear();
        } else {
            tables = new ArrayList();
        }

        if ((modelElement.getChildren(XMLTags.TABLE) != null) && (modelElement.getChildren(XMLTags.TABLE).size() > 0)) {
            List tableList = modelElement.getChildren(XMLTags.TABLE);

            for (int i = 0; i < tableList.size(); i++) {
                Element tableElement = (Element) tableList.get(i);

                if (tableElement.getAttributeValue(XMLTags.TABLE_TYPE) != null) {
                    String tableType = tableElement.getAttributeValue(XMLTags.TABLE_TYPE);

                    if (tableType.compareToIgnoreCase(XMLTags.TABLE) == 0) {
                        Table table = new Table(tableElement);
                        tables.add(table);
                    } else if (tableType.compareToIgnoreCase(XMLTags.VIEW) == 0) {
                    } else {
                        throw new UnsupportedAttributeValueException(tableElement, XMLTags.TABLE_TYPE);
                    }
                } else {
                    throw new MissingAttributeException(tableElement, XMLTags.TABLE_TYPE);
                }
            }
        }
    }
}
