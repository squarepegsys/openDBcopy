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
package opendbcopy.controller;

import java.io.File;
import java.io.IOException;

import java.util.StringTokenizer;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class ClasspathLoader {
    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static void addLibDirectoryToClasspath() throws IOException {
        String classpath = System.getProperty("java.class.path");

        File   libDir = new File("lib");

        File[] libFiles = libDir.listFiles();

        for (int i = 0; i < libFiles.length; i++) {
            if ((libFiles[i].getName().compareToIgnoreCase("CVS") != 0) && (libFiles[i].getName().compareToIgnoreCase("opendbcopy.jar") != 0)) {
                addResourceToClasspath(libFiles[i]);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param file DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static void addResourceToClasspath(File file) throws IOException {
        if (!checkIfAlreadyLoaded(file)) {
            ClassPathHacker.addFile(file);
            MainController.addResourceDynamicallyLoaded(file);
            System.out.println("added " + file.getAbsolutePath() + " dynamically to your classpath");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param file DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static boolean checkIfAlreadyLoaded(File file) {
        if (MainController.isResourcesDynamicallyLoaded(file)) {
            return true;
        } else {
            // check if already in classpath
            StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), file.getName());

            if (st.countTokens() == 1) {
                return true;
            } else {
                return false;
            }
        }
    }
}
