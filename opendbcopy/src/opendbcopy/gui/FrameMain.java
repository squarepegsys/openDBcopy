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
 * Revision 1.1  2004/01/09 18:10:51  iloveopensource
 * first release
 *
 * --------------------------------------------------------------------------*/
package opendbcopy.gui;

import opendbcopy.config.APM;
import opendbcopy.config.OperationType;

import opendbcopy.controller.MainController;

import opendbcopy.model.ProjectManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class FrameMain extends JFrame implements Observer {
    private static Logger          logger = Logger.getLogger(FrameMain.class.getName());
    public static final int        FRAME_WIDTH = 700;
    public static final int        FRAME_HEIGHT = 650;
    private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private static final String    CONNECTIONS = "1. Database Connections";
    private static final String    CONNECTION = "1. Database Connection";
    private static final String    MODELS = "2. Models";
    private static final String    MODEL = "2. Model";
    private static final String    TABLE_MAPPING_AND_PROCESS = "3. Table Mapping";
    private static final String    PROCESS_TABLE = "3. Process Tables";
    private static final String    COLUMN_MAPPING_AND_PROCESS_AND_FILTERS = "4. Column Mapping & SQL Filters";
    private static final String    PROCESS_COLUMN_AND_FILTERS = "4. Process Columns & SQL Filters";
    private static final String    GLOBAL_FILTERS = "5. Global Filters";
    private static final String    EXECUTE = "6. Execute";
    private Menu                   menu;
    private MainController         controller;
    private ProjectManager         pm;
    private PanelConnection        panelConnection;
    private PanelModel             panelModel;
    private PanelMappingTable      panelMappingTable;
    private PanelMappingColumn     panelMappingColumn;
    private PanelFilter            panelFilter;
    private PanelExecute           panelExecute;
    private String                 frameTitle;
    private JPanel                 contentPane;
    private JTextPane              statusBar = new JTextPane();
    private JScrollPane            scrollPane = null;
    private BorderLayout           borderLayout = new BorderLayout();
    private JPanel                 panelMain = new JPanel();
    private JPanel                 panelControl = new JPanel();
    private JTabbedPane            tab = new JTabbedPane();
    private GridLayout             gridLayout = new GridLayout();
    private JButton                buttonNext = new JButton();

    //Construct the frame
    public FrameMain(MainController controller,
                     ProjectManager projectManager) {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        this.controller     = controller;
        this.pm             = projectManager;
        this.menu           = new Menu(this, this.controller, projectManager);

        try {
            guiInit();
            enableTabsAndNextButton();
        } catch (Exception e) {
            logger.error(e.toString());
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
        enableTabsAndNextButton();
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     */
    public final void setSelectedTabIndex(int index) {
        tab.setSelectedIndex(index);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final PanelExecute getPanelExecute() {
        return panelExecute;
    }

    //Component initialization
    private void guiInit() throws Exception {
        contentPane = (JPanel) this.getContentPane();
        borderLayout.setHgap(10);
        borderLayout.setVgap(10);
        contentPane.setLayout(borderLayout);
        this.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));

        Properties p = this.controller.getApplicationProperties();

        this.frameTitle = p.getProperty(APM.APPLICATION_NAME) + " " + p.getProperty(APM.APPLICATION_VERSION) + " - " + p.getProperty(APM.APPLICATION_COPYRIGHT);
        super.setTitle(this.frameTitle);

        statusBar.setText(" ");
        statusBar.setPreferredSize(new Dimension(300, 40));
        statusBar.setBackground(null);
        scrollPane = new JScrollPane(statusBar);

        panelControl.setLayout(new BorderLayout(20, 20));
        panelMain.setLayout(gridLayout);
        gridLayout.setColumns(1);
        gridLayout.setHgap(0);
        panelMain.setBorder(BorderFactory.createEmptyBorder());
        buttonNext.setPreferredSize(new Dimension(120, 40));
        buttonNext.setText("Next >");
        buttonNext.addActionListener(new FrameMain_buttonNext_actionAdapter(this));
        panelControl.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        panelControl.setMinimumSize(new Dimension(600, 60));
        panelControl.setPreferredSize(new Dimension(600, 60));
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panelControl.add(scrollPane, BorderLayout.CENTER);
        panelControl.add(buttonNext, BorderLayout.EAST);

        contentPane.add(panelMain, BorderLayout.CENTER);
        contentPane.add(panelControl, BorderLayout.SOUTH);

        initPanels();
        setupTabbedPane();

        panelMain.add(tab, null);

        this.setJMenuBar(this.menu);
        this.menu.setVisible(true);

        centerDialog(this);
    }

    /**
     * DOCUMENT ME!
     */
    private void initPanels() {
        panelConnection        = new PanelConnection(this, controller, pm);
        panelModel             = new PanelModel(this, controller, pm);
        panelMappingTable      = new PanelMappingTable(this, controller, pm);
        panelMappingColumn     = new PanelMappingColumn(this, controller, pm);
        panelFilter            = new PanelFilter(this, controller, pm);
        panelExecute           = new PanelExecute(this, controller, pm);

        panelConnection.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        panelModel.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        panelMappingTable.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        panelMappingColumn.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        panelFilter.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        panelExecute.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));

        controller.registerObserver(panelConnection);
        controller.registerObserver(panelModel);
        controller.registerObserver(panelMappingTable);
        controller.registerObserver(panelMappingColumn);
        controller.registerObserver(panelFilter);
    }

    /**
     * DOCUMENT ME!
     */
    private void setupTabbedPane() {
        tab = new JTabbedPane();

        tab.add(CONNECTIONS, panelConnection);
        tab.add(MODELS, panelModel);
        tab.add(TABLE_MAPPING_AND_PROCESS, panelMappingTable);
        tab.add(COLUMN_MAPPING_AND_PROCESS_AND_FILTERS, panelMappingColumn);
        tab.add(GLOBAL_FILTERS, panelFilter);
        tab.add(EXECUTE, panelExecute);

        tab.addMouseListener(new FrameMain_tab_mouseAdapter(this));
    }

    /**
     * DOCUMENT ME!
     */
    private void enableTabsAndNextButton() {
        try {
            if (panelConnection.canContinue()) {
                tab.setEnabledAt(1, true);

                if (!panelModel.canContinue()) {
                    tab.setEnabledAt(2, false);
                    tab.setEnabledAt(3, false);
                    tab.setEnabledAt(4, false);
                    tab.setEnabledAt(5, false);
                    
					if (getSelectedTabIndex() == 0)
						buttonNext.setEnabled(true);
					if (getSelectedTabIndex() == 1)
						buttonNext.setEnabled(false);                    
                } else {
                    tab.setEnabledAt(2, true);
                    tab.setEnabledAt(3, true);
                    tab.setEnabledAt(4, true);
                    tab.setEnabledAt(5, true);
                    buttonNext.setEnabled(true);
                }
                
                	
            } else {
                tab.setEnabledAt(1, false);
                tab.setEnabledAt(2, false);
                tab.setEnabledAt(3, false);
                tab.setEnabledAt(4, false);
                tab.setEnabledAt(5, false);
                buttonNext.setEnabled(false);
            }

            // dual db_mode
            if (pm.getProjectModel().getDbMode() == pm.getProjectModel().DUAL_MODE) {
                // enable panels
                panelConnection.showPanelDestination(true);
                panelModel.showPanelDestination(true);

                // set texts
                tab.setTitleAt(0, CONNECTIONS);
                tab.setTitleAt(1, MODELS);
                tab.setTitleAt(2, TABLE_MAPPING_AND_PROCESS);
                tab.setTitleAt(3, COLUMN_MAPPING_AND_PROCESS_AND_FILTERS);
            }
            // single db_mode
            else {
                // disable panels
                panelConnection.showPanelDestination(false);
                panelModel.showPanelDestination(false);

                // set texts
                tab.setTitleAt(0, CONNECTION);
                tab.setTitleAt(1, MODEL);
                tab.setTitleAt(2, PROCESS_TABLE);
                tab.setTitleAt(3, PROCESS_COLUMN_AND_FILTERS);
            }

            // new project action
            if (pm.getCurrentOperation().compareTo(OperationType.NEW) == 0) {
                tab.setEnabledAt(1, false);
                tab.setEnabledAt(2, false);
                tab.setEnabledAt(3, false);
                tab.setEnabledAt(4, false);
                tab.setEnabledAt(5, false);
                buttonNext.setEnabled(false);
                tab.setSelectedIndex(0);
            }

            if (getSelectedTabIndex() == 5) {
                buttonNext.setVisible(false);
            } else {
                buttonNext.setVisible(true);
            }
        } catch (Exception e) {
            logger.error(e.toString());
            setStatusBar(e.toString(), Level.ERROR);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param parent DOCUMENT ME!
     */
    public final void centerDialog(Component parent) {
        //Center the window
        Dimension frameSize = parent.getSize();

        if (frameSize.height > SCREEN_SIZE.height) {
            frameSize.height = SCREEN_SIZE.height;
        }

        if (frameSize.width > SCREEN_SIZE.width) {
            frameSize.width = SCREEN_SIZE.width;
        }

        parent.setLocation(((SCREEN_SIZE.width - frameSize.width) / 2) + 40, (SCREEN_SIZE.height - frameSize.height) / 2);
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     * @param level DOCUMENT ME!
     */
    public final void setStatusBar(String message,
                                   Level  level) {
        if (level.isGreaterOrEqual(Level.WARN)) {
            statusBar.setText("ERROR: " + message);
            statusBar.setForeground(Color.RED);
        } else {
            if (message.length() == 0) {
                statusBar.setText(message);
            } else {
                statusBar.setText("INFO: " + message);
            }

            statusBar.setForeground(Color.BLACK);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getSelectedTabIndex() {
        return tab.getSelectedIndex();
    }

    //File | Exit action performed
    public final void jMenuFileExit_actionPerformed(ActionEvent e) {
        System.exit(0);
    }

    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            jMenuFileExit_actionPerformed(null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonNext_actionPerformed(ActionEvent e) {
        enableTabsAndNextButton();

        switch (tab.getSelectedIndex()) {
        case 0:

            if (tab.getComponentAt(1).isEnabled()) {
                tab.setSelectedIndex(1);
            }

            break;

        case 1:

            if (tab.getComponentAt(2).isEnabled()) {
                tab.setSelectedIndex(2);
                loadPanelsAccordingToStatus();
            }

            break;

        case 2:

            if (tab.getComponentAt(3).isEnabled()) {
                tab.setSelectedIndex(3);
                loadPanelsAccordingToStatus();
            }

            break;

        case 3:

            if (tab.getComponentAt(4).isEnabled()) {
                tab.setSelectedIndex(4);
            }

            break;

        case 4:

            if (tab.getComponentAt(5).isEnabled()) {
                tab.setSelectedIndex(5);
            }

            break;

        case 5:

            try {
                loadPanelsAccordingToStatus();
            } catch (Exception ex) {
                logger.error(ex.toString());
                setStatusBar(ex.toString(), Level.ERROR);
            }

            break;
        }
        
		enableTabsAndNextButton();
    }

    /**
     * DOCUMENT ME!
     */
    private void loadPanelsAccordingToStatus() {
        enableTabsAndNextButton();

        try {
            switch (tab.getSelectedIndex()) {
            // table
            case 2:

                try {
                    panelMappingTable.initTable();
                } catch (Exception ex) {
                    logger.error(ex.toString());
                    setStatusBar(ex.toString(), Level.ERROR);
                }

                break;

            // column
            case 3:

                try {
                    if (pm.getProjectModel().getDbMode() == pm.getProjectModel().DUAL_MODE) {
                        panelMappingColumn.loadMappingTables();
                    } else {
                        panelMappingColumn.loadSourceTables();
                    }
                } catch (Exception ex) {
                    logger.error(ex.toString());
                    setStatusBar(ex.toString(), Level.ERROR);
                }

                break;

            // execute	
            case 5:
                panelExecute.loadSelectedPlugin();

                break;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            setStatusBar(e.toString(), Level.ERROR);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void tab_mouseClicked(MouseEvent e) {
        loadPanelsAccordingToStatus();
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class FrameMain_buttonNext_actionAdapter implements java.awt.event.ActionListener {
    FrameMain adaptee;

    /**
     * Creates a new FrameMain_buttonNext_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    FrameMain_buttonNext_actionAdapter(FrameMain adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonNext_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class FrameMain_tab_mouseAdapter extends java.awt.event.MouseAdapter {
    FrameMain adaptee;

    /**
     * Creates a new FrameMain_tab_mouseAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    FrameMain_tab_mouseAdapter(FrameMain adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void mouseClicked(MouseEvent e) {
        adaptee.tab_mouseClicked(e);
    }
}
