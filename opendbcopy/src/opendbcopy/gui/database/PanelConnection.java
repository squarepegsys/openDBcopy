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
package opendbcopy.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Observable;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import opendbcopy.config.Driver;
import opendbcopy.config.OperationType;
import opendbcopy.config.XMLTags;
import opendbcopy.controller.MainController;
import opendbcopy.gui.DynamicPanel;
import opendbcopy.gui.PluginGui;
import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.UnsupportedAttributeValueException;

import org.jdom.Element;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PanelConnection extends DynamicPanel {
    private DatabaseModel  model;
    private boolean        sourceConnectionTested = false;
    private boolean        destinationConnectionTested = false;
    private JPanel         panelSource = new JPanel();
    private JPanel         panelDestination = new JPanel();
    private JPanel         panelMain = new JPanel();
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
    private JComboBox      comboBoxDriverS = new JComboBox();
    private JComboBox      comboBoxDriverD = new JComboBox();
    private JLabel         labelDriverNameS = new JLabel();
    private JLabel         labelDriverNameD = new JLabel();
    private JLabel         labelDriverS = new JLabel();
    private JLabel         labelURLS = new JLabel();
    private JLabel         labelUserNameS = new JLabel();
    private JLabel         labelPasswordS = new JLabel();
    private JLabel         labelURLD = new JLabel();
    private JLabel         labelDriverD = new JLabel();
    private JLabel         labelUserNameD = new JLabel();
    private JLabel         labelPasswordD = new JLabel();
    private TreeMap        drivers = null;

    /**
     * Creates a new PanelConnection object.
     *
     * @param controller DOCUMENT ME!
     * @param pluginGui DOCUMENT ME!
     * @param registerAsObserver DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public PanelConnection(MainController controller,
                           PluginGui    workingMode,
                           Boolean        registerAsObserver) throws Exception {
        super(controller, workingMode, registerAsObserver);

        model = (DatabaseModel) super.model;

        guiInit();
        retrieveDrivers();
        updateTextFields();
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param obj DOCUMENT ME!
     */
    public final void update(Observable o,
                             Object     obj) {
        //onSelect();
    }

    /**
     * DOCUMENT ME!
     */
    public final void onSelect() {
        try {
            updateTextFields();
        } catch (Exception e) {
            postException(e);
        }
    }

    /**
     * must check if default values must be overridden by attributes from project model
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    private void updateTextFields() throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException {
        // source connection
        if (model.getSourceConnection().getAttributes().size() > 0) {
            String classNameModel = model.getSourceConnection().getAttributeValue(XMLTags.DRIVER_CLASS);
            String urlModel = model.getSourceConnection().getAttributeValue(XMLTags.URL);
            String userNameModel = model.getSourceConnection().getAttributeValue(XMLTags.USERNAME);
            String passwordModel = model.getSourceConnection().getAttributeValue(XMLTags.PASSWORD);

            if ((classNameModel.length() > 0) && (classNameModel.compareTo(tfDriverClassNameS.getText()) != 0)) {
                tfDriverClassNameS.setText(classNameModel);
            }

            if ((urlModel.length() > 0) && (urlModel.compareTo(tfURLS.getText()) != 0)) {
                tfURLS.setText(urlModel);
            }

            if ((userNameModel.length() > 0) && (userNameModel.compareTo(tfUserNameS.getText()) != 0)) {
                tfUserNameS.setText(userNameModel);
            }

            if ((passwordModel.length() > 0) && (passwordModel.compareTo(new String(tfPasswordS.getPassword())) != 0)) {
                tfPasswordS.setText(passwordModel);
            }
        }

        // destination connection
        if (model.getDbMode() == model.DUAL_MODE) {
            if (model.getDestinationConnection().getAttributes().size() > 0) {
                String classNameModel = model.getDestinationConnection().getAttributeValue(XMLTags.DRIVER_CLASS);
                String urlModel = model.getDestinationConnection().getAttributeValue(XMLTags.URL);
                String userNameModel = model.getDestinationConnection().getAttributeValue(XMLTags.USERNAME);
                String passwordModel = model.getDestinationConnection().getAttributeValue(XMLTags.PASSWORD);

                if ((classNameModel.length() > 0) && (classNameModel.compareTo(tfDriverClassNameD.getText()) != 0)) {
                    tfDriverClassNameD.setText(classNameModel);
                }

                if ((urlModel.length() > 0) && (urlModel.compareTo(tfURLD.getText()) != 0)) {
                    tfURLD.setText(urlModel);
                }

                if ((userNameModel.length() > 0) && (userNameModel.compareTo(tfUserNameD.getText()) != 0)) {
                    tfUserNameD.setText(userNameModel);
                }

                if ((passwordModel.length() > 0) && (passwordModel.compareTo(new String(tfPasswordD.getPassword())) != 0)) {
                    tfPasswordD.setText(passwordModel);
                }
            }

            panelDestination.setVisible(true);
        } else {
            panelDestination.setVisible(false);
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void retrieveDrivers() {
        drivers = controller.getSqlDriverManager().getDrivers();

        Iterator itDrivers = drivers.values().iterator();

        comboBoxDriverS.removeAllItems();
        comboBoxDriverD.removeAllItems();

        comboBoxDriverS.addItem(rm.getString("text.connection.pickDriver"));
        comboBoxDriverD.addItem(rm.getString("text.connection.pickDriver"));

        while (itDrivers.hasNext()) {
            Driver driver = (Driver) itDrivers.next();

            comboBoxDriverS.addItem(driver.getName());
            comboBoxDriverD.addItem(driver.getName());

            // check for default values
            if ((driver.getDefault(XMLTags.SOURCE_DB) != null) && (driver.getDefault(XMLTags.SOURCE_DB).getName().compareTo(XMLTags.SOURCE_DB) == 0)) {
                comboBoxDriverS.setSelectedItem(driver.getName());
                tfDriverClassNameS.setText(driver.getClassName());
                tfURLS.setText(driver.getDefaultURL(XMLTags.SOURCE_DB));
                tfUserNameS.setText(driver.getDefaultUsername(XMLTags.SOURCE_DB));
            }

            if ((driver.getDefault(XMLTags.DESTINATION_DB) != null) && (driver.getDefault(XMLTags.DESTINATION_DB).getName().compareTo(XMLTags.DESTINATION_DB) == 0)) {
                comboBoxDriverD.setSelectedItem(driver.getName());
                tfDriverClassNameD.setText(driver.getClassName());
                tfURLD.setText(driver.getDefaultURL(XMLTags.DESTINATION_DB));
                tfUserNameD.setText(driver.getDefaultUsername(XMLTags.DESTINATION_DB));
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected final void guiInit() {
        gridLayoutMain.setColumns(1);
        gridLayoutMain.setHgap(10);
        gridLayoutMain.setRows(1);
        gridLayoutMain.setVgap(10);
        this.setLayout(gridLayoutMain);

        gridLayoutConnections.setColumns(1);
        gridLayoutConnections.setHgap(0);
        gridLayoutConnections.setRows(2);
        gridLayoutConnections.setVgap(0);
        panelConnections.setLayout(gridLayoutConnections);

        // Source layout
        panelSource.setLayout(null);
        labelDriverNameS.setText(rm.getString("text.connection.driverName"));
        labelDriverNameS.setBounds(new Rectangle(12, 30, 110, 15));
        comboBoxDriverS.setBounds(new Rectangle(140, 30, 300, 20));
        labelDriverS.setText(rm.getString("text.connection.driverClass"));
        labelDriverS.setBounds(new Rectangle(12, 60, 110, 15));
        tfDriverClassNameS.setToolTipText(rm.getString("text.connection.driverClass.toolTip"));
        tfDriverClassNameS.setBounds(new Rectangle(140, 60, 300, 20));
        labelURLS.setOpaque(true);
        labelURLS.setText(rm.getString("text.connection.url") + "(" + rm.getString("text.connection.required") + ")");
        labelURLS.setBounds(new Rectangle(12, 90, 110, 20));
        tfURLS.setBounds(new Rectangle(140, 90, 300, 18));
        labelUserNameS.setText(rm.getString("text.connection.userName"));
        labelUserNameS.setBounds(new Rectangle(12, 120, 110, 20));
        labelPasswordS.setText(rm.getString("text.connection.password"));
        labelPasswordS.setBounds(new Rectangle(12, 150, 110, 15));
        tfUserNameS.setToolTipText(rm.getString("text.connection.userName"));
        tfUserNameS.setText("");
        tfUserNameS.setBounds(new Rectangle(140, 120, 300, 20));
        tfPasswordS.setMinimumSize(new Dimension(6, 21));
        tfPasswordS.setPreferredSize(new Dimension(6, 21));
        tfPasswordS.setToolTipText(rm.getString("text.connection.password"));
        tfPasswordS.setText("");
        tfPasswordS.setBounds(new Rectangle(140, 150, 300, 20));

        comboBoxDriverS.addActionListener(new PanelConnection_comboBoxDriverS_actionAdapter(this));

        buttonTestS.setBounds(new Rectangle(460, 90, 180, 24));
        buttonTestS.setText(rm.getString("button.applyAndTest"));
        buttonTestS.addActionListener(new PanelConnection_buttonTestS_actionAdapter(this));

        // Destination layout
        panelDestination.setLayout(null);
        labelDriverNameD.setText(rm.getString("text.connection.driverName"));
        labelDriverNameD.setBounds(new Rectangle(12, 30, 110, 15));
        comboBoxDriverD.setBounds(new Rectangle(140, 30, 300, 20));
        labelDriverD.setText(rm.getString("text.connection.driverClass"));
        labelDriverD.setBounds(new Rectangle(12, 60, 110, 15));
        tfDriverClassNameD.setToolTipText(rm.getString("text.connection.driverClass.toolTip"));
        tfDriverClassNameD.setBounds(new Rectangle(140, 60, 300, 20));
        labelURLD.setOpaque(true);
        labelURLD.setText(rm.getString("text.connection.url") + "(" + rm.getString("text.connection.required") + ")");
        labelURLD.setBounds(new Rectangle(12, 90, 110, 20));
        tfURLD.setBounds(new Rectangle(140, 90, 300, 20));
        labelUserNameD.setText(rm.getString("text.connection.userName"));
        labelUserNameD.setBounds(new Rectangle(12, 120, 110, 20));
        labelPasswordD.setText(rm.getString("text.connection.password"));
        labelPasswordD.setBounds(new Rectangle(12, 150, 110, 15));
        tfUserNameD.setToolTipText(rm.getString("text.connection.userName"));
        tfUserNameD.setText("");
        tfUserNameD.setBounds(new Rectangle(140, 120, 300, 20));
        tfPasswordD.setMinimumSize(new Dimension(6, 21));
        tfPasswordD.setPreferredSize(new Dimension(6, 21));
        tfPasswordD.setToolTipText(rm.getString("text.connection.password"));
        tfPasswordD.setText("");
        tfPasswordD.setBounds(new Rectangle(140, 150, 300, 20));

        comboBoxDriverD.addActionListener(new PanelConnection_comboBoxDriverD_actionAdapter(this));

        buttonTestD.setBounds(new Rectangle(460, 90, 180, 24));
        buttonTestD.setText(rm.getString("button.applyAndTest"));
        buttonTestD.addActionListener(new PanelConnection_buttonTestD_actionAdapter(this));

        panelSource.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.connection.sourceDatabaseConnection") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panelDestination.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.connection.destinationDatabaseConnection") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        panelSource.add(labelDriverNameS, null);
        panelSource.add(comboBoxDriverS, null);
        panelSource.add(labelDriverS, null);
        panelSource.add(tfDriverClassNameS, null);
        panelSource.add(tfURLS, null);
        panelSource.add(tfUserNameS, null);
        panelSource.add(labelPasswordS, null);
        panelSource.add(labelUserNameS, null);
        panelSource.add(labelURLS, null);
        panelSource.add(tfPasswordS, null);
        panelSource.add(buttonTestS, null);

        panelDestination.add(labelDriverNameD, null);
        panelDestination.add(comboBoxDriverD, null);
        panelDestination.add(labelDriverD, null);
        panelDestination.add(tfDriverClassNameD, null);
        panelDestination.add(tfURLD, null);
        panelDestination.add(tfUserNameD, null);
        panelDestination.add(labelPasswordD, null);
        panelDestination.add(labelUserNameD, null);
        panelDestination.add(labelURLD, null);
        panelDestination.add(tfPasswordD, null);
        panelDestination.add(buttonTestD, null);

        panelConnections.add(panelSource);
        panelConnections.add(panelDestination);

        panelMain.setLayout(new BorderLayout());
        panelMain.add(panelConnections, BorderLayout.CENTER);

        this.add(panelMain);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonTestS_actionPerformed(ActionEvent e) {
        try {
            if (tfURLS.getText().length() > 0) {
                Driver driver = null;
                String selectedDriver = null;

                tfURLS.setBackground(SystemColor.WHITE);

                Element connection = model.getSourceConnection();
                connection.setAttribute(XMLTags.DRIVER_CLASS, tfDriverClassNameS.getText());
                connection.setAttribute(XMLTags.URL, tfURLS.getText());
                connection.setAttribute(XMLTags.USERNAME, tfUserNameS.getText());

                if (this.tfPasswordS.getPassword() != null) {
                    connection.setAttribute(XMLTags.PASSWORD, new String(tfPasswordS.getPassword()));
                } else {
                    connection.setAttribute(XMLTags.PASSWORD, "");
                }

                Element operation = new Element(XMLTags.OPERATION);
                operation.setAttribute(XMLTags.NAME, OperationType.TEST_SOURCE_CONNECTION);

                String[] param = { tfURLS.getText() };

                execute(operation, rm.getString("message.connection.successful", param));

                // now, if successful, register this driver as default for source_db
                if (comboBoxDriverS.getSelectedIndex() > 0) {
                    driver     = (Driver) drivers.get(comboBoxDriverS.getSelectedItem());
                    driver     = controller.getSqlDriverManager().saveDriver(driver.getName(), tfDriverClassNameS.getText(), tfURLS.getText());
                } else {
                    driver = controller.getSqlDriverManager().saveDriver(null, tfDriverClassNameS.getText(), tfURLS.getText());
                }

                driver.setDefault(XMLTags.SOURCE_DB, tfURLS.getText(), tfUserNameS.getText());

                controller.getSqlDriverManager().saveDriverFileIntoUserHome();
            } else {
                tfURLS.setBackground(SystemColor.RED);
            }
        } catch (Exception ex) {
            postException(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonTestD_actionPerformed(ActionEvent e) {
        try {
            if (tfURLD.getText().length() > 0) {
                Driver driver = null;
                String selectedDriver = null;

                tfURLD.setBackground(SystemColor.WHITE);

                Element connection = model.getDestinationConnection();
                connection.setAttribute(XMLTags.DRIVER_CLASS, tfDriverClassNameD.getText());
                connection.setAttribute(XMLTags.URL, tfURLD.getText());
                connection.setAttribute(XMLTags.USERNAME, tfUserNameD.getText());

                if (this.tfPasswordD.getPassword() != null) {
                    connection.setAttribute(XMLTags.PASSWORD, new String(tfPasswordD.getPassword()));
                } else {
                    connection.setAttribute(XMLTags.PASSWORD, "");
                }

                Element operation = new Element(XMLTags.OPERATION);
                operation.setAttribute(XMLTags.NAME, OperationType.TEST_DESTINATION_CONNECTION);

                String[] param = { tfURLD.getText() };

                execute(operation, rm.getString("message.connection.successful", param));

                // now, if successful, register this driver as default for destination_db
                if (comboBoxDriverD.getSelectedIndex() > 0) {
                    driver     = (Driver) drivers.get(comboBoxDriverD.getSelectedItem());
                    driver     = controller.getSqlDriverManager().saveDriver(driver.getName(), tfDriverClassNameD.getText(), tfURLD.getText());
                } else {
                    driver = controller.getSqlDriverManager().saveDriver(null, tfDriverClassNameD.getText(), tfURLD.getText());
                }

                driver.setDefault(XMLTags.DESTINATION_DB, tfURLD.getText(), tfUserNameD.getText());

                controller.getSqlDriverManager().saveDriverFileIntoUserHome();
            } else {
                tfURLD.setBackground(SystemColor.RED);
            }
        } catch (Exception ex) {
            postException(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void comboBoxDriverS_actionPerformed(ActionEvent e) {
        if (!comboBoxDriverS.getSelectedItem().equals("")) {
            if (drivers.containsKey(comboBoxDriverS.getSelectedItem())) {
                Driver driver = (Driver) drivers.get(comboBoxDriverS.getSelectedItem());

                if (driver != null) {
                    this.tfDriverClassNameS.setText(driver.getClassName());
                    tfURLS.setText(driver.getUrl());
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void comboBoxDriverD_actionPerformed(ActionEvent e) {
        if (!comboBoxDriverD.getSelectedItem().equals("")) {
            if (drivers.containsKey(comboBoxDriverD.getSelectedItem())) {
                Driver driver = (Driver) drivers.get(comboBoxDriverD.getSelectedItem());

                if (driver != null) {
                    this.tfDriverClassNameD.setText(driver.getClassName());
                    tfURLD.setText(driver.getUrl());
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


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelConnection_comboBoxDriverS_actionAdapter implements java.awt.event.ActionListener {
    PanelConnection adaptee;

    /**
     * Creates a new PanelFilter_checkBoxTrim_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelConnection_comboBoxDriverS_actionAdapter(PanelConnection adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.comboBoxDriverS_actionPerformed(e);
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelConnection_comboBoxDriverD_actionAdapter implements java.awt.event.ActionListener {
    PanelConnection adaptee;

    /**
     * Creates a new PanelFilter_checkBoxTrim_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelConnection_comboBoxDriverD_actionAdapter(PanelConnection adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.comboBoxDriverD_actionPerformed(e);
    }
}
