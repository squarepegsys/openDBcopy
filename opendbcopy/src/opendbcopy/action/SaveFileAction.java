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
package opendbcopy.action;

import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.gui.DialogFile;
import opendbcopy.gui.FrameMain;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.jdom.Element;

import java.awt.event.ActionEvent;

import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;


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
    private DialogFile                           dialogFile;
    private File                                 currentDir;

    /**
     * Creates a new SaveFileAction object.
     *
     * @param command DOCUMENT ME!
     * @param name DOCUMENT ME!
     * @param imageIcon DOCUMENT ME!
     * @param fileType DOCUMENT ME!
     * @param currentDir DOCUMENT ME!
     * @param frame DOCUMENT ME!
     * @param controller DOCUMENT ME!
     */
    public SaveFileAction(String         command,
                          String         name,
                          ImageIcon      imageIcon,
                          String         fileType,
                          File           currentDir,
                          FrameMain      frame,
                          MainController controller) {
        putValue(AbstractAction.NAME, name);
        putValue(AbstractAction.ACTION_COMMAND_KEY, command);
        putValue(AbstractAction.SMALL_ICON, imageIcon);

        operation = new Element(XMLTags.OPERATION);
        operation.setAttribute(XMLTags.NAME, command);
        operation.setAttribute(XMLTags.TITLE, name);
        operation.setAttribute(XMLTags.FILE_TYPE, fileType);

        this.currentDir     = currentDir;
        this.controller     = controller;
        this.frame          = frame;
        this.dialogFile     = frame.getDialogFile();

        this.setEnabled(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent evt) {
        operation.setAttribute(XMLTags.FILE, dialogFile.saveDialog(operation.getAttributeValue(XMLTags.TITLE), operation.getAttributeValue(XMLTags.FILE_TYPE), currentDir));

        if (operation.getAttributeValue(XMLTags.FILE).length() > 0) {
            try {
                controller.execute(operation);
            } catch (Exception e) {
                frame.postException(e, Level.ERROR);
            }
        }
    }
}
