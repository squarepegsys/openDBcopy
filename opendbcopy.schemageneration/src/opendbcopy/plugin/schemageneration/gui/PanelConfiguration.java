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
package opendbcopy.plugin.schemageneration.gui;

import info.clearthought.layout.TableLayout;

import opendbcopy.config.GUI;
import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.gui.DynamicPanel;
import opendbcopy.gui.PluginGui;

import org.jdom.Element;

import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PanelConfiguration extends DynamicPanel implements ActionListener, DocumentListener {
    private boolean guiLoaded = false;
    private Element     conf;
    private HashMap     hashMapDialects;
    private HashMap     hashMapKeyGenerators;
    private JPanel      panelMain;
    private JPanel      panelOpendbcopy;
    private JPanel      panelHibernateOptions;
    private JPanel      panelKeyGeneration;
    private JLabel      labelOutputDir;
    private JLabel      labelPackageName;
    private JLabel      labelHibernateDialect;
    private JLabel      labelOuterJoin;
    private JLabel      labelLazy;
    private JLabel      labelInverse;
    private JLabel      labelOutputFilelist;
    private JButton     buttonApply;
    private JButton     buttonBrowseDirectory;
    private JComboBox   comboBoxHibernateDialect;
    private JComboBox   comboBoxLazy;
    private JComboBox   comboBoxInverse;
    private JTextField  textFieldOutputDir;
    private JTextField  textFieldPackageName;
    private JTextField  textFieldOuterJoin;
    private JTextField  textFieldOutputFilelist;
    private ButtonGroup buttonGroupKeyGeneration;

    /**
     * Creates a new PanelConfiguration object.
     *
     * @param controller DOCUMENT ME!
     * @param workingMode DOCUMENT ME!
     * @param registerAsObserver DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public PanelConfiguration(MainController controller,
                              PluginGui      workingMode,
                              Boolean        registerAsObserver) throws Exception {
        super(controller, workingMode, registerAsObserver);

        conf = model.getConf();

        guiInit();
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     * @param obj DOCUMENT ME!
     */
    public final void update(Observable o,
                             Object     obj) {
    	guiLoaded = false;
    	onSelect();
    }

    /**
     * Set default values from model
     */
    public void onSelect() {
    	guiLoaded = false;
        textFieldOutputDir.setText(conf.getChild(XMLTags.DIR).getAttributeValue(XMLTags.VALUE));
        textFieldOutputFilelist.setText(conf.getChild(XMLTags.OUTPUT).getChild(XMLTags.FILELIST).getAttributeValue(XMLTags.VALUE));
        textFieldPackageName.setText(conf.getChild(XMLTags.PACKAGE_NAME).getAttributeValue(XMLTags.VALUE));
        textFieldOuterJoin.setText(conf.getChild(XMLTags.OUTER_JOIN).getAttributeValue(XMLTags.VALUE));

        comboBoxLazy.setSelectedItem(Boolean.valueOf(conf.getChild(XMLTags.LAZY).getAttributeValue(XMLTags.VALUE)));
        comboBoxInverse.setSelectedItem(Boolean.valueOf(conf.getChild(XMLTags.INVERSE).getAttributeValue(XMLTags.VALUE)));

        // hibernate dialect
        comboBoxHibernateDialect.setSelectedItem(conf.getChild(XMLTags.HIBERNATE_DIALECT).getAttributeValue(XMLTags.NAME));

        // set default key generation algorithm
        JRadioButton defaultRadioButton = (JRadioButton) hashMapKeyGenerators.get(conf.getChild(XMLTags.GENERATOR_CLASS).getAttributeValue(XMLTags.VALUE));
        buttonGroupKeyGeneration.setSelected(defaultRadioButton.getModel(), true);
        guiLoaded = true;
    }

    /**
     * DOCUMENT ME!
     */
    private void guiInit() {
        double[][] sizeMain = {
                                  { GUI.B, GUI.F, GUI.B }, // Columns
        { GUI.B, GUI.P, GUI.VG, GUI.P, GUI.VG, GUI.P, GUI.B }
        }; // Rows

        double[][] sizeOpendbcopy = {
                                        { GUI.B, GUI.P, GUI.HG, 400, GUI.HG, GUI.P, GUI.B }, // Columns
        { GUI.B, GUI.P, GUI.VS, GUI.P, GUI.B }
        }; // Rows

        double[][] sizeHibernateOptions = {
                                              { GUI.B, GUI.P, GUI.HG, GUI.P, GUI.B }, // Columns
        { GUI.B, GUI.P, GUI.VS, GUI.P, GUI.VS, GUI.P, GUI.VS, GUI.P, GUI.VS, GUI.P, GUI.B }
        }; // Rows

        this.setLayout(new GridLayout(1, 1));

        panelMain     = new JPanel(new TableLayout(sizeMain));

        panelOpendbcopy = new JPanel(new TableLayout(sizeOpendbcopy));
        panelOpendbcopy.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("plugin.opendbcopy.schemageneration.conf.title.opendbcopy") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        panelHibernateOptions = new JPanel(new TableLayout(sizeHibernateOptions));
        panelHibernateOptions.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("plugin.opendbcopy.schemageneration.conf.title.hibernateOptions") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // outputDir
        labelOutputDir            = new JLabel(rm.getString("plugin.opendbcopy.schemageneration.conf.outputDir"));
        textFieldOutputDir        = new JTextField();
        textFieldOutputDir.getDocument().addDocumentListener(this);
        
        buttonBrowseDirectory     = new JButton(rm.getString("plugin.opendbcopy.schemageneration.conf.outputDir.browse"));
        buttonBrowseDirectory.addActionListener(new PanelConfiguration_buttonBrowseDirectory_actionAdapter(this));
        panelOpendbcopy.add(labelOutputDir, "1, 1");
        panelOpendbcopy.add(textFieldOutputDir, "3, 1");
        panelOpendbcopy.add(buttonBrowseDirectory, "5, 1");

        // add to main panel
        panelMain.add(panelOpendbcopy, "1, 1");

        // output filelist
        labelOutputFilelist         = new JLabel(rm.getString("plugin.opendbcopy.schemageneration.conf.output.filelistIdentifier"));
        textFieldOutputFilelist     = new JTextField();
        textFieldOutputFilelist.getDocument().addDocumentListener(this);
        panelOpendbcopy.add(labelOutputFilelist, "1, 3");
        panelOpendbcopy.add(textFieldOutputFilelist, "3, 3");

        // hibernate dialect
        labelHibernateDialect        = new JLabel(rm.getString("plugin.opendbcopy.schemageneration.hibernateDialect"));
        comboBoxHibernateDialect     = new JComboBox();

        List listHibernateDialects = conf.getChild(XMLTags.HIBERNATE_DIALECT).getChildren();

        if ((listHibernateDialects != null) && (listHibernateDialects.size() > 0)) {
            hashMapDialects = new HashMap();

            for (int i = 0; i < listHibernateDialects.size(); i++) {
                Element hibernateDialect = (Element) listHibernateDialects.get(i);
                String  className = hibernateDialect.getAttributeValue(XMLTags.CLASS);
                String  rdbmsName = hibernateDialect.getAttributeValue(XMLTags.NAME);

                // store class name for later retrieval
                hashMapDialects.put(rdbmsName, className);
                comboBoxHibernateDialect.addItem(rdbmsName);
            }
        }
        comboBoxHibernateDialect.addActionListener(this);

        panelHibernateOptions.add(labelHibernateDialect, "1, 1");
        panelHibernateOptions.add(comboBoxHibernateDialect, "3, 1");

        // package name
        labelPackageName         = new JLabel(rm.getString("plugin.opendbcopy.schemageneration.conf.packageName"));
        textFieldPackageName     = new JTextField();
        textFieldPackageName.getDocument().addDocumentListener(this);
        panelHibernateOptions.add(labelPackageName, "1, 3");
        panelHibernateOptions.add(textFieldPackageName, "3, 3");

        // outer join
        labelOuterJoin         = new JLabel(rm.getString("plugin.opendbcopy.schemageneration.conf.outerJoin"));
        textFieldOuterJoin     = new JTextField();
        textFieldOuterJoin.getDocument().addDocumentListener(this);
        panelHibernateOptions.add(labelOuterJoin, "1, 5");
        panelHibernateOptions.add(textFieldOuterJoin, "3, 5");

        // lazy
        labelLazy        = new JLabel(rm.getString("plugin.opendbcopy.schemageneration.conf.lazy"));
        comboBoxLazy     = new JComboBox();
        comboBoxLazy.addItem(new Boolean(false));
        comboBoxLazy.addItem(new Boolean(true));
        comboBoxLazy.addActionListener(this);
        panelHibernateOptions.add(labelLazy, "1, 7");
        panelHibernateOptions.add(comboBoxLazy, "3, 7");

        // inverse
        labelInverse        = new JLabel(rm.getString("plugin.opendbcopy.schemageneration.conf.inverse"));
        comboBoxInverse     = new JComboBox();
        comboBoxInverse.addItem(new Boolean(false));
        comboBoxInverse.addItem(new Boolean(true));
        comboBoxInverse.addActionListener(this);
        panelHibernateOptions.add(labelInverse, "1, 9");
        panelHibernateOptions.add(comboBoxInverse, "3, 9");

        // add to main panel
        panelMain.add(panelHibernateOptions, "1, 3");

        // unique key generation
        Element generatorClasses = conf.getChild(XMLTags.GENERATOR_CLASS);

        List    listGeneratorClasses = generatorClasses.getChildren();

        if ((listGeneratorClasses != null) && (listGeneratorClasses.size() > 0)) {
            hashMapKeyGenerators         = new HashMap();
            buttonGroupKeyGeneration     = new ButtonGroup();
            panelKeyGeneration           = new JPanel(new GridLayout(listGeneratorClasses.size(), 1));
            panelKeyGeneration.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("plugin.opendbcopy.schemageneration.conf.title.generator_class") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

            for (int i = 0; i < listGeneratorClasses.size(); i++) {
                Element      generatorClass = (Element) listGeneratorClasses.get(i);
                String       description = rm.getString(generatorClass.getAttributeValue(XMLTags.DESCRIPTION));
                String       className = generatorClass.getAttributeValue(XMLTags.CLASS);
                JRadioButton radioButton = new JRadioButton(className);
                radioButton.setActionCommand(className);
                radioButton.setToolTipText(description);
                radioButton.addActionListener(this);
                panelKeyGeneration.add(radioButton);
                buttonGroupKeyGeneration.add(radioButton);

                // store key and object for later use
                hashMapKeyGenerators.put(className, radioButton);
            }

            panelMain.add(panelKeyGeneration, "1, 5");
        }

        this.add(new JScrollPane(panelMain, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        
        // call as user may not have imported model and panelConfiguration is default panel shown - and therefore not clicked first
        onSelect();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    private void applyChanges() {
    	if (guiLoaded) {
            conf.getChild(XMLTags.DIR).setAttribute(XMLTags.VALUE, textFieldOutputDir.getText());
            conf.getChild(XMLTags.OUTPUT).getChild(XMLTags.FILELIST).setAttribute(XMLTags.VALUE, textFieldOutputFilelist.getText());
            conf.getChild(XMLTags.PACKAGE_NAME).setAttribute(XMLTags.VALUE, textFieldPackageName.getText());
            conf.getChild(XMLTags.OUTER_JOIN).setAttribute(XMLTags.VALUE, textFieldOuterJoin.getText());
            conf.getChild(XMLTags.LAZY).setAttribute(XMLTags.VALUE, ((Boolean) comboBoxLazy.getSelectedItem()).toString());
            conf.getChild(XMLTags.INVERSE).setAttribute(XMLTags.VALUE, ((Boolean) comboBoxInverse.getSelectedItem()).toString());
            conf.getChild(XMLTags.GENERATOR_CLASS).setAttribute(XMLTags.VALUE, buttonGroupKeyGeneration.getSelection().getActionCommand());
            conf.getChild(XMLTags.HIBERNATE_DIALECT).setAttribute(XMLTags.NAME, (String) comboBoxHibernateDialect.getSelectedItem());
            conf.getChild(XMLTags.HIBERNATE_DIALECT).setAttribute(XMLTags.CLASS, (String) hashMapDialects.get(comboBoxHibernateDialect.getSelectedItem()));
    	}
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    void buttonBrowseDirectory_actionPerformed(ActionEvent e) {
        Element outputDirElement = conf.getChild(XMLTags.DIR);

        if ((outputDirElement.getAttributeValue(XMLTags.VALUE) != null) && (outputDirElement.getAttributeValue(XMLTags.VALUE).length() > 0)) {
            textFieldOutputDir.setText(controller.getFrame().getDialogFile().saveDialogAnyFile(rm.getString(outputDirElement.getAttributeValue(XMLTags.DESCRIPTION)), true, new File(outputDirElement.getAttributeValue(XMLTags.VALUE))));
        } else {
            textFieldOutputDir.setText(controller.getFrame().getDialogFile().saveDialogAnyFile(rm.getString(outputDirElement.getAttributeValue(XMLTags.DESCRIPTION)), true, controller.getInoutDir()));
        }
    }

    public void actionPerformed(ActionEvent e) {
    	applyChanges();
    }
    
    public void insertUpdate(DocumentEvent e) {
    	applyChanges();
    }

    public void removeUpdate(DocumentEvent e) {
    	applyChanges();
    }

    public void changedUpdate(DocumentEvent e) {
    	applyChanges();
    }
}


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
class PanelConfiguration_buttonBrowseDirectory_actionAdapter implements java.awt.event.ActionListener {
    PanelConfiguration adaptee;

    /**
     * Creates a new PanelConfiguration_buttonBrowseDirectory_actionAdapter object.
     *
     * @param adaptee DOCUMENT ME!
     */
    PanelConfiguration_buttonBrowseDirectory_actionAdapter(PanelConfiguration adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonBrowseDirectory_actionPerformed(e);
    }
}
