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
 * Revision 1.1  2004/01/09 18:11:52  iloveopensource
 * first release
 *
 * --------------------------------------------------------------------------*/
package opendbcopy.model;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import opendbcopy.config.APM;
import opendbcopy.config.XMLTags;
import opendbcopy.model.dependency.Dependency;
import opendbcopy.model.dependency.Mapper;

import org.jdom.Document;
import org.jdom.Element;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class ProjectModel {
    public final int SINGLE_MODE = 1;
    public final int DUAL_MODE = 2;
    public final int RUNLEVEL_SHELL = 1;
    public final int RUNLEVEL_GUI = 5;
    private Mapper   mapper;
    private Document project;
    private Element  root;
    private Element  sourceDb;
    private Element  sourceDriver;
    private Element  sourceMetadata;
    private Element  sourceConnection;
    private Element  sourceCatalog;
    private Element  sourceSchema;
    private Element  sourceTablePattern;
    private Element  sourceModel;
    private Element  sourceStatistics;
    private Element  destinationDb;
    private Element  destinationDriver;
    private Element  destinationMetadata;
    private Element  destinationConnection;
    private Element  destinationCatalog;
    private Element  destinationSchema;
    private Element  destinationTablePattern;
    private Element  destinationModel;
    private Element  destinationStatistics;
    private Element  mapping;
    private Element  filter;
    private Element  stringFilterTrim;
    private Element  stringFilterRemoveIntermediateWhitespaces;
    private Element  stringFilterSetNull;
    private Element  plugin;

    // Constructor creating new project
    public ProjectModel(Properties applicationProperties) {
        initProject(applicationProperties);
    }

    // Constructor using existing project
    public ProjectModel(Properties applicationProperties, Document project) throws Exception {
        initProject(applicationProperties, project);

        if (getDbMode() == DUAL_MODE) {
            mapper = new Mapper(this);
        }
    }

    /**
     * opendbcopy can be used in SINGLE or DUAL Database mode.
     *
     * @return 1 if dbMode = SINGLE_MODE, 2 if dbMode = DUAL_MODE
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final int getDbMode() throws Exception {
        if (root.getAttributeValue(XMLTags.DB_MODE).compareTo(XMLTags.SINGLE_DB) == 0) {
            return SINGLE_MODE;
        } else {
            return DUAL_MODE;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final int getRunlevel() {
        if (root.getAttributeValue(XMLTags.RUNLEVEL) != null) {
            return Integer.parseInt(root.getAttributeValue(XMLTags.RUNLEVEL));
        } else {
            return RUNLEVEL_GUI;
        }
    }

    /**
     * set the database mode
     *
     * @param db_mode 1 if dbMode = SINGLE_MODE, 2 if dbMode = DUAL_MODE
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final void setDbMode(int db_mode) throws Exception {
        switch (db_mode) {
        case SINGLE_MODE:
            root.setAttribute(XMLTags.DB_MODE, XMLTags.SINGLE_DB);

            break;

        case DUAL_MODE:
            root.setAttribute(XMLTags.DB_MODE, XMLTags.DUAL_DB);

            break;
        }
    }

    /**
     * Automatically find out which source table belongs to which destination table Same for columns
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final void setupMapping() throws Exception {
        if (isSourceModelCaptured() && isDestinationModelCaptured() && !isMappingSetup()) {
            mapper = new Mapper(this);
            mapper.createInitialMapping();
            mapper.findInitalMatches();
        }
    }

    /**
     * returns number of source tables
     *
     * @return number of source tables
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final int getNbrSourceTables() throws Exception {
        return getSourceTables().size();
    }

    /**
     * returns number of destination tables
     *
     * @return number of destination tables
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final int getNbrDestinationTables() throws Exception {
        return getDestinationTables().size();
    }

    /**
     * checks if source model has already been captured
     *
     * @return true or false
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final boolean isSourceModelCaptured() throws Exception {
        if (sourceModel.getChildren(XMLTags.TABLE).size() > 0) {
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
     * @throws Exception NullPointerException if element(s) not available
     */
    public final boolean isDestinationModelCaptured() throws Exception {
        if (destinationModel.getChildren(XMLTags.TABLE).size() > 0) {
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
     * @throws Exception NullPointerException if element(s) not available
     */
    public final boolean isMappingSetup() throws Exception {
        if (mapping.getChildren(XMLTags.TABLE).size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * tries to find further mappings for given source table makes sense if mapping has been changed manually
     *
     * @param sourceTableName
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final void checkForMappings(String sourceTableName) throws Exception {
        mapper.checkForMappings(sourceTableName);
    }

    /**
     * Returns unmapped columns for a given table
     *
     * @param tableName to get unmapped columns for
     *
     * @return List containing unmapped columns for this table
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getUnmappedColumns(String tableName) throws Exception {
        if (getMappingTables().size() > 0) {
            return mapper.getUnmappedColumns(tableName);
        } else {
            return null;
        }
    }

    /**
     * get source table
     *
     * @param tableName (source table)
     *
     * @return source table if existing
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final Element getSourceTable(String tableName) throws Exception {
        Iterator itTables = sourceModel.getChildren(XMLTags.TABLE).iterator();

        return getElement(itTables, tableName, XMLTags.NAME);
    }

    /**
     * gets all source tables
     *
     * @return List of source tables if existing
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getSourceTables() throws Exception {
        return sourceModel.getChildren(XMLTags.TABLE);
    }

    /**
     * get destination table
     *
     * @param tableName (destination table)
     *
     * @return destination table if existing
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final Element getDestinationTable(String tableName) throws Exception {
        Iterator itTables = destinationModel.getChildren(XMLTags.TABLE).iterator();

        return getElement(itTables, tableName, XMLTags.NAME);
    }

    /**
     * returns the qualified source table name, e.g. "catalog_name"."tableName", or "schema_name"."tableName"
     *
     * @param tableName (source table)
     *
     * @return qualified source table name
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final String getQualifiedSourceTableName(String tableName) throws Exception {
        if (getSourceCatalog().length() > 0) {
            return getSourceCatalog() + "." + tableName;
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
     * @throws Exception NullPointerException if element(s) not available
     */
    public final String getQualifiedDestinationTableName(String tableName) throws Exception {
        if (getDestinationCatalog().length() > 0) {
            return getDestinationCatalog() + "." + tableName;
        } else {
            if (getDestinationSchema().compareTo("%") == 0) {
                return tableName;
            } else {
                return getDestinationSchema() + "." + tableName;
            }
        }
    }

    /**
     * gets all destination tables
     *
     * @return List of all destination tables
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getDestinationTables() throws Exception {
        return destinationModel.getChildren(XMLTags.TABLE);
    }

    /**
     * given a source table and column name return column element
     *
     * @param tableName (source table)
     * @param columnName (source column)
     *
     * @return source column
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final Element getSourceColumn(String tableName,
                                         String columnName) throws Exception {
        Iterator itColumns = getSourceTable(tableName).getChildren(XMLTags.COLUMN).iterator();

        return getElement(itColumns, columnName, XMLTags.NAME);
    }

    /**
     * gets all source columns for a given source table name
     *
     * @param tableName (source table)
     *
     * @return List of source columns
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getSourceColumns(String tableName) throws Exception {
        return getSourceTable(tableName).getChildren(XMLTags.COLUMN);
    }

    /**
     * given a destination table and column name return column element
     *
     * @param tableName (destination table)
     * @param columnName (destination column)
     *
     * @return destination column
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final Element getDestinationColumn(String tableName,
                                              String columnName) throws Exception {
        Iterator itColumns = getDestinationTable(tableName).getChildren(XMLTags.COLUMN).iterator();

        return getElement(itColumns, columnName, XMLTags.NAME);
    }

    /**
     * gets all destination columns for a given destination table name
     *
     * @param tableName (destination table)
     *
     * @return List of destination columns
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getDestinationColumns(String tableName) throws Exception {
        return getDestinationTable(tableName).getChildren(XMLTags.COLUMN);
    }

    /**
     * gets a mapping table element given its source table name
     *
     * @param tableName (source table)
     *
     * @return mapping element
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final Element getMappingSourceTable(String tableName) throws Exception {
        Iterator itTables = mapping.getChildren(XMLTags.TABLE).iterator();

        return getElement(itTables, tableName, XMLTags.SOURCE_DB);
    }

    /**
     * gets a mapping table element given its destination table name
     *
     * @param tableName (destination table)
     *
     * @return mapping element
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final Element getMappingDestinationTable(String tableName) throws Exception {
        Iterator itTables = mapping.getChildren(XMLTags.TABLE).iterator();

        return getElement(itTables, tableName, XMLTags.DESTINATION_DB);
    }

    /**
     * gets all mapping table elements
     *
     * @return List of mapping table elements
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getMappingTables() throws Exception {
        return mapping.getChildren(XMLTags.TABLE);
    }

    /**
     * get mapping column element given source table and column name
     *
     * @param tableName source table
     * @param columnName source column
     *
     * @return mapping column element
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final Element getMappingSourceColumn(String tableName,
                                                String columnName) throws Exception {
        Iterator itColumns = getMappingSourceTable(tableName).getChildren(XMLTags.COLUMN).iterator();

        return getElement(itColumns, columnName, XMLTags.SOURCE_DB);
    }

    /**
     * get mapping column element given destination table and column name
     *
     * @param tableName destination table
     * @param columnName destination column
     *
     * @return mapping column element
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final Element getMappingDestinationColumn(String tableName,
                                                     String columnName) throws Exception {
        Iterator itColumns = getMappingDestinationTable(tableName).getChildren(XMLTags.COLUMN).iterator();

        return getElement(itColumns, columnName, XMLTags.DESTINATION_DB);
    }

    /**
     * get all mapping columns given its source table name
     *
     * @param tableName (source table)
     *
     * @return List of mapping column elements
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getMappingSourceColumns(String tableName) throws Exception {
        return getMappingSourceTable(tableName).getChildren(XMLTags.COLUMN);
    }

    /**
     * get all mapping columns given its destination table name
     *
     * @param tableName (destination table)
     *
     * @return List of mapping column elements
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getMappingDestinationColumns(String tableName) throws Exception {
        return getMappingDestinationTable(tableName).getChildren(XMLTags.COLUMN);
    }

    /**
     * get all source table elements which are not yet mapped
     *
     * @return List of source table elements
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getUnmappedSourceTables() throws Exception {
        Iterator itTables = sourceModel.getChildren(XMLTags.TABLE).iterator();

        return getUnmappedElements(itTables, XMLTags.SOURCE_DB);
    }

    /**
     * get all destination table elements which are not yet mapped
     *
     * @return List of destination table elements
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getUnmappedDestinationTables() throws Exception {
        Iterator itTables = destinationModel.getChildren(XMLTags.TABLE).iterator();

        return getUnmappedElements(itTables, XMLTags.DESTINATION_DB);
    }

    /**
     * get statistics for source table
     *
     * @param tableName (source table)
     *
     * @return statistics element
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final Element getSourceStatisticsTable(String tableName) throws Exception {
        Iterator itTables = sourceStatistics.getChildren(XMLTags.TABLE).iterator();

        return getElement(itTables, tableName, XMLTags.NAME);
    }

    /**
     * get statistics for destination table
     *
     * @param tableName (destination table)
     *
     * @return statistics element
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final Element getDestinationStatisticsTable(String tableName) throws Exception {
        Iterator itTables = destinationStatistics.getChildren(XMLTags.TABLE).iterator();

        return getElement(itTables, tableName, XMLTags.NAME);
    }

    /**
     * get all string filters
     *
     * @return List of string filters
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getStringFilters() throws Exception {
        return filter.getChildren(XMLTags.STRING);
    }

    /**
     * get string filter element
     *
     * @param filterName (see XMLTags)
     *
     * @return string filter element
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final Element getStringFilter(String filterName) throws Exception {
        Iterator itStringFilter = getStringFilters().iterator();

        return getElement(itStringFilter, filterName, XMLTags.NAME);
    }

    /**
     * get all table filters
     *
     * @return List of table filter elements
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getTableFilters() throws Exception {
        return filter.getChildren(XMLTags.TABLE);
    }

    /**
     * get table filter given source table name
     *
     * @param tableName (source table)
     *
     * @return table filter element
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final Element getTableFilter(String tableName) throws Exception {
        Iterator itTableFilter = getTableFilters().iterator();

        return getElement(itTableFilter, tableName, XMLTags.NAME);
    }

    /**
     * delete an existing table filter
     *
     * @param tableName (source table)
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final void deleteTableFilter(String tableName) throws Exception {
        Element tableFilter = getTableFilter(tableName);

        if (tableFilter != null) {
            filter.removeContent(tableFilter);
        }
    }

    /**
     * get the plugin element
     *
     * @return plugin element
     */
    public final Element getPlugin() {
        return plugin;
    }

    /**
     * get source schema name
     *
     * @return source schema name
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final String getSourceSchema() throws Exception {
        return sourceSchema.getAttributeValue(XMLTags.VALUE);
    }

    /**
     * set source schema
     *
     * @param schemaName schema name
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final void setSourceSchema(String schemaName) throws Exception {
    	if (schemaName != null)
        	sourceSchema.setAttribute(XMLTags.VALUE, schemaName);
    }

    /**
     * get destination schema name
     *
     * @return destination schema name
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final String getDestinationSchema() throws Exception {
        return destinationSchema.getAttributeValue(XMLTags.VALUE);
    }

    /**
     * set destination schema
     *
     * @param schemaName schema name
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final void setDestinationSchema(String schemaName) throws Exception {
    	if (schemaName != null)
        	destinationSchema.setAttribute(XMLTags.VALUE, schemaName);
    }

    /**
     * get source catalog name
     *
     * @return source catalog name
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final String getSourceCatalog() throws Exception {
        return sourceCatalog.getAttributeValue(XMLTags.VALUE);
    }

    /**
     * set source catalog name
     *
     * @param catalogName source
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final void setSourceCatalog(String catalogName) throws Exception {
    	if (catalogName != null)
        	sourceCatalog.setAttribute(XMLTags.VALUE, catalogName);
    }

    /**
     * get destination catalog name
     *
     * @return destination catalog name
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final String getDestinationCatalog() throws Exception {
        return destinationCatalog.getAttributeValue(XMLTags.VALUE);
    }

    /**
     * set destination catalog name
     *
     * @param catalogName destination
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final void setDestinationCatalog(String catalogName) throws Exception {
    	if (catalogName != null)
        	destinationCatalog.setAttribute(XMLTags.VALUE, catalogName);
    }

    /**
     * get source table pattern (may contain SQL wildcards)
     *
     * @return source table pattern
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final String getSourceTablePattern() throws Exception {
        return sourceTablePattern.getAttributeValue(XMLTags.VALUE);
    }

    /**
     * get destination table pattern (may contain SQL wildcards)
     *
     * @return destination table pattern
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final String getDestinationTablePattern() throws Exception {
        return destinationTablePattern.getAttributeValue(XMLTags.VALUE);
    }

    /**
     * set source table pattern (may contain SQL wildcards)
     *
     * @param tablePattern source table
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final void setSourceTablePattern(String tablePattern) throws Exception {
    	if (tablePattern.length() > 0)
        	sourceTablePattern.setAttribute(XMLTags.TABLE_PATTERN, tablePattern);
        else
			sourceTablePattern.setAttribute(XMLTags.TABLE_PATTERN, "%");
    }

    /**
     * set destination table pattern (may contain SQL wildcards)
     *
     * @param tablePattern destination table
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final void setDestinationTablePattern(String tablePattern) throws Exception {
		if (tablePattern.length() > 0)
	        destinationTablePattern.setAttribute(XMLTags.TABLE_PATTERN, tablePattern);
	    else
			destinationTablePattern.setAttribute(XMLTags.TABLE_PATTERN, "%");
    }

    /**
     * get source tables to process ordered by processing order (based upon foreign keys)
     *
     * @return List of source tables
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getSourceTablesToProcessOrdered() throws Exception {
        return getTablesToProcessOrdered(sourceModel);
    }

    /**
     * get destination tables to process ordered by processing order (based upon foreign keys)
     *
     * @return List of destination tables
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getDestinationTablesToProcessOrdered() throws Exception {
        return getTablesToProcessOrdered(destinationModel);
    }

    /**
     * get mapping column elements to process given source table name
     *
     * @param sourceTableName
     *
     * @return List of mapping column elements
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getSourceColumnsToProcess(String sourceTableName) throws Exception {
        Iterator itSourceColumns = getSourceColumns(sourceTableName).iterator();

        return getElementsToProcess(itSourceColumns);
    }

    /**
     * get mapping column elements to process given destination table name
     *
     * @param destinationTableName
     *
     * @return List of mapping column elements
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final List getMappingColumnsToProcessByDestinationTable(String destinationTableName) throws Exception {
        Iterator itMappingColumns = getMappingDestinationColumns(destinationTableName).iterator();

        return getElementsToProcess(itMappingColumns);
    }

    /**
     * set or reset tag PROCESS given an element
     *
     * @param element to set or reset
     * @param process true or false
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final void setElementProcess(Element element,
                                        boolean process) throws Exception {
        element.setAttribute(XMLTags.PROCESS, Boolean.toString(process));
    }

    /**
     * set or reset tag PROCESSED given an element
     *
     * @param element to set or reset
     * @param processed true or false
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    public final void setElementProcessed(Element element,
                                          boolean processed) throws Exception {
        element.setAttribute(XMLTags.PROCESSED, Boolean.toString(processed));
    }

    /**
     * DOCUMENT ME!
     *
     * @param elements DOCUMENT ME!
     * @param process DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final void setElementsProcessRecursively(Element elements,
                                                    boolean process) throws Exception {
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
     * @throws Exception DOCUMENT ME!
     */
    public final void setElementsProcessedRecursively(Element elements,
                                                      boolean processed) throws Exception {
        Iterator itElements = elements.getChildren().iterator();

        while (itElements.hasNext()) {
            Element element = (Element) itElements.next();

            element.setAttribute(XMLTags.PROCESSED, Boolean.toString(processed));

            // call recursively
            setElementsProcessedRecursively(element, processed);
        }
    }

    /**
     * set the plugin element
     *
     * @param element plugin
     */
    public final void setPlugin(Element element) {
        Element newPlugin = (Element) element.clone();

        if (plugin == null) {
            plugin = newPlugin.detach();
            root.addContent(plugin);
        } else {
            plugin = newPlugin.detach();
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
     * @throws Exception DOCUMENT ME!
     */
    public final Element getTypeInfoByTypeName(String typeName,
                                               String database) throws Exception {
        Iterator itTypeInfo;

        if (database.compareToIgnoreCase(XMLTags.SOURCE_DB) == 0) {
            itTypeInfo = getSourceTypeInfo().getChildren().iterator();
        } else {
            itTypeInfo = getDestinationTypeInfo().getChildren().iterator();
        }

        return getElement(itTypeInfo, typeName.toUpperCase(), XMLTags.TYPE_NAME);
    }

    /**
     * DOCUMENT ME!
     *
     * @param localTypeName DOCUMENT ME!
     * @param database DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final Element getTypeInfoByLocalTypeName(String localTypeName,
                                                    String database) throws Exception {
        Iterator itTypeInfo;

        if (database.compareToIgnoreCase(XMLTags.SOURCE_DB) == 0) {
            itTypeInfo = getSourceTypeInfo().getChildren().iterator();
        } else {
            itTypeInfo = getDestinationTypeInfo().getChildren().iterator();
        }

        return getElement(itTypeInfo, localTypeName.toUpperCase(), XMLTags.LOCAL_TYPE_NAME);
    }

    /**
     * DOCUMENT ME!
     *
     * @param dataType DOCUMENT ME!
     * @param database DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final Element getTypeInfoByDataType(String dataType,
                                               String database) throws Exception {
        Iterator itTypeInfo;

        if (database.compareToIgnoreCase(XMLTags.SOURCE_DB) == 0) {
            itTypeInfo = getSourceTypeInfo().getChildren().iterator();
        } else {
            itTypeInfo = getDestinationTypeInfo().getChildren().iterator();
        }

        return getElement(itTypeInfo, dataType, XMLTags.DATA_TYPE);
    }

    /**
     * get destination connection element
     *
     * @return destination connection element
     */
    public final Element getDestinationConnection() {
        return destinationConnection;
    }

    /**
     * get destination model element
     *
     * @return destination model element
     */
    public final Element getDestinationModel() {
        return destinationModel;
    }

    /**
     * get filter element (parent of all filter elements)
     *
     * @return filter element
     */
    public final Element getFilter() {
        return filter;
    }

    /**
     * get mapping element
     *
     * @return mapping element
     */
    public final Element getMapping() {
        return mapping;
    }

    /**
     * get project Document
     *
     * @return project Document
     */
    public final Document getProject() {
        return project;
    }

    /**
     * get root element
     *
     * @return root element
     */
    public final Element getRoot() {
        return root;
    }

    /**
     * get source connection element
     *
     * @return source connection element
     */
    public final Element getSourceConnection() {
        return sourceConnection;
    }

    /**
     * get source model element
     *
     * @return source model element
     */
    public final Element getSourceModel() {
        return sourceModel;
    }

    /**
     * get source statistics element
     *
     * @return source statistics element
     */
    public final Element getSourceStatistics() {
        return sourceStatistics;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public final Element getDestinationStatistics() {
        return destinationStatistics;
    }

    /**
     * get string filter removeIntermediateWhitespaces
     *
     * @return string filter RemoveIntermediateWhitespaces
     */
    public final Element getStringFilterRemoveIntermediateWhitespaces() {
        return stringFilterRemoveIntermediateWhitespaces;
    }

    /**
     * get string filter setNull
     *
     * @return string filter setNull
     */
    public final Element getStringFilterSetNull() {
        return stringFilterSetNull;
    }

    /**
     * get string filter trim
     *
     * @return string filter trim
     */
    public final Element getStringFilterTrim() {
        return stringFilterTrim;
    }

    /**
     * set source statistics element
     *
     * @param element
     */
    public final void setSourceStatistics(Element element) {
        sourceStatistics = element;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public final Element getDestinationDb() {
        return destinationDb;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public final Element getSourceDb() {
        return sourceDb;
    }

    /**
     * DOCUMENT ME!
     *
     * @param element
     */
    public final void setDestinationModel(Element element) {
        destinationModel = element;
    }

    /**
     * DOCUMENT ME!
     *
     * @param element
     */
    public final void setSourceModel(Element element) {
        sourceModel = element;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public final Element getDestinationDriver() {
        return destinationDriver;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public final Element getSourceDriver() {
        return sourceDriver;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public final Element getDestinationTypeInfo() {
        return destinationMetadata.getChild(XMLTags.TYPE_INFO);
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public final Element getSourceTypeInfo() {
        return sourceMetadata.getChild(XMLTags.TYPE_INFO);
    }

    /**
     * create a new project
     */
    private void initProject(Properties applicationProperties) {
        root = new Element(XMLTags.PROJECT);
        root.setAttribute(XMLTags.DB_MODE, XMLTags.DUAL_DB);
        root.setAttribute(APM.APPLICATION_NAME, applicationProperties.getProperty(APM.APPLICATION_NAME));
        root.setAttribute(APM.APPLICATION_VERSION, applicationProperties.getProperty(APM.APPLICATION_VERSION));
        root.setAttribute(APM.APPLICATION_WEBSITE, applicationProperties.getProperty(APM.APPLICATION_WEBSITE));
        
        project     = new Document(root);

        sourceDb     = new Element(XMLTags.SOURCE_DB);

        sourceDriver       = new Element(XMLTags.DRIVER);
        sourceMetadata     = new Element(XMLTags.METADATA);

        sourceConnection     = new Element(XMLTags.CONNECTION);

        sourceCatalog          = new Element(XMLTags.CATALOG);
        sourceSchema           = new Element(XMLTags.SCHEMA);
        sourceTablePattern     = new Element(XMLTags.TABLE_PATTERN);

        sourceCatalog.setAttribute(XMLTags.VALUE, "");
        sourceSchema.setAttribute(XMLTags.VALUE, "%");
        sourceTablePattern.setAttribute(XMLTags.VALUE, "%");

        sourceModel     = new Element(XMLTags.MODEL);

        sourceStatistics = new Element(XMLTags.STATISTICS);

        sourceDb.addContent(sourceDriver);
        sourceDb.addContent(sourceMetadata);
        sourceDb.addContent(sourceConnection);
        sourceDb.addContent(sourceCatalog);
        sourceDb.addContent(sourceSchema);
        sourceDb.addContent(sourceTablePattern);
        sourceDb.addContent(sourceModel);
        sourceDb.addContent(sourceStatistics);

        root.addContent(sourceDb);

        if (root.getAttributeValue(XMLTags.DB_MODE).compareTo(XMLTags.DUAL_DB) == 0) {
            destinationDb     = new Element(XMLTags.DESTINATION_DB);

            destinationDriver       = new Element(XMLTags.DRIVER);
            destinationMetadata     = new Element(XMLTags.METADATA);

            destinationConnection     = new Element(XMLTags.CONNECTION);

            destinationCatalog          = new Element(XMLTags.CATALOG);
            destinationSchema           = new Element(XMLTags.SCHEMA);
            destinationTablePattern     = new Element(XMLTags.TABLE_PATTERN);

            destinationCatalog.setAttribute(XMLTags.VALUE, "");
            destinationSchema.setAttribute(XMLTags.VALUE, "%");
            destinationTablePattern.setAttribute(XMLTags.VALUE, "%");

            destinationModel     = new Element(XMLTags.MODEL);

            destinationStatistics = new Element(XMLTags.STATISTICS);

            destinationDb.addContent(destinationDriver);
            destinationDb.addContent(destinationMetadata);

            destinationDb.addContent(destinationConnection);
            destinationDb.addContent(destinationCatalog);
            destinationDb.addContent(destinationSchema);
            destinationDb.addContent(destinationTablePattern);
            destinationDb.addContent(destinationModel);
            destinationDb.addContent(destinationStatistics);

            root.addContent(destinationDb);

            mapping = new Element(XMLTags.MAPPING);
            root.addContent(mapping);
        }

        filter = new Element(XMLTags.FILTER);

        root.addContent(filter);

        stringFilterTrim = new Element(XMLTags.STRING);
        stringFilterTrim.setAttribute(XMLTags.NAME, XMLTags.TRIM);
        stringFilterTrim.setAttribute(XMLTags.PROCESS, "false");

        stringFilterRemoveIntermediateWhitespaces = new Element(XMLTags.STRING);
        stringFilterRemoveIntermediateWhitespaces.setAttribute(XMLTags.NAME, XMLTags.REMOVE_INTERMEDIATE_WHITESPACES);
        stringFilterRemoveIntermediateWhitespaces.setAttribute(XMLTags.PROCESS, "false");

        stringFilterSetNull = new Element(XMLTags.STRING);
        stringFilterSetNull.setAttribute(XMLTags.NAME, XMLTags.SET_NULL);
        stringFilterSetNull.setAttribute(XMLTags.PROCESS, "false");

        filter.addContent(stringFilterTrim);
        filter.addContent(stringFilterRemoveIntermediateWhitespaces);
        filter.addContent(stringFilterSetNull);
    }

    /**
     * init Project using given project Document (Import of project)
     *
     * @param importedProject Document
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    private void initProject(Properties applicationProperties, Document importedProject) throws Exception {
        project     = new Document(importedProject.getRootElement().detach());
        root        = project.getRootElement();

        sourceDb               = root.getChild(XMLTags.SOURCE_DB);
        sourceDriver           = sourceDb.getChild(XMLTags.DRIVER);
        sourceMetadata         = sourceDb.getChild(XMLTags.METADATA);
        sourceConnection       = sourceDb.getChild(XMLTags.CONNECTION);
        sourceCatalog          = sourceDb.getChild(XMLTags.CATALOG);
        sourceSchema           = sourceDb.getChild(XMLTags.SCHEMA);
        sourceTablePattern     = sourceDb.getChild(XMLTags.TABLE_PATTERN);
        sourceModel            = sourceDb.getChild(XMLTags.MODEL);

        if (sourceDb.getChild(XMLTags.STATISTICS) != null) {
            sourceStatistics = sourceDb.getChild(XMLTags.STATISTICS);
        } else {
            sourceStatistics = new Element(XMLTags.STATISTICS);
            sourceDb.addContent(sourceStatistics);
        }

        if (root.getAttributeValue(XMLTags.DB_MODE) == null) {
            root.setAttribute(XMLTags.DB_MODE, XMLTags.DUAL_DB);
        }

        if (root.getAttributeValue(XMLTags.DB_MODE).compareTo(XMLTags.DUAL_DB) == 0) {
            destinationDb               = root.getChild(XMLTags.DESTINATION_DB);
            destinationDriver           = root.getChild(XMLTags.DRIVER);
            destinationMetadata         = destinationDb.getChild(XMLTags.METADATA);
            destinationConnection       = destinationDb.getChild(XMLTags.CONNECTION);
            destinationCatalog          = destinationDb.getChild(XMLTags.CATALOG);
            destinationSchema           = destinationDb.getChild(XMLTags.SCHEMA);
            destinationTablePattern     = destinationDb.getChild(XMLTags.TABLE_PATTERN);
            destinationModel            = destinationDb.getChild(XMLTags.MODEL);

            if (destinationDb.getChild(XMLTags.STATISTICS) != null) {
                destinationStatistics = destinationDb.getChild(XMLTags.STATISTICS);
            } else {
                destinationStatistics = new Element(XMLTags.STATISTICS);
                destinationDb.addContent(destinationStatistics);
            }

            mapping = root.getChild(XMLTags.MAPPING);
        }

        filter                                        = root.getChild(XMLTags.FILTER);
        stringFilterTrim                              = getStringFilter(XMLTags.TRIM);
        stringFilterRemoveIntermediateWhitespaces     = getStringFilter(XMLTags.REMOVE_INTERMEDIATE_WHITESPACES);
        stringFilterSetNull                           = getStringFilter(XMLTags.SET_NULL);

        if (root.getChild(XMLTags.PLUGIN) != null) {
            plugin = root.getChild(XMLTags.PLUGIN);
        }
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
     * @throws Exception NullPointerException if element(s) not available
     */
    private Element getElement(Iterator iterator,
                               String   elementName,
                               String   attributeName) throws Exception {
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
     * get all elements to process
     *
     * @param iterator (of all source table or mapping table elements)
     *
     * @return List of elements to be processed
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    private List getElementsToProcess(Iterator iterator) throws Exception {
        Vector  process = new Vector();
        Element element = null;

        while (iterator.hasNext()) {
            element = (Element) iterator.next();

            if (element.getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0) {
                process.add(element);
            }
        }

        return process;
    }

    /**
     * set or reset tag PROCESSED given an iterator
     *
     * @param iterator (source table or mapping table iterator)
     * @param processed true or false
     *
     * @throws Exception NullPointerException if element(s) not available
     */
    private void setProcessed(Iterator iterator,
                              boolean  processed) throws Exception {
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
     * @throws Exception NullPointerException if element(s) not available
     */
    private List getTablesToProcessOrdered(Element db_element) throws Exception {
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
     * @throws Exception NullPointerException if element(s) not available
     */
    private List getUnmappedElements(Iterator iteratorTable,
                                     String   attributeName) throws Exception {
        Vector  unmappedElements = new Vector();
        Element element = null;
        String  elementName = "";
        boolean mapped = false;

        while (iteratorTable.hasNext()) {
            mapped          = false;
            element         = (Element) iteratorTable.next();
            elementName     = element.getAttributeValue(XMLTags.NAME);

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
     * @return
     */
    public Element getDestinationMetadata() {
        return destinationMetadata;
    }

    /**
     * @return
     */
    public Element getSourceMetadata() {
        return sourceMetadata;
    }

    /**
     * @param element
     */
    public void setDestinationMetadata(Element element) {
        destinationMetadata = element;
    }

    /**
     * @param element
     */
    public void setSourceMetadata(Element element) {
        sourceMetadata = element;
    }
}
