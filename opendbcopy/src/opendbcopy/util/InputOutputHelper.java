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
package opendbcopy.util;

import opendbcopy.config.XMLTags;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;

import org.jdom.Element;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class InputOutputHelper {
    /**
     * DOCUMENT ME!
     *
     * @param file may be a single file or directory. If it is a directory, all files  and only files within this directory are returned inside the
     *        filelist element
     * @param identifier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static Element createFileList(File   file,
                                         String identifier) {
        if (file == null) {
            throw new IllegalArgumentException("Missing file or directory");
        }

        if (file.isDirectory()) {
            return createFileList(file.listFiles(), identifier);
        } else {
            Element filelist = new Element(XMLTags.FILELIST);

            if (identifier == null) {
                filelist.setAttribute(XMLTags.ID, XMLTags.UNKNOWN);
            } else {
                filelist.setAttribute(XMLTags.ID, identifier);
            }

            filelist.setAttribute(XMLTags.DIR, file.getParent());
            filelist.setAttribute(XMLTags.FILES, file.getName());

            return filelist;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param files DOCUMENT ME!
     * @param identifier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static Element createFileList(File[] files,
                                         String identifier) {
        if ((files == null) || (files.length == 0)) {
            throw new IllegalArgumentException("Missing files / directories");
        }

        Element filelist = new Element(XMLTags.FILELIST);

        if (identifier == null) {
            filelist.setAttribute(XMLTags.ID, XMLTags.UNKNOWN);
        } else {
            filelist.setAttribute(XMLTags.ID, identifier);
        }

        filelist.setAttribute(XMLTags.DIR, files[0].getParent());
        filelist.setAttribute(XMLTags.FILES, getFileNameList(files));

        return filelist;
    }

    /**
     * Retrieves files in filelist and checks if those exist! ArrayList returned contains files
     *
     * @param filelist DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static ArrayList getFileList(Element filelist) throws MissingAttributeException, MissingElementException, FileNotFoundException {
        String dir = null;

        if (filelist == null) {
            throw new IllegalArgumentException("Missing filelist");
        }

        if (filelist.getName().compareToIgnoreCase(XMLTags.FILELIST) != 0) {
            throw new MissingElementException(filelist, XMLTags.FILELIST);
        }

        if (filelist.getAttributeValue(XMLTags.DIR) == null) {
            throw new MissingAttributeException(filelist, XMLTags.DIR);
        } else {
            dir = filelist.getAttributeValue(XMLTags.DIR);
        }

        if (filelist.getAttributeValue(XMLTags.FILES) == null) {
            throw new MissingAttributeException(filelist, XMLTags.FILES);
        }

        // ok, required attributes are available
        ArrayList       files = new ArrayList();

        StringTokenizer st = new StringTokenizer(filelist.getAttributeValue(XMLTags.FILES), ",");

        while (st.hasMoreElements()) {
            files.add(getFile(dir, (String) st.nextElement()));
        }

        return files;
    }

    /**
     * Returns String array containing absolute path and file names
     *
     * @param files DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static String[] getFilePathNames(File[] files) {
        if ((files == null) || (files.length == 0)) {
            throw new IllegalArgumentException("Missing files");
        }

        ArrayList fileNames = new ArrayList();

        for (int i = 0; i < files.length; i++) {
            fileNames.add(files[i].getAbsolutePath());
        }

        return (String[]) fileNames.toArray();
    }

    /**
     * returns list of files as string, comma separated. Directories are excluded
     *
     * @param files DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static String getFileNameList(File[] files) {
        String list = "";
        String listSep = ",";

        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                list += (files[i].getName() + listSep);
            }
        }

        // return last listSep
        return list.substring(0, list.length() - 1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param dir DOCUMENT ME!
     * @param filename DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     */
    private static File getFile(String dir,
                                String filename) throws FileNotFoundException {
        File file = new File(dir + MainController.fileSep + filename);

        if (file.exists()) {
            return file;
        } else {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
    }
}
