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
package opendbcopy.plugin.schemageneration.model;

import opendbcopy.config.OperationType;
import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;

import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.database.DatabaseModelReader;
import opendbcopy.plugin.model.database.dependency.Mapper;
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
public class SchemagenerationModel extends DatabaseModel {
    private Element sourceStatistics;
    private Element destinationStatistics;
    private boolean source_db_connection_successful = false;
    private boolean destination_db_connection_successful = false;

    /**
     * Creates a new SchemagenerationModel object.
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
    public SchemagenerationModel(MainController controller,
                                Element        pluginElement) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, JDOMException, IOException {
        super(controller, pluginElement);
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
     */
    public void execute(Element operation) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, DriverNotFoundException, OpenConnectionException, CloseConnectionException, JDOMException, SQLException, IOException, Exception {
        String operationString = operation.getAttributeValue(XMLTags.NAME);

        // test source connection
        if (operationString.compareTo(OperationType.TEST_SOURCE_CONNECTION) == 0) {
            source_db_connection_successful = DBConnection.testConnection(getSourceConnection());

            if (source_db_connection_successful && destination_db_connection_successful) {
                readDatabaseMetadata();
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

        // capture source model
        if (operationString.compareTo(OperationType.CAPTURE_SOURCE_MODEL) == 0) {
            setSourceModel(DatabaseModelReader.readModel(getSourceDb()));

            // set elements process=true as ModelReader does not do this job
            if (getDbMode() == SINGLE_MODE) {
                setElementsProcessRecursively(getSourceModel(), true);
            }

            // copy source model into destination model
            setDestinationModel((Element) getSourceModel().clone());
            
            Mapper mapper = new Mapper(this);
            mapper.createInitialMapping();
            mapper.findInitalMatches();

            broadcast();
        }
    }
}
