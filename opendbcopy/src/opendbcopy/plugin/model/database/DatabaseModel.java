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
package opendbcopy.plugin.model.database;

import opendbcopy.config.OperationType;
import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;

import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;

import opendbcopy.plugin.model.*;
import opendbcopy.plugin.model.database.dependency.Dependency;
import opendbcopy.plugin.model.database.dependency.Mapper;
import opendbcopy.plugin.model.database.exception.DependencyNotSolvableException;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;
import opendbcopy.plugin.model.exception.UnsupportedAttributeValueException;

import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;

import java.sql.SQLException;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class DatabaseModel extends Model {
    public final int SINGLE_MODE = 1;
    public final int DUAL_MODE = 2;
    private Mapper   mapper;
    private Element  sourceDb;
    private Element  sourceDriver;
    private Element  sourceMetadata;
    private Element  sourceConnection;
    private Element  sourceCatalog;
    private Element  sourceSchema;
    private Element  sourceTablePattern;
    private Element  sourceModel;
    private Element  destinationDb;
    private Element  destinationDriver;
    private Element  destinationMetadata;
    private Element  destinationConnection;
    private Element  destinationCatalog;
    private Element  destinationSchema;
    private Element  destinationTablePattern;
    private Element  destinationModel;
    private Element  mapping;
    private Element  filter;
    private Element  stringFilterTrim;
    private Element  stringFilterRemoveIntermediateWhitespaces;
    private Element  stringFilterSetNull;
    private boolean  source_db_connection_successful = false;
    private boolean  destination_db_connection_successful = false;

    /**
     * Creates a new DatabasePluginModel object.
     *
     * @param pluginElement DOCUMENT ME!
     * @param encoding DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws JDOMException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public DatabaseModel(Element pluginElement,
                         String  encoding) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, JDOMException, IOException {
        super(pluginElement, encoding);

        loadExistingElements();
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
            setSourceModel(DatabaseModelReader.readModel(getSourceDb(), operation));

            // set elements process=true as ModelReader does not do this job
            if (getDbMode() == SINGLE_MODE) {
                setElementsProcessRecursively(getSourceModel(), true);
            }

            broadcast();
        }

        // capture destination model
        if (operationString.compareTo(OperationType.CAPTURE_DESTINATION_MODEL) == 0) {
            setDestinationModel(DatabaseModelReader.readModel(getDestinationDb(), operation));
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
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws DriverNotFoundException DOCUMENT ME!
     * @throws OpenConnectionException DOCUMENT ME!
     * @throws CloseConnectionException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     */
    private void readDatabaseMetadata() throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, DriverNotFoundException, OpenConnectionException, CloseConnectionException, SQLException {
        if (getDbMode() == DUAL_MODE) {
            if ((getSourceConnection().getAttributes().size() > 0) && (getDestinationConnection().getAttributes().size() > 0)) {
                if ((getSourceMetadata().getChildren().size() == 0) && (getDestinationMetadata().getChildren().size() == 0)) {
                    DatabaseModelReader.readDatabasesMetaData(this);
                }
            }
        } else {
            if (getSourceConnection().getAttributes().size() > 0) {
                if (getSourceMetadata().getChildren().size() == 0) {
                    DatabaseModelReader.readDatabasesMetaData(this);
                }
            }
        }
    }

    /**
     * tries to find further mappings for given source table makes sense if mapping has been changed manually
     *
     * @param sourceTableName
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void checkForMappings(String sourceTableName) throws MissingElementException {
        mapper.checkForMappings(sourceTableName);
    }

    /**
     * delete an existing table filter
     *
     * @param tableName (source table)
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void deleteTableFilter(String tableName) throws MissingElementException {
        Element tableFilter = getTableFilter(tableName);

        if (tableFilter != null) {
            filter.removeContent(tableFilter);
        }
    }

    /**
     * opendbcopy can be used in SINGLE or DUAL Database mode.
     *
     * @return 1 if dbMode = SINGLE_MODE, 2 if dbMode = DUAL_MODE
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     */
    public final int getDbMode() throws MissingElementException, MissingAttributeException, UnsupportedAttributeValueException {
        if ((sourceDb != null) && (destinationDb != null)) {
            return DUAL_MODE;
        } else {
            return SINGLE_MODE;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getDestinationDb() throws MissingElementException {
        return getElement(destinationDb, "destinationDb");
    }

    /**
     * get destination catalog name
     *
     * @return destination catalog name
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public final String getDestinationCatalog() throws MissingElementException, MissingAttributeException {
        return getAttributeValue(getElement(destinationCatalog, "destinationCatalog"), XMLTags.VALUE);
    }

    /**
     * It may be that the catalog separator could not be read by ModelReader. If so set it to "."
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public String getDestinationCatalogSeparator() throws MissingElementException {
        if (getDestinationMetadata().getChild(XMLTags.CATALOG_SEPARATOR) != null) {
            return getDestinationMetadata().getChild(XMLTags.CATALOG_SEPARATOR).getAttributeValue(XMLTags.VALUE);
        } else {
            Element catalogSeparator = getDestinationMetadata().getChild(XMLTags.CATALOG_SEPARATOR);
            catalogSeparator.setAttribute(XMLTags.VALUE, ".");

            return getDestinationCatalogSeparator();
        }
    }

    /**
     * given a destination table and column name return column element
     *
     * @param tableName (destination table)
     * @param columnName (destination column)
     *
     * @return destination column
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Element getDestinationColumn(String tableName,
                                              String columnName) throws MissingElementException {
        if ((tableName == null) || (columnName == null)) {
            throw new IllegalArgumentException("Missing arguments values: tableName=" + tableName + " columnName=" + columnName);
        }

        if (getDestinationTable(tableName).getChildren(XMLTags.COLUMN).size() > 0) {
            return getElement(getDestinationTable(tableName).getChildren(XMLTags.COLUMN).iterator(), columnName, XMLTags.NAME);
        } else {
            return null;
        }
    }

    /**
     * gets all destination columns for a given destination table name
     *
     * @param tableName (destination table)
     *
     * @return List of destination columns
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final List getDestinationColumns(String tableName) throws MissingElementException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        return getDestinationTable(tableName).getChildren(XMLTags.COLUMN);
    }

    /**
     * get destination connection element
     *
     * @return destination connection element
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getDestinationConnection() throws MissingElementException {
        if (destinationConnection == null) {
            destinationConnection = getChildElement(getElement(destinationDb, "destinationDb"), XMLTags.CONNECTION);
        }

        return getElement(destinationConnection, "destinationConnection");
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public String getDestinationDatabaseName() throws MissingElementException, MissingAttributeException {
        return getAttributeValue(getChildElement(getElement(getDestinationMetadata(), "destination database metadata"), XMLTags.DB_PRODUCT_NAME), XMLTags.VALUE);
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getDestinationDriver() throws MissingElementException {
        return getElement(destinationDriver, "destinationDriver");
    }

    /**
     * DOCUMENT ME!
     *
     * @param destinationTableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final List getDestinationExportedKeys(String destinationTableName) throws MissingElementException {
        if (destinationTableName == null) {
            throw new IllegalArgumentException("Missing destinationTableName");
        }

        return getKeys(getDestinationTable(destinationTableName), XMLTags.EXPORTED_KEY);
    }

    /**
     * DOCUMENT ME!
     *
     * @param destinationTableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final List getDestinationImportedKeys(String destinationTableName) throws MissingElementException {
        if (destinationTableName == null) {
            throw new IllegalArgumentException("Missing destinationTableName");
        }

        return getKeys(getDestinationTable(destinationTableName), XMLTags.IMPORTED_KEY);
    }

    /**
     * DOCUMENT ME!
     *
     * @param destinationTableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final List getDestinationIndexes(String destinationTableName) throws MissingElementException {
        if (destinationTableName == null) {
            throw new IllegalArgumentException("Missing destinationTableName");
        }

        return getKeys(getDestinationTable(destinationTableName), XMLTags.INDEX);
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public Element getDestinationMetadata() throws MissingElementException {
        return getElement(destinationMetadata, "destinationMetadata");
    }

    /**
     * get destination model element
     *
     * @return destination model element
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getDestinationModel() throws MissingElementException {
        return getElement(destinationModel, "destinationModel");
    }

    /**
     * DOCUMENT ME!
     *
     * @param destinationTableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final List getDestinationPrimaryKeys(String destinationTableName) throws MissingElementException {
        if (destinationTableName == null) {
            throw new IllegalArgumentException("Missing destinationTableName");
        }

        return getKeys(getDestinationTable(destinationTableName), XMLTags.PRIMARY_KEY);
    }

    /**
     * get destination schema name
     *
     * @return destination schema name
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     */
    public final String getDestinationSchema() throws MissingElementException, MissingAttributeException {
        return getAttributeValue(getElement(destinationSchema, "destinationSchema"), XMLTags.VALUE);
    }

    /**
     * get destination table
     *
     * @param tableName (destination table)
     *
     * @return destination table
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Element getDestinationTable(String tableName) throws MissingElementException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        return getElement(getElement(destinationModel, "destinationModel").getChildren(XMLTags.TABLE).iterator(), tableName, XMLTags.NAME);
    }

    /**
     * get destination table pattern (may contain SQL wildcards)
     *
     * @return destination table pattern
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final String getDestinationTablePattern() throws MissingAttributeException, MissingElementException {
        return getAttributeValue(getElement(destinationTablePattern, "destinationTablePattern"), (XMLTags.VALUE));
    }

    /**
     * gets all destination tables
     *
     * @return List of all destination tables
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final List getDestinationTables() throws MissingElementException {
        return getElement(destinationModel, "destinationModel").getChildren(XMLTags.TABLE);
    }

    /**
     * get destination tables to process ordered by processing order (based upon foreign keys)
     *
     * @return List of destination tables
     *
     * @throws DependencyNotSolvableException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final List getDestinationTablesToProcessOrdered() throws DependencyNotSolvableException, MissingAttributeException, UnsupportedAttributeValueException, MissingElementException {
        return getTablesToProcessOrdered(destinationModel);
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getDestinationTypeInfo() throws MissingElementException {
        return getChildElement(getElement(destinationMetadata, "destinationMetadata"), XMLTags.TYPE_INFO);
    }

    /**
     * get filter element (parent of all filter elements)
     *
     * @return filter element
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getFilter() throws MissingElementException {
        return getElement(filter, "filter");
    }

    /**
     * get mapping element
     *
     * @return mapping element
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getMapping() throws MissingElementException {
        return getElement(mapping, "mapping");
    }

    /**
     * get mapping column elements to process given destination table name
     *
     * @param destinationTableName
     *
     * @return List of mapping column elements
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final List getMappingColumnsToProcessByDestinationTable(String destinationTableName) throws MissingAttributeException, MissingElementException {
        if (destinationTableName == null) {
            throw new IllegalArgumentException("Missing destinationTableName");
        }

        return getElementsToProcess(getMappingDestinationColumns(destinationTableName).iterator());
    }

    /**
     * gets a mapping table element given its source table name
     *
     * @param tableName (source table)
     *
     * @return mapping element
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Element getMappingSourceTable(String tableName) throws MissingElementException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        return getElement(getElement(mapping, "mapping").getChildren(XMLTags.TABLE).iterator(), tableName, XMLTags.SOURCE_DB);
    }

    /**
     * gets a mapping table element given its destination table name
     *
     * @param tableName (destination table)
     *
     * @return mapping element
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Element getMappingDestinationTable(String tableName) throws MissingElementException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        return getElement(getElement(mapping, "mapping").getChildren(XMLTags.TABLE).iterator(), tableName, XMLTags.DESTINATION_DB);
    }

    /**
     * gets all mapping table elements
     *
     * @return List of mapping table elements
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final List getMappingTables() throws MissingElementException {
        return getElement(mapping, "mapping").getChildren(XMLTags.TABLE);
    }

    /**
     * get mapping column element given source table and column name
     *
     * @param tableName source table
     * @param columnName source column
     *
     * @return mapping column element
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Element getMappingSourceColumn(String tableName,
                                                String columnName) throws MissingElementException {
        if ((tableName == null) || (columnName == null)) {
            throw new IllegalArgumentException("Missing arguments values: tableName=" + tableName + " columnName=" + columnName);
        }

        return getElement(getMappingSourceTable(tableName).getChildren(XMLTags.COLUMN).iterator(), columnName, XMLTags.SOURCE_DB);
    }

    /**
     * get mapping column element given destination table and column name
     *
     * @param tableName destination table
     * @param columnName destination column
     *
     * @return mapping column element
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Element getMappingDestinationColumn(String tableName,
                                                     String columnName) throws MissingElementException {
        if ((tableName == null) || (columnName == null)) {
            throw new IllegalArgumentException("Missing arguments values: tableName=" + tableName + " columnName=" + columnName);
        }

        return getElement(getMappingDestinationTable(tableName).getChildren(XMLTags.COLUMN).iterator(), columnName, XMLTags.DESTINATION_DB);
    }

    /**
     * get all mapping columns given its source table name
     *
     * @param tableName (source table)
     *
     * @return List of mapping column elements
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final List getMappingSourceColumns(String tableName) throws MissingElementException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        return getMappingSourceTable(tableName).getChildren(XMLTags.COLUMN);
    }

    /**
     * get all mapping columns given its destination table name
     *
     * @param tableName (destination table)
     *
     * @return List of mapping column elements
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final List getMappingDestinationColumns(String tableName) throws MissingElementException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        return getMappingDestinationTable(tableName).getChildren(XMLTags.COLUMN);
    }

    /**
     * returns number of source tables
     *
     * @return number of source tables
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final int getNbrSourceTables() throws MissingElementException {
        return getSourceTables().size();
    }

    /**
     * returns number of destination tables
     *
     * @return number of destination tables
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final int getNbrDestinationTables() throws MissingElementException {
        return getDestinationTables().size();
    }

    /**
     * returns the qualified source table name, e.g. "catalog_name"."tableName", or "schema_name"."tableName"
     *
     * @param tableName (source table)
     *
     * @return qualified source table name
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final String getQualifiedSourceTableName(String tableName) throws MissingAttributeException, MissingElementException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        if (getSourceCatalog().length() > 0) {
            if (getSourceDatabaseName().compareToIgnoreCase("PostgreSQL") == 0) {
                return tableName;
            } else {
                return getSourceCatalog() + getSourceCatalogSeparator() + tableName;
            }
        } else {
            if (getSourceSchema().compareTo("%") == 0) {
                return tableName;
            } else {
                return getSourceSchema() + "." + tableName;
            }
        }
    }

    /**
     * returns the qualified destination table name, e.g. "catalog_name"."tableName", or "schema_name"."tableName"
     *
     * @param tableName (destination table)
     *
     * @return qualified destination table name
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final String getQualifiedDestinationTableName(String tableName) throws MissingAttributeException, MissingElementException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        if (getDestinationCatalog().length() > 0) {
            if (getDestinationDatabaseName().compareToIgnoreCase("PostgreSQL") == 0) {
                return tableName;
            } else {
                return getDestinationCatalog() + getDestinationCatalogSeparator() + tableName;
            }
        } else {
            if (getDestinationSchema().compareTo("%") == 0) {
                return tableName;
            } else {
                return getDestinationSchema() + "." + tableName;
            }
        }
    }

    /**
     * get root element
     *
     * @return root element
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getRoot() throws MissingElementException {
        return getElement(root, "root");
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public String getSourceDatabaseName() throws MissingAttributeException, MissingElementException {
        return getAttributeValue(getChildElement(getElement(getSourceMetadata(), "source metadata"), XMLTags.DB_PRODUCT_NAME), XMLTags.VALUE);
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getSourceDb() throws MissingElementException {
        return getElement(sourceDb, "sourceDb");
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getSourceDriver() throws MissingElementException {
        return getElement(sourceDriver, "sourceDriver");
    }

    /**
     * get source catalog name
     *
     * @return source catalog name
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final String getSourceCatalog() throws MissingAttributeException, MissingElementException {
        return getAttributeValue(getElement(sourceCatalog, "sourceCatalog"), XMLTags.VALUE);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public String getSourceCatalogSeparator() throws MissingElementException {
        if (getSourceMetadata().getChild(XMLTags.CATALOG_SEPARATOR) != null) {
            return getSourceMetadata().getChild(XMLTags.CATALOG_SEPARATOR).getAttributeValue(XMLTags.VALUE);
        } else {
            Element catalogSeparator = getSourceMetadata().getChild(XMLTags.CATALOG_SEPARATOR);
            catalogSeparator.setAttribute(XMLTags.VALUE, ".");

            return getSourceCatalogSeparator();
        }
    }

    /**
     * get mapping column elements to process given source table name
     *
     * @param sourceTableName
     *
     * @return List of mapping column elements
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final List getSourceColumnsToProcess(String sourceTableName) throws MissingAttributeException, MissingElementException {
        if (sourceTableName == null) {
            throw new IllegalArgumentException("Missing sourceTableName");
        }

        return getElementsToProcess(getSourceColumns(sourceTableName).iterator());
    }

    /**
     * get source connection element
     *
     * @return source connection element
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getSourceConnection() throws MissingElementException {
        if (sourceConnection == null) {
            sourceConnection = getChildElement(getElement(sourceDb, "sourceDb"), XMLTags.CONNECTION);
        }

        return getElement(sourceConnection, "sourceConnection");
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public Element getSourceMetadata() throws MissingElementException {
        return getElement(sourceMetadata, "sourceMetadata");
    }

    /**
     * get source model element
     *
     * @return source model element
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getSourceModel() throws MissingElementException {
        return getElement(sourceModel, "sourceModel");
    }

    /**
     * get source table
     *
     * @param tableName (source table)
     *
     * @return source table if existing
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Element getSourceTable(String tableName) throws MissingElementException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        return getElement(getElement(sourceModel, "sourceModel").getChildren(XMLTags.TABLE).iterator(), tableName, XMLTags.NAME);
    }

    /**
     * get source table pattern (may contain SQL wildcards)
     *
     * @return source table pattern
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final String getSourceTablePattern() throws MissingAttributeException, MissingElementException {
        return getAttributeValue(getElement(sourceTablePattern, "sourceTablePattern"), XMLTags.VALUE);
    }

    /**
     * gets all source tables
     *
     * @return List of source tables if existing
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final List getSourceTables() throws MissingElementException {
        return getElement(sourceModel, "sourceModel").getChildren(XMLTags.TABLE);
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getSourceTypeInfo() throws MissingElementException {
        return getChildElement(getElement(sourceMetadata, "sourceMetadata"), XMLTags.TYPE_INFO);
    }

    /**
     * DOCUMENT ME!
     *
     * @param sourceTableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final List getSourcePrimaryKeys(String sourceTableName) throws MissingElementException {
        if (sourceTableName == null) {
            throw new IllegalArgumentException("Missing sourceTableName");
        }

        return getKeys(getSourceTable(sourceTableName), XMLTags.PRIMARY_KEY);
    }

    /**
     * DOCUMENT ME!
     *
     * @param sourceTableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final List getSourceImportedKeys(String sourceTableName) throws MissingElementException {
        if (sourceTableName == null) {
            throw new IllegalArgumentException("Missing sourceTableName");
        }

        return getKeys(getSourceTable(sourceTableName), XMLTags.IMPORTED_KEY);
    }

    /**
     * DOCUMENT ME!
     *
     * @param sourceTableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final List getSourceExportedKeys(String sourceTableName) throws MissingElementException {
        if (sourceTableName == null) {
            throw new IllegalArgumentException("Missing sourceTableName");
        }

        return getKeys(getSourceTable(sourceTableName), XMLTags.EXPORTED_KEY);
    }

    /**
     * DOCUMENT ME!
     *
     * @param sourceTableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final List getSourceIndexes(String sourceTableName) throws MissingElementException {
        if (sourceTableName == null) {
            throw new IllegalArgumentException("Missing sourceTableName");
        }

        return getKeys(getSourceTable(sourceTableName), XMLTags.INDEX);
    }

    /**
     * given a source table and column name return column element
     *
     * @param tableName (source table)
     * @param columnName (source column)
     *
     * @return source column
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Element getSourceColumn(String tableName,
                                         String columnName) throws MissingElementException {
        if ((tableName == null) || (columnName == null)) {
            throw new IllegalArgumentException("Missing arguments values: tableName=" + tableName + " columnName=" + columnName);
        }

        return getElement(getSourceTable(tableName).getChildren(XMLTags.COLUMN).iterator(), columnName, XMLTags.NAME);
    }

    /**
     * gets all source columns for a given source table name
     *
     * @param tableName (source table)
     *
     * @return List of source columns
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final List getSourceColumns(String tableName) throws MissingElementException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        return getSourceTable(tableName).getChildren(XMLTags.COLUMN);
    }

    /**
     * get source schema name
     *
     * @return source schema name
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final String getSourceSchema() throws MissingAttributeException, MissingElementException {
        return getAttributeValue(getElement(sourceSchema, "sourceSchema"), XMLTags.VALUE);
    }

    /**
     * get source tables to process ordered by processing order (based upon foreign keys)
     *
     * @return List of source tables
     *
     * @throws DependencyNotSolvableException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final List getSourceTablesToProcessOrdered() throws DependencyNotSolvableException, MissingAttributeException, UnsupportedAttributeValueException, MissingElementException {
        return getTablesToProcessOrdered(getElement(sourceModel, "sourceModel"));
    }

    /**
     * get string filter element
     *
     * @param filterName (see XMLTags)
     *
     * @return string filter element
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Element getStringFilter(String filterName) throws MissingElementException {
        if (filterName == null) {
            throw new IllegalArgumentException("Missing filterName");
        }

        return getElement(getStringFilters().iterator(), filterName, XMLTags.NAME);
    }

    /**
     * get string filter removeIntermediateWhitespaces
     *
     * @return string filter RemoveIntermediateWhitespaces
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getStringFilterRemoveIntermediateWhitespaces() throws MissingElementException {
        return getElement(stringFilterRemoveIntermediateWhitespaces, "stringFilterRemoveIntermediateWhitespaces");
    }

    /**
     * get all string filters
     *
     * @return List of string filters
     *
     * @throws MissingElementException NullPointerException if element(s) not available
     */
    public final List getStringFilters() throws MissingElementException {
        return getElement(filter, "filter").getChildren(XMLTags.STRING);
    }

    /**
     * get string filter setNull
     *
     * @return string filter setNull
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getStringFilterSetNull() throws MissingElementException {
        return getElement(stringFilterSetNull, "stringFilterSetNull");
    }

    /**
     * get string filter trim
     *
     * @return string filter trim
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Element getStringFilterTrim() throws MissingElementException {
        return getElement(stringFilterTrim, "stringFilterTrim");
    }

    /**
     * get all table filters
     *
     * @return List of table filter elements
     *
     * @throws MissingElementException
     */
    public final List getTableFilters() throws MissingElementException {
        return getElement(filter, "filter").getChildren(XMLTags.TABLE);
    }

    /**
     * get table filter given source table name
     *
     * @param tableName (source table)
     *
     * @return table filter element
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Element getTableFilter(String tableName) throws MissingElementException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        return getElement(getTableFilters().iterator(), tableName, XMLTags.NAME);
    }

    /**
     * DOCUMENT ME!
     *
     * @param dataType DOCUMENT ME!
     * @param database DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Element getTypeInfoByDataType(String dataType,
                                               String database) throws MissingElementException {
        if ((dataType == null) || (database == null)) {
            throw new IllegalArgumentException("Missing arguments values: dataType=" + dataType + " database=" + database);
        }

        if (database.compareToIgnoreCase(XMLTags.SOURCE_DB) == 0) {
            return getElement(getSourceTypeInfo().getChildren().iterator(), dataType.toUpperCase(), XMLTags.DATA_TYPE);
        } else if (database.compareToIgnoreCase(XMLTags.DESTINATION_DB) == 0) {
            return getElement(getDestinationTypeInfo().getChildren().iterator(), dataType.toUpperCase(), XMLTags.DATA_TYPE);
        } else {
            throw new IllegalArgumentException("Invalid argument database=" + database);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param localTypeName DOCUMENT ME!
     * @param database DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Element getTypeInfoByLocalTypeName(String localTypeName,
                                                    String database) throws MissingElementException {
        if ((localTypeName == null) || (database == null)) {
            throw new IllegalArgumentException("Missing arguments values: localTypeName=" + localTypeName + " database=" + database);
        }

        if (database.compareToIgnoreCase(XMLTags.SOURCE_DB) == 0) {
            return getElement(getSourceTypeInfo().getChildren().iterator(), localTypeName.toUpperCase(), XMLTags.LOCAL_TYPE_NAME);
        } else if (database.compareToIgnoreCase(XMLTags.DESTINATION_DB) == 0) {
            return getElement(getDestinationTypeInfo().getChildren().iterator(), localTypeName.toUpperCase(), XMLTags.LOCAL_TYPE_NAME);
        } else {
            throw new IllegalArgumentException("Invalid argument database=" + database);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName DOCUMENT ME!
     * @param database DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final Element getTypeInfoByTypeName(String typeName,
                                               String database) throws MissingElementException {
        if ((typeName == null) || (database == null)) {
            throw new IllegalArgumentException("Missing arguments values: typeName=" + typeName + " database=" + database);
        }

        if (database.compareToIgnoreCase(XMLTags.SOURCE_DB) == 0) {
            return getElement(getSourceTypeInfo().getChildren().iterator(), typeName.toUpperCase(), XMLTags.TYPE_NAME);
        } else if (database.compareToIgnoreCase(XMLTags.DESTINATION_DB) == 0) {
            return getElement(getDestinationTypeInfo().getChildren().iterator(), typeName.toUpperCase(), XMLTags.TYPE_NAME);
        } else {
            throw new IllegalArgumentException("Invalid argument database=" + database);
        }
    }

    /**
     * Returns unmapped columns for a given table
     *
     * @param tableName to get unmapped columns for
     *
     * @return List containing unmapped columns for this table
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final List getUnmappedColumns(String tableName) throws MissingElementException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        if (getMappingTables().size() > 0) {
            return mapper.getUnmappedColumns(tableName);
        } else {
            return null;
        }
    }

    /**
     * get all source table elements which are not yet mapped
     *
     * @return List of source table elements
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final List getUnmappedSourceTables() throws MissingAttributeException, MissingElementException {
        return getUnmappedElements(getElement(sourceModel, "sourceModel").getChildren(XMLTags.TABLE).iterator(), XMLTags.SOURCE_DB);
    }

    /**
     * get all destination table elements which are not yet mapped
     *
     * @return List of destination table elements
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final List getUnmappedDestinationTables() throws MissingAttributeException, MissingElementException {
        return getUnmappedElements(getElement(destinationModel, "destinationModel").getChildren(XMLTags.TABLE).iterator(), XMLTags.DESTINATION_DB);
    }

    /**
     * Automatically find out which source table belongs to which destination table Same for columns
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void setupMapping() throws MissingElementException {
        if (isSourceModelCaptured() && isDestinationModelCaptured() && !isMappingSetup()) {
            mapper = new Mapper(this);
            mapper.createInitialMapping();
            mapper.findInitalMatches();
        }
    }

    /**
     * checks if source model has already been captured
     *
     * @return true or false
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final boolean isSourceModelCaptured() throws MissingElementException {
        if (getElement(sourceModel, "sourceModel").getChildren(XMLTags.TABLE).size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * check if destination model has already been captured
     *
     * @return true or false
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final boolean isDestinationModelCaptured() throws MissingElementException {
        if (getElement(destinationModel, "destinationModel").getChildren(XMLTags.TABLE).size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * checks if mapping has already been setup
     *
     * @return true or false
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final boolean isMappingSetup() throws MissingElementException {
        if (getElement(mapping, "mapping").getChildren(XMLTags.TABLE).size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * set source schema
     *
     * @param schemaName schema name
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void setSourceSchema(String schemaName) throws MissingElementException {
        if (schemaName == null) {
            schemaName = "%";
        }

        getElement(sourceSchema, "sourceSchema").setAttribute(XMLTags.VALUE, schemaName);
    }

    /**
     * set destination schema
     *
     * @param schemaName schema name
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void setDestinationSchema(String schemaName) throws MissingElementException {
        if (schemaName == null) {
            schemaName = "%";
        }

        getElement(destinationSchema, "destinationSchema").setAttribute(XMLTags.VALUE, schemaName);
    }

    /**
     * set source catalog name
     *
     * @param catalogName source
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void setSourceCatalog(String catalogName) throws MissingElementException {
        if (catalogName == null) {
            catalogName = "";
        }

        getElement(sourceCatalog, "sourceCatalog").setAttribute(XMLTags.VALUE, catalogName);
    }

    /**
     * set destination catalog name
     *
     * @param catalogName destination
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void setDestinationCatalog(String catalogName) throws MissingElementException {
        if (catalogName == null) {
            catalogName = "";
        }

        getElement(destinationCatalog, "destinationCatalog").setAttribute(XMLTags.VALUE, catalogName);
    }

    /**
     * set source table pattern (may contain SQL wildcards)
     *
     * @param tablePattern source table
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void setSourceTablePattern(String tablePattern) throws MissingElementException {
        if (tablePattern == null) {
            throw new IllegalArgumentException("Missing tablePattern");
        }

        if (tablePattern.length() > 0) {
            getElement(sourceTablePattern, "sourceTablePattern").setAttribute(XMLTags.TABLE_PATTERN, tablePattern);
        } else {
            getElement(sourceTablePattern, "sourceTablePattern").setAttribute(XMLTags.TABLE_PATTERN, "%");
        }
    }

    /**
     * set destination table pattern (may contain SQL wildcards)
     *
     * @param tablePattern destination table
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void setDestinationTablePattern(String tablePattern) throws MissingElementException {
        if (tablePattern == null) {
            throw new IllegalArgumentException("Missing tablePattern");
        }

        if (tablePattern.length() > 0) {
            getElement(destinationTablePattern, "destinationTablePattern").setAttribute(XMLTags.TABLE_PATTERN, tablePattern);
        } else {
            getElement(destinationTablePattern, "destinationTablePattern").setAttribute(XMLTags.TABLE_PATTERN, "%");
        }
    }

    /**
     * set or reset tag PROCESS given an element
     *
     * @param element to set or reset
     * @param process true or false
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void setElementProcess(Element element,
                                        boolean process) throws MissingElementException {
        if (element == null) {
            throw new IllegalArgumentException("Missing element");
        }

        element.setAttribute(XMLTags.PROCESS, Boolean.toString(process));
    }

    /**
     * set or reset tag PROCESSED given an element
     *
     * @param element to set or reset
     * @param processed true or false
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void setElementProcessed(Element element,
                                          boolean processed) throws MissingElementException {
        if (element == null) {
            throw new IllegalArgumentException("Missing element");
        }

        element.setAttribute(XMLTags.PROCESSED, Boolean.toString(processed));
    }

    /**
     * DOCUMENT ME!
     *
     * @param elements DOCUMENT ME!
     * @param process DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void setElementsProcessRecursively(Element elements,
                                                    boolean process) throws MissingElementException {
        if (elements == null) {
            throw new IllegalArgumentException("Missing elements");
        }

        Iterator itElements = elements.getChildren().iterator();

        while (itElements.hasNext()) {
            Element element = (Element) itElements.next();

            element.setAttribute(XMLTags.PROCESS, Boolean.toString(process));

            // call recursively
            setElementsProcessRecursively(element, process);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param elements DOCUMENT ME!
     * @param processed DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void setElementsProcessedRecursively(Element elements,
                                                      boolean processed) throws MissingElementException {
        if (elements == null) {
            throw new IllegalArgumentException("Missing elements");
        }

        Iterator itElements = elements.getChildren().iterator();

        while (itElements.hasNext()) {
            Element element = (Element) itElements.next();

            element.setAttribute(XMLTags.PROCESSED, Boolean.toString(processed));

            // call recursively
            setElementsProcessedRecursively(element, processed);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param element
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void setDestinationModel(Element element) {
        if (element == null) {
            throw new IllegalArgumentException("Missing element");
        }

        destinationModel = element;
    }

    /**
     * DOCUMENT ME!
     *
     * @param element
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public final void setSourceModel(Element element) {
        if (element == null) {
            throw new IllegalArgumentException("Missing element");
        }

        sourceModel = element;
    }

    /**
     * DOCUMENT ME!
     *
     * @param element
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public void setDestinationMetadata(Element element) {
        if (element == null) {
            throw new IllegalArgumentException("Missing element");
        }

        destinationMetadata = element;
    }

    /**
     * DOCUMENT ME!
     *
     * @param element
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public void setSourceMetadata(Element element) {
        if (element == null) {
            throw new IllegalArgumentException("Missing element");
        }

        sourceMetadata = element;
    }

    /**
     * get all elements to process
     *
     * @param iterator (of all source table or mapping table elements)
     *
     * @return List of elements to be processed
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private List getElementsToProcess(Iterator iterator) throws MissingAttributeException {
        if (iterator == null) {
            throw new IllegalArgumentException("Missing iterator");
        }

        Vector  process = new Vector();
        Element element = null;

        while (iterator.hasNext()) {
            element = (Element) iterator.next();

            if (getAttributeValue(element, XMLTags.PROCESS).compareTo("true") == 0) {
                process.add(element);
            }
        }

        return process;
    }

    /**
     * DOCUMENT ME!
     *
     * @param table DOCUMENT ME!
     * @param tagType DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private final List getKeys(Element table,
                               String  tagType) {
        if ((table == null) || (tagType == null)) {
            throw new IllegalArgumentException("Missing arguments values: table=" + table + " tagType=" + tagType);
        }

        return table.getChildren(tagType);
    }

    /**
     * set or reset tag PROCESSED given an iterator
     *
     * @param iterator (source table or mapping table iterator)
     * @param processed true or false
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private void setProcessed(Iterator iterator,
                              boolean  processed) {
        if (iterator == null) {
            throw new IllegalArgumentException("Missing iterator");
        }

        Element element = null;

        while (iterator.hasNext()) {
            element = (Element) iterator.next();
            element.setAttribute(XMLTags.PROCESSED, Boolean.toString(processed));
        }
    }

    /**
     * get elements to be processed given an iterator
     *
     * @param db_element DOCUMENT ME!
     *
     * @return List of elements to be processed
     *
     * @throws DependencyNotSolvableException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private List getTablesToProcessOrdered(Element db_element) throws DependencyNotSolvableException, MissingAttributeException, UnsupportedAttributeValueException, MissingElementException {
        if (db_element == null) {
            throw new IllegalArgumentException("Missing db_element");
        }

        Dependency dependency = new Dependency(this, db_element);
        dependency.setProcessOrder();

        TreeMap  treeMap = new TreeMap();

        Iterator itTables;

        if (getDbMode() == DUAL_MODE) {
            itTables = mapping.getChildren(XMLTags.TABLE).iterator();

            while (itTables.hasNext()) {
                Element table = (Element) itTables.next();

                if (table.getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0) {
                    // required in case project has not been saved with this tag or is running for first time
                    if (table.getAttributeValue(XMLTags.PROCESSED) == null) {
                        table.setAttribute(XMLTags.PROCESSED, "false");
                    }

                    if (table.getAttributeValue(XMLTags.PROCESSED).compareTo("false") == 0) {
                        treeMap.put(Integer.valueOf(table.getAttributeValue(XMLTags.PROCESS_ORDER)), table);
                    }
                }
            }

            return new Vector(treeMap.values());
        } else {
            itTables = sourceModel.getChildren(XMLTags.TABLE).iterator();

            while (itTables.hasNext()) {
                Element table = (Element) itTables.next();

                if (table.getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0) {
                    // required in case project has not been saved with this tag or is running for first time
                    if (table.getAttributeValue(XMLTags.PROCESSED) == null) {
                        table.setAttribute(XMLTags.PROCESSED, "false");
                    }

                    if (table.getAttributeValue(XMLTags.PROCESSED).compareTo("false") == 0) {
                        treeMap.put(Integer.valueOf(table.getAttributeValue(XMLTags.PROCESS_ORDER)), table);
                    }
                }
            }

            return new Vector(treeMap.values());
        }
    }

    /**
     * get list of elements which are not yet mapped
     *
     * @param iteratorTable (table iterator)
     * @param attributeName MAPPED
     *
     * @return list of unmapped table elements
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private List getUnmappedElements(Iterator iteratorTable,
                                     String   attributeName) throws MissingAttributeException, MissingElementException {
        if ((iteratorTable == null) || (attributeName == null)) {
            throw new IllegalArgumentException("Missing arguments values iteratorTable=" + iteratorTable + " attributeName=" + attributeName);
        }

        Vector  unmappedElements = new Vector();
        Element element = null;
        String  elementName = "";
        boolean mapped = false;

        while (iteratorTable.hasNext()) {
            mapped          = false;
            element         = (Element) iteratorTable.next();
            elementName     = getAttributeValue(element, XMLTags.NAME);

            Iterator itMapping = getMappingTables().iterator();

            while (itMapping.hasNext() && !mapped) {
                if (((Element) itMapping.next()).getAttributeValue(attributeName).compareTo(elementName) == 0) {
                    mapped = true;
                }
            }

            if (!mapped) {
                unmappedElements.addElement(element);
            }

            // reset
            mapped = false;
        }

        return unmappedElements;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    private void loadExistingElements() throws MissingAttributeException, MissingElementException {
        sourceDb               = getChildElement(root, XMLTags.SOURCE_DB);
        sourceDriver           = getChildElement(sourceDb, XMLTags.DRIVER);
        sourceMetadata         = getChildElement(sourceDb, XMLTags.METADATA);
        sourceConnection       = getChildElement(sourceDb, XMLTags.CONNECTION);
        sourceCatalog          = getChildElement(sourceDb, XMLTags.CATALOG);
        sourceSchema           = getChildElement(sourceDb, XMLTags.SCHEMA);
        sourceTablePattern     = getChildElement(sourceDb, XMLTags.TABLE_PATTERN);
        sourceModel            = getChildElement(sourceDb, XMLTags.MODEL);

        if (root.getChild(XMLTags.DESTINATION_DB) != null) {
            destinationDb               = getChildElement(root, XMLTags.DESTINATION_DB);
            destinationDriver           = getChildElement(destinationDb, XMLTags.DRIVER);
            destinationMetadata         = getChildElement(destinationDb, XMLTags.METADATA);
            destinationConnection       = getChildElement(destinationDb, XMLTags.CONNECTION);
            destinationCatalog          = getChildElement(destinationDb, XMLTags.CATALOG);
            destinationSchema           = getChildElement(destinationDb, XMLTags.SCHEMA);
            destinationTablePattern     = getChildElement(destinationDb, XMLTags.TABLE_PATTERN);
            destinationModel            = getChildElement(destinationDb, XMLTags.MODEL);
        }

        if (root.getChild(XMLTags.MAPPING) != null) {
            mapping = root.getChild(XMLTags.MAPPING);
        }
    }
}
