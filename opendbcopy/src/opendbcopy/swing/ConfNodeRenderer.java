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

import opendbcopy.resource.ResourceManager;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class ConfNodeRenderer extends DefaultTreeCellRenderer {
    ResourceManager rm;
    ImageIcon       iconDir;
    ImageIcon       iconFile;
    ImageIcon       iconConfig;

    /**
     * Creates a new ConfNodeRenderer object.
     *
     * @param rm DOCUMENT ME!
     * @param iconDir DOCUMENT ME!
     * @param iconFile DOCUMENT ME!
     * @param iconConfig DOCUMENT ME!
     */
    public ConfNodeRenderer(ResourceManager rm,
                            ImageIcon       iconDir,
                            ImageIcon       iconFile,
                            ImageIcon       iconConfig) {
        this.rm             = rm;
        this.iconDir        = iconDir;
        this.iconFile       = iconFile;
        this.iconConfig     = iconConfig;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tree DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @param sel DOCUMENT ME!
     * @param expanded DOCUMENT ME!
     * @param leaf DOCUMENT ME!
     * @param row DOCUMENT ME!
     * @param hasFocus DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Component getTreeCellRendererComponent(JTree   tree,
                                                  Object  value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int     row,
                                                  boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        ConfNode confNode = (ConfNode) value;

        if (leaf && (confNode.getAttributeType().compareToIgnoreCase(XMLTags.FILE) == 0)) {
            setIcon(iconFile);
        } else if (leaf && (confNode.getAttributeType().compareToIgnoreCase(XMLTags.DIR) == 0)) {
            setIcon(iconDir);
        } else if (leaf) {
            setIcon(iconConfig);
        }

        if (confNode.getElementDescription() != null) {
        	if (confNode.getName().compareTo(confNode.getElementDescription()) != 0) {
                setToolTipText(rm.getString(confNode.getElementDescription()));
        	}
        }

        return this;
    }
}
