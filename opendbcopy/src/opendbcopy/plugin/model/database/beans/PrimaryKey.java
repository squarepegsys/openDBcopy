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
public class PrimaryKey {
    private Element pkElement;
    private String  keyName;
    private String  columnName;
    private Integer keySequence;

    /**
     * Creates a new PrimaryKey object.
     */
    public PrimaryKey() {
    }

    /**
     * Creates a new PrimaryKey object.
     *
     * @param keyName DOCUMENT ME!
     */
    public PrimaryKey(String keyName) {
        this.keyName = keyName;
    }

    /**
     * Creates a new PrimaryKey object.
     *
     * @param pkElement DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public PrimaryKey(Element pkElement) throws MissingAttributeException {
        this.pkElement = pkElement;

        // key name
        if (pkElement.getAttributeValue(XMLTags.PK_NAME) != null) {
            keyName = pkElement.getAttributeValue(XMLTags.PK_NAME);
        } else {
            throw new MissingAttributeException(pkElement, XMLTags.PK_NAME);
        }

        // column name
        if (pkElement.getAttributeValue(XMLTags.COLUMN_NAME) != null) {
            columnName = pkElement.getAttributeValue(XMLTags.COLUMN_NAME);
        } else {
            throw new MissingAttributeException(pkElement, XMLTags.COLUMN_NAME);
        }

        // key sequence
        if (pkElement.getAttributeValue(XMLTags.KEY_SEQ) != null) {
            if (pkElement.getAttributeValue(XMLTags.KEY_SEQ).compareToIgnoreCase("null") != 0) {
                keySequence = new Integer(pkElement.getAttributeValue(XMLTags.KEY_SEQ));
            }
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
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the keyName.
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param keyName The keyName to set.
     */
    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the keySequence.
     */
    public Integer getKeySequence() {
        return keySequence;
    }

    /**
     * DOCUMENT ME!
     *
     * @param keySequence The keySequence to set.
     */
    public void setKeySequence(Integer keySequence) {
        this.keySequence = keySequence;
    }
}
