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
package opendbcopy.plugin;

import opendbcopy.controller.ClasspathLoader;
import opendbcopy.controller.MainController;

import opendbcopy.io.FileHandling;
import opendbcopy.io.ImportFromXML;

import opendbcopy.model.exception.MissingAttributeException;
import opendbcopy.model.exception.MissingElementException;
import opendbcopy.model.exception.UnsupportedAttributeValueException;

import org.jdom.JDOMException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PluginManager extends Observable {
    private MainController controller;
    private HashMap        plugins;
    private String         encoding;

    /**
     * Creates a new PluginManager object.
     *
     * @param controller DOCUMENT ME!
     * @param pluginsLocation DOCUMENT ME!
     * @param pluginFilename DOCUMENT ME!
     * @param workingModeFilename DOCUMENT ME!
     * @param encoding DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public PluginManager(MainController controller,
                         String         pluginsLocation,
                         String         pluginFilename,
                         String         workingModeFilename,
                         String         encoding) throws IllegalArgumentException, FileNotFoundException, Exception {
        if ((pluginsLocation == null) || (pluginFilename == null) || (workingModeFilename == null) || (encoding == null)) {
            throw new IllegalArgumentException("Missing arguments values: pluginsLocation=" + pluginsLocation + " pluginFilename=" + pluginFilename + " workingModeFilename=" + workingModeFilename + " encoding=" + encoding);
        }

        this.controller     = controller;
        this.encoding       = encoding;
        plugins             = new HashMap();

        loadPlugins(pluginsLocation, pluginFilename, workingModeFilename, encoding);
    }

    /**
     * DOCUMENT ME!
     */
    public final void broadcast() {
        setChanged();
        notifyObservers();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param observer DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void registerObserver(Observer observer) throws IllegalArgumentException {
        if (observer == null) {
            throw new IllegalArgumentException("Missing observer");
        }

        this.addObserver(observer);
    }

    /**
     * DOCUMENT ME!
     *
     * @param observer DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void deleteObserver(Observer observer) throws IllegalArgumentException {
        if (observer == null) {
            throw new IllegalArgumentException("Missing observer");
        }

        this.deleteObserver(observer);
    }

    /**
     * DOCUMENT ME!
     *
     * @param pluginsLocation DOCUMENT ME!
     * @param pluginFilename DOCUMENT ME!
     * @param workingModeFilename DOCUMENT ME!
     * @param encoding DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    private void loadPlugins(String pluginsLocation,
                             String pluginFilename,
                             String workingModeFilename,
                             String encoding) throws IllegalArgumentException, UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, JDOMException, FileNotFoundException, IOException {
        File   pluginsDirectory = FileHandling.getFile(pluginsLocation);
        File[] pluginDirectories = pluginsDirectory.listFiles();

        if (pluginDirectories.length > 0) {
            for (int i = 0; i < pluginDirectories.length; i++) {
                File pluginDir = pluginDirectories[i];

                // read files of plugin ... ignore file(s) if not within a directory
                if (pluginDir.isDirectory() && (pluginDir.getName().compareToIgnoreCase("CVS") != 0)) {
                    // load working mode
                    File workingModeFile = FileHandling.getFileInDirectory(pluginDir, workingModeFilename);

                    // load plugin file
                    File pluginFile = FileHandling.getFileInDirectory(pluginDir, pluginFilename);

                    // create new PluginMetadata
                    PluginMetadata pluginMetadata = new PluginMetadata(pluginFile, encoding);
                    plugins.put(pluginMetadata.getClassName(), pluginMetadata);

                    // add working mode
                    controller.addWorkingModeForPlugin(ImportFromXML.importFile(workingModeFile).getRootElement(), pluginMetadata.getClassName(), pluginMetadata.getDescription());

                    File   pluginLibDir = FileHandling.getFileInDirectory(pluginDir, "lib");
                    File[] pluginJars = FileHandling.getFilesInDirectory(pluginLibDir, "jar", "opendbcopy.jar");
                    File[] pluginZips = FileHandling.getFilesInDirectory(pluginLibDir, "zip", null);

                    if ((pluginJars.length == 0) && (pluginZips.length == 0)) {
                        throw new RuntimeException("Missing libraries (jar or zip) for plugin class " + pluginMetadata.getClassName());
                    }

                    for (int j = 0; j < pluginJars.length; j++) {
                        ClasspathLoader.addLibToClasspath(pluginJars[j].toURL());
                    }

                    for (int j = 0; j < pluginZips.length; j++) {
                        ClasspathLoader.addLibToClasspath(pluginZips[j].toURL());
                    }
                }
            }
        }
    }
}
