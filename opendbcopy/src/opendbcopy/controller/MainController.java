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

import opendbcopy.config.APM;

import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;

import opendbcopy.gui.FrameMain;
import opendbcopy.gui.WorkingModeManager;

import opendbcopy.io.ImportFromXML;
import opendbcopy.io.PropertiesToFile;

import opendbcopy.model.ProjectManager;

import opendbcopy.model.exception.MissingAttributeException;
import opendbcopy.model.exception.MissingElementException;
import opendbcopy.model.exception.UnsupportedAttributeValueException;

import opendbcopy.plugin.PluginManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.sql.SQLException;

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
    private static Logger             logger = Logger.getLogger(MainController.class.getName());
    private static Properties         applicationProperties;
    private static ProjectManager     pm;
    private static FrameMain          frameMain;
    private static WorkingModeManager workingModeManager;
    private static PluginManager      pluginManager;
    private boolean                   isGuiEnabled = false;
	private int frameWidth = 0;
	private int frameHeight = 0;

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
     * @throws IOException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public MainController(String[] args) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, DriverNotFoundException, OpenConnectionException, CloseConnectionException, JDOMException, SQLException, FileNotFoundException, IOException, Exception {
        applicationProperties = loadApplicationProperties();

        // check if the gui shall be shown or not
        if ((applicationProperties.getProperty(APM.SHOW_GUI) != null) && (applicationProperties.getProperty(APM.SHOW_GUI).compareToIgnoreCase("true") == 0)) {
            isGuiEnabled = true;
        }

        setupLog4j(applicationProperties.getProperty(APM.LOG4J_PROPERTIES_FILE));

        Document drivers = null;
        Document project = null;
        Document typeMapping = null;
        Document workingMode = null;

        try {
            // set the look and feel. If not existing, set default crossplatform look and feel
            if (applicationProperties.getProperty(APM.LOOK_AND_FEEL) != null) {
                if (applicationProperties.getProperty(APM.LOOK_AND_FEEL).compareToIgnoreCase(APM.SYSTEM) == 0) {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
            }
        } catch (Exception e) {
            System.err.println("Cannot set Look and Feel");
        }

        // evaluate arguments
        project     = Arguments.process(args);

        workingMode     = ImportFromXML.importFile(applicationProperties.getProperty(APM.STANDARD_WORKING_MODES_CONF_FILE));

        // read drivers file
        drivers     = ImportFromXML.importFile(applicationProperties.getProperty(APM.DRIVERS_CONF_FILE));

        // read SQL types mapping
        typeMapping = ImportFromXML.importFile(applicationProperties.getProperty(APM.SQL_TYPE_MAPPING_CONF_FILE));

        if (project == null) {
            pm = new ProjectManager(this, typeMapping, drivers);
        }

        if (isGuiEnabled()) {
        	try {
        		frameWidth = Integer.parseInt(applicationProperties.getProperty(APM.FRAME_WIDTH));
        		frameHeight = Integer.parseInt(applicationProperties.getProperty(APM.FRAME_HEIGHT));
        	} catch (Exception e) {
        		frameWidth = 800;
        		frameHeight = 650;
        	}

            workingModeManager     = new WorkingModeManager(this, workingMode, frameWidth, frameHeight);
        }

        pluginManager = new PluginManager(this, applicationProperties.getProperty(APM.PLUGINS_DIRECTORY), applicationProperties.getProperty(APM.PLUGINS_CONF_FILE), applicationProperties.getProperty(APM.WORKING_MODE_CONF_FILE), applicationProperties.getProperty(APM.ENCODING));

        // show the frame as the last operation of setup
        if (isGuiEnabled()) {
        	// must be initialised before workingModeManager
        	frameMain              = new FrameMain(this, pm, frameWidth, frameHeight);
            pm.addObserver(frameMain);

        	frameMain.setVisible(true);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        try {
            ClasspathLoader.addLibDirectoryToClasspath();

            new MainController(args);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(0);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(0);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param operation DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
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
     */
    public final void execute(Element operation) throws IllegalArgumentException, UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, DriverNotFoundException, OpenConnectionException, CloseConnectionException, JDOMException, SQLException, IOException, Exception {
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
    public final void registerObserver(Observer observer) throws IllegalArgumentException {
        if (observer == null) {
            throw new IllegalArgumentException("Missing observer");
        }

        pm.addObserver(observer);
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
     * @param pluginIdentifier DOCUMENT ME!
     * @param pluginDescription DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void addWorkingModeForPlugin(Element workingModeElement,
                                              String  pluginIdentifier,
                                              String  pluginDescription) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException {
        if (isGuiEnabled()) {
            workingModeManager.addWorkingModeForPlugin(workingModeElement, pluginIdentifier, pluginDescription);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    private static Properties loadApplicationProperties() throws IllegalArgumentException, FileNotFoundException, IOException {
        return PropertiesToFile.importPropertiesFromFile("conf/opendbcopy.properties");
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
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the isGuiEnabled.
     */
    public boolean isGuiEnabled() {
        return isGuiEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the pluginManager.
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the workingModeManager.
     */
    public WorkingModeManager getWorkingModeManager() {
        return workingModeManager;
    }
}
