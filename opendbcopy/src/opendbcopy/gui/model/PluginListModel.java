package opendbcopy.gui.model;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import opendbcopy.plugin.model.Model;

public class PluginListModel implements ListModel, Observer {
	private Vector modelListeners = new Vector();
	private LinkedList list;
	
	public PluginListModel(LinkedList list) {
		this.list = list;
	}
	
    public void update(Observable o,
            Object     arg) {
    	fireContentsChanged();
    }
    
	public int getSize() {
		return list.size();
	}
	
	public Object getElementAt(int index) {
		return ((Model) list.get(index)).getTitle();
	}
	
	public Model getModelAt(int index) {
		return (Model) list.get(index);
	}

	private void fireContentsChanged() {
        int            len = modelListeners.size();

        ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, list.size() - 1);
        
        for (int i = 0; i < len; i++) {
            ((ListDataListener) modelListeners.elementAt(i)).contentsChanged(e);
        }
	}
	
	public void addListDataListener(ListDataListener l) {
		modelListeners.add(l);
	}

	public void removeListDataListener(ListDataListener l) {
		modelListeners.remove(l);
	}
}
