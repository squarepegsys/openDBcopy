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

import opendbcopy.io.FileHandling;
import opendbcopy.io.PropertiesToFile;
import opendbcopy.io.Reader;
import opendbcopy.io.Writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

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
    private MainController      controller;
    private File                opendbcopyUserHomeDir;
    private File                logDir;
    private File                inoutDir;
    private File                personalJobsDir;
    private File                personalPluginsDir;
    private File                personalConfDir;
    private File                personalSQLDriversFile;
    private File                executionLogFile;
    private File                fileDefaultAppProperties;
    private File                filePersonalAppProperties;
    private String              pathFilenameConsoleOut;
    private Properties          defaultApplicationProperties;
    private Properties          personalApplicationProperties;

    /**
     * Creates a new ConfigManager object.
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    public ConfigManager() throws FileNotFoundException, IOException {
        fileDefaultAppProperties = new File(APM.CONF_DIR + APM.FILE_SEP + APM.APP_PROPERTIES_FILE);

        if (!fileDefaultAppProperties.exists()) {
            throw new RuntimeException(APM.CONF_DIR + APM.FILE_SEP + APM.APP_PROPERTIES_FILE + " does not exist!");
        }

        loadDefaultApplicationProperties();
        loadPersonalApplicationProperties();

        setupDirectoriesAndCreateLocalFiles();
        System.out.println("Checking required directories done");

        System.out.println("See " + pathFilenameConsoleOut + " for console output and full details of possible errors which may occur");

        // lead System.out into single file
        PrintStream out = new PrintStream(new FileOutputStream(pathFilenameConsoleOut));
        System.setOut(out);
        System.setErr(out);

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
     * Returns the personalised application properties
     *
     * @return DOCUMENT ME!
     */
    public Properties getApplicationProperties() {
        return personalApplicationProperties;
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

        if (personalApplicationProperties.containsKey(key)) {
            return personalApplicationProperties.getProperty(key);
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

        String    availableLanguages = getApplicationProperty(APM.OPENDBCOPY_RESOURCE_AVAILABLE);

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

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
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

            StringBuffer sb = Reader.read(filePersonalAppProperties);

            int          startIndex = sb.indexOf(toReplace);

            if (startIndex >= 0) {
                sb = sb.replace(startIndex, startIndex + toReplace.length(), key + "=" + value);
                Writer.write(sb, filePersonalAppProperties);

                // update properties
                personalApplicationProperties.setProperty(key, value);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    private void loadDefaultApplicationProperties() throws FileNotFoundException, IOException {
        defaultApplicationProperties = PropertiesToFile.importPropertiesFromFile(fileDefaultAppProperties.getAbsolutePath());
    }

    /**
     * DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    private void loadPersonalApplicationProperties() throws FileNotFoundException, IOException {
        // check that user's home directory for opendbcopy exists
        String userDir = null;

        if (Boolean.valueOf(defaultApplicationProperties.getProperty(APM.OPENDBCOPY_USER_HOME_DIR_IS_ABSOLUTE)).booleanValue()) {
            userDir = defaultApplicationProperties.getProperty(APM.OPENDBCOPY_USER_HOME_DIR);
        } else {
            userDir = System.getProperty("user.home") + APM.FILE_SEP + defaultApplicationProperties.getProperty(APM.OPENDBCOPY_USER_HOME_DIR);
        }

        try {
            opendbcopyUserHomeDir = FileHandling.getFile(userDir);
        } catch (FileNotFoundException e) {
            opendbcopyUserHomeDir = new File(userDir);
            opendbcopyUserHomeDir.mkdir();
        }

        // check that personal conf directory exists, else create it
        personalConfDir     = setupDirInOpendbcopyUserHome(APM.CONF_DIR);

        // check if personal conf properties file exists
        filePersonalAppProperties = new File(personalConfDir.getAbsolutePath() + APM.FILE_SEP + APM.APP_PROPERTIES_FILE);

        if (!filePersonalAppProperties.exists()) {
            FileHandling.copyFile(fileDefaultAppProperties, filePersonalAppProperties);
        }

        personalApplicationProperties = PropertiesToFile.importPropertiesFromFile(filePersonalAppProperties.getAbsolutePath());
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private void setupDirectoriesAndCreateLocalFiles() throws IOException {

        // check that log directory exists, else create it
        logDir     = setupDirInOpendbcopyUserHome("log");

        // check that plugin in out dir exists, else create it
        inoutDir     = setupDirInOpendbcopyUserHome(getApplicationProperty(APM.PLUGIN_IN_OUT_DIR));

        // check that local plugins folder exists, else create it
        personalPluginsDir     = setupDirInOpendbcopyUserHome(getApplicationProperty(APM.PLUGINS_DIRECTORY));

        // check that local projects folder exists, else create it
        personalJobsDir     = setupDirInOpendbcopyUserHome(getApplicationProperty(APM.JOBS_DIRECTORY));

        // create path filenames for console output
        pathFilenameConsoleOut = logDir.getAbsolutePath() + APM.FILE_SEP + APM.APPLICATION_LOG_FILE_NAME;

        // check if opendbcopy user home dir contains sql driver file, if not, copy a standard copy into this directory
        File   standardSQLDriverFile = FileHandling.getFile(getApplicationProperty(APM.DRIVERS_CONF_FILE));
        String standardSQLDriverFilename = standardSQLDriverFile.getName();
        String personalSQLDriverPathFilename = opendbcopyUserHomeDir.getAbsolutePath() + APM.FILE_SEP + getApplicationProperty(APM.DRIVERS_CONF_FILE);

        personalSQLDriversFile = FileHandling.getFileInDirectory(personalConfDir, standardSQLDriverFilename);

        if (personalSQLDriversFile == null) {
            personalSQLDriversFile = new File(opendbcopyUserHomeDir.getAbsolutePath() + APM.FILE_SEP + getApplicationProperty(APM.DRIVERS_CONF_FILE));

            FileHandling.copyFile(standardSQLDriverFile, personalSQLDriversFile);
        }

        // execution log file reference
        executionLogFile = new File(logDir.getAbsolutePath() + APM.FILE_SEP + APM.EXECUTION_LOG_FILE_NAME);
    }

    /**
     * DOCUMENT ME!
     *
     * @param dirName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private File setupDirInOpendbcopyUserHome(String dirName) {
        File file = null;

        try {
            file = FileHandling.getFile(opendbcopyUserHomeDir.getAbsolutePath() + APM.FILE_SEP + dirName);
        } catch (FileNotFoundException e) {
            file = new File(opendbcopyUserHomeDir.getAbsolutePath() + APM.FILE_SEP + dirName);
            file.mkdir();
        }

        return file;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the executionLogFile.
     */
    public File getExecutionLogFile() {
        return executionLogFile;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the inoutDir.
     */
    public File getInoutDir() {
        return inoutDir;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the logDir.
     */
    public File getLogDir() {
        return logDir;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the opendbcopyUserHomeDir.
     */
    public File getOpendbcopyUserHomeDir() {
        return opendbcopyUserHomeDir;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the pathFilenameConsoleOut.
     */
    public String getPathFilenameConsoleOut() {
        return pathFilenameConsoleOut;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the personalConfDir.
     */
    public File getPersonalConfDir() {
        return personalConfDir;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the personalJobsDir.
     */
    public File getPersonalJobsDir() {
        return personalJobsDir;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the personalPluginsDir.
     */
    public File getPersonalPluginsDir() {
        return personalPluginsDir;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the personalSQLDriversFile.
     */
    public File getPersonalSQLDriversFile() {
        return personalSQLDriversFile;
    }
}
