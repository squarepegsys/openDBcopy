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
package opendbcopy.action;

import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.gui.DialogFile;
import opendbcopy.gui.FrameMain;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.jdom.Element;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class SaveFileAction extends AbstractAction {
    private static Logger                        logger = Logger.getLogger(SaveFileAction.class.getName());
    private opendbcopy.controller.MainController controller;
    private opendbcopy.gui.FrameMain             frame;
    private Element                              operation;
    private DialogFile dialogFile;

    /**
     * Creates a new SaveFileAction object.
     *
     * @param command DOCUMENT ME!
     * @param fileType DOCUMENT ME!
     * @param frame DOCUMENT ME!
     * @param controller DOCUMENT ME!
     */
    public SaveFileAction(String         command,
                          String         fileType,
                          FrameMain      frame,
                          MainController controller) {
        putValue(AbstractAction.NAME, command);

        operation = new Element(XMLTags.OPERATION);
        operation.setAttribute(XMLTags.NAME, command);
        operation.setAttribute(XMLTags.FILE_TYPE, fileType);

        this.controller     = controller;
        this.frame          = frame;
        this.dialogFile = frame.getDialogFile();

        this.setEnabled(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent evt) {
        operation.setAttribute(XMLTags.FILE, dialogFile.saveDialog(operation.getAttributeValue(XMLTags.NAME), operation.getAttributeValue(XMLTags.FILE_TYPE)));

        if (operation.getAttributeValue(XMLTags.FILE).length() > 0) {
            try {
                controller.execute(operation);
            } catch (Exception e) {
                frame.postException(e, Level.ERROR);
            }
        }
    }
}
