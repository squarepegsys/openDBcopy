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

import opendbcopy.model.ProjectManager;

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

import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PanelConnection extends JPanel implements Observer {
    private static Logger  logger = Logger.getLogger(PanelConnection.class.getName());
    private FrameMain      parentFrame;
    private MainController controller;
    private ProjectManager pm;
    private boolean        sourceConnectionTested = false;
    private boolean        destinationConnectionTested = false;
    private JPanel         panelSource = new JPanel();
    private JPanel         panelDestination = new JPanel();
    private JPanel         panelMain = new JPanel();
    private JPanel         panelControl = new JPanel();
    private JPanel         panelConnections = new JPanel();
    private GridLayout     gridLayoutMain = new GridLayout();
    private GridLayout     gridLayoutConnections = new GridLayout();
    private JButton        buttonTestS = new JButton();
    private JButton        buttonTestD = new JButton();
    private JTextField     tfDriverClassNameS = new JTextField();
    private JTextField     tfURLS = new JTextField();
    private JTextField     tfUserNameS = new JTextField();
    private JPasswordField tfPasswordS = new JPasswordField();
    private JTextField     tfDriverClassNameD = new JTextField();
    private JTextField     tfURLD = new JTextField();
    private JTextField     tfUserNameD = new JTextField();
    private JPasswordField tfPasswordD = new JPasswordField();
    private JCheckBox      checkBoxDbMode = new JCheckBox();
    private JLabel         labelDriverS = new JLabel();
    private JLabel         labelURLS = new JLabel();
    private JLabel         labelUserNameS = new JLabel();
    private JLabel         labelPasswordS = new JLabel();
    private JLabel         labelURLD = new JLabel();
    private JLabel         labelDriverD = new JLabel();
    private JLabel         labelUserNameD = new JLabel();
    private JLabel         labelPasswordD = new JLabel();

    /**
     * Creates a new PanelConnection object.
     *
     * @param parentFrame DOCUMENT ME!
     * @param controller DOCUMENT ME!
     * @param projectManager DOCUMENT ME!
     */
    public PanelConnection(FrameMain      parentFrame,
                           MainController controller,
                           ProjectManager projectManager) {
        this.parentFrame     = parentFrame;
        this.controller      = controller;
        this.pm              = projectManager;

        //        Element connection = new Element(XMLTags.CONNECTION);
        try {
            guiInit();
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
        try {
            // db_mode
            if (pm.getProjectModel().getDbMode() == pm.getProjectModel().SINGLE_MODE) {
                checkBoxDbMode.setSelected(true);
                panelDestination.setVisible(false);
            } else {
                checkBoxDbMode.setSelected(false);
                panelDestination.setVisible(true);
            }

            // source connection
            if (pm.getProjectModel().getSourceConnection().getAttributes().size() > 0) {
                tfDriverClassNameS.setText(pm.getProjectModel().getSourceConnection().getAttributeValue(XMLTags.DRIVER_CLASS));
                tfURLS.setText(pm.getProjectModel().getSourceConnection().getAttributeValue(XMLTags.URL));
                tfUserNameS.setText(pm.getProjectModel().getSourceConnection().getAttributeValue(XMLTags.USERNAME));
                tfPasswordS.setText(pm.getProjectModel().getSourceConnection().getAttributeValue(XMLTags.PASSWORD));
            }

            if (pm.getProjectModel().getDbMode() == pm.getProjectModel().DUAL_MODE) {
                // destination connection
                if (pm.getProjectModel().getDestinationConnection().getAttributes().size() > 0) {
                    tfDriverClassNameD.setText(pm.getProjectModel().getDestinationConnection().getAttributeValue(XMLTags.DRIVER_CLASS));
                    tfURLD.setText(pm.getProjectModel().getDestinationConnection().getAttributeValue(XMLTags.URL));
                    tfUserNameD.setText(pm.getProjectModel().getDestinationConnection().getAttributeValue(XMLTags.USERNAME));
                    tfPasswordD.setText(pm.getProjectModel().getDestinationConnection().getAttributeValue(XMLTags.PASSWORD));
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
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final boolean canContinue() throws Exception {
        if (pm.getProjectModel().getDbMode() == pm.getProjectModel().DUAL_MODE) {
            if ((pm.getProjectModel().getSourceConnection().getAttributes().size() > 0) && (pm.getProjectModel().getDestinationConnection().getAttributes().size() > 0)) {
                return true;
            } else {
                return false;
            }
        } else {
            if (pm.getProjectModel().getSourceConnection().getAttributes().size() > 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void guiInit() throws Exception {
        gridLayoutMain.setColumns(1);
        gridLayoutMain.setHgap(10);
        gridLayoutMain.setRows(1);
        gridLayoutMain.setVgap(10);
        this.setLayout(gridLayoutMain);

        panelControl.setLayout(new GridLayout(1, 1));

        gridLayoutConnections.setColumns(1);
        gridLayoutConnections.setHgap(0);
        gridLayoutConnections.setRows(2);
        gridLayoutConnections.setVgap(0);
        panelConnections.setLayout(gridLayoutConnections);

        checkBoxDbMode.setText(" Single Database Mode");
        checkBoxDbMode.setSelected(false);
        checkBoxDbMode.addActionListener(new PanelConnection_checkBoxDbMode_actionAdapter(this));

        panelSource.setLayout(null);
        labelDriverS.setText("Driver Class");
        labelDriverS.setBounds(new Rectangle(12, 30, 89, 15));
        tfDriverClassNameS.setToolTipText("Driver Class (this is just an example for MySQL)");
        tfDriverClassNameS.setText("oracle.jdbc.driver.OracleDriver");
        tfDriverClassNameS.setBounds(new Rectangle(110, 28, 240, 18));
        labelURLS.setOpaque(true);
        labelURLS.setText("URL (required)");
        labelURLS.setBounds(new Rectangle(12, 60, 95, 20));
        tfURLS.setText("jdbc:oracle:thin:@mgs1300.gs-vbs.admin.ch:1525:najsp");
        tfURLS.setBounds(new Rectangle(110, 61, 240, 18));
        tfURLS.setToolTipText("URL (this is just an example for MySQL)");
        labelUserNameS.setText("User Name");
        labelUserNameS.setBounds(new Rectangle(12, 85, 76, 18));
        labelPasswordS.setText("Password");
        labelPasswordS.setBounds(new Rectangle(12, 111, 78, 15));
        tfUserNameS.setToolTipText("User Name");
        tfUserNameS.setText("");
        tfUserNameS.setBounds(new Rectangle(110, 85, 240, 18));
        tfPasswordS.setMinimumSize(new Dimension(6, 21));
        tfPasswordS.setPreferredSize(new Dimension(6, 21));
        tfPasswordS.setToolTipText("Password");
        tfPasswordS.setText("");
        tfPasswordS.setBounds(new Rectangle(110, 110, 240, 20));

        buttonTestS.setBounds(new Rectangle(380, 61, 180, 24));
        buttonTestS.setText("Apply and Test");
        buttonTestS.addActionListener(new PanelConnection_buttonTestS_actionAdapter(this));

        buttonTestD.setBounds(new Rectangle(380, 61, 180, 24));
        buttonTestD.setText("Apply and Test");
        buttonTestD.addActionListener(new PanelConnection_buttonTestD_actionAdapter(this));
        tfUserNameD.setText("");
        tfUserNameD.setBounds(new Rectangle(110, 85, 240, 18));
        tfUserNameD.setToolTipText("User Name");
        labelURLD.setText("URL (required)");
        labelURLD.setBounds(new Rectangle(12, 60, 95, 20));
        labelURLD.setOpaque(true);
        tfPasswordD.setText("");
        tfPasswordD.setBounds(new Rectangle(110, 110, 240, 19));
        tfPasswordD.setMinimumSize(new Dimension(6, 21));
        tfPasswordD.setPreferredSize(new Dimension(6, 21));
        tfPasswordD.setToolTipText("Password");

        tfURLD.setText("jdbc:mysql://localhost/destination_db");
        tfURLD.setBounds(new Rectangle(110, 61, 240, 18));
        tfURLD.setToolTipText("URL (this is just an example for MySQL)");
        tfDriverClassNameD.setText("com.mysql.jdbc.Driver");
        tfDriverClassNameD.setBounds(new Rectangle(110, 28, 240, 18));
        tfDriverClassNameD.setToolTipText("Driver Class (this is just an example for MySQL)");
        labelDriverD.setText("Driver Class");
        labelDriverD.setBounds(new Rectangle(12, 30, 89, 15));
        labelUserNameD.setText("User Name");
        labelUserNameD.setBounds(new Rectangle(12, 85, 76, 18));
        labelPasswordD.setText("Password");
        labelPasswordD.setBounds(new Rectangle(12, 111, 78, 15));
        panelDestination.setLayout(null);

        panelControl.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " Choose between working with two databases or only one "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panelSource.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " Source Database Connection "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panelDestination.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " Destination Database Connection "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        panelSource.add(labelDriverS, null);
        panelSource.add(tfDriverClassNameS, null);
        panelSource.add(tfURLS, null);
        panelSource.add(tfUserNameS, null);
        panelSource.add(labelPasswordS, null);
        panelSource.add(labelUserNameS, null);
        panelSource.add(labelURLS, null);
        panelSource.add(tfPasswordS, null);
        panelSource.add(buttonTestS, null);
        panelDestination.add(labelDriverD, null);
        panelDestination.add(tfDriverClassNameD, null);
        panelDestination.add(tfURLD, null);
        panelDestination.add(tfUserNameD, null);
        panelDestination.add(labelPasswordD, null);
        panelDestination.add(labelUserNameD, null);
        panelDestination.add(labelURLD, null);
        panelDestination.add(tfPasswordD, null);
        panelDestination.add(buttonTestD, null);

        panelControl.add(checkBoxDbMode);

        panelConnections.add(panelSource);
        panelConnections.add(panelDestination);

        panelMain.setLayout(new BorderLayout());
        panelMain.add(panelControl, BorderLayout.NORTH);
        panelMain.add(panelConnections, BorderLayout.CENTER);

        this.add(panelMain);
    }

    /**
     * DOCUMENT ME!
     *
     * @param visible DOCUMENT ME!
     */
    public void showPanelDestination(boolean visible) {
        panelDestination.setVisible(visible);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void checkBoxDbMode_actionPerformed(ActionEvent e) {
        try {
            if (checkBoxDbMode.isSelected()) {
                pm.getProjectModel().setDbMode(pm.getProjectModel().SINGLE_MODE);
                pm.broadcast();
            } else {
                pm.getProjectModel().setDbMode(pm.getProjectModel().DUAL_MODE);
                pm.broadcast();
            }
        } catch (Exception ex) {
            logger.error(ex.toString());
            parentFrame.setStatusBar(ex.toString(), Level.ERROR);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonTestS_actionPerformed(ActionEvent e) {
        if (tfURLS.getText().length() > 0) {
            tfURLS.setBackground(Color.WHITE);

            Element connection = pm.getProjectModel().getSourceConnection();
            connection.setAttribute(XMLTags.DRIVER_CLASS, tfDriverClassNameS.getText());
            connection.setAttribute(XMLTags.URL, tfURLS.getText());
            connection.setAttribute(XMLTags.USERNAME, tfUserNameS.getText());

            if (this.tfPasswordS.getPassword() != null) {
                connection.setAttribute(XMLTags.PASSWORD, new String(tfPasswordS.getPassword()));
            } else {
                connection.setAttribute(XMLTags.PASSWORD, "");
            }

            Element operation = new Element(XMLTags.OPERATION);
            operation.setAttribute(XMLTags.NAME, OperationType.TEST_SOURCE);

            try {
                this.controller.execute(operation);
                this.parentFrame.setStatusBar("Connection to " + tfURLS.getText() + " successful", Level.INFO);

                sourceConnectionTested = true;
            } catch (Exception ex) {
                logger.error(ex.toString());
                parentFrame.setStatusBar(ex.toString(), Level.ERROR);
            }
        } else {
            tfURLS.setBackground(Color.RED);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonTestD_actionPerformed(ActionEvent e) {
        if (tfURLD.getText().length() > 0) {
            tfURLD.setBackground(Color.WHITE);

            Element connection = pm.getProjectModel().getDestinationConnection();
            connection.setAttribute(XMLTags.DRIVER_CLASS, this.tfDriverClassNameD.getText());
            connection.setAttribute(XMLTags.URL, this.tfURLD.getText());
            connection.setAttribute(XMLTags.USERNAME, this.tfUserNameD.getText());

            if (this.tfPasswordD.getPassword() != null) {
                connection.setAttribute(XMLTags.PASSWORD, new String(this.tfPasswordD.getPassword()));
            } else {
                connection.setAttribute(XMLTags.PASSWORD, "");
            }

            Element operation = new Element(XMLTags.OPERATION);
            operation.setAttribute(XMLTags.NAME, OperationType.TEST_DESTINATION);

            try {
                this.controller.execute(operation);
                this.parentFrame.setStatusBar("Connection to " + this.tfURLD.getText() + " successful", Level.INFO);

                destinationConnectionTested = true;
            } catch (Exception ex) {
                logger.error(ex.toString());
                this.parentFrame.setStatusBar(ex.toString(), Level.ERROR);
            }
        } else {
            tfURLD.setBackground(Color.RED);
        }
    }
}


/**
 * class description
 *
 * @author smi
 * @version $Revision$
 */
class PanelConnection_checkBoxDbMode_actionAdapter implements java.awt.event.ActionListener {
    PanelConnection adaptee;

    /**
     * Creates a new PanelFilter_checkBoxTrim_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelConnection_checkBoxDbMode_actionAdapter(PanelConnection adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.checkBoxDbMode_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelConnection_buttonTestS_actionAdapter implements java.awt.event.ActionListener {
    PanelConnection adaptee;

    /**
     * Creates a new PanelConnection_buttonTestS_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelConnection_buttonTestS_actionAdapter(PanelConnection adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonTestS_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelConnection_buttonTestD_actionAdapter implements java.awt.event.ActionListener {
    PanelConnection adaptee;

    /**
     * Creates a new PanelConnection_buttonTestD_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelConnection_buttonTestD_actionAdapter(PanelConnection adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonTestD_actionPerformed(e);
    }
}
