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
 * $Log$
 * --------------------------------------------------------------------------*/
package opendbcopy.controller;

import opendbcopy.config.APM;
import opendbcopy.config.XMLTags;

import opendbcopy.gui.FrameMain;

import opendbcopy.io.ImportFromXML;
import opendbcopy.io.PropertiesToFile;

import opendbcopy.model.ProjectManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.jdom.Document;
import org.jdom.Element;

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
     */
    public MainController(String[] args) {
        applicationProperties = loadApplicationProperties();

        setupLog4j(applicationProperties.getProperty(APM.LOG4J_PROPERTIES_FILE));

        Document plugins = null;
        Document project = null;

        try {
            // set the look and feel. If not existing, set default crossplatform look and feel
            if (applicationProperties.getProperty(APM.LOOK_AND_FEEL) != null) {
                if (applicationProperties.getProperty(APM.LOOK_AND_FEEL).compareToIgnoreCase(APM.SYSTEM) == 0) {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
            }

            // evaluate arguments
            project     = Arguments.process(args);

            // read plugin file        
            plugins = ImportFromXML.importFile(applicationProperties.getProperty(APM.PLUGINS_CONF_FILE));

            if ((plugins != null) && (project != null)) {
                // in case operation is included to execute
                if ((project.getRootElement().getChild(XMLTags.OPERATION) != null) && (project.getRootElement().getChild(XMLTags.PLUGIN) != null)) {
                    Element operation = project.getRootElement().getChild(XMLTags.OPERATION).detach();
                    Element plugin = project.getRootElement().getChild(XMLTags.PLUGIN).detach();
                    operation.addContent(plugin);

                    initialiseMainController(plugins, project);

                    // now execute immediately
                    execute(operation);
                } else {
                    initialiseMainController(plugins, project);
                }
            } else if ((plugins != null) && (project == null)) {
                initialiseMainController(plugins);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        new MainController(args);
    }

    /**
     * DOCUMENT ME!
     *
     * @param operation DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final void execute(Element operation) throws Exception {
        pm.execute(operation);
    }

    /**
     * register observers on the fly
     *
     * @param observer DOCUMENT ME!
     */
    public final void registerObserver(Observer observer) {
        pm.addObserver(observer);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static Properties loadApplicationProperties() {
        try {
            return PropertiesToFile.importPropertiesFromFile("conf/opendbcopy.properties");
        } catch (Exception e) {
            logger.error(e.toString());
            logger.error("cannot launch opendbcopy without conf/opendbcopy.properties. Bye bye.");
            System.exit(0);
        }

        return null;
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
     * @param plugins DOCUMENT ME!
     * @param project DOCUMENT ME!
     */
    private void initialiseMainController(Document plugins,
                                          Document project) {
        try {
            String runlevel = project.getRootElement().getAttributeValue(XMLTags.RUNLEVEL);

            pm = new ProjectManager(this, plugins, project);

            if (runlevel != null) {
                if (runlevel.compareTo("5") == 0) {
                    frameMain = new FrameMain(this, pm);
                    pm.addObserver(frameMain);
                    frameMain.setVisible(true);
                }
            } else {
                frameMain = new FrameMain(this, pm);
                pm.addObserver(frameMain);
                frameMain.setVisible(true);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param plugins DOCUMENT ME!
     */
    private void initialiseMainController(Document plugins) {
        pm = new ProjectManager(this, plugins);

        try {
            frameMain = new FrameMain(this, pm);
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
