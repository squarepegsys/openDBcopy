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

import opendbcopy.config.APM;
import opendbcopy.config.SQLDriverManager;
import opendbcopy.config.XMLTags;

import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;

import opendbcopy.gui.FrameConsole;
import opendbcopy.gui.FrameMain;
import opendbcopy.gui.WorkingModeManager;

import opendbcopy.io.FileHandling;
import opendbcopy.io.ImportFromXML;
import opendbcopy.io.PropertiesToFile;

import opendbcopy.plugin.ProjectManager;

import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.PluginException;
import opendbcopy.plugin.model.exception.UnsupportedAttributeValueException;

import opendbcopy.resource.ResourceManager;

import org.apache.log4j.Category;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.lang.reflect.InvocationTargetException;

import java.sql.SQLException;

import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.Observer;
import java.util.Properties;

import javax.swing.UIManager;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class MainController {
    public static final String        fileSep = System.getProperty("file.separator");
    public static final String        lineSep = System.getProperty("line.separator");
    private static Logger             logger = Logger.getLogger(MainController.class.getName());
    private static Properties         applicationProperties;
    private static File               opendbcopyUserHomeDir;
    private static File               logDir;
    private static File               inoutDir;
    private static File               personalProjectsDir;
    private static File               personalPluginsDir;
    private static File               personalConfDir;
    private static File               personalSQLDriversFile;
    private static File               executionLogFile;
    private static String             pathFilenameConsoleOut;
    private static ProjectManager     pm;
    private static ResourceManager    rm;
    private static FrameMain          frameMain;
    private static FrameConsole       frameConsole;
    private static WorkingModeManager workingModeManager;
    private static SQLDriverManager   sqlDriverManager;
    private static TaskLauncher       taskLauncher;
    private static HashMap            resourcesDynamicallyLoaded;
    private static boolean            isGuiEnabled = false;
    private static int                frameWidth = 0;
    private static int                frameHeight = 0;

    /**
     * default Constructor args may be null or contain parameters
     *
     * @param args DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws DriverNotFoundException DOCUMENT ME!
     * @throws OpenConnectionException DOCUMENT ME!
     * @throws CloseConnectionException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public MainController(String[] args) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, DriverNotFoundException, OpenConnectionException, CloseConnectionException, JDOMException, SQLException, FileNotFoundException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException, Exception {
        if (isGuiEnabled()) {
            taskLauncher = new TaskLauncher(this, 5, frameWidth, frameHeight, pathFilenameConsoleOut, applicationProperties.getProperty(APM.OPENDBCOPY_LOGO_FILE));
            taskLauncher.go();
        }

        setupLog4j(applicationProperties.getProperty(APM.LOG4J_PROPERTIES_FILE));

        Document project = null;
        Document typeMapping = null;
        Document workingMode = null;
        Document standardModels = null;

        try {
            // set the look and feel. If not existing, set default crossplatform look and feel
            if (applicationProperties.getProperty(APM.LOOK_AND_FEEL) != null) {
                if (applicationProperties.getProperty(APM.LOOK_AND_FEEL).compareToIgnoreCase(APM.SYSTEM) == 0) {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
            }
        } catch (Exception e) {
            System.err.println(rm.getString("text.controller.cannotSetLF"));
        }

        // evaluate arguments
        project = Arguments.process(args);

        addMessage(rm.getString("text.controller.argumentsDone"));

        if (isGuiEnabled()) {
            try {
                frameWidth      = Integer.parseInt(applicationProperties.getProperty(APM.FRAME_MAIN_WIDTH));
                frameHeight     = Integer.parseInt(applicationProperties.getProperty(APM.FRAME_MAIN_HEIGHT));
            } catch (Exception e) {
                frameWidth      = 800;
                frameHeight     = 650;
            }

            workingModeManager = new WorkingModeManager(this, frameWidth, frameHeight);
        }

        addMessage(rm.getString("text.controller.workingModeManagerDone"));

        // read drivers file
        sqlDriverManager = new SQLDriverManager(personalSQLDriversFile, applicationProperties.getProperty(APM.ENCODING));
        addMessage(rm.getString("text.controller.sqlDriversDone"));

        // read SQL types mapping
        typeMapping = ImportFromXML.importFile(applicationProperties.getProperty(APM.SQL_TYPE_MAPPING_CONF_FILE));
        addMessage(rm.getString("text.controller.sqlJavaMappingDone"));

        if (project == null) {
            pm = new ProjectManager(this, typeMapping, standardModels, applicationProperties.getProperty(APM.PLUGINS_DIRECTORY), applicationProperties.getProperty(APM.PLUGINS_CONF_FILE), applicationProperties.getProperty(APM.WORKING_MODE_CONF_FILE), applicationProperties.getProperty(APM.ENCODING));
        } else {
            pm = new ProjectManager(this, typeMapping, standardModels, project, applicationProperties.getProperty(APM.PLUGINS_DIRECTORY), applicationProperties.getProperty(APM.PLUGINS_CONF_FILE), applicationProperties.getProperty(APM.WORKING_MODE_CONF_FILE), applicationProperties.getProperty(APM.ENCODING));
        }

        addMessage(rm.getString("text.controller.readingPluginDone"));

        // show the frame
        if (isGuiEnabled()) {
            // must be initialised before workingModeManager
            frameMain = new FrameMain(this, frameWidth, frameHeight);

            // add observer for project
            pm.addObserver(frameMain);

            // add observer for working mode
            workingModeManager.registerObserver(frameMain);

            frameMain.setVisible(true);
        }

        // execute project immediately
        if (project != null) {
            // load project, working mode in gui
            if (isGuiEnabled()) {
                if (pm.getCurrentModel().getWorkingMode() != null) {
                    //frameMain.loadWorkingMode(pm.getCurrentModel().getWorkingMode());
                }
            }

            Element operation = new Element(XMLTags.OPERATION);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        try {
            applicationProperties = loadApplicationProperties();

            System.out.println("reading language specific resources");
            rm = new ResourceManager("resources", applicationProperties.getProperty(APM.LANGUAGE));

            setupDirectoriesAndCreateLocalFiles();
            System.out.println(rm.getString("text.controller.checkingDirectoriesDone"));

            String[] params = { pathFilenameConsoleOut };
            System.out.println(rm.getString("text.controller.consoleRedirect", params));

            // lead System.out into single file
            PrintStream out = new PrintStream(new FileOutputStream(pathFilenameConsoleOut));
            System.setOut(out);
            System.setErr(out);

            // check if the gui shall be shown or not
            if ((applicationProperties.getProperty(APM.SHOW_GUI) != null) && (applicationProperties.getProperty(APM.SHOW_GUI).compareToIgnoreCase("true") == 0)) {
                isGuiEnabled = true;
            }

            if (isGuiEnabled()) {
                frameWidth      = 300;
                frameHeight     = 400;

                try {
                    frameWidth      = Integer.parseInt(applicationProperties.getProperty(APM.FRAME_CONSOLE_WIDTH));
                    frameHeight     = Integer.parseInt(applicationProperties.getProperty(APM.FRAME_CONSOLE_HEIGHT));
                } catch (Exception e) {
                    // use default values as specified above
                }
            }

            ClasspathLoader.addLibDirectoryToClasspath();

            new MainController(args);
        } catch (MissingResourceException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param operation DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws DriverNotFoundException DOCUMENT ME!
     * @throws OpenConnectionException DOCUMENT ME!
     * @throws CloseConnectionException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void execute(Element operation) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, DriverNotFoundException, OpenConnectionException, CloseConnectionException, JDOMException, SQLException, IOException, Exception {
        if (operation == null) {
            throw new IllegalArgumentException("Missing operation");
        }

        pm.execute(operation);
    }

    /**
     * register observers on the fly
     *
     * @param observer DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void registerObserver(Observer observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Missing observer");
        }

        // register project observer
        pm.addObserver(observer);
    }

    /**
     * DOCUMENT ME!
     *
     * @param observer DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void deleteObserver(Observer observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Missing observer");
        }

        pm.deleteObserver(observer);
    }

    /**
     * Used to post Exceptions given a general Exception and Log level to the appropriate GUI, if available. If not, exceptions are logged in
     * MainController's logger
     *
     * @param e DOCUMENT ME!
     * @param level DOCUMENT ME!
     */
    public final void postException(Exception e,
                                    Level     level) {
        if (isGuiEnabled()) {
            frameMain.postException(e, level);
        } else {
            if (level.isGreaterOrEqual(Level.FATAL)) {
                logger.fatal(e);
            } else if (level.isGreaterOrEqual(Level.ERROR)) {
                logger.error(e);
            } else if (level.isGreaterOrEqual(Level.WARN)) {
                logger.error(e);
            }
        }
    }

    /**
     * Used to post Messages to the appropriate GUI, if available. If not, messages are logged in MainController's logger
     *
     * @param message DOCUMENT ME!
     */
    public final void postMessage(String message) {
        if (isGuiEnabled()) {
            frameMain.postMessage(message);
        } else {
            logger.info(message);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param workingModeElement DOCUMENT ME!
     * @param pluginElement DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void addWorkingModeForPlugin(Element workingModeElement,
                                              Element pluginElement) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException {
        if (isGuiEnabled()) {
            workingModeManager.addWorkingMode(workingModeElement, pluginElement);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param workingModeIdentifier DOCUMENT ME!
     * @param isExecutableModel DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws PluginException DOCUMENT ME!
     */
    public final void loadWorkingModeForPlugin(String  workingModeIdentifier,
                                               boolean isExecutableModel) throws MissingAttributeException, MissingElementException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException, PluginException {
        if (isGuiEnabled()) {
            workingModeManager.loadWorkingMode(workingModeIdentifier, isExecutableModel);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    private static Properties loadApplicationProperties() throws FileNotFoundException, IOException {
        return PropertiesToFile.importPropertiesFromFile("conf/opendbcopy.properties");
    }

    /**
     * Creates required directories in user_home for opendbcopy.
     *
     * @throws IOException DOCUMENT ME!
     */
    private static void setupDirectoriesAndCreateLocalFiles() throws IOException {
        // check that user's home directory for opendbcopy exists
        String userDir = System.getProperty("user.home") + fileSep + applicationProperties.getProperty(APM.OPENDBCOPY_USER_HOME_DIR);

        try {
            opendbcopyUserHomeDir = FileHandling.getFile(userDir);
        } catch (FileNotFoundException e) {
            opendbcopyUserHomeDir = new File(userDir);
            opendbcopyUserHomeDir.mkdir();

            String[] param = { opendbcopyUserHomeDir.getAbsolutePath() };
            System.out.println(rm.getString("text.controller.created", param));
        }

        // check that log directory exists, else create it
        logDir     = setupDirInOpendbcopyUserHome("log");

        // check that personal conf directory exists, else create it
        personalConfDir     = setupDirInOpendbcopyUserHome("conf");

        // check that plugin in out dir exists, else create it
        inoutDir     = setupDirInOpendbcopyUserHome(applicationProperties.getProperty(APM.PLUGIN_IN_OUT_DIR));

        // check that local plugins folder exists, else create it
        personalPluginsDir     = setupDirInOpendbcopyUserHome(applicationProperties.getProperty(APM.PLUGINS_DIRECTORY));

        // check that local projects folder exists, else create it
        personalProjectsDir     = setupDirInOpendbcopyUserHome(applicationProperties.getProperty(APM.PROJECTS_DIRECTORY));

        // create path filenames for console output
        pathFilenameConsoleOut = logDir.getAbsolutePath() + fileSep + "application_log.txt";

        // check if opendbcopy user home dir contains sql driver file, if not, copy a standard copy into this directory
        File   standardSQLDriverFile = FileHandling.getFile(applicationProperties.getProperty(APM.DRIVERS_CONF_FILE));
        String standardSQLDriverFilename = standardSQLDriverFile.getName();
        String personalSQLDriverPathFilename = opendbcopyUserHomeDir.getAbsolutePath() + fileSep + applicationProperties.getProperty(APM.DRIVERS_CONF_FILE);

        personalSQLDriversFile = FileHandling.getFileInDirectory(personalConfDir, standardSQLDriverFilename);

        if (personalSQLDriversFile == null) {
            personalSQLDriversFile = new File(opendbcopyUserHomeDir.getAbsolutePath() + fileSep + applicationProperties.getProperty(APM.DRIVERS_CONF_FILE));

            FileHandling.copyFile(standardSQLDriverFile, personalSQLDriversFile);
        }

        // execution log file reference
        executionLogFile = new File(logDir.getAbsolutePath() + fileSep + "execution_log.txt");
    }

    /**
     * DOCUMENT ME!
     *
     * @param dirName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static File setupDirInOpendbcopyUserHome(String dirName) {
        File file = null;

        try {
            file = FileHandling.getFile(opendbcopyUserHomeDir.getAbsolutePath() + fileSep + dirName);
        } catch (FileNotFoundException e) {
            file = new File(opendbcopyUserHomeDir.getAbsolutePath() + fileSep + dirName);
            file.mkdir();

            String[] param = { file.getAbsolutePath() };
            System.out.println(rm.getString("text.controller.created", param));
        }

        return file;
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     */
    private void addMessage(String message) {
        if (isGuiEnabled()) {
            taskLauncher.setMessage(message);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final ProjectManager getProjectManager() {
        return pm;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final Properties getApplicationProperties() {
        return applicationProperties;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final FrameMain getFrame() {
        return frameMain;
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     */
    private static void setupLog4j(String fileName) {
        PropertyConfigurator.configure(fileName);

        try {
            // setup fileAppender for application logging
            FileAppender fa = new FileAppender(new PatternLayout("%5p %d %m%n"), executionLogFile.getAbsolutePath(), false);

            //            fa.setThreshold(Priority.INFO);
            Category cat = Category.getInstance("opendbcopy.plugin");
            cat.addAppender(fa);
        } catch (IOException e) {
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the isGuiEnabled.
     */
    public static boolean isGuiEnabled() {
        return isGuiEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the workingModeManager.
     */
    public WorkingModeManager getWorkingModeManager() {
        return workingModeManager;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the sqlDriverManager.
     */
    public SQLDriverManager getSqlDriverManager() {
        return sqlDriverManager;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the rm.
     */
    public ResourceManager getResourceManager() {
        return rm;
    }

    /**
     * DOCUMENT ME!
     *
     * @param file DOCUMENT ME!
     *
     * @return Returns the resourcesDynamicallyLoaded.
     */
    public static final boolean isResourcesDynamicallyLoaded(File file) {
        if (resourcesDynamicallyLoaded != null) {
            if (resourcesDynamicallyLoaded.containsKey(file.getName())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param file DOCUMENT ME!
     */
    public static final void addResourceDynamicallyLoaded(File file) {
        if (resourcesDynamicallyLoaded == null) {
            resourcesDynamicallyLoaded = new HashMap();
        }

        resourcesDynamicallyLoaded.put(file.getName(), file);
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the personalConfDir.
     */
    public final File getPersonalConfDir() {
        return personalConfDir;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the personalPluginsDir.
     */
    public final File getPersonalPluginsDir() {
        return personalPluginsDir;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the personalProjectsDir.
     */
    public final File getPersonalProjectsDir() {
        return personalProjectsDir;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the frameConsole.
     */
    public final FrameConsole getFrameConsole() {
        return frameConsole;
    }

    /**
     * DOCUMENT ME!
     *
     * @param frameConsole The frameConsole to set.
     */
    public final void setFrameConsole(FrameConsole frameConsole) {
        MainController.frameConsole = frameConsole;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the executionLogFile.
     */
    public final File getExecutionLogFile() {
        return executionLogFile;
    }
}
