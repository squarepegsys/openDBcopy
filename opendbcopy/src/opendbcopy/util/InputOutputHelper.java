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

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Iterator;
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
     * @param file may be a single file or directory. If it is a directory, all files and only files within this directory are returned inside the
     *        filelist element
     * @param identifier DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static Element createFileListElement(File   file,
                                                String identifier) {
        if (file == null) {
            throw new IllegalArgumentException("Missing file or directory");
        }

        if (file.isDirectory()) {
            return createFileListElement(file.listFiles(), identifier);
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
     * @param file DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static Element createFileElement(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Missing file");
        }

        Element fileElement = new Element(XMLTags.FILE);
        fileElement.setAttribute(XMLTags.VALUE, file.getAbsolutePath());

        return fileElement;
    }

    /**
     * DOCUMENT ME!
     *
     * @param file DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static Element createDirElement(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Missing directory");
        }

        Element fileElement = new Element(XMLTags.DIR);
        fileElement.setAttribute(XMLTags.VALUE, file.getAbsolutePath());

        return fileElement;
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
    public static Element createFileListElement(File[] files,
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
     * DOCUMENT ME!
     *
     * @param urls DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static Element createURLListElement(URL[] urls) {
        if (urls == null) {
            throw new IllegalArgumentException("Missing urls");
        }

        Element urllist = new Element(XMLTags.URLLIST);

        for (int i = 0; i < urls.length; i++) {
            URL     url = urls[i];
            Element urlElement = new Element(XMLTags.URL);
            urlElement.setAttribute(XMLTags.VALUE, url.toExternalForm());
            urllist.addContent(urlElement);
        }

        return urllist;
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
     * DOCUMENT ME!
     *
     * @param urlList DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MalformedURLException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static URL[] getURLList(Element urlList) throws MalformedURLException {
        if (urlList == null) {
            throw new IllegalArgumentException("Missing urlList");
        }

        if (urlList.getChildren(XMLTags.URL).size() == 0) {
            return null;
        }

        ArrayList urls = new ArrayList();

        Iterator  itUrls = urlList.getChildren(XMLTags.URL).iterator();

        while (itUrls.hasNext()) {
            Element urlElement = (Element) itUrls.next();
            urls.add(new URL(urlElement.getAttributeValue(XMLTags.VALUE)));
        }

        URL[] urlArray = new URL[urls.size()];

        return (URL[]) urls.toArray(urlArray);
    }

    /**
     * Given a file or dir element retrieves the File which can be a file or directory. If file does not exist a FileNotFoundException is thrown
     *
     * @param file DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws FileNotFoundException DOCUMENT ME!
     */
    public static File getFile(Element file) throws MissingAttributeException, MissingElementException, FileNotFoundException {
        if (file == null) {
            throw new MissingElementException(new Element(XMLTags.FILE), XMLTags.FILE);
        }

        if (file.getAttributeValue(XMLTags.VALUE) == null) {
            throw new MissingAttributeException(file, XMLTags.VALUE);
        }

        File realFile = new File(file.getAttributeValue(XMLTags.VALUE));

        if (realFile.exists()) {
            return realFile;
        } else {
            throw new FileNotFoundException("File " + realFile.getAbsolutePath() + " does not exist");
        }
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
     * DOCUMENT ME!
     *
     * @param file DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static String getFilePathName(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Missing file");
        }

        return file.getAbsolutePath();
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
