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
package opendbcopy.plugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Observable;
import java.util.Properties;

import opendbcopy.config.APM;
import opendbcopy.config.OperationType;
import opendbcopy.config.XMLTags;
import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;
import opendbcopy.controller.MainController;
import opendbcopy.io.ExportToXML;
import opendbcopy.io.ImportFromXML;
import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.PluginException;
import opendbcopy.plugin.model.exception.UnsupportedAttributeValueException;

import org.apache.log4j.Level;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class JobManager extends Observable {
    private MainController controller;
    private PluginManager  pluginManager;
    private Document       job;
    private Document       typeMapping;

    private Element jobRoot;
    private Element plugins;
    private String  encoding;

    /**
     * Creates a new JobManager object.
     *
     * @param controller DOCUMENT ME!
     * @param typeMapping DOCUMENT ME!
     * @param pluginsLocation DOCUMENT ME!
     * @param pluginFilename DOCUMENT ME!
     * @param workingModeFilename DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws PluginException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public JobManager(MainController controller,
                          Document       typeMapping,
                          String         pluginsLocation,
                          String         pluginFilename,
                          String         workingModeFilename) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, JDOMException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException, PluginException {
        if ((controller == null) || (typeMapping == null)) {
            throw new IllegalArgumentException("Missing arguments values: controller=" + controller + " typeMapping=" + typeMapping);
        }

        this.controller      = controller;
        this.typeMapping     = typeMapping;

        initJob(controller.getApplicationProperties());

        this.pluginManager     = new PluginManager(controller, this, plugins, pluginsLocation, pluginFilename, workingModeFilename);
    }

    /**
     * Creates a new JobManager object.
     *
     * @param controller DOCUMENT ME!
     * @param typeMapping DOCUMENT ME!
     * @param job DOCUMENT ME!
     * @param pluginsLocation DOCUMENT ME!
     * @param pluginFilename DOCUMENT ME!
     * @param workingModeFilename DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws PluginException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public JobManager(MainController controller,
                          Document       typeMapping,
                          Document       job,
                          String         pluginsLocation,
                          String         pluginFilename,
                          String         workingModeFilename) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, JDOMException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException, PluginException {
        if ((controller == null) || (typeMapping == null) || (job == null)) {
            throw new IllegalArgumentException("Missing arguments values: controller=" + controller + " typeMapping=" + typeMapping + " job=" + job);
        }

        this.controller      = controller;
        this.typeMapping     = typeMapping;
        this.job         = job;

        initJob(controller.getApplicationProperties());

        this.pluginManager     = new PluginManager(controller, this, plugins, pluginsLocation, pluginFilename, workingModeFilename);

        // inform Observers that job has been loaded
        broadcast();
    }

    /**
     * DOCUMENT ME!
     */
    public final void broadcast() {
        setChanged();
        notifyObservers();
    }

    /**
     * Used to post a message
     *
     * @param message DOCUMENT ME!
     */
    public final void postMessage(String message) {
        controller.postMessage(message);
    }

    /**
     * Used to post Exceptions given a general Exception and Log level to the controller
     *
     * @param e DOCUMENT ME!
     * @param level DOCUMENT ME!
     */
    public final void postException(Exception e,
                                    Level     level) {
        controller.postException(e, level);
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
     * @throws Exception should only be thrown by plugins
     */
    public final void execute(Element operation) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, DriverNotFoundException, OpenConnectionException, CloseConnectionException, JDOMException, SQLException, IOException, Exception {
        String operationString = operation.getAttributeValue(XMLTags.NAME);

        // export job to xml file
        if (operationString.compareTo(OperationType.EXPORT_JOB) == 0) {
            if (job != null) {
                // exisiting plugins in plugins element are removed first and then added in correct process order with process_order attribute
                pluginManager.addPluginsToProject();
                ExportToXML.createXML(job, operation.getAttributeValue(XMLTags.FILE));
            }
        }
        // import job from xml file
        else if (operationString.compareTo(OperationType.IMPORT_JOB) == 0) {
            job         = ImportFromXML.importFile(operation.getAttributeValue(XMLTags.FILE));
            jobRoot     = job.getRootElement();

            if (jobRoot != null) {
                if (jobRoot.getChild(XMLTags.PLUGINS) != null) {
                    plugins = jobRoot.getChild(XMLTags.PLUGINS);
                    pluginManager.resetModels();
                    pluginManager.loadPluginsFromProject(plugins);
                } else {
                    throw new MissingElementException(new Element(XMLTags.PLUGINS), XMLTags.PLUGINS);
                }
            } else {
                throw new MissingElementException(new Element(XMLTags.JOB), XMLTags.JOB);
            }
            broadcast();
        }
        // create new job model
        else if (operationString.compareTo(OperationType.NEW_JOB) == 0) {
            pluginManager.resetModels();
        }
        // add plugin to plugin chain
        else if (operationString.compareTo(OperationType.ADD_PLUGIN) == 0) {
            pluginManager.addPluginToExecute(pluginManager.getCurrentModel(), Integer.parseInt(operation.getAttributeValue(XMLTags.INDEX)));
        }
        // import plugin file
        else if (operationString.compareTo(OperationType.IMPORT_PLUGIN) == 0) {
            Element plugin = ImportFromXML.importFile(operation.getAttributeValue(XMLTags.FILE)).getRootElement();

            // title will be set by pluginGuiManager if required
            Model model = pluginManager.loadModel(plugin, "");
            controller.loadPluginGuiFromModel(model, false);
        }
        // export plugin
        else if (operationString.compareTo(OperationType.EXPORT_PLUGIN) == 0) {
            pluginManager.getCurrentModel().saveModel(operation.getAttributeValue(XMLTags.FILE));
        }
        // execute plugins
        else if (operationString.compareToIgnoreCase(OperationType.EXECUTE) == 0) {
            pluginManager.executePlugins();
            
            postMessage(controller.getResourceManager().getString("text.execute.done"));
        }
        // cancel -> interrupt thread
        else if (operationString.compareTo(OperationType.CANCEL) == 0) {
            pluginManager.interruptPlugins();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final Model getCurrentModel() {
        return pluginManager.getCurrentModel();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final Document getTypeMapping() {
        return this.typeMapping;
    }

    /**
     * DOCUMENT ME!
     *
     * @param ap DOCUMENT ME!
     */
    private void initJob(Properties ap) {
        if (job == null) {
            jobRoot = new Element(XMLTags.JOB);
            jobRoot.setAttribute(APM.APPLICATION_NAME, ap.getProperty(APM.APPLICATION_NAME));
            jobRoot.setAttribute(APM.APPLICATION_VERSION, ap.getProperty(APM.APPLICATION_VERSION));
            jobRoot.setAttribute(APM.APPLICATION_WEBSITE, ap.getProperty(APM.APPLICATION_WEBSITE));
            jobRoot.setAttribute(XMLTags.SHUTDOWN_ON_COMPLETION, "false");

            plugins = new Element(XMLTags.PLUGINS);
            jobRoot.addContent(plugins);

            job = new Document(jobRoot);
        } else {
            jobRoot = job.getRootElement();
            jobRoot.setAttribute(APM.APPLICATION_NAME, ap.getProperty(APM.APPLICATION_NAME));
            jobRoot.setAttribute(APM.APPLICATION_VERSION, ap.getProperty(APM.APPLICATION_VERSION));
            jobRoot.setAttribute(APM.APPLICATION_WEBSITE, ap.getProperty(APM.APPLICATION_WEBSITE));

            plugins = jobRoot.getChild(XMLTags.PLUGINS);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the pluginManager.
     */
    public final PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isShutdownOnCompletion() {
        return Boolean.valueOf(jobRoot.getAttributeValue(XMLTags.SHUTDOWN_ON_COMPLETION)).booleanValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @param shutdown DOCUMENT ME!
     */
    public void setShutdownOnCompletion(boolean shutdown) {
        jobRoot.setAttribute(XMLTags.SHUTDOWN_ON_COMPLETION, Boolean.toString(shutdown));
    }
}
