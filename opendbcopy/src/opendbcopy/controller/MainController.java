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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.Observer;
import java.util.Properties;

import opendbcopy.config.APM;
import opendbcopy.config.ConfigManager;
import opendbcopy.config.OperationType;
import opendbcopy.config.SQLDriverManager;
import opendbcopy.config.XMLTags;
import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;
import opendbcopy.gui.FrameConsole;
import opendbcopy.gui.FrameMain;
import opendbcopy.gui.PluginGuiManager;
import opendbcopy.io.ImportFromXML;
import opendbcopy.plugin.JobManager;
import opendbcopy.plugin.model.Model;
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


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class MainController {
    private static Logger           logger = Logger.getLogger(MainController.class.getName());
    private static ConfigManager    cm;
    private static JobManager       jm;
    private static ResourceManager  rm;
    private static FrameMain        frameMain;
    private static FrameConsole     frameConsole;
    private static PluginGuiManager pluginGuiManager;
    private static SQLDriverManager sqlDriverManager;
    private static TaskLauncher     taskLauncher;
    private static HashMap          resourcesDynamicallyLoaded;
    private static boolean          isGuiEnabled = false;
    private static int              frameWidth = 0;
    private static int              frameHeight = 0;

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
            taskLauncher = new TaskLauncher(this, 5, frameWidth, frameHeight, cm.getPathFilenameConsoleOut(), cm.getApplicationProperty(APM.OPENDBCOPY_LOGO_FILE));
            taskLauncher.go();
        }

        setupLog4j(cm.getApplicationProperty(APM.LOG4J_PROPERTIES_FILE));

        Document project = null;
        Document typeMapping = null;
        Document workingMode = null;

//        try {
//            // set the look and feel. If not existing, set default crossplatform look and feel
//            if (cm.getApplicationProperty(APM.LOOK_AND_FEEL) != null) {
//                if (cm.getApplicationProperty(APM.LOOK_AND_FEEL).compareToIgnoreCase(APM.SYSTEM) == 0) {
//                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//                }
//            }
//        } catch (Exception e) {
//            System.err.println(rm.getString("text.controller.cannotSetLF"));
//        }

        // evaluate arguments
        project = Arguments.process(args);

        addMessage(rm.getString("text.controller.argumentsDone"));

        if (isGuiEnabled()) {
            try {
                frameWidth      = Integer.parseInt(cm.getApplicationProperty(APM.FRAME_MAIN_WIDTH));
                frameHeight     = Integer.parseInt(cm.getApplicationProperty(APM.FRAME_MAIN_HEIGHT));
            } catch (Exception e) {
                frameWidth      = 800;
                frameHeight     = 650;
            }

            pluginGuiManager = new PluginGuiManager(this);
        }

        addMessage(rm.getString("text.controller.workingModeManagerDone"));

        // read drivers file
        sqlDriverManager = new SQLDriverManager(cm.getPersonalSQLDriversFile());
        addMessage(rm.getString("text.controller.sqlDriversDone"));

        // read SQL types mapping
        typeMapping = ImportFromXML.importFile(cm.getApplicationProperty(APM.SQL_TYPE_MAPPING_CONF_FILE));
        addMessage(rm.getString("text.controller.sqlJavaMappingDone"));

        if (project == null) {
            jm = new JobManager(this, typeMapping, cm.getApplicationProperty(APM.PLUGINS_DIRECTORY), cm.getApplicationProperty(APM.PLUGINS_CONF_FILE), cm.getApplicationProperty(APM.PLUGINS_GUI_CONF_FILE));
        } else {
            jm = new JobManager(this, typeMapping, project, cm.getApplicationProperty(APM.PLUGINS_DIRECTORY), cm.getApplicationProperty(APM.PLUGINS_CONF_FILE), cm.getApplicationProperty(APM.PLUGINS_GUI_CONF_FILE));
        }

        addMessage(rm.getString("text.controller.readingPluginDone"));

        // show the frame
        if (isGuiEnabled()) {
            // must be initialised before workingModeManager
            frameMain = new FrameMain(this, frameWidth, frameHeight);

            // add observer for project
            jm.addObserver(frameMain);

            // add observer for working mode
            pluginGuiManager.registerObserver(frameMain);

            frameMain.setVisible(true);
        }

        // execute project immediately
        if (project != null) {
            Element operation = new Element(XMLTags.OPERATION);
            operation.setAttribute(XMLTags.NAME, OperationType.EXECUTE);

            try {
                execute(operation);
            } catch (Exception e) {
                postException(e, Level.ERROR);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        try {
            cm = new ConfigManager();

            System.out.println("reading language specific resources");
            rm = new ResourceManager(cm.getApplicationProperty(APM.OPENDBCOPY_RESOURCE_BUNDLE_DIR), cm.getApplicationProperty(APM.OPENDBCOPY_RESOURCE_NAME), cm.getApplicationProperty(APM.DEFAULT_LANGUAGE));

            // check if the gui shall be shown or not
            if ((cm.getApplicationProperty(APM.SHOW_GUI) != null) && (cm.getApplicationProperty(APM.SHOW_GUI).compareToIgnoreCase("true") == 0)) {
                isGuiEnabled = true;
            }

            if (isGuiEnabled()) {
                frameWidth      = 300;
                frameHeight     = 400;

                try {
                    frameWidth      = Integer.parseInt(cm.getApplicationProperty(APM.FRAME_CONSOLE_WIDTH));
                    frameHeight     = Integer.parseInt(cm.getApplicationProperty(APM.FRAME_CONSOLE_HEIGHT));
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
            System.exit(9);
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

        jm.execute(operation);
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
        jm.addObserver(observer);
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

        jm.deleteObserver(observer);
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
                logger.warn(e);
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
     * @param pluginGuiElement DOCUMENT ME!
     * @param pluginElement DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void addPluginGuiForPlugin(Element pluginGuiElement,
                                            Element pluginElement) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException {
        if (isGuiEnabled()) {
            pluginGuiManager.addPluginGui(pluginGuiElement, pluginElement);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param pluginIdentifier DOCUMENT ME!
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
    public final void loadPluginGuiForPlugin(String  pluginIdentifier,
                                             boolean isExecutableModel) throws MissingAttributeException, MissingElementException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException, PluginException {
        if (isGuiEnabled()) {
            pluginGuiManager.loadPluginGui(pluginIdentifier, isExecutableModel);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param model DOCUMENT ME!
     * @param modelToExecute DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws PluginException DOCUMENT ME!
     */
    public final void loadPluginGuiFromModel(Model   model,
                                             boolean modelToExecute) throws MissingAttributeException, MissingElementException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException, PluginException {
        if (isGuiEnabled()) {
            pluginGuiManager.loadPluginGuiFromModel(model, modelToExecute);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public final void shutdownOpendbcopy() {
        if (!isGuiEnabled()) {
            logger.info(rm.getString("text.controller.shutdown"));
            System.exit(0);
        }
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
    public final JobManager getJobManager() {
        return jm;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final Properties getApplicationProperties() {
        return cm.getApplicationProperties();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static final String getEncoding() {
        if (cm != null) {
            return cm.getApplicationProperty(APM.ENCODING);
        } else {
            return "UTF-8";
        }
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
            FileAppender fa = new FileAppender(new PatternLayout("%5p %d %m%n"), cm.getExecutionLogFile().getAbsolutePath(), false);

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
     * @return Returns the pluginGuiManager.
     */
    public PluginGuiManager getPluginGuiManager() {
        return pluginGuiManager;
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
     * @return Returns the personalPluginsDir.
     */
    public final File getPersonalPluginsDir() {
        return cm.getPersonalPluginsDir();
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the personalJobsDir.
     */
    public final File getPersonalJobsDir() {
        return cm.getPersonalJobsDir();
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
     * @return Returns the configManager
     */
    public final ConfigManager getConfigManager() {
        return cm;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the executionLogFile.
     */
    public final File getExecutionLogFile() {
        return cm.getExecutionLogFile();
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the inoutDir.
     */
    public final File getInoutDir() {
        return cm.getInoutDir();
    }
}
