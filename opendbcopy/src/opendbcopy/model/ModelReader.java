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

import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;

import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;

import opendbcopy.model.exception.MissingAttributeException;
import opendbcopy.model.exception.MissingElementException;
import opendbcopy.model.exception.UnsupportedAttributeValueException;
import opendbcopy.model.exception.UnsupportedJDBCActionException;

import org.apache.log4j.Logger;

import org.jdom.Element;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public abstract class ModelReader {
    private static Logger logger = Logger.getLogger(ModelReader.class.getName());

    /**
     * DOCUMENT ME!
     *
     * @param projectModel DOCUMENT ME!
     *
     * @throws UnsupportedAttributeValueException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws OpenConnectionException DOCUMENT ME!
     * @throws CloseConnectionException DOCUMENT ME!
     * @throws DriverNotFoundException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     */
    public static void readDatabasesMetaData(ProjectModel projectModel) throws UnsupportedAttributeValueException, MissingAttributeException, MissingElementException, OpenConnectionException, CloseConnectionException, DriverNotFoundException, SQLException {
        if (projectModel.getDbMode() == projectModel.DUAL_MODE) {
            if ((projectModel.getSourceConnection().getAttributes().size() > 0) && (projectModel.getDestinationConnection().getAttributes().size() > 0)) {
                readDatabaseMetaData(projectModel.getSourceConnection(), projectModel.getSourceDriver(), projectModel.getSourceMetadata());
                setDefaultCatalog(projectModel.getSourceDb());
                setDefaultSchema(projectModel.getSourceDb());

                readDatabaseMetaData(projectModel.getDestinationConnection(), projectModel.getDestinationDriver(), projectModel.getDestinationMetadata());
                setDefaultCatalog(projectModel.getDestinationDb());
                setDefaultSchema(projectModel.getDestinationDb());
            }
        } else {
            if (projectModel.getSourceConnection().getAttributes().size() > 0) {
                readDatabaseMetaData(projectModel.getSourceConnection(), projectModel.getSourceDriver(), projectModel.getSourceMetadata());
                setDefaultCatalog(projectModel.getSourceDb());
                setDefaultSchema(projectModel.getSourceDb());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param connection DOCUMENT ME!
     * @param driver DOCUMENT ME!
     * @param metadata DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws DriverNotFoundException DOCUMENT ME!
     * @throws OpenConnectionException DOCUMENT ME!
     * @throws CloseConnectionException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     */
    private static void readDatabaseMetaData(Element connection,
                                             Element driver,
                                             Element metadata) throws IllegalArgumentException, MissingAttributeException, DriverNotFoundException, OpenConnectionException, CloseConnectionException, SQLException {
        if ((connection == null) || (driver == null) || (metadata == null)) {
            throw new IllegalArgumentException("Missing arguments values: connection=" + connection + " driver=" + driver + " metadata=" + metadata);
        }

        Connection       conn = null;
        DatabaseMetaData meta = null;
        ResultSet        rs = null;

        try {
            conn     = DBConnection.getConnection(connection);
            meta     = conn.getMetaData();

            try {
                driver.setAttribute(XMLTags.NAME, meta.getDriverName());
            } catch (SQLException e) {
                logger.warn("Driver does not support reading DriverName");
            }

            try {
                driver.setAttribute(XMLTags.DRIVER_VERSION, meta.getDriverVersion());
            } catch (SQLException e) {
                logger.warn("Driver does not support reading Driver Version");
            }

            try {
                Element dbProductName = new Element(XMLTags.DB_PRODUCT_NAME);
                dbProductName.setAttribute(XMLTags.VALUE, meta.getDatabaseProductName());
                metadata.addContent(dbProductName);
            } catch (SQLException e) {
                logger.warn("Driver does not support reading Database Product Name");
            }

            try {
                Element db_product_version = new Element(XMLTags.DB_PRODUCT_VERSION);
                db_product_version.setAttribute(XMLTags.VALUE, meta.getDatabaseProductVersion());
                metadata.addContent(db_product_version);
            } catch (SQLException e) {
                logger.warn("Driver does not support reading Database Product Version");
            }

            try {
                Element catalogSeparator = new Element(XMLTags.CATALOG_SEPARATOR);
                catalogSeparator.setAttribute(XMLTags.VALUE, meta.getCatalogSeparator());
                metadata.addContent(catalogSeparator);
            } catch (SQLException e) {
                logger.warn("Driver does not support reading Catalog Separator");
            }

            try {
                Element identifierQuoteString = new Element(XMLTags.IDENTIFIER_QUOTE_STRING);
                identifierQuoteString.setAttribute(XMLTags.VALUE, meta.getIdentifierQuoteString());
                metadata.addContent(identifierQuoteString);
            } catch (SQLException e) {
                logger.warn("Driver does not support reading Identifier Quote String");
            }

            Element catalog = null;

            // read catalogs
            try {
                catalog     = new Element(XMLTags.CATALOG);

                rs = meta.getCatalogs();

                while (rs.next()) {
                    Element element = new Element(XMLTags.ELEMENT);
                    element.setAttribute(XMLTags.NAME, rs.getString(1));
                    catalog.addContent(element);
                }

                metadata.addContent(catalog);

                rs.close();
            } catch (SQLException e) {
                logger.warn("Driver does not support reading Catalogs");
                metadata.addContent(catalog);
            }

            Element schema = null;

            // read schemas
            try {
                schema     = new Element(XMLTags.SCHEMA);

                rs = meta.getSchemas();

                while (rs.next()) {
                    Element element = new Element(XMLTags.ELEMENT);
                    element.setAttribute(XMLTags.NAME, rs.getString(1));
                    schema.addContent(element);
                }

                metadata.addContent(schema);

                rs.close();
            } catch (SQLException e) {
                logger.warn("Driver does not support reading Schemas");
                metadata.addContent(schema);
            }

            Element typeInfo = new Element(XMLTags.TYPE_INFO);

            try {
                // read type info for given database
                readTypeInfo(meta, typeInfo);
                metadata.addContent(typeInfo);
            } catch (SQLException e) {
                logger.warn("Driver does not support reading TypeInfo");
                metadata.addContent(typeInfo);
            }

            DBConnection.closeConnection(conn);
        } catch (SQLException e) {
            logger.error(e.toString());

            if (conn != null) {
                DBConnection.closeConnection(conn);
            }

            throw e;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param db_element DOCUMENT ME!
     * @param operation DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws DriverNotFoundException DOCUMENT ME!
     * @throws OpenConnectionException DOCUMENT ME!
     * @throws CloseConnectionException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     */
    public static Element readModel(Element db_element,
                                    Element operation) throws IllegalArgumentException, MissingAttributeException, DriverNotFoundException, OpenConnectionException, CloseConnectionException, SQLException {
        if ((db_element == null) || (operation == null)) {
            throw new IllegalArgumentException("Missing arguments values: db_element=" + db_element + " operation=" + operation);
        }

        DatabaseMetaData metadata = null;
        Connection       conn = null;
        Element          model = null;
        Element          connection = null;

        boolean          primary_keys_enabled = true;
        boolean          foreign_keys_enabled = true;
        boolean          indexes_enabled = true;

        try {
            connection = db_element.getChild(XMLTags.CONNECTION);

            if (connection.getAttributes().size() > 0) {
                conn         = DBConnection.getConnection(connection);
                metadata     = conn.getMetaData();

                String[]  tables = { "TABLE" };
                String[]  tables_and_views = { "TABLE", "VIEW" };
                String    dbProductName = db_element.getChild(XMLTags.METADATA).getChild(XMLTags.DB_PRODUCT_NAME).getAttributeValue(XMLTags.VALUE);

                ResultSet rs = null;

                if (dbProductName.compareToIgnoreCase("ACCESS") == 0) {
                    rs = metadata.getTables(null, null, null, tables);
                } else {
                    rs = metadata.getTables(null, db_element.getChild(XMLTags.SCHEMA).getAttributeValue(XMLTags.VALUE), db_element.getChild(XMLTags.TABLE_PATTERN).getAttributeValue(XMLTags.VALUE), tables_and_views);
                }

                if (db_element.getChild(XMLTags.MODEL) != null) {
                    db_element.removeChild(XMLTags.MODEL);
                    model = new Element(XMLTags.MODEL);
                    db_element.addContent(model);
                }

                while (rs.next()) {
                    Element tableElement = new Element(XMLTags.TABLE);
                    tableElement.setAttribute(XMLTags.NAME, rs.getString("TABLE_NAME"));
                    tableElement.setAttribute(XMLTags.TABLE_TYPE, rs.getString("TABLE_TYPE"));
                    model.addContent(tableElement);
                }

                rs.close();

                // read table columns and foreign keys
                Iterator iterator = model.getChildren(XMLTags.TABLE).iterator();

                while (iterator.hasNext()) {
                    Element table = ((Element) iterator.next());
                    String  tableName = table.getAttributeValue(XMLTags.NAME);
                    readTableColumns(metadata, table, dbProductName, db_element.getChild(XMLTags.SCHEMA).getAttributeValue(XMLTags.VALUE), db_element.getChild(XMLTags.CATALOG).getAttributeValue(XMLTags.VALUE));

                    // the following methods are not mandatory
                    // 1. copy attributes from operation into this model
                    model.setAttribute(XMLTags.READ_PRIMARY_KEYS, operation.getAttributeValue(XMLTags.READ_PRIMARY_KEYS));
                    model.setAttribute(XMLTags.READ_FOREIGN_KEYS, operation.getAttributeValue(XMLTags.READ_FOREIGN_KEYS));
                    model.setAttribute(XMLTags.READ_INDEXES, operation.getAttributeValue(XMLTags.READ_INDEXES));

                    // now only read what is requested
                    if (Boolean.valueOf(model.getAttributeValue(XMLTags.READ_PRIMARY_KEYS)).booleanValue()) {
                        if (primary_keys_enabled) {
                            try {
                                readTablePrimaryKeys(metadata, table, dbProductName, db_element.getChild(XMLTags.SCHEMA).getAttributeValue(XMLTags.VALUE), db_element.getChild(XMLTags.CATALOG).getAttributeValue(XMLTags.VALUE));
                            } catch (UnsupportedJDBCActionException e) {
                                logger.warn("Driver does not support reading Primary Keys");
                                primary_keys_enabled = false;
                            }
                        }
                    }

                    if (Boolean.valueOf(model.getAttributeValue(XMLTags.READ_FOREIGN_KEYS)).booleanValue()) {
                        if (foreign_keys_enabled) {
                            try {
                                readTableForeignKeys(metadata, table, dbProductName, db_element.getChild(XMLTags.SCHEMA).getAttributeValue(XMLTags.VALUE), db_element.getChild(XMLTags.CATALOG).getAttributeValue(XMLTags.VALUE));
                            } catch (UnsupportedJDBCActionException e) {
                                logger.warn("Driver does not support reading Foreign Keys");
                                foreign_keys_enabled = false;
                            }
                        }
                    }

                    if (Boolean.valueOf(model.getAttributeValue(XMLTags.READ_INDEXES)).booleanValue()) {
                        if (indexes_enabled) {
                            try {
                                readIndexInfo(metadata, table, dbProductName, db_element.getChild(XMLTags.SCHEMA).getAttributeValue(XMLTags.VALUE), db_element.getChild(XMLTags.CATALOG).getAttributeValue(XMLTags.VALUE));
                            } catch (UnsupportedJDBCActionException e) {
                                logger.warn("Driver does not support reading Index Info");
                            }
                        }
                    }
                }

                DBConnection.closeConnection(conn);

                logger.debug("Model for Database " + db_element.getAttributeValue(XMLTags.NAME) + " url=" + db_element.getChild(XMLTags.CONNECTION).getAttributeValue(XMLTags.URL) + " successfully loaded");

                model.setAttribute(XMLTags.CAPTURE_DATE, new Date().toString());
            }
        } catch (SQLException e) {
            logger.error(e.toString());

            if (conn != null) {
                DBConnection.closeConnection(conn);
            }

            throw e;
        }

        return model;
    }

    /**
     * DOCUMENT ME!
     *
     * @param meta java.sql.DatabaseMetaData
     * @param table opendbcopy.model.DbTable
     * @param dbProductName DOCUMENT ME!
     * @param schema_pattern DOCUMENT ME!
     * @param catalogPattern DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     */
    private static void readTableColumns(DatabaseMetaData meta,
                                         Element          table,
                                         String           dbProductName,
                                         String           schema_pattern,
                                         String           catalogPattern) throws IllegalArgumentException, SQLException {
        if ((meta == null) || (table == null) || (dbProductName == null) || (schema_pattern == null) || (catalogPattern == null)) {
            throw new IllegalArgumentException("Missing arguments values: meta=" + meta + " table=" + table + " dbProductName=" + dbProductName + " schema_pattern=" + schema_pattern + " catalogPattern=" + catalogPattern);
        }

        if (dbProductName.compareToIgnoreCase("ACCESS") == 0) {
            schema_pattern = null;
        }

        if (catalogPattern.length() == 0) {
            catalogPattern = null;
        }

        ResultSet columnsResultSet = meta.getColumns(catalogPattern, schema_pattern, table.getAttributeValue(XMLTags.NAME), "%");

        while (columnsResultSet.next()) {
            Element column = new Element(XMLTags.COLUMN);
            column.setAttribute(XMLTags.NAME, transformNull(columnsResultSet.getString(XMLTags.COLUMN_NAME)));
            column.setAttribute(XMLTags.TYPE_NAME, transformNull(columnsResultSet.getString(XMLTags.TYPE_NAME)));
            column.setAttribute(XMLTags.DATA_TYPE, transformNull(columnsResultSet.getString(XMLTags.DATA_TYPE)));
            column.setAttribute(XMLTags.COLUMN_SIZE, transformNull(columnsResultSet.getString(XMLTags.COLUMN_SIZE)));
            column.setAttribute(XMLTags.DECIMAL_DIGITS, transformNull(columnsResultSet.getString(XMLTags.DECIMAL_DIGITS)));

            if (columnsResultSet.getInt(XMLTags.NULLABLE) == 1) {
                column.setAttribute(XMLTags.NULLABLE, "true");
            } else {
                column.setAttribute(XMLTags.NULLABLE, "false");
            }

            table.addContent(column);
        }

        columnsResultSet.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @param meta DOCUMENT ME!
     * @param table DOCUMENT ME!
     * @param dbProductName DOCUMENT ME!
     * @param schema_pattern DOCUMENT ME!
     * @param catalogPattern DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws UnsupportedJDBCActionException DOCUMENT ME!
     */
    private static void readTablePrimaryKeys(DatabaseMetaData meta,
                                             Element          table,
                                             String           dbProductName,
                                             String           schema_pattern,
                                             String           catalogPattern) throws IllegalArgumentException, UnsupportedJDBCActionException {
        if ((meta == null) || (table == null) || (dbProductName == null) || (catalogPattern == null)) {
            throw new IllegalArgumentException("Missing arguments values: meta=" + meta + " table=" + table + " dbProductName=" + dbProductName + " schema_pattern=" + schema_pattern + " catalogPattern=" + catalogPattern);
        }

        if (dbProductName.compareToIgnoreCase("ACCESS") != 0) {
            if (catalogPattern.length() == 0) {
                catalogPattern = null;
            }

            try {
                ResultSet primaryKeysResultSet = meta.getPrimaryKeys(catalogPattern, schema_pattern, table.getAttributeValue(XMLTags.NAME));

                while (primaryKeysResultSet.next()) {
                    Element primaryKey = new Element(XMLTags.PRIMARY_KEY);
                    setPkAttributes(table, primaryKey, primaryKeysResultSet);
                }

                primaryKeysResultSet.close();
            } catch (SQLException e) {
                throw new UnsupportedJDBCActionException(XMLTags.READ_PRIMARY_KEYS, e.getMessage(), e.getSQLState(), e.getErrorCode());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param meta DOCUMENT ME!
     * @param table DOCUMENT ME!
     * @param dbProductName DOCUMENT ME!
     * @param schema_pattern DOCUMENT ME!
     * @param catalogPattern DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws UnsupportedJDBCActionException DOCUMENT ME!
     */
    private static void readTableForeignKeys(DatabaseMetaData meta,
                                             Element          table,
                                             String           dbProductName,
                                             String           schema_pattern,
                                             String           catalogPattern) throws IllegalArgumentException, UnsupportedJDBCActionException {
        if ((meta == null) || (table == null) || (dbProductName == null) || (catalogPattern == null)) {
            throw new IllegalArgumentException("Missing arguments values: meta=" + meta + " table=" + table + " dbProductName=" + dbProductName + " schema_pattern=" + schema_pattern + " catalogPattern=" + catalogPattern);
        }

        if (dbProductName.compareToIgnoreCase("ACCESS") != 0) {
            if (catalogPattern.length() == 0) {
                catalogPattern = null;
            }

            ResultSet foreignKeysResultSet = null;

            try {
                // imported keys
                foreignKeysResultSet = meta.getImportedKeys(catalogPattern, schema_pattern, table.getAttributeValue(XMLTags.NAME));

                while (foreignKeysResultSet.next()) {
                    Element foreignKey = new Element(XMLTags.IMPORTED_KEY);
                    setFkAttributes(table, foreignKey, foreignKeysResultSet);
                }

                foreignKeysResultSet.close();

                // exported keys
                foreignKeysResultSet = meta.getExportedKeys(catalogPattern, schema_pattern, table.getAttributeValue(XMLTags.NAME));

                while (foreignKeysResultSet.next()) {
                    Element foreignKey = new Element(XMLTags.EXPORTED_KEY);
                    setFkAttributes(table, foreignKey, foreignKeysResultSet);
                }

                foreignKeysResultSet.close();
            } catch (SQLException e) {
                throw new UnsupportedJDBCActionException(XMLTags.READ_FOREIGN_KEYS, e.getMessage(), e.getSQLState(), e.getErrorCode());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param meta DOCUMENT ME!
     * @param table DOCUMENT ME!
     * @param dbProductName DOCUMENT ME!
     * @param schema_pattern DOCUMENT ME!
     * @param catalogPattern DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws UnsupportedJDBCActionException DOCUMENT ME!
     */
    private static void readIndexInfo(DatabaseMetaData meta,
                                      Element          table,
                                      String           dbProductName,
                                      String           schema_pattern,
                                      String           catalogPattern) throws IllegalArgumentException, UnsupportedJDBCActionException {
        if ((meta == null) || (table == null) || (dbProductName == null) || (catalogPattern == null)) {
            throw new IllegalArgumentException("Missing arguments values: meta=" + meta + " table=" + table + " dbProductName=" + dbProductName + " schema_pattern=" + schema_pattern + " catalogPattern=" + catalogPattern);
        }

        if (catalogPattern.length() == 0) {
            catalogPattern = null;
        }

        if (dbProductName.compareToIgnoreCase("ACCESS") == 0) {
            schema_pattern = null;
        }

        ResultSet indexResultSet = null;

        /*
         * Parameters:
        * catalog - a catalog name; must match the catalog name as it is stored in this database; "" retrieves those without a catalog; null means that the catalog name should not be used to narrow the search
        * schema - a schema name; must match the schema name as it is stored in this database; "" retrieves those without a schema; null means that the schema name should not be used to narrow the search
        * table - a table name; must match the table name as it is stored in this database
        * unique - when true, return only indices for unique values; when false, return indices regardless of whether unique or not
        * approximate - when true, result is allowed to reflect approximate or out of data values; when false, results are requested to be accurate
         */
        try {
            // only read and add indexes which are not of type statistics, which can be identified when INDEX_NAME is not null
            indexResultSet = meta.getIndexInfo(catalogPattern, schema_pattern, table.getAttributeValue(XMLTags.NAME), false, true);
        } catch (SQLException e) {
            throw new UnsupportedJDBCActionException(XMLTags.READ_INDEXES, e.getMessage(), e.getSQLState(), e.getErrorCode());
        }

        try {
            while (indexResultSet.next()) {
                Element index = new Element(XMLTags.INDEX);
                setIndexAttributes(table, index, indexResultSet);
            }

            indexResultSet.close();
        } catch (SQLException e1) {
            // ignore exceptions here because not all databases can read all types of indexes 
            logger.warn(e1.toString());

            if (indexResultSet != null) {
                try {
                    indexResultSet.close();
                } catch (SQLException e2) {
                    // ignore exceptions here because not all databases can read all types of indexes 
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param meta DOCUMENT ME!
     * @param typeInfoElement DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws UnsupportedJDBCActionException DOCUMENT ME!
     */
    private static void readTypeInfo(DatabaseMetaData meta,
                                     Element          typeInfoElement) throws IllegalArgumentException, UnsupportedJDBCActionException {
        if ((meta == null) || (typeInfoElement == null)) {
            throw new IllegalArgumentException("Missing arguments values: meta=" + meta + " typeInfoElement=" + typeInfoElement);
        }

        ResultSet typeInfoResultSet = null;

        try {
            typeInfoResultSet = meta.getTypeInfo();

            while (typeInfoResultSet.next()) {
                Element element = new Element(XMLTags.ELEMENT);
                element.setAttribute(XMLTags.TYPE_NAME, transformNull(typeInfoResultSet.getString(XMLTags.TYPE_NAME)).toUpperCase());
                element.setAttribute(XMLTags.LOCAL_TYPE_NAME, transformNull(typeInfoResultSet.getString(XMLTags.LOCAL_TYPE_NAME)).toUpperCase());
                element.setAttribute(XMLTags.DATA_TYPE, transformNull(typeInfoResultSet.getString(XMLTags.DATA_TYPE)));
                element.setAttribute(XMLTags.PRECISION, transformNull(typeInfoResultSet.getString(XMLTags.PRECISION)));
                element.setAttribute(XMLTags.LITERAL_PREFIX, transformNull(typeInfoResultSet.getString(XMLTags.LITERAL_PREFIX)));
                element.setAttribute(XMLTags.LITERAL_SUFFIX, transformNull(typeInfoResultSet.getString(XMLTags.LITERAL_SUFFIX)));

                if (typeInfoResultSet.getInt(XMLTags.NULLABLE) == 1) {
                    element.setAttribute(XMLTags.NULLABLE, "true");
                } else {
                    element.setAttribute(XMLTags.NULLABLE, "false");
                }

                element.setAttribute(XMLTags.CASE_SENSITIVE, transformNull(typeInfoResultSet.getString(XMLTags.CASE_SENSITIVE)));

                typeInfoElement.addContent(element);
            }

            typeInfoResultSet.close();
        } catch (SQLException e1) {
            if (typeInfoResultSet != null) {
                try {
                    typeInfoResultSet.close();
                } catch (SQLException e2) {
                    // bad luck
                }
            }

            throw new UnsupportedJDBCActionException("Cannot read TypeInfo", e1.getMessage(), e1.getSQLState(), e1.getErrorCode());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param table DOCUMENT ME!
     * @param primaryKey DOCUMENT ME!
     * @param primaryKeysResultSet DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    private static void setPkAttributes(Element   table,
                                        Element   primaryKey,
                                        ResultSet primaryKeysResultSet) throws SQLException {
        primaryKey.setAttribute(XMLTags.TABLE_CAT, transformNull(primaryKeysResultSet.getString(XMLTags.TABLE_CAT)));
        primaryKey.setAttribute(XMLTags.TABLE_SCHEM, transformNull(primaryKeysResultSet.getString(XMLTags.TABLE_SCHEM)));
        primaryKey.setAttribute(XMLTags.TABLE_NAME, transformNull(primaryKeysResultSet.getString(XMLTags.TABLE_NAME)));
        primaryKey.setAttribute(XMLTags.COLUMN_NAME, transformNull(primaryKeysResultSet.getString(XMLTags.COLUMN_NAME)));
        primaryKey.setAttribute(XMLTags.KEY_SEQ, transformNull(primaryKeysResultSet.getString(XMLTags.KEY_SEQ)));
        primaryKey.setAttribute(XMLTags.PK_NAME, transformNull(primaryKeysResultSet.getString(XMLTags.PK_NAME)));

        table.addContent(primaryKey);
    }

    /**
     * DOCUMENT ME!
     *
     * @param table DOCUMENT ME!
     * @param foreignKey DOCUMENT ME!
     * @param foreignKeysResultSet DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    private static void setFkAttributes(Element   table,
                                        Element   foreignKey,
                                        ResultSet foreignKeysResultSet) throws SQLException {
        foreignKey.setAttribute(XMLTags.PKTABLE_CAT, transformNull(foreignKeysResultSet.getString(XMLTags.PKTABLE_CAT)));
        foreignKey.setAttribute(XMLTags.PKTABLE_SCHEM, transformNull(foreignKeysResultSet.getString(XMLTags.PKTABLE_SCHEM)));
        foreignKey.setAttribute(XMLTags.PKTABLE_NAME, transformNull(foreignKeysResultSet.getString(XMLTags.PKTABLE_NAME)));
        foreignKey.setAttribute(XMLTags.PKCOLUMN_NAME, transformNull(foreignKeysResultSet.getString(XMLTags.PKCOLUMN_NAME)));
        foreignKey.setAttribute(XMLTags.FKTABLE_CAT, transformNull(foreignKeysResultSet.getString(XMLTags.FKTABLE_CAT)));
        foreignKey.setAttribute(XMLTags.FKTABLE_SCHEM, transformNull(foreignKeysResultSet.getString(XMLTags.FKTABLE_SCHEM)));
        foreignKey.setAttribute(XMLTags.FKTABLE_NAME, transformNull(foreignKeysResultSet.getString(XMLTags.FKTABLE_NAME)));
        foreignKey.setAttribute(XMLTags.FKCOLUMN_NAME, transformNull(foreignKeysResultSet.getString(XMLTags.FKCOLUMN_NAME)));
        foreignKey.setAttribute(XMLTags.KEY_SEQ, transformNull(foreignKeysResultSet.getString(XMLTags.KEY_SEQ)));
        foreignKey.setAttribute(XMLTags.UPDATE_RULE, transformNull(foreignKeysResultSet.getString(XMLTags.UPDATE_RULE)));
        foreignKey.setAttribute(XMLTags.DELETE_RULE, transformNull(foreignKeysResultSet.getString(XMLTags.DELETE_RULE)));
        foreignKey.setAttribute(XMLTags.FK_NAME, transformNull(foreignKeysResultSet.getString(XMLTags.FK_NAME)));
        foreignKey.setAttribute(XMLTags.PK_NAME, transformNull(foreignKeysResultSet.getString(XMLTags.PK_NAME)));
        foreignKey.setAttribute(XMLTags.DEFERRABILITY, transformNull(foreignKeysResultSet.getString(XMLTags.DEFERRABILITY)));

        table.addContent(foreignKey);
    }

    /**
     * // only read and add indexes which are not of type statistics, which can be identified when INDEX_NAME is not null
     *
     * @param table DOCUMENT ME!
     * @param index DOCUMENT ME!
     * @param indexResultSet DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    private static void setIndexAttributes(Element   table,
                                           Element   index,
                                           ResultSet indexResultSet) throws SQLException {
        String indexName = indexResultSet.getString(XMLTags.INDEX_NAME);

        if (indexName != null) {
            index.setAttribute(XMLTags.TABLE_CAT, transformNull(indexResultSet.getString(XMLTags.TABLE_CAT)));
            index.setAttribute(XMLTags.TABLE_SCHEM, transformNull(indexResultSet.getString(XMLTags.TABLE_SCHEM)));
            index.setAttribute(XMLTags.TABLE_NAME, transformNull(indexResultSet.getString(XMLTags.TABLE_NAME)));
            index.setAttribute(XMLTags.NON_UNIQUE, transformNull(indexResultSet.getString(XMLTags.NON_UNIQUE)));
            index.setAttribute(XMLTags.INDEX_QUALIFIER, transformNull(indexResultSet.getString(XMLTags.INDEX_QUALIFIER)));
            index.setAttribute(XMLTags.INDEX_NAME, transformNull(indexName));
            index.setAttribute(XMLTags.TYPE, transformNull(indexResultSet.getString(XMLTags.TYPE)));
            index.setAttribute(XMLTags.ORDINAL_POSITION, transformNull(indexResultSet.getString(XMLTags.ORDINAL_POSITION)));
            index.setAttribute(XMLTags.COLUMN_NAME, transformNull(indexResultSet.getString(XMLTags.COLUMN_NAME)));
            index.setAttribute(XMLTags.ASC_OR_DESC, transformNull(indexResultSet.getString(XMLTags.ASC_OR_DESC)));
            index.setAttribute(XMLTags.CARDINALITY, transformNull(indexResultSet.getString(XMLTags.CARDINALITY)));
            index.setAttribute(XMLTags.PAGES, transformNull(indexResultSet.getString(XMLTags.PAGES)));
            index.setAttribute(XMLTags.FILTER_CONDITION, transformNull(indexResultSet.getString(XMLTags.FILTER_CONDITION)));

            table.addContent(index);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param db_element DOCUMENT ME!
     */
    private static void setDefaultCatalog(Element db_element) {
        if ((db_element).getChild(XMLTags.METADATA).getChild(XMLTags.CATALOG) != null) {
            List catalogs = db_element.getChild(XMLTags.METADATA).getChild(XMLTags.CATALOG).getChildren(XMLTags.ELEMENT);

            if ((catalogs != null) && (db_element.getChild(XMLTags.CATALOG) != null) && (db_element.getChild(XMLTags.CONNECTION).getAttributeValue(XMLTags.URL) != null)) {
                String defaultCatalog = findCatalog(catalogs.iterator(), db_element.getChild(XMLTags.CONNECTION).getAttributeValue(XMLTags.URL));

                if ((defaultCatalog != null) && (defaultCatalog.length() > 0)) {
                    db_element.getChild(XMLTags.CATALOG).setAttribute(XMLTags.VALUE, defaultCatalog);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param db_element DOCUMENT ME!
     */
    private static void setDefaultSchema(Element db_element) {
        if ((db_element).getChild(XMLTags.METADATA).getChild(XMLTags.SCHEMA) != null) {
            List schemas = db_element.getChild(XMLTags.METADATA).getChild(XMLTags.SCHEMA).getChildren(XMLTags.ELEMENT);

            if ((schemas != null) && (db_element.getChild(XMLTags.SCHEMA) != null) && (db_element.getChild(XMLTags.CONNECTION).getAttributeValue(XMLTags.USERNAME) != null)) {
                String defaultSchema = findSchema(schemas.iterator(), db_element.getChild(XMLTags.CONNECTION).getAttributeValue(XMLTags.USERNAME));

                if ((defaultSchema != null) && (defaultSchema.length() > 0)) {
                    db_element.getChild(XMLTags.SCHEMA).setAttribute(XMLTags.VALUE, defaultSchema);
                } else if (db_element.getChild(XMLTags.SCHEMA).getAttributeValue(XMLTags.VALUE).length() == 0) {
                    db_element.getChild(XMLTags.SCHEMA).setAttribute(XMLTags.VALUE, "%");
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param itCatalogs DOCUMENT ME!
     * @param url DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static String findCatalog(Iterator itCatalogs,
                                      String   url) {
        StringTokenizer st = new StringTokenizer(url, "/");
        String          lastElement = "";

        // get last element
        while (st.hasMoreElements()) {
            lastElement = (String) st.nextElement();
        }

        while (itCatalogs.hasNext()) {
            Element catalogElement = (Element) itCatalogs.next();

            if ((catalogElement.getAttributeValue(XMLTags.NAME) != null) && (catalogElement.getAttributeValue(XMLTags.NAME).compareToIgnoreCase(lastElement) == 0)) {
                return catalogElement.getAttributeValue(XMLTags.NAME);
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param itSchemas DOCUMENT ME!
     * @param userName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static String findSchema(Iterator itSchemas,
                                     String   userName) {
        while (itSchemas.hasNext()) {
            Element schemaElement = (Element) itSchemas.next();

            if ((schemaElement.getAttributeValue(XMLTags.NAME) != null) && (schemaElement.getAttributeValue(XMLTags.NAME).compareToIgnoreCase(userName) == 0)) {
                return schemaElement.getAttributeValue(XMLTags.NAME);
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param in DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static String transformNull(String in) {
        if (in != null) {
            return in;
        } else {
            return "null";
        }
    }
}
