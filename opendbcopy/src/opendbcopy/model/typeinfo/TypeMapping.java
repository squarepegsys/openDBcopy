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

import org.jdom.Document;
import org.jdom.Element;

import java.util.HashMap;
import java.util.Iterator;


/**
 * class description
 *
 * @author iloveopensource
 * @version $Revision$
 */
public class TypeMapping {
    private HashMap typeMap;

    /**
     * Creates a new TypeMapping object.
     *
     * @param typeMapping DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public TypeMapping(Document typeMapping) throws Exception {
        setupTypeMapping(typeMapping);
    }

    /**
     * DOCUMENT ME!
     *
     * @param sqlTypeNumber DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getJavaType(String sqlTypeNumber) {
        if (sqlTypeNumber != null) {
            if (typeMap.containsKey(sqlTypeNumber)) {
                return (String) typeMap.get(sqlTypeNumber);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeMapping DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void setupTypeMapping(Document typeMapping) throws Exception {
        Iterator itTypes = typeMapping.getRootElement().getChildren(XMLTags.TYPE).iterator();
        Element  typeElement = null;
        typeMap = new HashMap();

        while (itTypes.hasNext()) {
            typeElement = (Element) itTypes.next();

            if ((typeElement.getAttributeValue(XMLTags.NUMBER) != null) && (typeElement.getAttributeValue(XMLTags.NUMBER).length() > 0)) {
                if ((typeElement.getAttributeValue(XMLTags.MAPPING) != null) && (typeElement.getAttributeValue(XMLTags.MAPPING).length() > 0)) {
                    typeMap.put(typeElement.getAttributeValue(XMLTags.NUMBER), typeElement.getAttributeValue(XMLTags.MAPPING));
                }
            }
        }
    }
}
