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
package opendbcopy.config;

import com.Ostermiller.util.Browser;

import opendbcopy.controller.MainController;

import opendbcopy.io.PropertiesToFile;
import opendbcopy.io.Reader;
import opendbcopy.io.Writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class ConfigManager {
    private MainController controller;
    private File           fileAppProperties;
    private Properties     applicationProperties;

    /**
     * Creates a new ConfigManager object.
     *
     * @param controller DOCUMENT ME!
     * @param pathFileNameAppProperties DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    public ConfigManager(String         pathFileNameAppProperties) throws FileNotFoundException, IOException {
        fileAppProperties     = new File(pathFileNameAppProperties);

        if (!fileAppProperties.exists()) {
            throw new RuntimeException(pathFileNameAppProperties + " does not exist!");
        }

        loadApplicationProperties();

        if ((getApplicationProperty(APM.SHOW_GUI) != null) && Boolean.valueOf(getApplicationProperty(APM.SHOW_GUI)).booleanValue()) {
            loadBrowserSettings();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private void loadBrowserSettings() throws IOException {
        String   defaultPaths = getApplicationProperty(APM.BROWSER_PATHS);
        String[] peaces = null;

        if ((defaultPaths != null) && (defaultPaths.trim().length() > 0)) {
            Pattern pattern = Pattern.compile(",");
            peaces = pattern.split(defaultPaths);

            // just in case someone entered spaces
            for (int i = 0; i < peaces.length; i++) {
                peaces[i] = peaces[i].trim();
            }
        } else {
            peaces = Browser.defaultCommands();

            String propertiesString = "";

            if ((peaces != null) && (peaces.length > 0)) {
                for (int i = 0; i < peaces.length; i++) {
                    propertiesString += (peaces[i] + ",");
                }

                propertiesString = propertiesString.substring(0, propertiesString.length() - ",".length());
                updateApplicationProperty(APM.BROWSER_PATHS, propertiesString);
            }
        }

        Browser.exec = peaces;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Properties getApplicationProperties() {
        return applicationProperties;
    }

    /**
     * DOCUMENT ME!
     *
     * @param key DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public String getApplicationProperty(String key) {
        if (key == null) {
            return null;
        }

        if (applicationProperties.containsKey(key)) {
            return applicationProperties.getProperty(key);
        } else {
            throw new RuntimeException("Key " + key + " does not exist in opendbcopy.properties");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List getAvailableGuiLanguages() {
        ArrayList locales = new ArrayList();

        String availableLanguages = getApplicationProperty(APM.OPENDBCOPY_RESOURCE_AVAILABLE);

        if ((availableLanguages != null) && (availableLanguages.length() > 0)) {
            Pattern  pattern = Pattern.compile(",");
            String[] peaces = pattern.split(availableLanguages);

            if ((peaces != null) && (peaces.length > 0)) {
                for (int i = 0; i < peaces.length; i++) {
                    locales.add(new Locale(peaces[i].trim()));
                }
            }
        }

        return locales;
    }
    
    public Locale getDefaultLocale() {
    	return new Locale(getApplicationProperty(APM.DEFAULT_LANGUAGE));
    }

    /**
     * DOCUMENT ME!
     *
     * @param key DOCUMENT ME!
     * @param value DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void updateApplicationProperty(String key,
                                          String value) throws IOException {
        // check if allowed to update
        if (getApplicationProperty(key) != null) {
            String       oldValue = getApplicationProperty(key);
            String       toReplace = key + "=" + oldValue;

            StringBuffer sb = Reader.read(fileAppProperties);

            int          startIndex = sb.indexOf(toReplace);

            if (startIndex >= 0) {
                sb = sb.replace(startIndex, startIndex + toReplace.length(), key + "=" + value);
                Writer.write(sb, fileAppProperties);

                // update properties
                applicationProperties.setProperty(key, value);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    private void loadApplicationProperties() throws FileNotFoundException, IOException {
        applicationProperties = PropertiesToFile.importPropertiesFromFile(fileAppProperties.getAbsolutePath());
    }
}
