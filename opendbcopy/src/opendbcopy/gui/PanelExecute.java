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
import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.log4j.gui.TextAreaAppender;

import opendbcopy.plugin.PluginManager;

import opendbcopy.plugin.model.Model;

import org.apache.log4j.Category;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;

import org.jdom.Element;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PanelExecute extends DynamicPanel {
    private final static int TIMER_SLOT = 100;
    private PluginManager    pluginManager;
    private Model            currentModel;
    private TextAreaAppender taa;
    private Timer            timer;
    private HashMap          availablePluginThreads;
    private BorderLayout     borderLayout = new BorderLayout();
    private JPanel           panelPlugin = new JPanel();
    private JPanel           panelMain = new JPanel();
    private JPanel           panelControl = new JPanel();
    private JPanel           panelProgress = new JPanel();
    private JComboBox        comboBoxPlugin = new JComboBox();
    private BorderLayout     borderLayoutMain = new BorderLayout();
    private JTextArea        textAreaLog = null;
    private JScrollPane      scrollPane = null;
    private JProgressBar     progressBarTable = null;
    private JProgressBar     progressBarRecord = null;
    private JButton          buttonControl = null;

    /**
     * Creates a new PanelExecute object.
     *
     * @param controller DOCUMENT ME!
     * @param workingMode DOCUMENT ME!
     * @param registerAsObserver DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public PanelExecute(MainController controller,
                        WorkingMode    workingMode,
                        Boolean        registerAsObserver) throws Exception {
        super(controller, workingMode, registerAsObserver);

        pluginManager     = pm.getPluginManager();

        currentModel = super.model;

        guiInit();
        retrievePlugins();

        // register this panel as observer of PluginManager
        pluginManager.registerObserver(this);
    }

    /**
     * Listens for changes on plugins executed
     *
     * @param o DOCUMENT ME!
     * @param obj DOCUMENT ME!
     */
    public final void update(Observable o,
                             Object     obj) {
        if (pluginManager.isDone() || pluginManager.isInterrupted() || pluginManager.isExceptionOccured()) {
            timer.stop();

            // retrieve latest progress information
            if (progressBarTable.getMaximum() < currentModel.getLengthProgressTable()) {
                progressBarTable.setMaximum(currentModel.getLengthProgressTable());
            }

            if (progressBarRecord.getMaximum() < currentModel.getLengthProgressRecord()) {
                progressBarRecord.setMaximum(currentModel.getLengthProgressRecord());
            }

            progressBarTable.setValue(currentModel.getCurrentProgressTable());
            progressBarRecord.setValue(currentModel.getCurrentProgressRecord());

            if ((currentModel.getProgressMessage() != null) && (currentModel.getProgressMessage().length() > 0)) {
                progressBarTable.setString(currentModel.getProgressMessage());
            }

            if (pluginManager.isDone()) {
                postMessage(rm.getString("text.execute.done"));
                buttonControl.setText(OperationType.EXECUTE);
                buttonControl.setActionCommand(OperationType.EXECUTE);

                // disable text area appender
                taa.setEnabled(false);
            } else if (pluginManager.isInterrupted()) {
                postMessage(rm.getString("text.execute.interrupted"));
            }
        }
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

        // setup textAreaAppender and append it
        taa = new TextAreaAppender(new PatternLayout("%5p %d %m%n"), "panelLogText");
        taa.setThreshold(Priority.INFO);

        Category cat = Category.getInstance("opendbcopy.plugin");
        cat.addAppender(taa);

        textAreaLog = taa.getTextArea();
        textAreaLog.setText(rm.getString("text.execute.log.toolTip") + System.getProperty("line.separator"));

        panelPlugin.setLayout(new GridLayout(1, 1));
        panelPlugin.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.execute.select") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        panelMain.setLayout(borderLayoutMain);

        panelProgress.setLayout(new GridLayout(2, 1, 10, 10));
        panelControl.setLayout(new BorderLayout(20, 20));

        panelControl.setPreferredSize(new Dimension(392, 50));

        progressBarTable = new JProgressBar(0, 0);
        progressBarTable.setMinimum(0);
        progressBarTable.setValue(0);
        progressBarTable.setStringPainted(true);

        progressBarRecord = new JProgressBar(0, 0);
        progressBarRecord.setMinimum(0);
        progressBarRecord.setValue(0);
        progressBarRecord.setStringPainted(true);

        buttonControl = new JButton();
        buttonControl.setPreferredSize(new Dimension(120, 40));
        buttonControl.setText(rm.getString("button.execute"));
        buttonControl.setActionCommand(OperationType.EXECUTE);

        buttonControl.addActionListener(new PanelExecute_buttonControl_actionAdapter(this));

        progressBarTable.setBounds(new Rectangle(11, 0, 466, 16));
        progressBarRecord.setBounds(new Rectangle(11, 22, 466, 16));

        panelPlugin.add(comboBoxPlugin);

        panelProgress.add(progressBarTable);
        panelProgress.add(progressBarRecord);
        panelControl.add(panelProgress, BorderLayout.CENTER);
        panelControl.add(buttonControl, BorderLayout.EAST);

        scrollPane = new JScrollPane(textAreaLog);
        scrollPane.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.black, 1), " " + rm.getString("text.execute.log") + " "));

        panelMain.add(scrollPane, BorderLayout.CENTER);

        this.add(panelPlugin, BorderLayout.NORTH);
        this.add(panelMain, BorderLayout.CENTER);
        this.add(panelControl, BorderLayout.SOUTH);
    }

    /**
     * DOCUMENT ME!
     */
    private void retrievePlugins() {
        availablePluginThreads = workingMode.getAvailablePluginThreads();

        Iterator itAvailablePlugins = availablePluginThreads.values().iterator();

        while (itAvailablePlugins.hasNext()) {
            String pluginDescription = (String) itAvailablePlugins.next();
            comboBoxPlugin.addItem(pluginDescription);
        }
    }

    /**
     * Retrieve the plugin thread class name given the description I know, a little bit complicate, maybe you find a nicer implementation
     *
     * @return DOCUMENT ME!
     */
    private String getSelectedPluginThread() {
        String   pluginThreadDescription = (String) comboBoxPlugin.getSelectedItem();
        String   pluginThreadClass = null;

        Iterator itPluginKeys = availablePluginThreads.keySet().iterator();

        while (itPluginKeys.hasNext()) {
            pluginThreadClass = (String) itPluginKeys.next();

            if (((String) availablePluginThreads.get(pluginThreadClass)).compareTo(pluginThreadDescription) == 0) {
                return pluginThreadClass;
            }
        }

        return null;
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

                buttonControl.setText(OperationType.CANCEL);
                buttonControl.setActionCommand(OperationType.CANCEL);

                // initialize timer for updating execution log
                if (timer == null) {
                    timer = new Timer(TIMER_SLOT, new TimerUpdateListener());
                    timer.start();
                }
                // if timer already exists restart it
                else {
                    timer.restart();
                }

                currentModel.setThreadClassName(getSelectedPluginThread());

                // enable text area appender
                taa.setEnabled(true);

                pluginManager.executePlugin(currentModel);
            } catch (Exception ex) {
                postException(ex);
            }
        }
        // Cancel
        else {
            try {
                Element operation = new Element(XMLTags.OPERATION);
                operation.setAttribute(XMLTags.NAME, OperationType.CANCEL);

                buttonControl.setText(OperationType.EXECUTE);
                buttonControl.setActionCommand(OperationType.EXECUTE);

                execute(operation, rm.getString("text.execute.interrupted"));
            } catch (Exception ex) {
                postException(ex);
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
            // update status bars if possible and if changed
            if (currentModel != null) {
                // set table max value
                if (currentModel.getLengthProgressTable() != progressBarTable.getMaximum()) {
                    progressBarTable.setMaximum(currentModel.getLengthProgressTable());
                }

                // set record max value
                if (currentModel.getLengthProgressRecord() != progressBarRecord.getMaximum()) {
                    progressBarRecord.setMaximum(currentModel.getLengthProgressRecord());
                }

                if (currentModel.getCurrentProgressTable() != progressBarTable.getValue()) {
                    progressBarTable.setValue(currentModel.getCurrentProgressTable());

                    if ((currentModel.getProgressMessage() != null) && (currentModel.getProgressMessage().length() > 0)) {
                        progressBarTable.setString(currentModel.getProgressMessage());
                    }
                }

                if (currentModel.getCurrentProgressRecord() != progressBarRecord.getValue()) {
                    progressBarRecord.setValue(currentModel.getCurrentProgressRecord());
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
