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
package opendbcopy.gui;

import java.awt.BorderLayout;
import java.awt.SystemColor;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PanelWorkingMode extends JPanel {
    private JPanel             panelMain = new JPanel();
    private JLabel             labelOpenProject = new JLabel();
    private JScrollPane        scrollPaneWorkingModes;
    private JList              listWorkingModes;
    private FrameMain          parentFrame;
    private WorkingModeManager wmm;

    /**
     * Creates a new PanelConnection object.
     *
     * @param parentFrame DOCUMENT ME!
     * @param wmm DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public PanelWorkingMode(FrameMain          parentFrame,
                            WorkingModeManager wmm) throws Exception {
        this.parentFrame     = parentFrame;
        this.wmm             = wmm;
        guiInit();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final String getSelectedWorkingMode() {
        return (String) listWorkingModes.getSelectedValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected final void guiInit() throws Exception {
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setHgap(20);
        borderLayout.setVgap(10);
        this.setLayout(borderLayout);

        this.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " Select a Working Mode and click Next to load appropriate dialogs "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        labelOpenProject.setText("To open an existing project click on 'Project -> Import Project ...'");

        // initialise the list of available working modes
        listWorkingModes = new JList(wmm.getAvailableWorkingModes());
        listWorkingModes.setLayoutOrientation(JList.VERTICAL);
        listWorkingModes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listWorkingModes.setSelectedIndex(0);
        listWorkingModes.setToolTipText("Select a working mode and click Next");

        scrollPaneWorkingModes = new JScrollPane(listWorkingModes);

        this.add(labelOpenProject, BorderLayout.NORTH);
        this.add(scrollPaneWorkingModes, BorderLayout.CENTER);
    }
}
