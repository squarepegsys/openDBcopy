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
package opendbcopy.model.typeinfo;

import opendbcopy.config.XMLTags;

import opendbcopy.model.exception.MissingAttributeException;
import opendbcopy.model.exception.UnsupportedAttributeValueException;

import org.jdom.Element;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class TypeInfo {
    private String  typeName = "";
    private String  localTypeName = "";
    private String  literalPrefix = "";
    private String  literalSuffix = "";
    private int     dataType = 0;
    private int     precision = 0;
    private boolean nullable = false;
    private boolean caseSensitive = false;

    /**
     * Creates a new TypeInfo object.
     *
     * @param element DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public TypeInfo(Element element) throws IllegalArgumentException, UnsupportedAttributeValueException, MissingAttributeException {
        if (element == null) {
            throw new IllegalArgumentException("Missing element");
        }

        if (element != null) {
            if (element.getAttributeValue(XMLTags.DATA_TYPE) == null) {
                throw new MissingAttributeException(element, XMLTags.DATA_TYPE);
            }

            if (element.getAttributeValue(XMLTags.PRECISION) == null) {
                throw new MissingAttributeException(element, XMLTags.PRECISION);
            }

            if (element.getAttributeValue(XMLTags.NULLABLE) == null) {
                throw new MissingAttributeException(element, XMLTags.NULLABLE);
            }

            if (element.getAttributeValue(XMLTags.CASE_SENSITIVE) == null) {
                throw new MissingAttributeException(element, XMLTags.CASE_SENSITIVE);
            }

            typeName          = element.getAttributeValue(XMLTags.TYPE_NAME);
            localTypeName     = element.getAttributeValue(XMLTags.LOCAL_TYPE_NAME);
            literalPrefix     = element.getAttributeValue(XMLTags.LITERAL_PREFIX);
            literalSuffix     = element.getAttributeValue(XMLTags.LITERAL_SUFFIX);

            try {
                dataType      = Integer.parseInt(element.getAttributeValue(XMLTags.DATA_TYPE));
                precision     = Integer.parseInt(element.getAttributeValue(XMLTags.PRECISION));
            } catch (NumberFormatException e) {
                throw new UnsupportedAttributeValueException(element, XMLTags.DATA_TYPE + " and " + XMLTags.PRECISION + " must be numeric");
            }

            nullable          = Boolean.valueOf(element.getAttributeValue(XMLTags.NULLABLE)).booleanValue();
            caseSensitive     = Boolean.valueOf(element.getAttributeValue(XMLTags.CASE_SENSITIVE)).booleanValue();

            // check values that are null
            if (literalPrefix.compareToIgnoreCase("null") == 0) {
                literalPrefix = null;
            }

            if (literalSuffix.compareToIgnoreCase("null") == 0) {
                literalSuffix = null;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public final boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public final int getDataType() {
        return dataType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public final String getLiteralPrefix() {
        return literalPrefix;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public final String getLiteralSuffix() {
        return literalSuffix;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public final String getLocalTypeName() {
        return localTypeName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public final boolean isNullable() {
        return nullable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public final int getPrecision() {
        return precision;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public final String getTypeName() {
        return typeName;
    }
}
