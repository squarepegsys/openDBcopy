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

import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class ElementTreeModel implements TreeModel {
    private Vector   treeModelListeners = new Vector();
    private ConfNode root;

    /**
     * Creates a new ElementTreeModel object.
     *
     * @param root DOCUMENT ME!
     */
    public ElementTreeModel(ConfNode root) {
        this.root = root;
    }

    /**
     * The only event raised by this model is TreeStructureChanged with the root as path, i.e. the whole tree has changed.
     *
     * @param oldRoot DOCUMENT ME!
     */
    protected void fireTreeStructureChanged(ConfNode oldRoot) {
        int            len = treeModelListeners.size();
        TreeModelEvent e = new TreeModelEvent(this, new Object[] { oldRoot });

        for (int i = 0; i < len; i++) {
            ((TreeModelListener) treeModelListeners.elementAt(i)).treeStructureChanged(e);
        }
    }

    /**
     * Notifies all listeners that have registered interest for notification on this event type.  The event instance  is lazily created using the
     * parameters passed into  the fire method.
     *
     * @param confNode the node being changed
     *
     * @see EventListenerList
     */
    protected void fireTreeNodesChanged(ConfNode confNode) {
        int            len = treeModelListeners.size();
        TreeModelEvent e = new TreeModelEvent(this, new Object[] { confNode });

        for (int i = 0; i < len; i++) {
            ((TreeModelListener) treeModelListeners.elementAt(i)).treeNodesChanged(e);
        }
    }

    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     *
     * @param l DOCUMENT ME!
     */
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.addElement(l);
    }

    /**
     * Returns the child of parent at index index in the parent's child array.
     *
     * @param parent DOCUMENT ME!
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public Object getChild(Object parent,
                           int    index) {
        if (parent == null) {
            throw new IllegalArgumentException("Missing parent");
        }

        return ((ConfNode) parent).getChildren().get(index);
    }

    /**
     * Returns the number of children of parent.
     *
     * @param parent DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getChildCount(Object parent) {
        return ((ConfNode) parent).getChildren().size();
    }

    /**
     * Returns the index of child in parent.
     *
     * @param parent DOCUMENT ME!
     * @param child DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getIndexOfChild(Object parent,
                               Object child) {
        ConfNode parentConfNode = (ConfNode) parent;
        ConfNode childConfNode = (ConfNode) child;

        return parentConfNode.getChildren().indexOf(childConfNode);
    }

    /**
     * Returns the root of the tree.
     *
     * @return DOCUMENT ME!
     */
    public Object getRoot() {
        return root;
    }

    /**
     * Returns true if node is a leaf.
     *
     * @param node DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isLeaf(Object node) {
        if (((ConfNode) node).getChildren().size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes a listener previously added with addTreeModelListener().
     *
     * @param l DOCUMENT ME!
     */
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.removeElement(l);
    }

    /**
     * Messaged when the user has altered the value for the item identified by path to newValue.  Not used by this model.
     *
     * @param path DOCUMENT ME!
     * @param newValue DOCUMENT ME!
     */
    public void valueForPathChanged(TreePath path,
                                    Object   newValue) {
        fireTreeNodesChanged((ConfNode) newValue);
    }
}
