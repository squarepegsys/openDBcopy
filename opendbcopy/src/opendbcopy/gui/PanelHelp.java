package opendbcopy.gui;

import java.awt.GridLayout;
import java.awt.SystemColor;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import opendbcopy.resource.ResourceManager;

public class PanelHelp extends JPanel {
	private ResourceManager rm;
	private JLabel labelQuickHelp;
	
	
	public PanelHelp(ResourceManager rm) {
		this.rm = rm;
		guiInit();
	}
	
	private void guiInit() {
		labelQuickHelp = new JLabel(rm.getString("text.workingMode.placeMouseHere"));
		labelQuickHelp.setToolTipText(rm.getString("text.workingMode.help"));
		
		this.setLayout(new GridLayout(1, 1));
        this.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.help.quick") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.add(labelQuickHelp);
	}
}
