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
import opendbcopy.config.XMLTags;

import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;

import opendbcopy.gui.FrameMain;

import opendbcopy.io.ImportFromXML;
import opendbcopy.io.PropertiesToFile;

import opendbcopy.model.ProjectManager;

import opendbcopy.model.exception.MissingAttributeException;
import opendbcopy.model.exception.MissingElementException;
import opendbcopy.model.exception.UnsupportedAttributeValueException;

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
    private static Logger  logger = Logger.getLogger(MainController.class.getName());
    private Properties     applicationProperties;
    private ProjectManager pm;
    private FrameMain      frameMain;

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

        setupLog4j(applicationProperties.getProperty(APM.LOG4J_PROPERTIES_FILE));

        Document plugins = null;
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

        workingMode     = ImportFromXML.importFile(applicationProperties.getProperty(APM.WORKING_MODE_CONF_FILE));

        // read drivers file
        drivers     = ImportFromXML.importFile(applicationProperties.getProperty(APM.DRIVERS_CONF_FILE));

        // read plugin file        
        plugins     = ImportFromXML.importFile(applicationProperties.getProperty(APM.PLUGINS_CONF_FILE));

        // read SQL types mapping
        typeMapping = ImportFromXML.importFile(applicationProperties.getProperty(APM.SQL_TYPE_MAPPING_CONF_FILE));

        if ((workingMode != null) && (plugins != null) && (drivers != null) && (project != null)) {
            // in case operation is included to execute
            if ((project.getRootElement().getChild(XMLTags.OPERATION) != null) && (project.getRootElement().getChild(XMLTags.PLUGIN) != null)) {
                Element operation = project.getRootElement().getChild(XMLTags.OPERATION).detach();
                Element plugin = project.getRootElement().getChild(XMLTags.PLUGIN).detach();
                operation.addContent(plugin);

                initialiseMainController(workingMode, plugins, typeMapping, drivers, project);

                // now execute immediately
                execute(operation);
            } else {
                initialiseMainController(workingMode, plugins, typeMapping, project);
            }
        } else if ((workingMode != null) && (plugins != null) && (project == null)) {
            initialiseMainController(workingMode, plugins, typeMapping, drivers);
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
     * @param workingMode DOCUMENT ME!
     * @param plugins DOCUMENT ME!
     * @param typeMapping DOCUMENT ME!
     * @param drivers DOCUMENT ME!
     * @param project DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    private void initialiseMainController(Document workingMode,
                                          Document plugins,
                                          Document typeMapping,
                                          Document drivers,
                                          Document project) throws IllegalArgumentException, UnsupportedAttributeValueException, MissingAttributeException, MissingElementException {
        if ((workingMode == null) || (plugins == null) || (typeMapping == null) || (drivers == null) || (project == null)) {
            throw new IllegalArgumentException("missing argument(s) to initialise MainController");
        }

        String runlevel = project.getRootElement().getAttributeValue(XMLTags.RUNLEVEL);

        pm = new ProjectManager(this, plugins, typeMapping, drivers, project);

        if (runlevel != null) {
            if (runlevel.compareTo("5") == 0) {
                frameMain = new FrameMain(this, workingMode, pm);
                pm.addObserver(frameMain);
                frameMain.setVisible(true);
            }
        } else {
            frameMain = new FrameMain(this, workingMode, pm);
            pm.addObserver(frameMain);
            frameMain.setVisible(true);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param workingMode DOCUMENT ME!
     * @param plugins DOCUMENT ME!
     * @param typeMapping DOCUMENT ME!
     * @param drivers DOCUMENT ME!
     */
    private void initialiseMainController(Document workingMode,
                                          Document plugins,
                                          Document typeMapping,
                                          Document drivers) {
        pm = new ProjectManager(this, plugins, typeMapping, drivers);

        try {
            frameMain = new FrameMain(this, workingMode, pm);
            pm.addObserver(frameMain);
            frameMain.setVisible(true);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     */
    private static void setupLog4j(String fileName) {
        PropertyConfigurator.configure(fileName);
    }
}
