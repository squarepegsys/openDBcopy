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
package opendbcopy.plugin.model;

import opendbcopy.config.XMLTags;

import opendbcopy.io.ExportToXML;

import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.UnsupportedAttributeValueException;

import org.jdom.Document;
import org.jdom.Element;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class Model extends Observable {
    protected HashMap registeredObservers;
    protected Element root;
    protected Element conf;
    protected Element threads;
    protected Element input;
    protected Element output;
    private String    title;
    private String    identifier;
    private String    encoding;
    private String    threadClassName;
    private String    modelClassName;
    private String    pluginStatus;
    private int       processIndex;
    private int       lengthProgressTable = 0;
    private int       lengthProgressRecord = 0;
    private int       currentProgressTable = 0;
    private int       currentProgressRecord = 0;
    private String    progressMessage;
    private HashMap   threadsMap;

    /**
     * Creates a new PluginMetadata object.
     *
     * @param root DOCUMENT ME!
     * @param encoding DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public Model(Element root,
                 String  encoding) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException {
        this.root         = root;
        this.encoding     = encoding;

        processIndex = -1; // to denote that processIndex is not yet set

        loadPlugin(root);
    }

    /**
     * Override this method in subclass if operations shall be executed on model, passed in by Controller -> ProjectManager
     *
     * @param operation DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void execute(Element operation) throws Exception {
    }

    /**
     * DOCUMENT ME!
     *
     * @param plugin DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    private void loadPlugin(Element plugin) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException {
        if (plugin == null) {
            throw new MissingElementException(new Element("plugin"), "plugin");
        }

        if ((plugin.getAttributeValue(XMLTags.IDENTIFIER) == null) || (plugin.getAttributeValue(XMLTags.IDENTIFIER).length() == 0)) {
            throw new MissingAttributeException(plugin, XMLTags.IDENTIFIER);
        } else {
            identifier = plugin.getAttributeValue(XMLTags.IDENTIFIER);
        }

        if ((plugin.getAttributeValue(XMLTags.MODEL_CLASS) == null) || (plugin.getAttributeValue(XMLTags.MODEL_CLASS).length() == 0)) {
            throw new MissingAttributeException(plugin, XMLTags.MODEL_CLASS);
        }

        if (plugin.getAttributeValue(XMLTags.PROCESS_ORDER) != null) {
            processIndex = Integer.parseInt(plugin.getAttributeValue(XMLTags.PROCESS_ORDER));
        }

        this.conf = plugin.getChild(XMLTags.CONF);

        if (plugin.getChild(XMLTags.THREADS) == null) {
            throw new MissingElementException(plugin, XMLTags.THREADS);
        } else {
            threads = plugin.getChild(XMLTags.THREADS);
        }

        if (plugin.getChild(XMLTags.THREADS).getChildren(XMLTags.THREAD).size() == 0) {
            throw new MissingElementException(plugin.getChild(XMLTags.THREADS), XMLTags.THREAD);
        }

        if (threadsMap == null) {
            threadsMap = new HashMap();
        }

        Iterator itThreads = plugin.getChild(XMLTags.THREADS).getChildren(XMLTags.THREAD).iterator();

        while (itThreads.hasNext()) {
            Element thread = (Element) itThreads.next();
            threadsMap.put(thread.getAttributeValue(XMLTags.THREAD_CLASS), thread.getAttributeValue(XMLTags.DESCRIPTION));
        }

        // not mandatory
        if (plugin.getChild(XMLTags.INPUT) != null) {
            this.input = plugin.getChild(XMLTags.INPUT);
        }

        // not mandatory
        if (plugin.getChild(XMLTags.OUTPUT) != null) {
            this.output = plugin.getChild(XMLTags.OUTPUT);
        }

        // read execution status if available
        if (plugin.getAttributeValue(XMLTags.EXECUTION_STATUS) != null) {
            pluginStatus = plugin.getAttributeValue(XMLTags.EXECUTION_STATUS);
        } else {
            pluginStatus = "";
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected final void broadcast() {
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

        if (registeredObservers == null) {
            registeredObservers = new HashMap();
        }

        if (!registeredObservers.containsKey(observer)) {
            this.addObserver(observer);
            registeredObservers.put(observer, null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param pathFilename DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public final void saveModel(String pathFilename) throws IOException {
        Document doc = new Document((Element) root.clone());
        ExportToXML.createXML(doc, pathFilename, encoding);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final boolean isInputRequired() {
        if (root.getAttributeValue(XMLTags.REQUIRES_INPUT) != null) {
            return Boolean.valueOf(root.getAttributeValue(XMLTags.REQUIRES_INPUT)).booleanValue();
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param required DOCUMENT ME!
     */
    public final void setInputRequired(boolean required) {
        root.setAttribute(XMLTags.REQUIRES_INPUT, Boolean.toString(required));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public String getWorkingMode() throws MissingAttributeException {
        return getAttributeValue(getElement(root, "root"), XMLTags.WORKING_MODE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param workingMode DOCUMENT ME!
     */
    public void setWorkingMode(String workingMode) {
        getElement(root, "root").setAttribute(XMLTags.WORKING_MODE, workingMode);
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the className.
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public String getThreadClassName() throws MissingAttributeException {
        return getAttributeValue(getElement(root, "root"), XMLTags.THREAD_CLASS);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public String getCurrentThreadDescription() throws MissingAttributeException {
        if (threads.getChildren(XMLTags.THREAD).size() > 1) {
            if (getThreadClassName() == null) {
                throw new MissingAttributeException(root, XMLTags.THREAD_CLASS);
            } else {
                Iterator itThreads = threads.getChildren(XMLTags.THREAD).iterator();

                while (itThreads.hasNext()) {
                    Element thread = (Element) itThreads.next();

                    if (getThreadClassName().compareTo(thread.getAttributeValue(XMLTags.THREAD_CLASS)) == 0) {
                        return thread.getAttributeValue(XMLTags.DESCRIPTION);
                    }
                }

                // no description found
                return null;
            }
        } else {
            return threads.getChild(XMLTags.THREAD).getAttributeValue(XMLTags.DESCRIPTION);
        }
    }

    /**
     * Can be set at runtime
     *
     * @param threadClassName DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void setThreadClassName(String threadClassName) {
        if (threadClassName == null) {
            throw new IllegalArgumentException("Missing threadClassName");
        }

        root.setAttribute(XMLTags.THREAD_CLASS, threadClassName);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public String getModelClassName() throws MissingAttributeException {
        return getAttributeValue(getElement(root, "root"), XMLTags.MODEL_CLASS);
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the conf.
     */
    public Element getConf() {
        return conf;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the plugin.
     */
    public Element getPlugin() {
        return root;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isStarted() {
        if (pluginStatus.compareTo(XMLTags.STARTED) == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void setStarted() {
        pluginStatus = XMLTags.STARTED;
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isDone() {
        if (pluginStatus.compareTo(XMLTags.DONE) == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected void setDone() {
        pluginStatus = XMLTags.DONE;
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the interrupted.
     */
    public boolean isInterrupted() {
        if (pluginStatus.compareTo(XMLTags.INTERRUPTED) == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void setInterrupted() {
        pluginStatus = XMLTags.INTERRUPTED;
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the paused.
     */
    public boolean isSuspended() {
        if (pluginStatus.compareTo(XMLTags.SUSPENDED) == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void setSuspended() {
        pluginStatus = XMLTags.SUSPENDED;
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isResumed() {
        if (pluginStatus.compareTo(XMLTags.RESUMED) == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void setResumed() {
        pluginStatus = XMLTags.RESUMED;
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isExceptionOccured() {
        if (pluginStatus.compareTo(XMLTags.EXCEPTION_OCCURED) == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void setExceptionOccured() {
        pluginStatus = XMLTags.EXCEPTION_OCCURED;
        broadcast();
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the lengthProgressRecord.
     */
    public final int getLengthProgressRecord() {
        return lengthProgressRecord;
    }

    /**
     * DOCUMENT ME!
     *
     * @param lengthProgressRecord The lengthProgressRecord to set.
     */
    public final void setLengthProgressRecord(int lengthProgressRecord) {
        this.lengthProgressRecord = lengthProgressRecord;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the lengthProgressTable.
     */
    public final int getLengthProgressTable() {
        return lengthProgressTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param lengthProgressTable The lengthProgressTable to set.
     */
    public final void setLengthProgressTable(int lengthProgressTable) {
        this.lengthProgressTable = lengthProgressTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the currentProgressRecord.
     */
    public final int getCurrentProgressRecord() {
        return currentProgressRecord;
    }

    /**
     * DOCUMENT ME!
     *
     * @param currentProgressRecord The currentProgressRecord to set.
     */
    public final void setCurrentProgressRecord(int currentProgressRecord) {
        this.currentProgressRecord = currentProgressRecord;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the currentProgressTable.
     */
    public final int getCurrentProgressTable() {
        return currentProgressTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param currentProgressTable The currentProgressTable to set.
     */
    public final void setCurrentProgressTable(int currentProgressTable) {
        this.currentProgressTable = currentProgressTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the progressMessage.
     */
    public final String getProgressMessage() {
        return progressMessage;
    }

    /**
     * DOCUMENT ME!
     *
     * @param progressMessage The progressMessage to set.
     */
    public final void setProgressMessage(String progressMessage) {
        this.progressMessage = progressMessage;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the threadsMap.
     */
    public final HashMap getThreadsMap() {
        return threadsMap;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the input.
     */
    public final Element getInput() {
        return input;
    }

    /**
     * DOCUMENT ME!
     *
     * @param input The input to set.
     */
    public final void setInput(Element input) {
        Element tempInput = (Element) input.clone();
        tempInput.setName(XMLTags.INPUT);

        if (this.input == null) {
            root.addContent(tempInput);
        } else {
            root.removeChild(XMLTags.INPUT);
            root.addContent(tempInput);
        }

        this.input = tempInput;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the output.
     */
    public final Element getOutput() {
        return output;
    }

    /**
     * DOCUMENT ME!
     *
     * @param output The output to set.
     */
    public final void setOutput(Element output) {
        Element tempOutput = (Element) output.clone();
        tempOutput.setName(XMLTags.OUTPUT);

        if (this.output == null) {
            root.addContent(tempOutput);
        } else {
            root.removeChild(XMLTags.OUTPUT);
            root.addContent(tempOutput);
        }

        this.output = tempOutput;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the processOrder.
     */
    public final int getProcessOrder() {
        return processIndex;
    }

    /**
     * DOCUMENT ME!
     *
     * @param processIndex The processOrder to set.
     */
    public final void setProcessOrder(int processIndex) {
        this.processIndex = processIndex;
        root.setAttribute(XMLTags.PROCESS_ORDER, Integer.toString(processIndex));
    }

    /**
     * get an element
     *
     * @param iterator to parse
     * @param elementName String
     * @param attributeName String
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    protected Element getElement(Iterator iterator,
                                 String   elementName,
                                 String   attributeName) {
        if ((iterator == null) || (elementName == null) || (attributeName == null)) {
            throw new IllegalArgumentException("Missing arguments to get Element (given: iterator=" + iterator + " elementName=" + elementName + " attributeName=" + attributeName);
        }

        Element element = null;

        while (iterator.hasNext()) {
            element = (Element) iterator.next();

            if (element.getAttributeValue(attributeName).compareTo(elementName) == 0) {
                return element;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param element DOCUMENT ME!
     * @param elementName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    protected Element getElement(Element element,
                                 String  elementName) {
        if ((element == null) || (elementName == null)) {
            throw new IllegalArgumentException("Missing arguments values: element=" + element + " elementName=" + elementName);
        }

        return element;
    }

    /**
     * DOCUMENT ME!
     *
     * @param parent DOCUMENT ME!
     * @param childElementName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    protected Element getChildElement(Element parent,
                                      String  childElementName) throws MissingElementException {
        if ((parent == null) || (childElementName == null)) {
            throw new IllegalArgumentException("Missing arguments values: parent=" + parent + " childElementName=" + childElementName);
        }

        Element element = parent.getChild(childElementName);

        if (element == null) {
            throw new MissingElementException(parent, "childElementName");
        }

        return element;
    }

    /**
     * DOCUMENT ME!
     *
     * @param element DOCUMENT ME!
     * @param attributeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    protected String getAttributeValue(Element element,
                                       String  attributeName) throws MissingAttributeException {
        if ((element == null) || (attributeName == null)) {
            throw new IllegalArgumentException("Missing element or attributeName values: element=" + element + " attributeName=" + attributeName);
        }

        String value = element.getAttributeValue(attributeName);

        if (value == null) {
            throw new MissingAttributeException(element, attributeName);
        }

        return value;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the identifier.
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the title.
     */
    public final String getTitle() {
        return title;
    }

    /**
     * Title is NOT stored in plugin.xml
     *
     * @param title The title to set.
     */
    public final void setTitle(String title) {
        this.title = title;
    }
}
