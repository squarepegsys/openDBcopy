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
package opendbcopy.model;

import opendbcopy.config.APM;
import opendbcopy.config.OperationType;
import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;

import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;

import opendbcopy.controller.MainController;

import opendbcopy.io.ExportToXML;
import opendbcopy.io.ImportFromXML;

import opendbcopy.model.exception.MissingAttributeException;
import opendbcopy.model.exception.MissingElementException;
import opendbcopy.model.exception.UnsupportedAttributeValueException;

import opendbcopy.task.TaskExecute;

import org.apache.log4j.Level;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;

import java.sql.SQLException;

import java.util.Observable;
import java.util.Vector;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class ProjectManager extends Observable {
    private MainController controller;
    private ProjectModel   projectModel;
    private TaskExecute    taskExecute;
    private Document       typeMapping;
    private Document       drivers;
    private Vector         operationsExecuted;
    private boolean        source_db_connection_successful = false;
    private boolean        destination_db_connection_successful = false;

    /**
     * Creates a new ProjectManager object.
     *
     * @param controller DOCUMENT ME!
     * @param typeMapping DOCUMENT ME!
     * @param drivers DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public ProjectManager(MainController controller,
                          Document       typeMapping,
                          Document       drivers) throws IllegalArgumentException {
        if ((controller == null) || (typeMapping == null) || (drivers == null)) {
            throw new IllegalArgumentException("Missing arguments values: controller=" + controller + " typeMapping=" + typeMapping + " drivers=" + drivers);
        }

        this.controller             = controller;
        this.typeMapping            = typeMapping;
        this.drivers                = drivers;
        this.projectModel           = new ProjectModel(controller.getApplicationProperties());
        this.operationsExecuted     = new Vector();
    }

    /**
     * Creates a new ProjectManager object.
     *
     * @param controller DOCUMENT ME!
     * @param typeMapping DOCUMENT ME!
     * @param drivers DOCUMENT ME!
     * @param project DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public ProjectManager(MainController controller,
                          Document       typeMapping,
                          Document       drivers,
                          Document       project) throws IllegalArgumentException, UnsupportedAttributeValueException, MissingAttributeException, MissingElementException {
        if ((controller == null) || (typeMapping == null) || (drivers == null) || (project == null)) {
            throw new IllegalArgumentException("Missing arguments values: controller=" + controller + " typeMapping=" + typeMapping + " drivers=" + drivers + " project=" + project);
        }

        this.controller             = controller;
        this.typeMapping            = typeMapping;
        this.drivers                = drivers;
        this.projectModel           = new ProjectModel(controller.getApplicationProperties(), project);
        this.operationsExecuted     = new Vector();

        // inform Observers that project has been loaded
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
        operationsExecuted.add(operationString);

        // test source connection
        if (operationString.compareTo(OperationType.TEST_SOURCE) == 0) {
            source_db_connection_successful = DBConnection.testConnection(projectModel.getSourceConnection());
            readDatabaseMetadata();
            broadcast();
        }

        // test destination connection
        if (operationString.compareTo(OperationType.TEST_DESTINATION) == 0) {
            destination_db_connection_successful = DBConnection.testConnection(projectModel.getDestinationConnection());
            readDatabaseMetadata();
            broadcast();
        }

        // read database metadata of both databases (if SINGLE_MODE just source database)
        if (operationString.compareTo(OperationType.READ_METADATA) == 0) {
            ModelReader.readDatabasesMetaData(projectModel);
        }

        // test table filter
        if (operationString.compareTo(OperationType.TEST_TABLE_FILTER) == 0) {
            Test.testTableFilter(projectModel, operation.getAttributeValue(XMLTags.TABLE));
            broadcast();
        }

        // capture source model
        if (operationString.compareTo(OperationType.CAPTURE_SOURCE_MODEL) == 0) {
            projectModel.setSourceModel(ModelReader.readModel(projectModel.getSourceDb(), operation));

            // set elements process=true as ModelReader does not do this job
            if (projectModel.getDbMode() == projectModel.SINGLE_MODE) {
                projectModel.setElementsProcessRecursively(projectModel.getSourceModel(), true);
            }

            broadcast();
        }

        // capture destination model
        if (operationString.compareTo(OperationType.CAPTURE_DESTINATION_MODEL) == 0) {
            projectModel.setDestinationModel(ModelReader.readModel(projectModel.getDestinationDb(), operation));
            broadcast();
        }

        // export project to xml file
        if (operationString.compareTo(OperationType.EXPORT_PROJECT) == 0) {
            ExportToXML.createXML(projectModel.getProject(), operation.getAttributeValue(XMLTags.FILE), controller.getApplicationProperties().getProperty(APM.ENCODING));
        }

        // import project from xml file
        if (operationString.compareTo(OperationType.IMPORT_PROJECT) == 0) {
            Document doc = ImportFromXML.importFile(operation.getAttributeValue(XMLTags.FILE));
            projectModel = new ProjectModel(controller.getApplicationProperties(), doc);
            broadcast();
        }

        if (operationString.compareTo(OperationType.NEW) == 0) {
            projectModel                             = new ProjectModel(controller.getApplicationProperties());
            source_db_connection_successful          = false;
            destination_db_connection_successful     = false;
            broadcast();
        }

        // create mapping if both models are loaded and mapping not yet set up
        if (projectModel.getDbMode() == projectModel.DUAL_MODE) {
            if (projectModel.isSourceModelCaptured() && projectModel.isDestinationModelCaptured() && !projectModel.isMappingSetup()) {
                projectModel.setupMapping();
                broadcast();
            }
        }

        // execute
        if (operationString.compareToIgnoreCase(OperationType.EXECUTE) == 0) {
            // get the plugin given by parameters and add it to the project
            projectModel.setPlugin(operation.getChild(XMLTags.PLUGIN));

            // in case several operations are executed by client (do not reset flags if user cancelled last operation)
            if (getLastOperation().compareTo(OperationType.CANCEL) != 0) {
                if (projectModel.getDbMode() == projectModel.DUAL_MODE) {
                    projectModel.setElementsProcessedRecursively(projectModel.getMapping(), false);
                } else {
                    projectModel.setElementsProcessedRecursively(projectModel.getSourceModel(), false);
                }
            }

            taskExecute = new TaskExecute(this);

            if (projectModel.getRunlevel() == projectModel.RUNLEVEL_GUI) {
                // not very nice software design!
                //                this.controller.getFrame().getPanelExecute().setTaskExecute(taskExecute);
                //                taskExecute.addObserver(this.controller.getFrame().getPanelExecute());
                //                this.controller.getFrame().setSelectedTabIndex(5);
            }

            taskExecute.go();
        }

        // cancel -> interrupt thread
        if (operationString.compareTo(OperationType.CANCEL) == 0) {
            taskExecute.interrupt();
            taskExecute = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws DriverNotFoundException DOCUMENT ME!
     * @throws OpenConnectionException DOCUMENT ME!
     * @throws CloseConnectionException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     */
    private void readDatabaseMetadata() throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, DriverNotFoundException, OpenConnectionException, CloseConnectionException, SQLException {
        if (projectModel.getDbMode() == projectModel.DUAL_MODE) {
            if ((projectModel.getSourceConnection().getAttributes().size() > 0) && (projectModel.getDestinationConnection().getAttributes().size() > 0)) {
                if ((projectModel.getSourceMetadata().getChildren().size() == 0) && (projectModel.getDestinationMetadata().getChildren().size() == 0)) {
                    ModelReader.readDatabasesMetaData(projectModel);
                }
            }
        } else {
            if (projectModel.getSourceConnection().getAttributes().size() > 0) {
                if (projectModel.getSourceMetadata().getChildren().size() == 0) {
                    ModelReader.readDatabasesMetaData(projectModel);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final String getLastOperation() {
        if (operationsExecuted.size() < 2) {
            return "";
        } else {
            return (String) operationsExecuted.elementAt(operationsExecuted.size() - 2);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final String getCurrentOperation() {
        if (operationsExecuted.size() < 1) {
            return "";
        } else {
            return (String) operationsExecuted.elementAt(operationsExecuted.size() - 1);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final boolean getStatusSourceConnection() {
        return source_db_connection_successful;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final boolean getStatusDestinationConnection() {
        return destination_db_connection_successful;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final ProjectModel getProjectModel() {
        return projectModel;
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
     * @return DOCUMENT ME!
     */
    public final Document getDrivers() {
        return this.drivers;
    }
}
