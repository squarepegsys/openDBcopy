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

import opendbcopy.config.GUI;
import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.model.Model;

import opendbcopy.swing.ConfNode;
import opendbcopy.swing.ConfNodeRenderer;
import opendbcopy.swing.JDOMTree;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.SystemColor;

import java.io.File;

import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.border.TitledBorder;
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
    private JDOMTree               tree;
    private DefaultMutableTreeNode top;
    private JPanel                 panelMain;
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
                              PluginGui      workingMode,
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
        ConfNode rootConfNode = new ConfNode(model.getConf());

        tree = new JDOMTree(rootConfNode);

        tree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    ConfNode node = (ConfNode) tree.getLastSelectedPathComponent();

                    if (node == null) {
                        return;
                    }

                    if (node.isLeaf()) {
                        if (node.isEditable()) {
                            provideSelection(node);
                        }
                    }
                }
            });

        // opens first two rows below root
        tree.expandRow(0);
        tree.expandRow(1);
        tree.expandRow(2);

        // set default height in pixels
        tree.setRowHeight(40);

        // hide root as it does not make too much sense to show
        tree.setRootVisible(false);

        // set cell renderer
        ToolTipManager.sharedInstance().registerComponent(tree);

        tree.setCellRenderer(new ConfNodeRenderer(rm, GUI.getImageIconDir(), GUI.getImageIconFile(), GUI.getImageIconConfig()));

        scrollPaneTree     = new JScrollPane(tree);

        panelMain = new JPanel(new BorderLayout());
        panelMain.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.configuration.title") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        panelMain.add(scrollPaneTree);
        this.setLayout(new GridLayout(1, 1));
        this.add(panelMain);
    }

    /**
     * DOCUMENT ME!
     *
     * @param confNode DOCUMENT ME!
     */
    private void provideSelection(ConfNode confNode) {
        Object[] possibilitiesBoolean = { "true", "false" };
        Object[] possibilitiesFileDirFilelistSelection = { XMLTags.FILE, XMLTags.DIR, XMLTags.FILELIST };

        Object[] possibilities = null;
        String   input = null;

        if (confNode.getAttributeType().compareToIgnoreCase(XMLTags.STRING) == 0) {
            input = (String) JOptionPane.showInputDialog(this, rm.getString(confNode.getElementDescription()), rm.getString(confNode.getElementDescription()), JOptionPane.PLAIN_MESSAGE, GUI.getImageIconConfig(), possibilities, confNode.getAttributeValue());
        }

        if (confNode.getAttributeType().compareToIgnoreCase(XMLTags.FILE_DIR_FILELISTS_SELECTION) == 0) {
            input = (String) JOptionPane.showInputDialog(this, rm.getString(confNode.getElementDescription()), rm.getString(confNode.getElementDescription()), JOptionPane.PLAIN_MESSAGE, GUI.getImageIconConfig(), possibilitiesFileDirFilelistSelection, confNode.getAttributeValue());
        }

        if (confNode.getAttributeType().compareToIgnoreCase(XMLTags.BOOLEAN) == 0) {
            input = (String) JOptionPane.showInputDialog(this, rm.getString(confNode.getElementDescription()), rm.getString(confNode.getElementDescription()), JOptionPane.PLAIN_MESSAGE, GUI.getImageIconConfig(), possibilitiesBoolean, confNode.getAttributeValue());
        }

        if (confNode.getAttributeType().compareToIgnoreCase(XMLTags.INT) == 0) {
            input = (String) JOptionPane.showInputDialog(this, rm.getString(confNode.getElementDescription()), rm.getString(confNode.getElementDescription()), JOptionPane.PLAIN_MESSAGE, GUI.getImageIconConfig(), possibilities, confNode.getAttributeValue());

            try {
                int intInput = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), e.getMessage(), JOptionPane.ERROR_MESSAGE);
                input = null;
            }
        }

        if (confNode.getAttributeType().compareToIgnoreCase(XMLTags.FILE) == 0) {
            input = controller.getFrame().getDialogFile().saveDialogAnyFile(rm.getString(confNode.getElementDescription()), false, controller.getInoutDir());
        }

        if (confNode.getAttributeType().compareToIgnoreCase(XMLTags.DIR) == 0) {
            if ((confNode.getAttributeValue() != null) && (confNode.getAttributeValue().length() > 0)) {
                input = controller.getFrame().getDialogFile().saveDialogAnyFile(rm.getString(confNode.getElementDescription()), true, new File(confNode.getAttributeValue()));
            } else {
                input = controller.getFrame().getDialogFile().saveDialogAnyFile(rm.getString(confNode.getElementDescription()), true, controller.getInoutDir());
            }
        }

        if (input != null) {
            confNode.setAttributeValue(input);

            //tree.updateUI(); creates a strange exception which I cannot catch somehow ... but it would update the nodes name
        }
    }
}
