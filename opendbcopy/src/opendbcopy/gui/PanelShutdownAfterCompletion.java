package opendbcopy.gui;

import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import opendbcopy.plugin.ProjectManager;
import opendbcopy.resource.ResourceManager;

public class PanelShutdownAfterCompletion extends JPanel implements ItemListener, Observer {
	private ProjectManager pm;
	private ResourceManager rm;
	private JCheckBox checkBoxShutdown;
	
	public PanelShutdownAfterCompletion(ProjectManager pm, ResourceManager rm) {
		this.pm = pm;
		this.rm = rm;
		
		guiInit();
	}
	
    public void update(Observable o,
            Object     arg) {
    	checkBoxShutdown.setSelected(pm.isShutdownOnCompletion());
    }
    
	private void guiInit() {
        this.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1), " " + rm.getString("text.pluginChain.projectOptions") + " "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.setLayout(new GridLayout(1, 1));

        checkBoxShutdown = new JCheckBox(" " + rm.getString("text.pluginChain.shutdownAfterDone"));
        checkBoxShutdown.addItemListener(this);
        checkBoxShutdown.setSelected(pm.isShutdownOnCompletion());

        this.add(checkBoxShutdown);
	}
	
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();

        if (source == checkBoxShutdown) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
            	pm.setShutdownOnCompletion(false);
            } else {
            	pm.setShutdownOnCompletion(true);
            }
        }
	}
}
