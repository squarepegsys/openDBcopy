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

import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.ProjectManager;

import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.UnsupportedAttributeValueException;

import opendbcopy.resource.ResourceManager;

import org.apache.log4j.Level;

import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;

import java.sql.SQLException;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class DynamicPanel extends JPanel implements Observer {
    protected MainController  controller;
    protected WorkingMode     workingMode;
    protected ProjectManager  pm;
    protected ResourceManager rm;
    protected Model           model;

    /**
     * Creates a new DynamicPanel object.
     *
     * @param controller DOCUMENT ME!
     * @param workingMode DOCUMENT ME!
     * @param registerAsObserver DOCUMENT ME!
     */
    public DynamicPanel(MainController controller,
                        WorkingMode    workingMode,
                        Boolean        registerAsObserver) {
        this.controller      = controller;
        this.rm              = controller.getResourceManager();
        this.pm              = controller.getProjectManager();
        this.workingMode     = workingMode;
        this.model           = workingMode.getModel();

        if (registerAsObserver.booleanValue()) {
            model.registerObserver(this);
        }
    }

    /**
     * This method must be overriden by each subclass if required
     *
     * @param o DOCUMENT ME!
     * @param obj DOCUMENT ME!
     */
    public void update(Observable o,
                       Object     obj) {
    }

    /**
     * This method must be overriden by each subclass if required
     */
    protected void onSelect() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param operation DOCUMENT ME!
     * @param messageSuccessful DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws DriverNotFoundException DOCUMENT ME!
     * @throws OpenConnectionException DOCUMENT ME!
     * @throws CloseConnectionException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    protected final void execute(Element operation,
                                 String  messageSuccessful) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, DriverNotFoundException, OpenConnectionException, CloseConnectionException, JDOMException, SQLException, IOException, Exception {
        workingMode.execute(operation, messageSuccessful);
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     */
    protected final void postMessage(String message) {
        workingMode.postMessage(message);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    protected void postException(Exception e) {
        workingMode.postException(e, Level.ERROR);
    }
}
