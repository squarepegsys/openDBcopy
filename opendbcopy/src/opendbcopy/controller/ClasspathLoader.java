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
package opendbcopy.controller;

import java.io.File;
import java.io.IOException;

import java.net.URL;


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
        String   classpath = System.getProperty("java.class.path");

        File     libDir = new File("lib");

        String[] libFiles = libDir.list();

        for (int i = 0; i < libFiles.length; i++) {
            if (!checkIfInClassPath(classpath, libFiles[i])) {
                if ((libFiles[i].compareToIgnoreCase("CVS") != 0) && (libFiles[i].compareToIgnoreCase("opendbcopy.jar") != 0)) {
                    URL newLib = new URL(libDir.toURL() + libFiles[i]);
                    ClassPathHacker.addURL(newLib);
                    System.out.println("added " + newLib + " dynamically to your classpath");
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param classpath DOCUMENT ME!
     * @param archive DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static boolean checkIfInClassPath(String classpath,
                                              String archive) {
        if (classpath.indexOf(archive) > -1) {
            return true;
        } else {
            return false;
        }
    }
}
