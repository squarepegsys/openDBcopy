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

import opendbcopy.gui.model.PluginListModel;

import opendbcopy.plugin.PluginManager;

import opendbcopy.plugin.model.exception.PluginException;

import opendbcopy.resource.ResourceManager;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
public class PanelPluginChain extends JPanel implements FocusListener, Observer {
    private static final ImageIcon imageIconLeft = new ImageIcon("resource/images/left.gif");
    private static final ImageIcon imageIconRight = new ImageIcon("resource/images/right.gif");
    private static final ImageIcon imageIconUp = new ImageIcon("resource/images/up.gif");
    private static final ImageIcon imageIconDown = new ImageIcon("resource/images/down.gif");
    private static final ImageIcon imageIconDelete = new ImageIcon("resource/images/delete.gif");
    private static final String    SHIFT_LEFT = "shift_left";
    private static final String    SHIFT_RIGHT = "shift_right";
    private FrameMain              frameMain;
    private MainController         controller;
    private PluginGuiManager       pluginGuiManager;
    private PluginManager          pluginManager;
    private ResourceManager        rm;
    private JPanel                 panelLists;
    private JPanel                 panelLeft;
    private JPanel                 panelRight;
    private JPanel                 panelControlExecute;
    private JScrollPane            scrollPaneLeft;
    private JScrollPane            scrollPaneRight;
    private JButton                buttonShift;
    private JButton                buttonShiftUp;
    private JButton                buttonShiftDown;
    private JButton                buttonRemoveSelection;
    private JButton                buttonExecute;
    private JList                  listLoaded;
    private JList                  listToExecute;
    private PluginListModel        modelsLoaded;
    private PluginListModel        modelsToExecute;
    private boolean                executionStarted = false;

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
        this.pluginManager        = controller.getProjectManager().getPluginManager();
        this.rm                   = controller.getResourceManager();

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
        if (pluginManager.isDone() || pluginManager.isInterrupted() || pluginManager.isExceptionOccured()) {
            buttonExecute.setText(OperationType.EXECUTE);
            buttonExecute.setActionCommand(OperationType.EXECUTE);

            panelLists.updateUI();

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
            } else {
                buttonShiftUp.setEnabled(false);
                buttonShiftDown.setEnabled(false);
                buttonRemoveSelection.setEnabled(false);
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void guiInit() {
        panelLists              = new JPanel(new GridLayout(1, 2, 20, 20));
        panelLeft               = new JPanel(new BorderLayout(20, 20));
        panelRight              = new JPanel(new BorderLayout(20, 20));
        panelControlExecute     = new JPanel(new GridLayout(3, 1));

        panelLeft.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.pluginChain.modelsLoaded") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panelRight.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.pluginChain.modelsToExecute") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // set up linked list for models loaded
        modelsLoaded = new PluginListModel(controller.getProjectManager().getPluginManager().getModelsLoaded());
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
        modelsToExecute = new PluginListModel(controller.getProjectManager().getPluginManager().getModelsToExecute());
        modelsToExecute.addListDataListener(new javax.swing.event.ListDataListener() {
                public void intervalAdded(javax.swing.event.ListDataEvent e) {
                }

                public void intervalRemoved(javax.swing.event.ListDataEvent e) {
                }

                public void contentsChanged(javax.swing.event.ListDataEvent e) {
                    listToExecute.updateUI();
                    listToExecute.setSelectedIndex(controller.getProjectManager().getPluginManager().getCurrentExecuteIndex());
                }
            });

        pluginManager.registerObserver(modelsToExecute);

        listLoaded        = new JList(modelsLoaded);
        listToExecute     = new JList(modelsToExecute);

        listLoaded.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        listToExecute.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        listLoaded.addListSelectionListener(new ModelsLoadedListSelectionHandler());
        listToExecute.addListSelectionListener(new ModelsToExecuteListSelectionHandler());

        buttonShift = new JButton(imageIconRight);
        buttonShift.setActionCommand(SHIFT_RIGHT);
        buttonShift.addActionListener(new FramePluginChain_buttonShift_actionAdapter(this));

        buttonShiftUp = new JButton(imageIconUp);
        buttonShiftUp.addActionListener(new FramePluginChain_buttonShiftUp_actionAdapter(this));

        buttonShiftDown = new JButton(imageIconDown);
        buttonShiftDown.addActionListener(new FramePluginChain_buttonShiftDown_actionAdapter(this));

        buttonRemoveSelection = new JButton(imageIconDelete);
        buttonRemoveSelection.addActionListener(new FramePluginChain_buttonRemoveSelection_actionAdapter(this));

        buttonExecute = new JButton();
        buttonExecute.setText(rm.getString("button.execute"));
        buttonExecute.setActionCommand(OperationType.EXECUTE);
        buttonExecute.addActionListener(new FramePluginChain_buttonExecute_actionAdapter(this));

        panelControlExecute.add(buttonShiftUp);
        panelControlExecute.add(buttonRemoveSelection);
        panelControlExecute.add(buttonShiftDown);

        scrollPaneLeft = new JScrollPane(listLoaded);
        panelLeft.add(scrollPaneLeft, BorderLayout.CENTER);
        panelLeft.add(buttonShift, BorderLayout.EAST);

        scrollPaneRight = new JScrollPane(listToExecute);
        panelRight.add(scrollPaneRight, BorderLayout.CENTER);
        panelRight.add(panelControlExecute, BorderLayout.EAST);
        panelRight.add(buttonExecute, BorderLayout.AFTER_LAST_LINE);

        // add focus listeners
        listLoaded.addFocusListener(this);
        listToExecute.addFocusListener(this);

        panelLists.add(panelLeft);
        panelLists.add(panelRight);

        this.setLayout(new GridLayout(1, 1));
        this.add(panelLists);

        enableComponents();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void focusGained(FocusEvent e) {
        if (e.getSource() == listLoaded) {
            buttonShift.setIcon(imageIconRight);
            buttonShift.setActionCommand(SHIFT_RIGHT);
            enableComponents();
        }

        if (e.getSource() == listToExecute) {
        	if (listToExecute.getModel().getSize() > 0) {
                buttonShift.setIcon(imageIconLeft);
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
                buttonShift.setIcon(imageIconLeft);
                buttonShift.setActionCommand(SHIFT_LEFT);
                buttonShift.setEnabled(true);
            } else {
                buttonShift.setIcon(imageIconRight);
                buttonShift.setActionCommand(SHIFT_RIGHT);
                buttonShift.setEnabled(false);
            }
        }

        if (listToExecute.getModel().getSize() == 0) {
            buttonShift.setIcon(imageIconRight);
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
