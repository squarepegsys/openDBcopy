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
package opendbcopy.model.dependency;

import java.util.HashMap;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class Node {
    private String  name = "";
    private HashMap childNodes = null;
    private HashMap parentNodes = null;

    /**
     * Creates a new Node object.
     *
     * @param name DOCUMENT ME!
     */
    public Node(String name) {
        this.name = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final String getName() {
        return this.name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     */
    public final void addChild(Node node) {
        if (childNodes == null) {
            childNodes = new HashMap();
        }

        childNodes.put(node.getName(), node);
    }

    /**
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     */
    public final void addParent(Node node) {
        if (parentNodes == null) {
            parentNodes = new HashMap();
        }

        parentNodes.put(node.getName(), node);
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final Node findChild(String name) {
        if (childNodes.containsKey(name)) {
            return (Node) childNodes.get(name);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final Node findParent(String name) {
        if (parentNodes.containsKey(name)) {
            return (Node) parentNodes.get(name);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final boolean containsChildren() {
        if (childNodes != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final HashMap getChildren() {
        if (childNodes != null) {
            return childNodes;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final boolean containsParents() {
        if (parentNodes != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final HashMap getParents() {
        if (parentNodes != null) {
            return parentNodes;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final boolean compareTo(Node node) {
        if (this.getName().compareTo(node.getName()) == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param object DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final boolean equals(Object object) {
        if (this.getName().compareTo(((Node) object).getName()) == 0) {
            return true;
        } else {
            return false;
        }
    }
}
