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

import opendbcopy.config.OperationType;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.PluginManager;

import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.exception.PluginException;

import opendbcopy.resource.ResourceManager;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class FramePluginChain extends JFrame implements Observer {
    private FrameMain       frameMain;
    private MainController  controller;
    private PluginManager   pluginManager;
    private ResourceManager rm;
    private JTabbedPane     tabModelsLoaded;
    private JTabbedPane     tabModelsToExecute;
    private JPanel          panelMain;
    private JPanel          panelInfo;
    private JPanel          panelLists;
    private JPanel          panelLeft;
    private JPanel          panelRight;
    private JPanel          panelControlExecute;
    private JButton         buttonShiftRight;
    private JButton         buttonShiftUp;
    private JButton         buttonShiftDown;
    private JButton         buttonRemoveSelection;
    private JButton         buttonExecute;
    private JList           listLoaded;
    private JList           listToExecute;
    private LinkedListModel modelLoaded;
    private LinkedListModel modelToExecute;
    private JLabel          labelInfo;

    /**
     * Creates a new FramePluginChain object.
     *
     * @param frameMain DOCUMENT ME!
     * @param controller DOCUMENT ME!
     */
    public FramePluginChain(FrameMain      frameMain,
                            MainController controller) {
        this.frameMain              = frameMain;
        this.controller             = controller;
        this.pluginManager          = controller.getProjectManager().getPluginManager();
        this.rm                     = controller.getResourceManager();
        this.tabModelsLoaded        = controller.getWorkingModeManager().getTabModelsLoaded();
        this.tabModelsToExecute     = controller.getWorkingModeManager().getTabModelsToExecute();

        guiInit();

        // register as observer on pluginManager
        controller.getProjectManager().getPluginManager().registerObserver(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param arg DOCUMENT ME!
     */
    public void update(Observable o,
                       Object     arg) {
        if ((listLoaded != null) && (listToExecute != null)) {
            listLoaded.updateUI();
            listToExecute.updateUI();

            enableButtons();

            panelMain.updateUI();
        }
    }

    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            this.hide();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void enableButtons() {
        if ((listLoaded != null) && (listToExecute != null)) {
            if (((String) listLoaded.getModel().getElementAt(0)).compareTo(rm.getString("text.pluginChain.NoSelection")) == 0) {
                // nothing selected
                buttonShiftRight.setEnabled(false);
            } else {
                if (listLoaded.getSelectedIndex() >= 0) {
                    buttonShiftRight.setEnabled(true);
                } else {
                    buttonShiftRight.setEnabled(false);
                }
            }

            if (((String) listToExecute.getModel().getElementAt(0)).compareTo(rm.getString("text.pluginChain.NoSelection")) == 0) {
                // nothing selected
                buttonShiftUp.setEnabled(false);
                buttonShiftDown.setEnabled(false);
                buttonRemoveSelection.setEnabled(false);
                buttonExecute.setEnabled(false);
            } else {
                if (listToExecute.getSelectedIndex() >= 0) {
                    buttonShiftUp.setEnabled(true);
                    buttonShiftDown.setEnabled(true);
                    buttonRemoveSelection.setEnabled(true);
                } else {
                    buttonShiftUp.setEnabled(false);
                    buttonShiftDown.setEnabled(false);
                    buttonRemoveSelection.setEnabled(false);
                }

                buttonExecute.setEnabled(true);
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void guiInit() {
        this.setTitle(rm.getString("text.pluginChain.title"));
        this.setSize(650, 300);

        panelMain               = new JPanel(new BorderLayout(20, 20));
        panelInfo               = new JPanel(new GridLayout(1, 1));
        panelLists              = new JPanel(new GridLayout(1, 2, 20, 20));
        panelLeft               = new JPanel(new BorderLayout(20, 20));
        panelRight              = new JPanel(new BorderLayout(20, 20));
        panelControlExecute     = new JPanel(new GridLayout(3, 1));

        panelInfo.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.help.quick") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        panelLeft.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.pluginChain.modelsLoaded") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panelRight.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.pluginChain.modelsToExecute") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // set up linked list
        modelLoaded        = new LinkedListModel(controller.getProjectManager().getPluginManager().getModelsLoaded(), tabModelsLoaded);
        modelToExecute     = new LinkedListModel(controller.getProjectManager().getPluginManager().getModelsToExecute(), tabModelsLoaded);

        listLoaded        = new JList(modelLoaded);
        listToExecute     = new JList(modelToExecute);

        listLoaded.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        listToExecute.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        listLoaded.addListSelectionListener(new SharedListSelectionHandler());
        listToExecute.addListSelectionListener(new SharedListSelectionHandler());

        //buttonShiftRight = new JButton(rm.getString("button.shiftRight"));
        buttonShiftRight = new JButton(new ImageIcon("resource/images/right.gif"));
        buttonShiftRight.addActionListener(new FramePluginChain_buttonShiftRight_actionAdapter(this));

        buttonShiftUp = new JButton(new ImageIcon("resource/images/up.gif"));
        buttonShiftUp.addActionListener(new FramePluginChain_buttonShiftUp_actionAdapter(this));

        buttonShiftDown = new JButton(new ImageIcon("resource/images/down.gif"));
        buttonShiftDown.addActionListener(new FramePluginChain_buttonShiftDown_actionAdapter(this));

        buttonRemoveSelection = new JButton(new ImageIcon("resource/images/delete.gif"));
        buttonRemoveSelection.addActionListener(new FramePluginChain_buttonRemoveSelection_actionAdapter(this));

        buttonExecute = new JButton();
        buttonExecute.setText(rm.getString("button.execute"));
        buttonExecute.setActionCommand(OperationType.EXECUTE);
        buttonExecute.addActionListener(new FramePluginChain_buttonExecute_actionAdapter(this));

        panelControlExecute.add(buttonShiftUp);
        panelControlExecute.add(buttonRemoveSelection);
        panelControlExecute.add(buttonShiftDown);

        labelInfo = new JLabel(rm.getString("text.pluginChain.info"));
        panelInfo.add(labelInfo);

        panelLeft.add(new JScrollPane(listLoaded), BorderLayout.CENTER);
        panelLeft.add(buttonShiftRight, BorderLayout.EAST);

        panelRight.add(new JScrollPane(listToExecute), BorderLayout.CENTER);
        panelRight.add(panelControlExecute, BorderLayout.EAST);
        panelRight.add(buttonExecute, BorderLayout.AFTER_LAST_LINE);

        panelLists.add(panelLeft);
        panelLists.add(panelRight);

        panelMain.add(panelInfo, BorderLayout.NORTH);
        panelMain.add(panelLists, BorderLayout.CENTER);

        this.getContentPane().add(panelMain);

        enableButtons();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonShiftRight_actionPerformed(ActionEvent e) {
        pluginManager.addPluginToExecuteLast(pluginManager.getModelLoaded(listLoaded.getSelectedIndex()));
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonShiftUp_actionPerformed(ActionEvent e) {
        if (modelToExecute.getSize() > 1) {
            if (listToExecute.getSelectedIndex() > 0) {
                pluginManager.changeOrderPluginToExecute(pluginManager.getModelLoaded(listToExecute.getSelectedIndex()), listToExecute.getSelectedIndex() - 1);
                listToExecute.setSelectedIndex(listToExecute.getSelectedIndex() - 1);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonShiftDown_actionPerformed(ActionEvent e) {
        if (modelToExecute.getSize() > 1) {
            if (listToExecute.getSelectedIndex() < (modelToExecute.getSize() - 1)) {
                pluginManager.changeOrderPluginToExecute(pluginManager.getModelLoaded(listToExecute.getSelectedIndex()), listToExecute.getSelectedIndex() + 1);
                listToExecute.setSelectedIndex(listToExecute.getSelectedIndex() + 1);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonRemoveSelection_actionPerformed(ActionEvent e) {
        if (listToExecute.getSelectedIndex() >= 0) {
            pluginManager.removePluginToExecute(listToExecute.getSelectedIndex());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonExecute_actionPerformed(ActionEvent e) {
        // Execute
        if (buttonExecute.getActionCommand().compareTo(OperationType.EXECUTE) == 0) {
            try {
                buttonExecute.setText(OperationType.CANCEL);
                buttonExecute.setActionCommand(OperationType.CANCEL);

                pluginManager.executePlugins();

                if (frameMain.getFrameExecutionLog() != null) {
                    frameMain.getFrameExecutionLog().show();
                }
            } catch (PluginException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Oooooops!", JOptionPane.ERROR_MESSAGE);
            }
        }
        // Cancel
        else {
            buttonExecute.setText(OperationType.EXECUTE);
            buttonExecute.setActionCommand(OperationType.EXECUTE);
            pluginManager.interruptPlugins();
        }
    }

    /**
     * class description
     *
     * @author Anthony Smith
     * @version $Revision$
     */
    class LinkedListModel extends AbstractListModel implements Observer {
        private JTabbedPane tab;
        private LinkedList  list;

        /**
         * Creates a new LinkedListModel object.
         *
         * @param list DOCUMENT ME!
         * @param tab DOCUMENT ME!
         */
        public LinkedListModel(LinkedList  list,
                               JTabbedPane tab) {
            this.list     = list;
            this.tab      = tab;
        }

        /**
         * DOCUMENT ME!
         *
         * @param o DOCUMENT ME!
         * @param arg DOCUMENT ME!
         */
        public void update(Observable o,
                           Object     arg) {
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public int getSize() {
            if (list.size() == 0) {
                return 1;
            } else {
                return list.size();
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param index DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Object getElementAt(int index) {
            if (list.size() == 0) {
                return rm.getString("text.pluginChain.NoSelection");
            } else {
                return ((Model) list.get(index)).getTitle();
            }
        }
    }

    /**
     * class description
     *
     * @author Anthony Smith
     * @version $Revision$
     */
    class SharedListSelectionHandler implements ListSelectionListener {
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void valueChanged(ListSelectionEvent e) {
            enableButtons();
        }
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class FramePluginChain_buttonShiftRight_actionAdapter implements java.awt.event.ActionListener {
    FramePluginChain adaptee;

    /**
     * Creates a new FramePluginChain_buttonNext_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    FramePluginChain_buttonShiftRight_actionAdapter(FramePluginChain adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonShiftRight_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class FramePluginChain_buttonShiftUp_actionAdapter implements java.awt.event.ActionListener {
    FramePluginChain adaptee;

    /**
     * Creates a new FramePluginChain_buttonNext_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    FramePluginChain_buttonShiftUp_actionAdapter(FramePluginChain adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonShiftUp_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class FramePluginChain_buttonShiftDown_actionAdapter implements java.awt.event.ActionListener {
    FramePluginChain adaptee;

    /**
     * Creates a new FramePluginChain_buttonNext_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    FramePluginChain_buttonShiftDown_actionAdapter(FramePluginChain adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonShiftDown_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class FramePluginChain_buttonRemoveSelection_actionAdapter implements java.awt.event.ActionListener {
    FramePluginChain adaptee;

    /**
     * Creates a new FramePluginChain_buttonNext_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    FramePluginChain_buttonRemoveSelection_actionAdapter(FramePluginChain adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonRemoveSelection_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class FramePluginChain_buttonExecute_actionAdapter implements java.awt.event.ActionListener {
    FramePluginChain adaptee;

    /**
     * Creates a new PanelExecute_buttonControl_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    FramePluginChain_buttonExecute_actionAdapter(FramePluginChain adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonExecute_actionPerformed(e);
    }
}
