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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.HashMap;
import java.util.TreeMap;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class DependencyNotSolvableException extends Exception {
    private HashMap unsortedNodes;
    private TreeMap sortedNodes;
    private int     nbrTries;
    private String  message;

    /**
     * Creates a new DependencyNotSolvableException object.
     *
     * @param unsortedNodes DOCUMENT ME!
     * @param sortedNodes DOCUMENT ME!
     * @param nbrTries DOCUMENT ME!
     * @param message DOCUMENT ME!
     */
    public DependencyNotSolvableException(HashMap unsortedNodes,
                                          TreeMap sortedNodes,
                                          int     nbrTries,
                                          String  message) {
        this.unsortedNodes     = unsortedNodes;
        this.sortedNodes       = sortedNodes;
        this.nbrTries          = nbrTries;
        this.message           = message;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getMessage() {
        return message + "\nUNSORTED ELEMENTS\n" + getUnsortedNodesListed() + "\n\nSORTED ELEMENTS\n" + getSortedNodesListed() + "\n\ngiven up after " + getNbrTries() + " tries";
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getNbrTries() {
        return nbrTries;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getUnsortedNodesListed() {
        return ToStringBuilder.reflectionToString(unsortedNodes, ToStringStyle.MULTI_LINE_STYLE);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getSortedNodesListed() {
        return ToStringBuilder.reflectionToString(sortedNodes, ToStringStyle.MULTI_LINE_STYLE);
    }
}
