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
public class Index {
    private Element indexElement;
    private String  indexName;
    private String  columnName;
    private Boolean isUnique;
    private Integer ordinalPosition;

    /**
     * Creates a new Index object.
     */
    public Index() {
    }

    /**
     * Creates a new Index object.
     *
     * @param indexElement DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public Index(Element indexElement) throws MissingAttributeException {
        this.indexElement = indexElement;

        // index name
        if (indexElement.getAttributeValue(XMLTags.INDEX_NAME) != null) {
            indexName = indexElement.getAttributeValue(XMLTags.INDEX_NAME);
        } else {
            throw new MissingAttributeException(indexElement, XMLTags.INDEX_NAME);
        }

        // column name
        if (indexElement.getAttributeValue(XMLTags.COLUMN_NAME) != null) {
            columnName = indexElement.getAttributeValue(XMLTags.COLUMN_NAME);
        } else {
            throw new MissingAttributeException(indexElement, XMLTags.COLUMN_NAME);
        }

        // isUnique
        if (indexElement.getAttributeValue(XMLTags.NON_UNIQUE) != null) {
            if (indexElement.getAttributeValue(XMLTags.NON_UNIQUE).compareTo("0") == 0) {
                isUnique = new Boolean(true);
            } else if (indexElement.getAttributeValue(XMLTags.NON_UNIQUE).compareTo("1") == 0) {
                isUnique = new Boolean(false);
            }
        } else {
            throw new MissingAttributeException(indexElement, XMLTags.NON_UNIQUE);
        }

        // ordinal position
        if (indexElement.getAttributeValue(XMLTags.ORDINAL_POSITION) != null) {
            if (indexElement.getAttributeValue(XMLTags.ORDINAL_POSITION).compareToIgnoreCase("null") != 0) {
                ordinalPosition = new Integer(indexElement.getAttributeValue(XMLTags.ORDINAL_POSITION));
            }
        } else {
            throw new MissingAttributeException(indexElement, XMLTags.ORDINAL_POSITION);
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
     * @return Returns the indexName.
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param indexName The indexName to set.
     */
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the isUnique.
     */
    public Boolean getIsUnique() {
        return isUnique;
    }

    /**
     * DOCUMENT ME!
     *
     * @param isUnique The isUnique to set.
     */
    public void setIsUnique(Boolean isUnique) {
        this.isUnique = isUnique;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the ordinalPosition.
     */
    public Integer getOrdinalPosition() {
        return ordinalPosition;
    }

    /**
     * DOCUMENT ME!
     *
     * @param ordinalPosition The ordinalPosition to set.
     */
    public void setOrdinalPosition(Integer ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }
}
