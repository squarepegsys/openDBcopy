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
 * $Log$
 * --------------------------------------------------------------------------*/
package opendbcopy.gui;

import opendbcopy.config.OperationType;
import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.io.Reader;

import opendbcopy.model.ProjectManager;

import opendbcopy.task.TaskExecute;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.jdom.Element;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PanelExecute extends JPanel implements Observer {
    private final static int TIMER_SLOT = 50;
    private static Logger    logger = Logger.getLogger(PanelExecute.class.getName());
    private FrameMain        parentFrame;
    private MainController   controller;
    private ProjectManager   pm;
    private TaskExecute      taskExecute;
    private StringBuffer     logBuffer;
    private Timer            timer;
    private Hashtable        pluginsHashtable; // contains the plugin elements, using selected index has identifier
    private BorderLayout     borderLayout = new BorderLayout();
    private JPanel           panelPlugin = new JPanel();
    private JPanel           panelMain = new JPanel();
    private JPanel           panelControl = new JPanel();
    private JPanel           panelProgress = new JPanel();
    private JComboBox        comboBoxPlugin = new JComboBox();
    private BorderLayout     borderLayoutMain = new BorderLayout();
    private JTextPane        textPaneLog = new JTextPane();
    private JScrollPane      scrollPane = null;
    private JProgressBar     progressBarTable = null;
    private JProgressBar     progressBarRecord = null;
    private JButton          buttonControl = null;
    private String           currentTable = null;

    /**
     * Creates a new PanelExecute object.
     *
     * @param parentFrame DOCUMENT ME!
     * @param controller DOCUMENT ME!
     * @param projectManager DOCUMENT ME!
     */
    public PanelExecute(FrameMain      parentFrame,
                        MainController controller,
                        ProjectManager projectManager) {
        this.parentFrame     = parentFrame;
        this.controller      = controller;
        this.pm              = projectManager;
        this.logBuffer       = new StringBuffer();

        try {
            retrievePlugins();
            guiInit();
        } catch (Exception e) {
            logger.error(e.toString());
            this.parentFrame.setStatusBar(e.toString(), Level.ERROR);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param obj DOCUMENT ME!
     */
    public final void update(Observable o,
                             Object     obj) {
        // only if this tab is visible -> update it
        if (parentFrame.getSelectedTabIndex() == 5) {
            if (taskExecute.getTaskStatus() == taskExecute.started) {
                // update status bars
                progressBarTable.setMaximum(taskExecute.getLengthOfTaskTable());
                progressBarRecord.setMaximum(taskExecute.getLengthOfTaskRecord());
                progressBarTable.setValue(taskExecute.getCurrentTable());
                progressBarTable.setString(taskExecute.getMessage());

                progressBarRecord.setValue(taskExecute.getCurrentRecord());
                progressBarRecord.setString(Integer.toString(taskExecute.getCurrentRecord()));
            } else if (taskExecute.getTaskStatus() == taskExecute.done) {
                progressBarTable.setValue(taskExecute.getCurrentTable());
                progressBarTable.setString(taskExecute.getCurrentTable() + " tables processed");

                buttonControl.setText(OperationType.EXECUTE);
                buttonControl.setActionCommand(OperationType.EXECUTE);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final void loadSelectedPlugin() throws Exception {
        retrievePlugins();

        Element selectedPlugin = pm.getProjectModel().getPlugin();

        if (selectedPlugin != null) {
            if (selectedPlugin.getChild(XMLTags.DESCRIPTION).getAttributeValue(XMLTags.VALUE) != null) {
                comboBoxPlugin.setSelectedItem(selectedPlugin.getChild(XMLTags.DESCRIPTION).getAttributeValue(XMLTags.VALUE));
            }
        }
    }

    // set by ProjectModel when taskExecute is initalized
    public final void setTaskExecute(TaskExecute taskExecute) {
        this.taskExecute     = taskExecute;

        // initialize timer for updating execution log
        timer = new Timer(TIMER_SLOT, new TimerUpdateListener());

        timer.start();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void guiInit() throws Exception {
        borderLayout.setHgap(10);
        borderLayout.setVgap(10);
        this.setLayout(borderLayout);
        panelPlugin.setLayout(new GridLayout(1, 1));
        panelPlugin.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " Choose a plugin to execute "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        panelMain.setLayout(borderLayoutMain);

        panelProgress.setLayout(new GridLayout(2, 1, 10, 10));
        panelControl.setLayout(new BorderLayout(20, 20));

        panelControl.setPreferredSize(new Dimension(392, 50));

        this.progressBarTable = new JProgressBar(0, 0);
        this.progressBarTable.setMinimum(0);
        this.progressBarTable.setValue(0);
        this.progressBarTable.setStringPainted(true);

        this.progressBarRecord = new JProgressBar(0, 0);
        this.progressBarRecord.setMinimum(0);
        this.progressBarRecord.setValue(0);
        this.progressBarRecord.setStringPainted(true);

        buttonControl = new JButton();
        buttonControl.setPreferredSize(new Dimension(120, 40));
        buttonControl.setText(OperationType.EXECUTE);
        buttonControl.setActionCommand(OperationType.EXECUTE);

        buttonControl.addActionListener(new PanelExecute_buttonControl_actionAdapter(this));

        progressBarTable.setBounds(new Rectangle(11, 0, 466, 16));
        progressBarRecord.setBounds(new Rectangle(11, 22, 466, 16));

        panelPlugin.add(comboBoxPlugin);

        panelProgress.add(progressBarTable);
        panelProgress.add(progressBarRecord);
        panelControl.add(panelProgress, BorderLayout.CENTER);
        panelControl.add(buttonControl, BorderLayout.EAST);

        scrollPane = new JScrollPane(textPaneLog);
        scrollPane.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.black, 1), " Execution Log "));

        panelMain.add(scrollPane, BorderLayout.CENTER);

        this.add(panelPlugin, BorderLayout.NORTH);
        this.add(panelMain, BorderLayout.CENTER);
        this.add(panelControl, BorderLayout.SOUTH);

        textPaneLog.setText("Shows execution_log_file which is stored in the sub directory 'log'");
    }

    /**
     * DOCUMENT ME!
     */
    private void retrievePlugins() {
        try {
            Iterator itPlugins = pm.getPlugins().getRootElement().getChildren(XMLTags.PLUGIN).iterator();

            pluginsHashtable = new Hashtable();

            int index = 0;
            comboBoxPlugin.removeAllItems();

            while (itPlugins.hasNext()) {
                Element plugin = (Element) itPlugins.next();

                // in single mode only show plugins that work in single or both modes
                if (pm.getProjectModel().getDbMode() == pm.getProjectModel().SINGLE_MODE) {
                    if ((plugin.getAttributeValue(XMLTags.DB_MODE).compareTo(XMLTags.SINGLE_DB) == 0) || (plugin.getAttributeValue(XMLTags.DB_MODE).compareTo(XMLTags.BOTH_DB) == 0)) {
                        pluginsHashtable.put(Integer.toString(index), plugin);
                        comboBoxPlugin.addItem(plugin.getChild(XMLTags.DESCRIPTION).getAttributeValue(XMLTags.VALUE));
                        index++;
                    }
                }
                // in single mode only show plugins that work in dual or both modes
                else {
                    if ((plugin.getAttributeValue(XMLTags.DB_MODE).compareTo(XMLTags.DUAL_DB) == 0) || (plugin.getAttributeValue(XMLTags.DB_MODE).compareTo(XMLTags.BOTH_DB) == 0)) {
                        pluginsHashtable.put(Integer.toString(index), plugin);
                        comboBoxPlugin.addItem(plugin.getChild(XMLTags.DESCRIPTION).getAttributeValue(XMLTags.VALUE));
                        index++;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
            parentFrame.setStatusBar(e.toString(), Level.ERROR);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonControl_actionPerformed(ActionEvent e) {
        // Execute
        if (buttonControl.getActionCommand().compareTo(OperationType.EXECUTE) == 0) {
            try {
                Element operation = new Element(XMLTags.OPERATION);
                operation.setAttribute(XMLTags.NAME, OperationType.EXECUTE);

                Element plugin = (Element) pluginsHashtable.get(Integer.toString(comboBoxPlugin.getSelectedIndex()));
                Element usePlugin = (Element) plugin.clone();
                operation.addContent(usePlugin.detach());

                buttonControl.setText(OperationType.CANCEL);
                buttonControl.setActionCommand(OperationType.CANCEL);

                this.controller.execute(operation);

                this.parentFrame.setStatusBar("", Level.INFO);
            } catch (Exception ex) {
                logger.error(ex.toString());
                this.parentFrame.setStatusBar(ex.toString(), Level.ERROR);
            }
        }
        // Cancel
        else {
            try {
                Element operation = new Element(XMLTags.OPERATION);
                operation.setAttribute(XMLTags.NAME, OperationType.CANCEL);

                buttonControl.setText(OperationType.EXECUTE);
                buttonControl.setActionCommand(OperationType.EXECUTE);

                this.controller.execute(operation);
            } catch (Exception ex) {
                logger.error(ex.toString());
                this.parentFrame.setStatusBar(ex.toString(), Level.ERROR);
            }
        }
    }

    /**
     * class description
     *
     * @author Anthony Smith
     * @version $Revision$
     */
    class TimerUpdateListener implements ActionListener {
        /**
         * DOCUMENT ME!
         *
         * @param evt DOCUMENT ME!
         */
        public void actionPerformed(ActionEvent evt) {
            if (taskExecute.getTaskStatus() == taskExecute.started) {
                // read log file and show in log pane
                try {
                    textPaneLog.setText(Reader.read("log/opendbcopy_execution_log_file.log").toString());
                } catch (Exception e) {
                    // do nothing as it is possible that user deleted log or whatever ...
                }
            } else if ((taskExecute.getTaskStatus() == taskExecute.interrupted) || (taskExecute.getTaskStatus() == taskExecute.done)) {
                // read log file a last time
                try {
                    textPaneLog.setText(Reader.read("log/opendbcopy_execution_log_file.log").toString());
                } catch (Exception e) {
                    // do nothing as it is possible that user deleted log or whatever ...
                }

                timer.stop();
            } else {
                timer.stop(); // just in case this state is currently active
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
class PanelExecute_buttonControl_actionAdapter implements java.awt.event.ActionListener {
    PanelExecute adaptee;

    /**
     * Creates a new PanelExecute_buttonControl_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelExecute_buttonControl_actionAdapter(PanelExecute adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonControl_actionPerformed(e);
    }
}
