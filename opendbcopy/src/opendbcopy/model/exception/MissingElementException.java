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
package opendbcopy.model.exception;

import opendbcopy.model.ProjectModel;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import org.jdom.Element;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class MissingElementException extends Exception {
    private ProjectModel projectModel;
    private Element      element;
    private String       elementName;

    /**
     * Creates a new MissingElementException object.
     *
     * @param projectModel DOCUMENT ME!
     * @param elementName DOCUMENT ME!
     */
    public MissingElementException(ProjectModel projectModel,
                                   String       elementName) {
        this.projectModel     = projectModel;
        this.elementName      = elementName;
    }

    /**
     * Creates a new MissingElementException object.
     *
     * @param element DOCUMENT ME!
     * @param elementName DOCUMENT ME!
     */
    public MissingElementException(Element element,
                                   String  elementName) {
        this.element         = element;
        this.elementName     = elementName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getMessage() {
        return getMissingElementName() + "\n\nAVAILABLE ELEMENTS\n" + getAvailableElements();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getMissingElementName() {
        return elementName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getAvailableElements() {
        if (element == null) {
            return ToStringBuilder.reflectionToString(projectModel, ToStringStyle.MULTI_LINE_STYLE);
        } else {
            return ToStringBuilder.reflectionToString(element, ToStringStyle.MULTI_LINE_STYLE);
        }
    }
}
