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
public class Column {
    private Element columnElement;
    private String  name;
    private String  typeName;
    private Integer dataType;
    private Integer columnSize;
    private Integer decimalDigits;
    private Boolean isNullable;

    /**
     * Creates a new Column object.
     */
    public Column() {
    }

    /**
     * Creates a new Column object.
     *
     * @param name DOCUMENT ME!
     */
    public Column(String name) {
        this.name = name;
    }

    /**
     * Creates a new Column object.
     *
     * @param columnElement DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public Column(Element columnElement) throws MissingAttributeException {
        this.columnElement = columnElement;

        // name
        if (columnElement.getAttributeValue(XMLTags.NAME) != null) {
            name = columnElement.getAttributeValue(XMLTags.NAME);
        } else {
            throw new MissingAttributeException(columnElement, XMLTags.NAME);
        }

        // typeName
        if (columnElement.getAttributeValue(XMLTags.TYPE_NAME) != null) {
            typeName = columnElement.getAttributeValue(XMLTags.TYPE_NAME);
        } else {
            throw new MissingAttributeException(columnElement, XMLTags.TYPE_NAME);
        }

        // dataType
        if (columnElement.getAttributeValue(XMLTags.DATA_TYPE) != null) {
            dataType = new Integer(columnElement.getAttributeValue(XMLTags.DATA_TYPE));
        } else {
            throw new MissingAttributeException(columnElement, XMLTags.DATA_TYPE);
        }

        // columnSize
        if (columnElement.getAttributeValue(XMLTags.COLUMN_SIZE) != null) {
            columnSize = new Integer(columnElement.getAttributeValue(XMLTags.COLUMN_SIZE));
        } else {
            throw new MissingAttributeException(columnElement, XMLTags.COLUMN_SIZE);
        }

        // decimalDigits - may be null
        if (columnElement.getAttributeValue(XMLTags.DECIMAL_DIGITS) != null) {
            if (columnElement.getAttributeValue(XMLTags.DECIMAL_DIGITS).compareToIgnoreCase("null") == 0) {
                decimalDigits = null;
            } else {
                decimalDigits = new Integer(columnElement.getAttributeValue(XMLTags.DECIMAL_DIGITS));
            }
        } else {
            throw new MissingAttributeException(columnElement, XMLTags.DECIMAL_DIGITS);
        }

        // isNullable
        if (columnElement.getAttributeValue(XMLTags.NULLABLE) != null) {
            isNullable = Boolean.valueOf((columnElement.getAttributeValue(XMLTags.NULLABLE)));
        } else {
            throw new MissingAttributeException(columnElement, XMLTags.COLUMN_SIZE);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the columnSize.
     */
    public Integer getColumnSize() {
        return columnSize;
    }

    /**
     * DOCUMENT ME!
     *
     * @param columnSize The columnSize to set.
     */
    public void setColumnSize(Integer columnSize) {
        this.columnSize = columnSize;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the dataType.
     */
    public Integer getDataType() {
        return dataType;
    }

    /**
     * DOCUMENT ME!
     *
     * @param dataType The dataType to set.
     */
    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the decimalDigits.
     */
    public Integer getDecimalDigits() {
        return decimalDigits;
    }

    /**
     * DOCUMENT ME!
     *
     * @param decimalDigits The decimalDigits to set.
     */
    public void setDecimalDigits(Integer decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the isNullable.
     */
    public Boolean isNullable() {
        return isNullable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param isNullable The isNullable to set.
     */
    public void setNullable(Boolean isNullable) {
        this.isNullable = isNullable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the typeName.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName The typeName to set.
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
