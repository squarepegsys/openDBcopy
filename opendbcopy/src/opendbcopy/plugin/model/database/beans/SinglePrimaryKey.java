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


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class SinglePrimaryKey {
    private Element primaryKeyElement;
    private String  pkName;
    private String  columnName;
    private boolean isUnique;

    /**
     * Creates a new SinglePrimaryKey object.
     *
     * @param primaryKeyElement DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public SinglePrimaryKey(Element primaryKeyElement) throws MissingAttributeException {
        this.primaryKeyElement = primaryKeyElement;

        if (primaryKeyElement.getAttributeValue(XMLTags.COLUMN_NAME) == null) {
            throw new MissingAttributeException(primaryKeyElement, XMLTags.COLUMN_NAME);
        } else {
            columnName = primaryKeyElement.getAttributeValue(XMLTags.COLUMN_NAME);
        }

        if (primaryKeyElement.getAttributeValue(XMLTags.PK_NAME) == null) {
            throw new MissingAttributeException(primaryKeyElement, XMLTags.PK_NAME);
        } else {
            pkName = primaryKeyElement.getAttributeValue(XMLTags.PK_NAME);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the columnName.
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param columnName The columnName to set.
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
        primaryKeyElement.setAttribute(XMLTags.COLUMN_NAME, columnName);
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the pkName.
     */
    public String getPkName() {
        return pkName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pkName The pkName to set.
     */
    public void setPkName(String pkName) {
        this.pkName = pkName;
        primaryKeyElement.setAttribute(XMLTags.PK_NAME, pkName);
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the isUnique.
     */
    public boolean isUnique() {
        return isUnique;
    }

    /**
     * DOCUMENT ME!
     *
     * @param isUnique The isUnique to set.
     */
    public void setUnique(boolean isUnique) {
        this.isUnique = isUnique;
    }
}
