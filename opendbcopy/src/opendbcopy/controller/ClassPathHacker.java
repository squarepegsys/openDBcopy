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

import java.lang.reflect.Method;

import java.net.URL;
import java.net.URLClassLoader;


/**
 * Modify Classpath At Runtime see http://forum.java.sun.com/thread.jsp?forum=32&thread=300557&tstart=0&trange=15
 *
 * @author antony_miguel
 * @version $Revision$
 */
public class ClassPathHacker {
    private static final Class[] parameters = new Class[] { URL.class };

    /**
     * DOCUMENT ME!
     *
     * @param s DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }

    /**
     * DOCUMENT ME!
     *
     * @param f DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static void addFile(File f) throws IOException {
        addURL(f.toURL());
    }

    /**
     * DOCUMENT ME!
     *
     * @param u DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static void addURL(URL u) throws IOException {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class          sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { u });
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL " + u + " to system classloader");
        }
    }
}
