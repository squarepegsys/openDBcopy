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
package opendbcopy.gui;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.model.Model;

import opendbcopy.swing.JDOMTree;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PanelConfiguration extends DynamicPanel {
    private Model                  model;
    private JPanel                 panelRight;
    private JSplitPane             splitPane;
    private JDOMTree               tree;
    private DefaultMutableTreeNode top;
    private JScrollPane            scrollPaneTree;

    /**
     * Creates a new PanelConfiguration object.
     *
     * @param controller DOCUMENT ME!
     * @param workingMode DOCUMENT ME!
     * @param registerAsObserver DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public PanelConfiguration(MainController controller,
                              WorkingMode    workingMode,
                              Boolean        registerAsObserver) throws Exception {
        super(controller, workingMode, registerAsObserver);
        model = super.model;
        guiInit();
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param obj DOCUMENT ME!
     */
    public final void update(Observable o,
                             Object     obj) {
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void guiInit() throws Exception {
        top      = new DefaultMutableTreeNode("Plugin Configuration");
        tree     = new JDOMTree(top, model.getConf());
        tree.setEditable(true);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                    if (node == null) {
                        return;
                    }

                    if (node.getLevel() == 1) {
                        System.out.println(node);
                    }
                }
            });

        scrollPaneTree     = new JScrollPane(tree);

        panelRight     = new JPanel(new BorderLayout(10, 10));

        splitPane = new JSplitPane();
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOpaque(false);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.3);

        splitPane.add(scrollPaneTree, JSplitPane.LEFT);
        splitPane.add(panelRight, JSplitPane.RIGHT);

        this.setLayout(new GridLayout(1, 1, 20, 20));
        this.add(splitPane);
        this.add(panelRight);
    }
}
