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
 * $Log$
 * --------------------------------------------------------------------------*/
package opendbcopy.gui;

import opendbcopy.config.FileType;

import org.apache.log4j.Logger;

import java.awt.Component;

import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.JFrame;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class DialogFile extends JFrame {
    private static Logger       logger = Logger.getLogger(DialogFile.class.getName());
    private static final String POINT = ".";
    private static final String EMPTY_STRING = "";
    private Component           parent;
    private String              title;
    private String              fileType;
    private String              fileName = "";
    private JFileChooser        chooser;

    /**
     * Creates a new DialogFile object.
     *
     * @param parent DOCUMENT ME!
     * @param title DOCUMENT ME!
     * @param fileType DOCUMENT ME!
     */
    public DialogFile(Component parent,
                      String    title,
                      String    fileType) {
        this.parent       = parent;
        this.title        = title;
        this.fileType     = fileType;

        // this is a workaround for http://developer.java.sun.com/developer/bugParade/bugs/4458949.html, fixed in jre 1.4.0
        /*      SecurityManager backup = System.getSecurityManager();
              System.setSecurityManager( null );
              this.chooser = new JFileChooser( java.lang.System.getProperty( "user.home" ) );
              System.setSecurityManager( backup );
        */
        this.chooser = new JFileChooser(java.lang.System.getProperty("user.dir"));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final String openDialog() {
        boolean fileEndingOk = false;
        int     DialogReturnValue = 0;

        chooser.setDialogTitle(this.title);

        ExampleFileFilter filter = new ExampleFileFilter();

        filter.addExtension(this.fileType);
        filter.setDescription(this.fileType);
        chooser.setFileFilter(filter);

        DialogReturnValue = chooser.showOpenDialog(this.parent);

        while (!fileEndingOk && (DialogReturnValue != JFileChooser.CANCEL_OPTION)) {
            if (DialogReturnValue == JFileChooser.APPROVE_OPTION) {
                this.fileName = chooser.getSelectedFile().getAbsolutePath();

                if (checkFileEnding()) {
                    fileEndingOk = true;
                } else {
                    chooser.setApproveButtonToolTipText(filter.getDescription());
                    DialogReturnValue = chooser.showOpenDialog(this.parent);
                }
            }
        }

        if (DialogReturnValue == JFileChooser.APPROVE_OPTION) {
            return this.fileName;
        } else {
            return EMPTY_STRING;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final String saveDialog() {
        boolean fileEndingOk = false;
        int     dialogReturnValue = 0;

        chooser.setDialogTitle(this.title);

        ExampleFileFilter filter = new ExampleFileFilter();

        filter.addExtension(this.fileType);
        filter.setDescription(this.fileType);
        chooser.setFileFilter(filter);

        dialogReturnValue = chooser.showSaveDialog(this.parent);

        while (!fileEndingOk && (dialogReturnValue != JFileChooser.CANCEL_OPTION)) {
            if (dialogReturnValue == JFileChooser.APPROVE_OPTION) {
                this.fileName = chooser.getSelectedFile().getAbsolutePath();

                if (checkFileEnding()) {
                    fileEndingOk = true;
                } else {
                    this.fileName += ("." + this.fileType);
                    fileEndingOk = true;
                }
            }
        }

        if (dialogReturnValue == JFileChooser.APPROVE_OPTION) {
            return this.fileName;
        } else {
            return EMPTY_STRING;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private boolean checkFileEnding() {
        boolean         validFileEnding = false;
        String          token = "";

        StringTokenizer st = new StringTokenizer(this.fileName, POINT);

        while (st.hasMoreTokens()) {
            token = st.nextToken();

            if (this.fileType.compareTo(FileType.HTML_FILE) == 0) {
                if (token.compareToIgnoreCase(FileType.HTML_FILE) == 0) {
                    validFileEnding = true;
                }
            }

            // file ending must be pdf
            if (this.fileType.compareToIgnoreCase(FileType.PDF_FILE) == 0) {
                if (token.compareToIgnoreCase(FileType.PDF_FILE) == 0) {
                    validFileEnding = true;
                }
            }

            // file ending must be xml
            if (this.fileType.compareToIgnoreCase(FileType.XML_FILE) == 0) {
                if (token.compareToIgnoreCase(FileType.XML_FILE) == 0) {
                    validFileEnding = true;
                }
            }

            // file ending must be sql
            if (this.fileType.compareToIgnoreCase(FileType.SQL_FILE) == 0) {
                if (token.compareToIgnoreCase(FileType.SQL_FILE) == 0) {
                    validFileEnding = true;
                }
            }

            // file ending must be properties
            if (this.fileType.compareToIgnoreCase(FileType.PROPERTIES_FILE) == 0) {
                if (token.compareToIgnoreCase(FileType.PROPERTIES_FILE) == 0) {
                    validFileEnding = true;
                }
            }
        }

        return validFileEnding;
    }
}
