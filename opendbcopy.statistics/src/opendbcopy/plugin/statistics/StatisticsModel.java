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
package opendbcopy.plugin.statistics;

import opendbcopy.config.OperationType;
import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;
import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;
import opendbcopy.controller.MainController;

import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.database.DatabaseModelReader;
import opendbcopy.plugin.model.database.DbTest;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.UnsupportedAttributeValueException;

import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.sql.SQLException;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class StatisticsModel extends DatabaseModel {
    private Element sourceStatistics;
    private Element destinationStatistics;

    /**
     * Creates a new StatisticsModel object.
     *
     * @param controller DOCUMENT ME!
     * @param pluginElement DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public StatisticsModel(MainController controller,
                           Element        pluginElement) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, JDOMException, IOException {
        super(controller, pluginElement);
        loadStatistics(pluginElement);
    }

    public void execute(Element operation) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, DriverNotFoundException, OpenConnectionException, CloseConnectionException, JDOMException, SQLException, IOException, Exception {
        String operationString = operation.getAttributeValue(XMLTags.NAME);
        
        // test source connection
        if (operationString.compareTo(OperationType.TEST_SOURCE_CONNECTION) == 0) {
            source_db_connection_successful = DBConnection.testConnection(getSourceConnection());

            if (getDbMode() == DUAL_MODE) {
                if (source_db_connection_successful && destination_db_connection_successful) {
                    readDatabaseMetadata();
                }
            } else {
                if (source_db_connection_successful) {
                    readDatabaseMetadata();
                }
            }

            broadcast();
        }

        // test destination connection
        if (operationString.compareTo(OperationType.TEST_DESTINATION_CONNECTION) == 0) {
            destination_db_connection_successful = DBConnection.testConnection(getDestinationConnection());

            if (source_db_connection_successful && destination_db_connection_successful) {
                readDatabaseMetadata();
            }

            broadcast();
        }

        // read database metadata of both databases (if SINGLE_MODE just source database)
        if (operationString.compareTo(OperationType.READ_METADATA) == 0) {
            DatabaseModelReader.readDatabasesMetaData(this);
        }

        // test table filter
        if (operationString.compareTo(OperationType.TEST_TABLE_FILTER) == 0) {
            DbTest.testTableFilter(this, operation.getAttributeValue(XMLTags.TABLE));
            broadcast();
        }

        // capture source model
        if (operationString.compareTo(OperationType.CAPTURE_SOURCE_MODEL) == 0) {
            setSourceModel(DatabaseModelReader.readModel(getSourceDb()));

            // set elements process=true as ModelReader does not do this job
            if (getDbMode() == SINGLE_MODE) {
                setElementsProcessRecursively(getSourceModel(), true);
            }

            broadcast();
        }

        // capture destination model
        if (operationString.compareTo(OperationType.CAPTURE_DESTINATION_MODEL) == 0) {
            setDestinationModel(DatabaseModelReader.readModel(getDestinationDb()));
            broadcast();
        }

        // create mapping if both models are loaded and mapping not yet set up
        if (getDbMode() == DUAL_MODE) {
            if (isSourceModelCaptured() && isDestinationModelCaptured() && !isMappingSetup()) {
                setupMapping();
                broadcast();
            }
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getDestinationStatistics() throws MissingElementException {
        return getElement(destinationStatistics, "destinationStatistics");
    }

    /**
     * get statistics for source table
     *
     * @param tableName (source table)
     *
     * @return statistics element
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Element getSourceStatisticsTable(String tableName) throws MissingElementException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        return getElement(sourceStatistics.getChildren(XMLTags.TABLE).iterator(), tableName, XMLTags.NAME);
    }

    /**
     * get statistics for destination table
     *
     * @param tableName (destination table)
     *
     * @return statistics element
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Element getDestinationStatisticsTable(String tableName) throws MissingElementException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        return getElement(getElement(destinationStatistics, "destinationStatistics").getChildren(XMLTags.TABLE).iterator(), tableName, XMLTags.NAME);
    }

    /**
     * set source statistics element
     *
     * @param element
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void setSourceStatistics(Element element) {
        if (element == null) {
            throw new IllegalArgumentException("Missing element");
        }

        sourceStatistics = element;
    }

    /**
     * get source statistics element
     *
     * @return source statistics element
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getSourceStatistics() throws MissingElementException {
        return getElement(sourceStatistics, "sourceStatistics");
    }

    /**
     * DOCUMENT ME!
     *
     * @param pluginElement DOCUMENT ME!
     */
    private void loadStatistics(Element pluginElement) {
        if (pluginElement.getChild(XMLTags.SOURCE_DB).getChild(XMLTags.STATISTICS) != null) {
            sourceStatistics = pluginElement.getChild(XMLTags.SOURCE_DB).getChild(XMLTags.STATISTICS);
        } else {
            sourceStatistics = new Element(XMLTags.STATISTICS);
            pluginElement.getChild(XMLTags.SOURCE_DB).addContent(sourceStatistics);
        }

        if (pluginElement.getChild(XMLTags.DESTINATION_DB).getChild(XMLTags.STATISTICS) != null) {
            destinationStatistics = pluginElement.getChild(XMLTags.DESTINATION_DB).getChild(XMLTags.STATISTICS);
        } else {
            destinationStatistics = new Element(XMLTags.STATISTICS);
            pluginElement.getChild(XMLTags.DESTINATION_DB).addContent(destinationStatistics);
        }
    }
}
