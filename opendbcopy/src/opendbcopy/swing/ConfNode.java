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

import opendbcopy.config.XMLTags;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class ConfNode {
    ConfNode        parent;
    ArrayList       children;
    private Element element;
    private String  elementName;
    private String  elementDescription;
    private String  attributeValue;
    private String  attributeType;
    private boolean required;
    private boolean editable;

    /**
     * Creates a new ConfNode object.
     *
     * @param element DOCUMENT ME!
     */
    public ConfNode(Element element) {
        this.element                = element;
        this.elementName            = element.getName();
        this.attributeValue         = element.getAttributeValue(XMLTags.VALUE);
        this.attributeType          = element.getAttributeValue(XMLTags.TYPE);
        this.elementDescription     = element.getAttributeValue(XMLTags.DESCRIPTION);

        // set default values if not provided by element
        if (attributeType == null) {
            attributeType = "String";
        }

        if (elementDescription == null) {
            elementDescription = elementName;
        }

        if (element.getAttributeValue(XMLTags.REQUIRED) != null) {
            required = Boolean.valueOf(element.getAttributeValue(XMLTags.REQUIRED)).booleanValue();
        } else {
            required = false;
        }

        if (element.getAttributeValue(XMLTags.EDITABLE) != null) {
            editable = Boolean.valueOf(element.getAttributeValue(XMLTags.EDITABLE)).booleanValue();
        } else {
            editable = true;
        }

        children = new ArrayList();

        if (element.getChildren().size() > 0) {
            retrieveChildren();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void retrieveChildren() {
        Iterator itChildren = element.getChildren().iterator();

        while (itChildren.hasNext()) {
            children.add(new ConfNode((Element) itChildren.next()));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        if (isRequired()) {
            return "* " + elementName + " (" + attributeValue + ")";
        } else {
            if (attributeValue != null) {
                return elementName + " (" + attributeValue + ")";
            } else {
                return elementName;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return elementName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ConfNode getParent() {
        return parent;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getChildCount() {
        return children.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ConfNode getChildAt(int index) {
        return (ConfNode) children.get(index);
    }

    /**
     * DOCUMENT ME!
     *
     * @param confNodeChild DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getIndexOfChild(ConfNode confNodeChild) {
        return children.indexOf(confNodeChild);
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the children.
     */
    public final ArrayList getChildren() {
        return children;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the attributeValue.
     */
    public final String getAttributeValue() {
        return attributeValue;
    }

    /**
     * DOCUMENT ME!
     *
     * @param attributeValue The attributeValue to set.
     */
    public final void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
        element.setAttribute(XMLTags.VALUE, attributeValue);
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the attributeType.
     */
    public final String getAttributeType() {
        return attributeType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the elementDescription.
     */
    public final String getElementDescription() {
        return elementDescription;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the elementName.
     */
    public final String getElementName() {
        return elementName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the required.
     */
    public final boolean isRequired() {
        return required;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final boolean isLeaf() {
        if (children.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the editable.
     */
    public final boolean isEditable() {
        return editable;
    }
}
