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
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import opendbcopy.config.APM;
import opendbcopy.config.ConfigManager;
import opendbcopy.config.GUI;
import opendbcopy.resource.ResourceManager;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class DialogConfig extends JDialog {
    private ConfigManager   cm;
    private ResourceManager rm;
    private JPanel          panelMain;
    private JLabel          labelGuiLanguage;
    private JLabel          labelEncoding;
    private JLabel          labelDefaultBrowser;
    private JComboBox       comboBoxGuiLanguages;
    private JTextField      tfEncoding;
    private JTextField tfBrowserPaths;
    private JCheckBox       checkBoxShowGui;
    private JTextPane       textPaneDefaultBrowser;
    private JButton         buttonCancel;
    private JButton         buttonOk;

    /**
     * Creates a new DialogConfig object.
     *
     * @param parentFrame DOCUMENT ME!
     * @param cm DOCUMENT ME!
     * @param rm DOCUMENT ME!
     * @param title DOCUMENT ME!
     * @param modal DOCUMENT ME!
     */
    public DialogConfig(JFrame          parentFrame,
                        ConfigManager   cm,
                        ResourceManager rm,
                        String          title,
                        boolean         modal) {
        super(parentFrame, title, modal);

        this.cm     = cm;
        this.rm     = rm;

        guiInit();
    }

    /**
     * DOCUMENT ME!
     */
    private void guiInit() {
        double[][] size = {
                              { GUI.B, GUI.P, GUI.HG, GUI.P, GUI.B }, // Columns
        { GUI.B, GUI.P, GUI.VG, GUI.P, GUI.VG, GUI.P, GUI.VG, GUI.P, GUI.VG, GUI.P, GUI.B }
        }; // Rows

        panelMain     = new JPanel(new TableLayout(size));

        labelGuiLanguage        = new JLabel(rm.getString("text.config.guiLanguage"));
        labelEncoding           = new JLabel(rm.getString("text.config.encoding"));
        labelDefaultBrowser     = new JLabel(rm.getString("text.config.defaultBrowser"));

        checkBoxShowGui            = new JCheckBox(rm.getString("text.config.showGui"));
        checkBoxShowGui.setSelected(new Boolean(cm.getApplicationProperty(APM.SHOW_GUI)).booleanValue());

        comboBoxGuiLanguages = new JComboBox();
        fillGuiLanguages();

        tfEncoding = new JTextField();
        tfEncoding.setText(cm.getApplicationProperty(APM.ENCODING));
        
        tfBrowserPaths = new JTextField();
        tfBrowserPaths.setText(cm.getApplicationProperty(APM.BROWSER_PATHS));
        
        buttonCancel = new JButton(rm.getString("button.cancel"));
        buttonCancel.addActionListener(new DialogConfig_buttonCancel_actionAdapter(this));

        buttonOk = new JButton(rm.getString("button.ok"));
        buttonOk.addActionListener(new DialogConfig_buttonOk_actionAdapter(this));

        panelMain.add(checkBoxShowGui, "1, 1, 3, 1");
        panelMain.add(labelGuiLanguage, "1, 3");
        panelMain.add(comboBoxGuiLanguages, "3, 3");
        panelMain.add(labelEncoding, "1, 5");
        panelMain.add(tfEncoding, "3, 5");
        panelMain.add(labelDefaultBrowser, "1, 7");
        panelMain.add(tfBrowserPaths, "3, 7");
        
        JPanel panelControl = new JPanel(new GridLayout(1, 2, 10, 10));
        panelControl.add(buttonCancel);
        panelControl.add(buttonOk);
        panelMain.add(panelControl, "1, 9, 3, 9");

        this.getContentPane().setLayout(new GridLayout(1, 1));
        this.getContentPane().add(panelMain);
    }

    /**
     * DOCUMENT ME!
     */
    private void fillGuiLanguages() {
        List locales = cm.getAvailableGuiLanguages();
        Locale defaultLocale = cm.getDefaultLocale();

        // default language is the first one in comboBox
        if (locales != null && locales.size() > 0) {
        	for (int i = 0; i < locales.size(); i++) {
        		Locale locale = (Locale) locales.get(i);
        		
        		comboBoxGuiLanguages.addItem(new LocaleBean(locale));
        		if (locale.equals(defaultLocale)) {
        			comboBoxGuiLanguages.setSelectedIndex(i);
        		}
        	}
        }
    }
    
    void buttonCancel_actionPerformed(ActionEvent e) {
    	this.hide();
    }

    void buttonOk_actionPerformed(ActionEvent e) {
    	try {
    		// show gui
        	cm.updateApplicationProperty(APM.SHOW_GUI, Boolean.toString(checkBoxShowGui.isSelected()));
        	
        	// default language
        	cm.updateApplicationProperty(APM.DEFAULT_LANGUAGE, ((LocaleBean) comboBoxGuiLanguages.getSelectedItem()).getLocale().getLanguage());
        	
        	// encoding
        	cm.updateApplicationProperty(APM.ENCODING, tfEncoding.getText());
        	
        	// browser paths
        	cm.updateApplicationProperty(APM.BROWSER_PATHS, tfBrowserPaths.getText());
        	
        	this.hide();
        	
    	} catch (IOException ioex) {
    		ioex.printStackTrace();
    	}
    }
}

class LocaleBean {
	private Locale locale;
	
	LocaleBean(Locale locale) {
		this.locale = locale;
	}
	
	public String toString() {
		return locale.getDisplayLanguage();
	}
	
	public Locale getLocale() {
		return locale;
	}
}

class DialogConfig_buttonCancel_actionAdapter implements java.awt.event.ActionListener {
    DialogConfig adaptee;

    DialogConfig_buttonCancel_actionAdapter(DialogConfig adaptee) {
        this.adaptee = adaptee;
    }

    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonCancel_actionPerformed(e);
    }
}

class DialogConfig_buttonOk_actionAdapter implements java.awt.event.ActionListener {
    DialogConfig adaptee;

    DialogConfig_buttonOk_actionAdapter(DialogConfig adaptee) {
        this.adaptee = adaptee;
    }

    public final void actionPerformed(ActionEvent e) {
        adaptee.buttonOk_actionPerformed(e);
    }
}
