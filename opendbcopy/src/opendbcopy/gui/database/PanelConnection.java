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

import info.clearthought.layout.TableLayout;

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
import opendbcopy.config.GUI;
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
            if (driver.isDefault(XMLTags.SOURCE_DB)) {
                comboBoxDriverS.setSelectedItem(driver.getName());
                tfDriverClassNameS.setText(driver.getClassName());
                tfURLS.setText(driver.getDefaultURL(XMLTags.SOURCE_DB));
                tfUserNameS.setText(driver.getDefaultUsername(XMLTags.SOURCE_DB));
            }

            if (driver.isDefault(XMLTags.DESTINATION_DB)) {
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
        double[][] sizeMain = {
                { GUI.B, GUI.F, GUI.B }, // Columns
				{ GUI.B, GUI.F, GUI.VG, GUI.F, GUI.B } // Rows
        		};

        double[][] sizeConnectionDetails = {
                { GUI.B, GUI.P, GUI.HG, 0.6, 20, GUI.P, GUI.HG, 0.4, GUI.B }, // Columns
				{ GUI.B, GUI.P, GUI.VG, GUI.P, GUI.VG, GUI.P, GUI.VG, GUI.P, GUI.VG, GUI.P, GUI.B } // Rows
        		};

        this.setLayout(new TableLayout(sizeMain));
        
		panelSource.setLayout(new TableLayout(sizeConnectionDetails));
		panelDestination.setLayout(new TableLayout(sizeConnectionDetails));

        panelSource.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.connection.sourceDatabaseConnection") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panelDestination.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.connection.destinationDatabaseConnection") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Source layout
        labelDriverNameS.setText(rm.getString("text.connection.driverName"));
        labelDriverS.setText(rm.getString("text.connection.driverClass"));
        tfDriverClassNameS.setToolTipText(rm.getString("text.connection.driverClass.toolTip"));
        labelURLS.setOpaque(true);
        labelURLS.setText(rm.getString("text.connection.url") + "(" + rm.getString("text.connection.required") + ")");
        labelUserNameS.setText(rm.getString("text.connection.userName"));
        labelPasswordS.setText(rm.getString("text.connection.password"));
        tfUserNameS.setToolTipText(rm.getString("text.connection.userName"));
        tfUserNameS.setText("");
        tfPasswordS.setToolTipText(rm.getString("text.connection.password"));
        tfPasswordS.setText("");

        comboBoxDriverS.addActionListener(new PanelConnection_comboBoxDriverS_actionAdapter(this));

        buttonTestS.setText(rm.getString("button.applyAndTest"));
        buttonTestS.addActionListener(new PanelConnection_buttonTestS_actionAdapter(this));

        // Destination layout
        labelDriverNameD.setText(rm.getString("text.connection.driverName"));
        labelDriverD.setText(rm.getString("text.connection.driverClass"));
        tfDriverClassNameD.setToolTipText(rm.getString("text.connection.driverClass.toolTip"));
        labelURLD.setOpaque(true);
        labelURLD.setText(rm.getString("text.connection.url") + "(" + rm.getString("text.connection.required") + ")");
        labelUserNameD.setText(rm.getString("text.connection.userName"));
        labelPasswordD.setText(rm.getString("text.connection.password"));
        tfUserNameD.setToolTipText(rm.getString("text.connection.userName"));
        tfUserNameD.setText("");
        tfPasswordD.setToolTipText(rm.getString("text.connection.password"));
        tfPasswordD.setText("");

        comboBoxDriverD.addActionListener(new PanelConnection_comboBoxDriverD_actionAdapter(this));

        buttonTestD.setText(rm.getString("button.applyAndTest"));
        buttonTestD.addActionListener(new PanelConnection_buttonTestD_actionAdapter(this));

        panelSource.add(labelDriverNameS, "1, 1");
        panelSource.add(comboBoxDriverS, "3, 1");
        panelSource.add(labelDriverS, "1, 3");
        panelSource.add(tfDriverClassNameS, "3, 3");
        panelSource.add(labelURLS, "1, 5");
        panelSource.add(tfURLS, "3, 5");
        panelSource.add(labelUserNameS, "1, 7");
        panelSource.add(tfUserNameS, "3, 7");
        panelSource.add(labelPasswordS, "1, 9");
        panelSource.add(tfPasswordS, "3, 9");
        panelSource.add(buttonTestS, "7, 1");

        panelDestination.add(labelDriverNameD, "1, 1");
        panelDestination.add(comboBoxDriverD, "3, 1");
        panelDestination.add(labelDriverD, "1, 3");
        panelDestination.add(tfDriverClassNameD, "3, 3");
        panelDestination.add(labelURLD, "1, 5");
        panelDestination.add(tfURLD, "3, 5");
        panelDestination.add(labelUserNameD, "1, 7");
        panelDestination.add(tfUserNameD, "3, 7");
        panelDestination.add(labelPasswordD, "1, 9");
        panelDestination.add(tfPasswordD, "3, 9");
        panelDestination.add(buttonTestD, "7, 1");

        this.add(panelSource, "1, 1");
        this.add(panelDestination, "1, 3");
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

                if (this.tfPasswordS.getPassword().length > 0) {
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

                controller.getSqlDriverManager().setSourceDriverDefault(driver, tfURLS.getText(), tfUserNameS.getText());
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

                if (this.tfPasswordD.getPassword().length > 0) {
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

                controller.getSqlDriverManager().setDestinationDriverDefault(driver, tfURLD.getText(), tfUserNameD.getText());
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
                	if (driver.getDefault(XMLTags.SOURCE_DB) != null) {
                		if (driver.getDefaultURL(XMLTags.SOURCE_DB) != null) {
                    		tfURLS.setText(driver.getDefaultURL(XMLTags.SOURCE_DB));
                		}
                		if (driver.getDefaultUsername(XMLTags.SOURCE_DB) != null) {
                			tfUserNameS.setText(driver.getDefaultUsername(XMLTags.SOURCE_DB));
                		}
                	} else {
                        tfURLS.setText(driver.getUrl());
                	}
            		tfDriverClassNameS.setText(driver.getClassName());
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
