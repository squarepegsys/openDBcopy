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

import info.clearthought.layout.TableLayout;

import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import opendbcopy.config.GUI;
import opendbcopy.config.OperationType;
import opendbcopy.controller.MainController;
import opendbcopy.gui.model.PluginListModel;
import opendbcopy.plugin.PluginManager;
import opendbcopy.plugin.model.exception.PluginException;
import opendbcopy.resource.ResourceManager;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PanelPluginChain extends JPanel implements ItemListener, FocusListener, Observer {
    private static final String SHIFT_LEFT = "shift_left";
    private static final String SHIFT_RIGHT = "shift_right";
    private FrameMain           frameMain;
    private MainController      controller;
    private PluginGuiManager    pluginGuiManager;
    private PluginManager       pluginManager;
    private ResourceManager     rm;
    private JPanel              panelLeft;
    private JPanel              panelRight;
    private JScrollPane         scrollPaneLeft;
    private JScrollPane         scrollPaneRight;
    private JButton             buttonShift;
    private JButton             buttonShiftUp;
    private JButton             buttonShiftDown;
    private JButton             buttonRemoveSelection;
    private JButton             buttonExecute;
    private JList               listLoaded;
    private JList               listToExecute;
    private JCheckBox           checkBoxShutdown;
    private PluginListModel     modelsLoaded;
    private PluginListModel     modelsToExecute;
    private boolean             executionStarted = false;

    /**
     * Creates a new PanelPluginChain object.
     *
     * @param frameMain DOCUMENT ME!
     * @param controller DOCUMENT ME!
     */
    public PanelPluginChain(FrameMain      frameMain,
                            MainController controller) {
        this.frameMain            = frameMain;
        this.controller           = controller;
        this.pluginGuiManager     = controller.getPluginGuiManager();
        this.pluginManager        = controller.getJobManager().getPluginManager();
        this.rm                   = controller.getResourceManager();

        guiInit();

        // register as observer on pluginManager
        controller.getJobManager().getPluginManager().registerObserver(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param arg DOCUMENT ME!
     */
    public void update(Observable o,
                       Object     arg) {
        if (pluginManager.isDone() || pluginManager.isInterrupted() || pluginManager.isExceptionOccured()) {
            buttonExecute.setText(OperationType.EXECUTE);
            buttonExecute.setActionCommand(OperationType.EXECUTE);

            this.updateUI();

            frameMain.getFrameExecutionLog().refreshFile();

            if (pluginManager.isDone()) {
                if (executionStarted) {
                    JOptionPane.showMessageDialog(this, rm.getString("text.execute.done"), "Info", JOptionPane.INFORMATION_MESSAGE);
                }

                executionStarted = false;
            } else if (pluginManager.isInterrupted()) {
                JOptionPane.showMessageDialog(this, rm.getString("text.execute.interrupted"), "Info", JOptionPane.WARNING_MESSAGE);
            }
        }

        checkBoxShutdown.setSelected(controller.getJobManager().isShutdownOnCompletion());

        enableComponents();
    }

    /**
     * DOCUMENT ME!
     */
    private void enableComponents() {
        if ((listLoaded != null) && (listToExecute != null)) {
            if (buttonShift.getActionCommand().compareTo(SHIFT_RIGHT) == 0) {
                if ((listLoaded.getModel().getSize() > 0) && (listLoaded.getSelectedIndex() >= 0)) {
                    buttonShift.setEnabled(true);
                } else {
                    buttonShift.setEnabled(false);
                }
            } else {
                if ((listToExecute.getModel().getSize() > 0) && (listToExecute.getSelectedIndex() >= 0)) {
                    buttonShift.setEnabled(true);
                } else {
                    buttonShift.setEnabled(false);
                }
            }

            if (listToExecute.getModel().getSize() > 0) {
                buttonExecute.setEnabled(true);
            } else {
                buttonExecute.setEnabled(false);
            }

            if ((listToExecute.getModel().getSize() > 0) && (listToExecute.getSelectedIndex() >= 0)) {
                buttonShiftUp.setEnabled(true);
                buttonShiftDown.setEnabled(true);
                buttonRemoveSelection.setEnabled(true);
                checkBoxShutdown.setEnabled(true);
            } else {
                buttonShiftUp.setEnabled(false);
                buttonShiftDown.setEnabled(false);
                buttonRemoveSelection.setEnabled(false);
                checkBoxShutdown.setEnabled(false);
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void guiInit() {
        double[][] sizeMain = {
                                  { 0, GUI.F, GUI.HG, GUI.P, GUI.HG, GUI.F, 0 }, // Columns
        { GUI.B, 25, 93, GUI.F, GUI.B }
        }; // Rows

        this.setLayout(new TableLayout(sizeMain));

        double[][] sizeRight = {
                                   { GUI.B, GUI.F, GUI.HG, GUI.P, GUI.B }, // Colums
        { GUI.B, 30, 1, 30, 1, 30, 3, GUI.P, 3, GUI.P, GUI.B }
        }; // Rows

        panelLeft      = new JPanel(new GridLayout(1, 1));
        panelRight     = new JPanel();
        panelRight.setLayout(new TableLayout(sizeRight));

        panelLeft.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.pluginChain.modelsLoaded") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panelRight.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.pluginChain.modelsToExecute") + " "), BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        checkBoxShutdown = new JCheckBox(" " + rm.getString("text.pluginChain.shutdownAfterDone"));
        checkBoxShutdown.addItemListener(this);
        checkBoxShutdown.setSelected(controller.getJobManager().isShutdownOnCompletion());

        // set up linked list for models loaded
        modelsLoaded = new PluginListModel(controller.getJobManager().getPluginManager().getModelsLoaded());
        modelsLoaded.addListDataListener(new javax.swing.event.ListDataListener() {
                public void intervalAdded(javax.swing.event.ListDataEvent e) {
                }

                public void intervalRemoved(javax.swing.event.ListDataEvent e) {
                }

                public void contentsChanged(javax.swing.event.ListDataEvent e) {
                    listLoaded.updateUI();
                }
            });

        pluginManager.registerObserver(modelsLoaded);

        // set up linked list for models to execute
        modelsToExecute = new PluginListModel(controller.getJobManager().getPluginManager().getModelsToExecute());
        modelsToExecute.addListDataListener(new javax.swing.event.ListDataListener() {
                public void intervalAdded(javax.swing.event.ListDataEvent e) {
                }

                public void intervalRemoved(javax.swing.event.ListDataEvent e) {
                }

                public void contentsChanged(javax.swing.event.ListDataEvent e) {
                    listToExecute.updateUI();
                    listToExecute.setSelectedIndex(controller.getJobManager().getPluginManager().getCurrentExecuteIndex());
                }
            });

        pluginManager.registerObserver(modelsToExecute);

        listLoaded        = new JList(modelsLoaded);
        listToExecute     = new JList(modelsToExecute);

        listLoaded.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        listToExecute.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        listLoaded.addListSelectionListener(new ModelsLoadedListSelectionHandler());
        listToExecute.addListSelectionListener(new ModelsToExecuteListSelectionHandler());

        buttonShift = new JButton(GUI.getImageIconRight());
        buttonShift.setActionCommand(SHIFT_RIGHT);
        buttonShift.addActionListener(new FramePluginChain_buttonShift_actionAdapter(this));

        buttonShiftUp = new JButton(GUI.getImageIconUp());
        buttonShiftUp.addActionListener(new FramePluginChain_buttonShiftUp_actionAdapter(this));

        buttonShiftDown = new JButton(GUI.getImageIconDown());
        buttonShiftDown.addActionListener(new FramePluginChain_buttonShiftDown_actionAdapter(this));

        buttonRemoveSelection = new JButton(GUI.getImageIconDelete());
        buttonRemoveSelection.addActionListener(new FramePluginChain_buttonRemoveSelection_actionAdapter(this));

        buttonExecute = new JButton();
        buttonExecute.setText(rm.getString("button.executeJob"));
        buttonExecute.setActionCommand(OperationType.EXECUTE);
        buttonExecute.addActionListener(new FramePluginChain_buttonExecute_actionAdapter(this));

        scrollPaneLeft = new JScrollPane(listLoaded);
        panelLeft.add(scrollPaneLeft, null);

        scrollPaneRight = new JScrollPane(listToExecute);

        panelRight.add(scrollPaneRight, "1, 1, 1, 5");
        panelRight.add(buttonShiftUp, "3, 1");
        panelRight.add(buttonRemoveSelection, "3, 3");
        panelRight.add(buttonShiftDown, "3, 5");
        panelRight.add(checkBoxShutdown, "1, 7");
        panelRight.add(buttonExecute, "1, 9");

        // add focus listeners
        listLoaded.addFocusListener(this);
        listToExecute.addFocusListener(this);

        // first line
        this.add(panelLeft, "1, 1, 1, 3");
        this.add(buttonShift, "3, 2");
        this.add(panelRight, "5, 1, 5, 3");

        enableComponents();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();

        if (source == checkBoxShutdown) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                controller.getJobManager().setShutdownOnCompletion(false);
            } else {
                controller.getJobManager().setShutdownOnCompletion(true);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void focusGained(FocusEvent e) {
        if (e.getSource() == listLoaded) {
            buttonShift.setIcon(GUI.getImageIconRight());
            buttonShift.setActionCommand(SHIFT_RIGHT);
            enableComponents();
        }

        if (e.getSource() == listToExecute) {
            if (listToExecute.getModel().getSize() > 0) {
                buttonShift.setIcon(GUI.getImageIconLeft());
                buttonShift.setActionCommand(SHIFT_LEFT);
            }

            enableComponents();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void focusLost(FocusEvent e) {
        // not used
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonShift_actionPerformed(ActionEvent e) {
        if (buttonShift.getActionCommand().compareTo(SHIFT_RIGHT) == 0) {
            if (listLoaded.getSelectedIndex() >= 0) {
                pluginGuiManager.addPluginGuiToExecuteLast(listLoaded.getSelectedIndex());
                pluginManager.addPluginToExecuteLast(pluginManager.getModelLoaded(listLoaded.getSelectedIndex()));
            } else {
                buttonShift.setEnabled(false);
            }
        } else {
            if (listToExecute.getSelectedIndex() >= 0) {
                pluginGuiManager.addPluginGuiLoadedLast(listToExecute.getSelectedIndex());
                pluginManager.addPluginLoadedLast(pluginManager.getModelToExecute(listToExecute.getSelectedIndex()));
            } else {
                buttonShift.setEnabled(false);
            }
        }

        if (listLoaded.getModel().getSize() == 0) {
            if (listToExecute.getModel().getSize() > 0) {
                buttonShift.setIcon(GUI.getImageIconLeft());
                buttonShift.setActionCommand(SHIFT_LEFT);
                buttonShift.setEnabled(true);
            } else {
                buttonShift.setIcon(GUI.getImageIconRight());
                buttonShift.setActionCommand(SHIFT_RIGHT);
                buttonShift.setEnabled(false);
            }
        }

        if (listToExecute.getModel().getSize() == 0) {
            buttonShift.setIcon(GUI.getImageIconRight());
            buttonShift.setActionCommand(SHIFT_RIGHT);

            if (listLoaded.getModel().getSize() > 0) {
                buttonShift.setEnabled(true);
            } else {
                buttonShift.setEnabled(false);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonShiftUp_actionPerformed(ActionEvent e) {
        if (modelsToExecute.getSize() > 1) {
            if (listToExecute.getSelectedIndex() > 0) {
                int index = listToExecute.getSelectedIndex();
                pluginGuiManager.changeOrderPluginToExecute(index, index - 1);
                pluginManager.changeOrderPluginToExecute(index, index - 1);
                listToExecute.setSelectedIndex(index - 1);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonShiftDown_actionPerformed(ActionEvent e) {
        if (modelsToExecute.getSize() > 1) {
            if (listToExecute.getSelectedIndex() < (modelsToExecute.getSize() - 1)) {
                int index = listToExecute.getSelectedIndex();
                pluginGuiManager.changeOrderPluginToExecute(index, index + 1);
                pluginManager.changeOrderPluginToExecute(index, index + 1);
                listToExecute.setSelectedIndex(index + 1);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonRemoveSelection_actionPerformed(ActionEvent e) {
        if ((listToExecute.getModel().getSize() > 0) && (listToExecute.getSelectedIndex() >= 0)) {
            int removeIndex = listToExecute.getSelectedIndex();
            pluginGuiManager.removePluginGuiToExecute(removeIndex);
            pluginManager.removePluginToExecute(removeIndex);
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

                executionStarted = true;

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
    class ModelsLoadedListSelectionHandler implements ListSelectionListener {
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void valueChanged(ListSelectionEvent e) {
            enableComponents();

            if ((listLoaded.getModel().getSize() > 0) && (listLoaded.getSelectedIndex() >= 0)) {
                if (pluginGuiManager.getPluginGuiLoaded(listLoaded.getSelectedIndex()) != null) {
                    pluginGuiManager.setCurrentPluginGui((PluginGui) pluginGuiManager.getPluginGuiLoaded(listLoaded.getSelectedIndex()));
                }
            }
        }
    }

    /**
     * class description
     *
     * @author Anthony Smith
     * @version $Revision$
     */
    class ModelsToExecuteListSelectionHandler implements ListSelectionListener {
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void valueChanged(ListSelectionEvent e) {
            enableComponents();

            if ((listToExecute.getModel().getSize() > 0) && (listToExecute.getSelectedIndex() >= 0)) {
                if (pluginGuiManager.getPluginGuiToExecute(listToExecute.getSelectedIndex()) != null) {
                    pluginGuiManager.setCurrentPluginGui((PluginGui) pluginGuiManager.getPluginGuiToExecute(listToExecute.getSelectedIndex()));
                }
            }
        }
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class FramePluginChain_buttonShift_actionAdapter implements java.awt.event.ActionListener {
    PanelPluginChain adaptee;

    /**
     * Creates a new FramePluginChain_buttonNext_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    FramePluginChain_buttonShift_actionAdapter(PanelPluginChain adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonShift_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class FramePluginChain_buttonShiftUp_actionAdapter implements java.awt.event.ActionListener {
    PanelPluginChain adaptee;

    /**
     * Creates a new FramePluginChain_buttonNext_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    FramePluginChain_buttonShiftUp_actionAdapter(PanelPluginChain adaptee) {
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
    PanelPluginChain adaptee;

    /**
     * Creates a new FramePluginChain_buttonNext_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    FramePluginChain_buttonShiftDown_actionAdapter(PanelPluginChain adaptee) {
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
    PanelPluginChain adaptee;

    /**
     * Creates a new FramePluginChain_buttonNext_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    FramePluginChain_buttonRemoveSelection_actionAdapter(PanelPluginChain adaptee) {
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
    PanelPluginChain adaptee;

    /**
     * Creates a new PanelExecute_buttonControl_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    FramePluginChain_buttonExecute_actionAdapter(PanelPluginChain adaptee) {
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
