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
 *
 * --------------------------------------------------------------------------*/
package opendbcopy.gui;

import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;

import opendbcopy.controller.MainController;

import opendbcopy.model.ProjectManager;

import opendbcopy.model.exception.MissingAttributeException;
import opendbcopy.model.exception.MissingElementException;
import opendbcopy.model.exception.UnsupportedAttributeValueException;

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
    protected MainController controller;
    protected FrameMain      parentFrame;
    protected ProjectManager pm;

    /**
     * Creates a new DynamicPanel object.
     *
     * @param controller DOCUMENT ME!
     */
    public DynamicPanel(MainController controller) {
        this.controller      = controller;
        this.pm              = controller.getProjectManager();
        this.parentFrame     = controller.getFrame();
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
        parentFrame.execute(operation, messageSuccessful);
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     */
    protected final void postMessage(String message) {
        parentFrame.postMessage(message);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    protected void postException(Exception e) {
        parentFrame.postException(e, Level.ERROR);
    }
}
