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
package opendbcopy.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.StringTokenizer;
import java.util.Vector;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class FileHandling {
    /**
     * code borrowed from http://www.ragnon.com/javadetails/java-0064.html
     *
     * @param in DOCUMENT ME!
     * @param out DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static void copyFile(File in,
                                File out) throws IOException {
        FileInputStream  fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        byte[]           buf = new byte[1024];
        int              i = 0;

        while ((i = fis.read(buf)) != -1) {
            fos.write(buf, 0, i);
        }

        fis.close();
        fos.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @param pathFilename may be relative or absolute
     *
     * @return valid file or directory
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static File getFile(String pathFilename) throws FileNotFoundException {
        if (pathFilename == null) {
            throw new IllegalArgumentException("Missing pathFilename");
        }

        File file = new File(pathFilename);

        if (file.exists()) {
            return file;
        } else {
            throw new FileNotFoundException("Cannot open file " + file.toURI());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param directory DOCUMENT ME!
     * @param filename just the filename with file type extension
     *
     * @return DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static File getFileInDirectory(File   directory,
                                          String filename) throws FileNotFoundException {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("File directory must be a directory, which it isn't");
        }

        File[] files = directory.listFiles();

        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().compareTo(filename) == 0) {
                if (files[i].exists()) {
                    return files[i];
                } else {
                    throw new FileNotFoundException("Cannot open file " + files[i].toURI());
                }
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param directory DOCUMENT ME!
     * @param fileType DOCUMENT ME!
     * @param excludeFilename DOCUMENT ME!
     *
     * @return returns array of valid files, if such exist, else return an empty array
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static File[] getFilesInDirectory(File   directory,
                                             String fileType,
                                             String excludeFilename) throws FileNotFoundException {
        Vector filesVector = new Vector();

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("File directory must be a directory, which it isn't");
        }

        File[] files = directory.listFiles();

        for (int i = 0; i < files.length; i++) {
            if (getFileEnding(files[i].getName()).compareTo(fileType) == 0) {
                if (excludeFilename != null) {
                    if (files[i].getName().compareTo(excludeFilename) != 0) {
                        filesVector.add(files[i]);
                    }
                } else {
                    filesVector.add(files[i]);
                }
            }
        }

        // now create a file array containing the matching files
        File[] matchingFiles = new File[filesVector.size()];

        for (int i = 0; i < filesVector.size(); i++) {
            matchingFiles[i] = (File) filesVector.get(i);
        }

        return matchingFiles;
    }

    /**
     * DOCUMENT ME!
     *
     * @param filename can be a simple file name or complete path with filename.
     *
     * @return last token separated by a dot "."
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static String getFileEnding(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Missing filename");
        }

        String          fileEnding = "";
        StringTokenizer st = new StringTokenizer(filename, ".");

        while (st.hasMoreElements()) {
            fileEnding = (String) st.nextElement();
        }

        return fileEnding;
    }
}
