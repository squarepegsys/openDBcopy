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
package opendbcopy.model.dependency;

import opendbcopy.config.XMLTags;

import opendbcopy.model.ProjectModel;

import opendbcopy.model.exception.MissingElementException;

import org.jdom.Element;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class Mapper {
    private ProjectModel projectModel;
    private Element      mapping;
    private Element      source_db_model;
    private Element      destination_db_model;

    /**
     * Creates a new LinkingModel object.
     *
     * @param projectModel DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public Mapper(ProjectModel projectModel) throws MissingElementException {
        this.projectModel = projectModel;

        if (projectModel.getMapping() != null) {
            this.mapping = projectModel.getMapping();
        } else {
            this.mapping = new Element(XMLTags.MAPPING);
            projectModel.getRoot().addContent(this.mapping);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void createInitialMapping() throws MissingElementException {
        // remove all children first
        mapping.getChildren().clear();

        source_db_model          = projectModel.getSourceModel();
        destination_db_model     = projectModel.getDestinationModel();

        List     source_db_tables = projectModel.getSourceTables();
        List     destination_db_tables = projectModel.getDestinationTables();

        Iterator itSourceTables = source_db_tables.iterator();

        while (itSourceTables.hasNext()) {
            Element table = (Element) itSourceTables.next();

            Element mapping_table = new Element(XMLTags.TABLE);
            mapping_table.setAttribute(XMLTags.SOURCE_DB, table.getAttributeValue(XMLTags.NAME));
            mapping_table.setAttribute(XMLTags.DESTINATION_DB, "");
            mapping_table.setAttribute(XMLTags.MAPPED, "false");
            mapping_table.setAttribute(XMLTags.PROCESS, "false");
            mapping_table.setAttribute(XMLTags.PROCESSED, "false");

            Iterator itSourceColumns = table.getChildren(XMLTags.COLUMN).iterator();

            while (itSourceColumns.hasNext()) {
                Element column = (Element) itSourceColumns.next();

                Element mapping_column = new Element(XMLTags.COLUMN);
                mapping_column.setAttribute(XMLTags.SOURCE_DB, column.getAttributeValue(XMLTags.NAME));
                mapping_column.setAttribute(XMLTags.DESTINATION_DB, "");
                mapping_column.setAttribute(XMLTags.MAPPED, "false");
                mapping_column.setAttribute(XMLTags.PROCESS, "false");

                mapping_table.addContent(mapping_column);
            }

            mapping.addContent(mapping_table);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public final void findInitalMatches() {
        List     tables = mapping.getChildren(XMLTags.TABLE);

        Iterator itTables = tables.iterator();

        while (itTables.hasNext()) {
            Element mapping_table = (Element) itTables.next();

            Element table = findMatchingTable(mapping_table.getAttributeValue(XMLTags.SOURCE_DB));

            if (table != null) {
                mapping_table.setAttribute(XMLTags.DESTINATION_DB, table.getAttributeValue(XMLTags.NAME));
                mapping_table.setAttribute(XMLTags.MAPPED, "true");
                mapping_table.setAttribute(XMLTags.PROCESS, "true");

                Iterator itColumns = mapping_table.getChildren(XMLTags.COLUMN).iterator();

                while (itColumns.hasNext()) {
                    Element mapping_column = (Element) itColumns.next();

                    Element column = findMatchingColumn(table, mapping_column.getAttributeValue(XMLTags.SOURCE_DB));

                    if (column != null) {
                        mapping_column.setAttribute(XMLTags.DESTINATION_DB, column.getAttributeValue(XMLTags.NAME));
                        mapping_column.setAttribute(XMLTags.MAPPED, "true");
                        mapping_column.setAttribute(XMLTags.PROCESS, "true");
                    }
                }
            }
        }

        setProcessOrder();
    }

    /**
     * DOCUMENT ME!
     *
     * @param sourceTableName DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final void checkForMappings(String sourceTableName) throws IllegalArgumentException, MissingElementException {
        if (sourceTableName == null) {
            throw new IllegalArgumentException("Missing sourceTableName");
        }

        Element mapping_table = projectModel.getMappingSourceTable(sourceTableName);

        if ((mapping_table != null) && (mapping_table.getAttributeValue(XMLTags.DESTINATION_DB).length() > 0)) {
            Element  destination_table = projectModel.getDestinationTable(mapping_table.getAttributeValue(XMLTags.DESTINATION_DB));

            Iterator itColumns = mapping_table.getChildren(XMLTags.COLUMN).iterator();

            while (itColumns.hasNext()) {
                Element mapping_column = (Element) itColumns.next();
                Element destination_column = findMatchingColumn(destination_table, mapping_column.getAttributeValue(XMLTags.SOURCE_DB));

                if (destination_column != null) {
                    mapping_column.setAttribute(XMLTags.DESTINATION_DB, destination_column.getAttributeValue(XMLTags.NAME));
                    mapping_column.setAttribute(XMLTags.MAPPED, "true");
                    mapping_column.setAttribute(XMLTags.PROCESS, "true");
                }
            }
        }

        setProcessOrder();
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     */
    public final Vector getUnmappedColumns(String tableName) throws IllegalArgumentException, MissingElementException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        Vector  unmatchedColumns = new Vector();
        Element tableDestination = projectModel.getDestinationTable(tableName);

        if (tableDestination != null) {
            Iterator itColumnsDestination = tableDestination.getChildren(XMLTags.COLUMN).iterator();

            while (itColumnsDestination.hasNext()) {
                Element column_destination = (Element) itColumnsDestination.next();
                Element mapping_column = projectModel.getMappingDestinationColumn(tableName, column_destination.getAttributeValue(XMLTags.NAME));

                if (mapping_column == null) {
                    unmatchedColumns.addElement(column_destination);
                }
            }
        }

        return unmatchedColumns;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private Element findMatchingTable(String tableName) throws IllegalArgumentException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        Iterator itTables = destination_db_model.getChildren(XMLTags.TABLE).iterator();

        while (itTables.hasNext()) {
            Element table = (Element) itTables.next();

            if (table.getAttributeValue(XMLTags.NAME).compareToIgnoreCase(tableName) == 0) {
                return table;
            }
        }

        // no match found
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param table DOCUMENT ME!
     * @param columnName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private Element findMatchingColumn(Element table,
                                       String  columnName) throws IllegalArgumentException {
        if ((table == null) || (columnName == null)) {
            throw new IllegalArgumentException("Missing arguments values: table=" + table + " columnName=" + columnName);
        }

        Iterator itColumns = table.getChildren(XMLTags.COLUMN).iterator();

        while (itColumns.hasNext()) {
            Element column = (Element) itColumns.next();

            if (column.getAttributeValue(XMLTags.NAME).compareToIgnoreCase(columnName) == 0) {
                return column;
            }
        }

        // no match found
        return null;
    }

    /**
     * DOCUMENT ME!
     */
    private void setProcessOrder() {
        Iterator itMappingTables = mapping.getChildren(XMLTags.TABLE).iterator();

        int      order = 0;

        while (itMappingTables.hasNext()) {
            Element mappingTable = (Element) itMappingTables.next();
            mappingTable.setAttribute(XMLTags.PROCESS_ORDER, Integer.toString(order));
            order++;
        }
    }
}
