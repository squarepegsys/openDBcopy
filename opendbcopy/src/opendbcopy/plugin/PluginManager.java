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

import opendbcopy.config.XMLTags;

import opendbcopy.controller.ClasspathLoader;
import opendbcopy.controller.MainController;

import opendbcopy.io.FileHandling;
import opendbcopy.io.ImportFromXML;

import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.PluginException;
import opendbcopy.plugin.model.exception.UnsupportedAttributeValueException;

import opendbcopy.resource.ResourceManager;

import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.TimeZone;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PluginManager extends Observable {
    private static final String    TIME_FORMAT = "HH:mm:ss:SSS";
    private static Logger          logger = Logger.getLogger(PluginManager.class.getName());
    private static PluginScheduler pluginScheduler;
    private MainController         controller;
    private ResourceManager        rm;
    private Model                  currentModel;
    private SimpleDateFormat       df;

    //    private HashMap                models;
    private HashMap    observers;
    private LinkedList modelsToExecute;
    private LinkedList modelsLoaded;
    private Element    plugins;
    private String     encoding;
    private boolean    done = false;
    private boolean    started = false;
    private boolean    suspended = false;
    private boolean    interrupted = false;
    private boolean    exceptionOccured = false;
    private long       executionStarted = 0;
    private int        currentPluginIndex = 0;

    /**
     * Creates a new PluginManager object.
     *
     * @param controller DOCUMENT ME!
     * @param plugins DOCUMENT ME!
     * @param pluginsLocation DOCUMENT ME!
     * @param pluginFilename DOCUMENT ME!
     * @param workingModeFilename DOCUMENT ME!
     * @param encoding DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws PluginException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public PluginManager(MainController controller,
                         Element        plugins,
                         String         pluginsLocation,
                         String         pluginFilename,
                         String         workingModeFilename,
                         String         encoding) throws FileNotFoundException, UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, JDOMException, IOException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException, PluginException {
        if ((pluginsLocation == null) || (pluginFilename == null) || (workingModeFilename == null) || (encoding == null)) {
            throw new IllegalArgumentException("Missing arguments values: pluginsLocation=" + pluginsLocation + " pluginFilename=" + pluginFilename + " workingModeFilename=" + workingModeFilename + " encoding=" + encoding);
        }

        this.controller     = controller;
        this.rm             = controller.getResourceManager();
        this.plugins        = plugins;
        this.encoding       = encoding;

        modelsLoaded        = new LinkedList();
        modelsToExecute     = new LinkedList();

        loadPlugins(pluginsLocation, pluginFilename, workingModeFilename, encoding);
        loadPluginsFromProject(plugins);

        // set time formatting
        df = new SimpleDateFormat(TIME_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
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
    public final void registerObserver(Observer observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Missing observer");
        }

        this.addObserver(observer);
    }

    /**
     * DOCUMENT ME!
     *
     * @param modelElement DOCUMENT ME!
     * @param title DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     */
    public final void loadModel(Element modelElement,
                                String  title) throws MissingAttributeException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException {
        Model model = (Model) dynamicallyLoadPluginModel(modelElement);
        model.setTitle(title);
        modelsLoaded.add(model);
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final Model getModelLoaded(int index) {
        return (Model) modelsLoaded.get(index);
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final Model getModelToExecute(int index) {
        return (Model) modelsToExecute.get(index);
    }

    /**
     * DOCUMENT ME!
     *
     * @param model DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void setCurrentModel(Model model) {
        if (model == null) {
            throw new IllegalArgumentException("Missing model");
        }

        currentModel = model;
    }

    /**
     * inserts given model at index given and shifts former model to the right of the list (index + 1)
     *
     * @param model DOCUMENT ME!
     * @param index DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void changeOrderPluginToExecute(Model model,
                                                 int   index) {
        if (model == null) {
            throw new IllegalArgumentException("Missing model");
        }

        boolean removed = modelsToExecute.remove(model);

        if (removed) {
            modelsToExecute.add(index, model);
        }

        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @param model DOCUMENT ME!
     * @param index DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void addPluginToExecute(Model model,
                                         int   index) {
        if (model == null) {
            throw new IllegalArgumentException("Missing model");
        }

        modelsToExecute.add(index, model);
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @param model DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void addPluginToExecuteLast(Model model) {
        if (model == null) {
            throw new IllegalArgumentException("Missing model");
        }

        modelsToExecute.add(model);
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     */
    public final void removePluginToExecute(int index) {
        modelsToExecute.remove(index);
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public final void executePlugins() throws PluginException {
        if ((modelsToExecute == null) || (modelsToExecute.size() == 0)) {
            throw new PluginException("No models to execute");
        }

        done                   = false;
        interrupted            = false;
        exceptionOccured       = false;
        suspended              = false;
        currentPluginIndex     = 0;

        pluginScheduler = PluginScheduler.getInstance(this, controller);

        try {
            // ensure that plugins are within project ... just in case someone wants to use it.
            addPluginsToProject();

            executionStarted = System.currentTimeMillis();

            LinkedList tempPluginsExecuteMetadata = (LinkedList) modelsToExecute.clone();

            pluginScheduler.executePlugins(tempPluginsExecuteMetadata);
        } catch (MissingAttributeException e) {
            throw new PluginException(e);
        } catch (ClassNotFoundException e) {
            throw new PluginException(e);
        } catch (IllegalAccessException e) {
            throw new PluginException(e);
        } catch (InstantiationException e) {
            throw new PluginException(e);
        } catch (InvocationTargetException e) {
            throw new PluginException(e);
        }
    }

    /**
     * Use this method to test or run a single model -> does not conflict with model chain
     *
     * @param model DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public final void executePlugin(Model model) throws PluginException {
        if (model == null) {
            throw new PluginException("No model to execute");
        }

        done                   = false;
        interrupted            = false;
        exceptionOccured       = false;
        suspended              = false;
        currentPluginIndex     = 0;

        pluginScheduler = PluginScheduler.getInstance(this, controller);

        try {
            executionStarted = System.currentTimeMillis();
            pluginScheduler.executePlugin(model);
        } catch (MissingAttributeException e) {
            throw new PluginException(e);
        } catch (ClassNotFoundException e) {
            throw new PluginException(e);
        } catch (IllegalAccessException e) {
            throw new PluginException(e);
        } catch (InstantiationException e) {
            throw new PluginException(e);
        } catch (InvocationTargetException e) {
            throw new PluginException(e);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public final void interruptPlugins() {
        if (pluginScheduler != null) {
            pluginScheduler.interruptCurrentPlugin();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param dynamicPluginModel DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     */
    private Object dynamicallyLoadPluginThread(Model dynamicPluginModel) throws MissingAttributeException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException {
        Class         dynClass = Class.forName(dynamicPluginModel.getThreadClassName());

        Constructor[] constructors = dynClass.getConstructors();

        Object[]      params = new Object[2];
        params[0]     = controller;
        params[1]     = dynamicPluginModel;

        // works as long there is only one constructor
        return constructors[0].newInstance(params);
    }

    /**
     * DOCUMENT ME!
     *
     * @param model DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private Object dynamicallyLoadPluginModel(Element model) throws MissingAttributeException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException {
        if (model == null) {
            throw new IllegalArgumentException("Missing model");
        }

        if (model.getAttributeValue(XMLTags.MODEL_CLASS) == null) {
            throw new MissingAttributeException(model, XMLTags.MODEL_CLASS);
        } else {
            Class         dynClass = Class.forName(model.getAttributeValue(XMLTags.MODEL_CLASS));

            Constructor[] constructors = dynClass.getConstructors();

            Object[]      params = new Object[2];
            params[0]     = model;
            params[1]     = encoding;

            // works as long there is only one constructor
            return constructors[0].newInstance(params);
        }
    }

    /**
     * loads all available plugins, adds libraries to classpath, adds plugin to working mode creates a clone of plugin root to avoid overwriting of
     * original configuration!
     *
     * @param pluginsLocation DOCUMENT ME!
     * @param pluginFilename DOCUMENT ME!
     * @param workingModeFilename DOCUMENT ME!
     * @param encoding DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    private void loadPlugins(String pluginsLocation,
                             String pluginFilename,
                             String workingModeFilename,
                             String encoding) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, JDOMException, FileNotFoundException, IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException {
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
                    File     pluginFile = FileHandling.getFileInDirectory(pluginDir, pluginFilename);

                    Document pluginDoc = ImportFromXML.importFile(pluginFile);

                    // clone plugin root here so that it does overwrite default plugin settings
                    Element pluginRoot = (Element) pluginDoc.getRootElement().clone();

                    if (pluginRoot.getAttributeValue(XMLTags.IDENTIFIER) == null) {
                        throw new MissingAttributeException(pluginRoot, XMLTags.IDENTIFIER);
                    }

                    File   pluginLibDir = FileHandling.getFileInDirectory(pluginDir, "lib");
                    File[] pluginJars = FileHandling.getFilesInDirectory(pluginLibDir, "jar", "opendbcopy.jar");
                    File[] pluginZips = FileHandling.getFilesInDirectory(pluginLibDir, "zip", null);
                    File[] pluginResources = FileHandling.getFilesInDirectory(pluginLibDir, "properties", null);

                    if ((pluginJars.length == 0) && (pluginZips.length == 0)) {
                        throw new RuntimeException("Missing libraries (jar or zip) for plugin class " + pluginRoot.getAttributeValue(XMLTags.IDENTIFIER));
                    }

                    for (int j = 0; j < pluginJars.length; j++) {
                        ClasspathLoader.addResourceToClasspath(pluginJars[j]);
                    }

                    for (int j = 0; j < pluginZips.length; j++) {
                        ClasspathLoader.addResourceToClasspath(pluginZips[j]);
                    }

                    for (int j = 0; j < pluginResources.length; j++) {
                        ClasspathLoader.addResourceToClasspath(pluginResources[j]);
                    }

                    // add working mode
                    controller.addWorkingModeForPlugin(ImportFromXML.importFile(workingModeFile).getRootElement(), pluginRoot);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected final void addPluginsToProject() {
        if (modelsToExecute.size() > 0) {
            // if project contains already plugin elements, remove those first so that content is ordered like process_order
            plugins.removeChildren(XMLTags.PLUGIN);

            Iterator itPlugins = modelsToExecute.iterator();

            int      processOrder = 0;

            while (itPlugins.hasNext()) {
                Model model = (Model) itPlugins.next();
                model.setProcessOrder(processOrder);

                Element pluginClone = (Element) model.getPlugin().clone();
                plugins.addContent(pluginClone.detach());
                processOrder++;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param plugins DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws ClassNotFoundException DOCUMENT ME!
     * @throws InstantiationException DOCUMENT ME!
     * @throws InvocationTargetException DOCUMENT ME!
     * @throws IllegalAccessException DOCUMENT ME!
     * @throws PluginException DOCUMENT ME!
     */
    protected final void loadPluginsFromProject(Element plugins) throws MissingAttributeException, MissingElementException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException, PluginException {
        this.plugins = plugins;

        if (plugins.getChildren(XMLTags.PLUGIN).size() > 0) {
            Iterator itPlugin = plugins.getChildren(XMLTags.PLUGIN).iterator();

            while (itPlugin.hasNext()) {
                Model model = (Model) dynamicallyLoadPluginModel((Element) itPlugin.next());

                if (model.getProcessOrder() >= 0) {
                    addPluginToExecute(model, model.getProcessOrder());
                    controller.loadWorkingModeForPlugin(model.getIdentifier(), true);
                } else {
                    throw new MissingAttributeException(model.getPlugin(), XMLTags.PROCESS_ORDER);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected final void resetModels() {
        if (plugins.getChildren(XMLTags.PLUGIN).size() > 0) {
            plugins.removeChildren(XMLTags.PLUGIN);
        }

        currentModel = null;
        modelsToExecute.clear();
    }

    /**
     * DOCUMENT ME!
     */
    protected final void resetCurrentModel() {
        currentModel = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getNbrPluginsToExecute() {
        if (modelsToExecute == null) {
            return 0;
        } else {
            return modelsToExecute.size();
        }
    }

    /**
     * Is set and only set if all plugins were executed without having thrown exceptions. Exceptions which are  caught by plugins do not necessarily
     * stop the whole plugin chain
     *
     * @return Returns the done.
     */
    public final boolean isDone() {
        return done;
    }

    /**
     * DOCUMENT ME!
     *
     * @param done The done to set.
     */
    protected final void setDone(boolean done) {
        this.done = done;

        String[] param = { df.format(new Date(System.currentTimeMillis() - executionStarted)) };
        logger.info(rm.getString("text.execute.done") + " (" + rm.getString("text.execute.time", param) + ")");
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the exceptionOccured.
     */
    public final boolean isExceptionOccured() {
        return exceptionOccured;
    }

    /**
     * DOCUMENT ME!
     *
     * @param exceptionOccured The exceptionOccured to set.
     */
    protected final void setExceptionOccured(boolean exceptionOccured) {
        this.exceptionOccured = exceptionOccured;
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the interrupted.
     */
    public final boolean isInterrupted() {
        return interrupted;
    }

    /**
     * DOCUMENT ME!
     *
     * @param interrupted The interrupted to set.
     */
    protected final void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
        logger.info(rm.getString("text.execute.interruptRequested"));
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the suspended.
     */
    public final boolean isSuspended() {
        return suspended;
    }

    /**
     * DOCUMENT ME!
     *
     * @param suspended The suspended to set.
     */
    protected final void setSuspended(boolean suspended) {
        this.suspended = suspended;
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getCurrentIndex() {
        return currentPluginIndex;
    }

    /**
     * DOCUMENT ME!
     */
    protected final void incrementCurrentIndex() {
        currentPluginIndex++;
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the currentPluginModel.
     */
    public final Model getCurrentModel() {
        return currentModel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the modelsToExecute.
     */
    public final LinkedList getModelsToExecute() {
        return modelsToExecute;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the modelsLoaded.
     */
    public final LinkedList getModelsLoaded() {
        return modelsLoaded;
    }
}
