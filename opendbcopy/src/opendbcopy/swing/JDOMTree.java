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

import org.jdom.Attribute;
import org.jdom.Element;

import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class JDOMTree extends JTree {
    private Element rootElement;

    /**
     * Creates a new JDOMTree object.
     *
     * @param treeRoot DOCUMENT ME!
     * @param rootElement DOCUMENT ME!
     */
    public JDOMTree(DefaultMutableTreeNode treeRoot,
                    Element                rootElement) {
        super(treeRoot);

        this.rootElement = rootElement;

        processElement(rootElement, treeRoot);
    }

    /**
     * DOCUMENT ME!
     *
     * @param el DOCUMENT ME!
     * @param dmtn DOCUMENT ME!
     */
    protected void processElement(Element                el,
                                  DefaultMutableTreeNode dmtn) {
        DefaultMutableTreeNode dmtnLocal = new DefaultMutableTreeNode(el.getName());
        String                 elText = el.getTextNormalize();

        if ((elText != null) && !elText.equals("")) {
            dmtnLocal.add(new DefaultMutableTreeNode(elText));
        }

        processAttributes(el, dmtnLocal);

        Iterator iter = el.getChildren().iterator();

        while (iter.hasNext()) {
            Element nextEl = (Element) iter.next();
            processElement(nextEl, dmtnLocal);
        }

        dmtn.add(dmtnLocal);
    }

    /**
     * DOCUMENT ME!
     *
     * @param el DOCUMENT ME!
     * @param dmtn DOCUMENT ME!
     */
    protected void processAttributes(Element                el,
                                     DefaultMutableTreeNode dmtn) {
        Iterator atts = el.getAttributes().iterator();

        while (atts.hasNext()) {
            Attribute              att = (Attribute) atts.next();
            DefaultMutableTreeNode node = new DefaultMutableTreeNode("@" + att.getName());
            node.add(new DefaultMutableTreeNode(att.getValue()));
            dmtn.add(node);
        }
    }
}
