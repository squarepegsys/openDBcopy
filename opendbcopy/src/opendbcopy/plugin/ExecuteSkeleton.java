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
package opendbcopy.plugin;

import opendbcopy.config.XMLTags;

import opendbcopy.filter.StringConverter;

import opendbcopy.model.ProjectModel;

import opendbcopy.plugin.exception.PluginException;

import opendbcopy.sql.SQL;

import opendbcopy.task.TaskExecute;

import org.jdom.Element;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Iterator;
import java.util.List;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public abstract class ExecuteSkeleton implements Execute {
    /**
     * Each subclass has to override this method
     *
     * @param task
     * @param model
     * @param datasourceSource
     * @param datasourceDestination
     * @param copyProperties
     */
    abstract public void doExecute(TaskExecute  task,
                                   ProjectModel projectModel) throws PluginException;

    /**
     * DOCUMENT ME!
     *
     * @param in DOCUMENT ME!
     * @param returnNullWhenEmpty DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Object trimObject(Object  in,
                                    boolean returnNullWhenEmpty) {
        if (in instanceof String || in instanceof Character) {
            return StringConverter.trimString(in, returnNullWhenEmpty);
        } else {
            return in;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param in DOCUMENT ME!
     * @param returnNullWhenEmpty DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Object trimAndRemoveMultipleIntermediateWhitespaces(Object  in,
                                                                      boolean returnNullWhenEmpty) {
        if (in instanceof String || in instanceof Character) {
            return StringConverter.trimAndRemoveMultipleIntermediateWhitespaces(in, returnNullWhenEmpty);
        } else {
            return in;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param in DOCUMENT ME!
     * @param returnNullWhenEmpty DOCUMENT ME!
     * @param projectModel DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static Object applyStringFilters(Object       in,
                                            boolean      returnNullWhenEmpty,
                                            ProjectModel projectModel) throws Exception {
        if (in instanceof String || in instanceof Character) {
            boolean trimString = false;
            boolean trimAndRemoveMultipleIntermediateWhitespaces = false;
            boolean trimAndReturnNullWhenEmpty = false;

            if (projectModel.getStringFilterTrim().getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0) {
                trimString = true;
            }

            if (projectModel.getStringFilterRemoveIntermediateWhitespaces().getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0) {
                trimAndRemoveMultipleIntermediateWhitespaces = true;
            }

            if (projectModel.getStringFilterSetNull().getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0) {
                trimAndReturnNullWhenEmpty = true;
            }

            if (trimAndRemoveMultipleIntermediateWhitespaces && trimAndReturnNullWhenEmpty) {
                return StringConverter.trimAndRemoveMultipleIntermediateWhitespaces(in, returnNullWhenEmpty);
            } else if (trimAndRemoveMultipleIntermediateWhitespaces && !trimAndReturnNullWhenEmpty) {
                return StringConverter.trimAndRemoveMultipleIntermediateWhitespaces(in, false);
            } else if (trimString && trimAndReturnNullWhenEmpty) {
                return StringConverter.trimString(in, returnNullWhenEmpty);
            } else if (trimString && !trimAndReturnNullWhenEmpty) {
                return StringConverter.trimString(in, false);
            } else {
                return in;
            }
        } else {
            return in;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param qs DOCUMENT ME!
     * @param projectModel DOCUMENT ME!
     * @param database DOCUMENT ME!
     * @param tableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public static int getNumberOfRecordsFiltered(Statement    qs,
                                                 ProjectModel projectModel,
                                                 String       database,
                                                 String       tableName) throws SQLException, Exception {
        int    numberRecords = 0;
        String stm = "";
        String qualifiedTableName = "";

        if (database.compareTo(XMLTags.SOURCE_DB) == 0) {
            qualifiedTableName = projectModel.getQualifiedSourceTableName(tableName);
        } else {
            qualifiedTableName = projectModel.getQualifiedDestinationTableName(tableName);
        }

        // no table filters specified
        if (projectModel.getTableFilter(tableName) == null) {
            stm = SQL.SELECT + "COUNT(*)" + SQL.FROM + qualifiedTableName;
        }
        // table filters specified
        else {
            stm = SQL.SELECT + "COUNT(*)" + SQL.FROM + qualifiedTableName + SQL.SPACE + SQL.WHERE + SQL.SPACE + projectModel.getTableFilter(tableName).getAttributeValue(XMLTags.VALUE);
        }

        ResultSet rs = qs.executeQuery(stm);

        while (rs.next()) {
            numberRecords = rs.getInt(1);
        }

        rs.close();

        return numberRecords;
    }

    /**
     * DOCUMENT ME!
     *
     * @param qs DOCUMENT ME!
     * @param projectModel DOCUMENT ME!
     * @param database DOCUMENT ME!
     * @param tableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public static int getNumberOfRecords(Statement    qs,
                                         ProjectModel projectModel,
                                         String       database,
                                         String       tableName) throws SQLException, Exception {
        int    numberRecords = 0;
        String qualifiedTableName = "";

        if (database.compareTo(XMLTags.SOURCE_DB) == 0) {
            qualifiedTableName = projectModel.getQualifiedSourceTableName(tableName);
        } else {
            qualifiedTableName = projectModel.getQualifiedDestinationTableName(tableName);
        }

        String    stm = SQL.SELECT + "COUNT(*)" + SQL.FROM + qualifiedTableName;

        ResultSet rs = qs.executeQuery(stm);

        while (rs.next()) {
            numberRecords = rs.getInt(1);
        }

        rs.close();

        return numberRecords;
    }

    /**
     * DOCUMENT ME!
     *
     * @param projectModel DOCUMENT ME!
     * @param tableName DOCUMENT ME!
     * @param attributeName DOCUMENT ME!
     * @param processColumns DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static String getSelectStatement(ProjectModel projectModel,
                                            String       tableName,
                                            String       attributeName,
                                            List         processColumns) throws Exception {
        StringBuffer stm = new StringBuffer();

        stm.append(SQL.SELECT + SQL.SPACE);

        Iterator itColumns = processColumns.iterator();

        while (itColumns.hasNext()) {
            stm.append(((Element) itColumns.next()).getAttributeValue(attributeName) + SQL.COMMA);
        }

        // replace last COMMA with SPACE
        stm.replace(stm.length() - 1, stm.length(), SQL.SPACE);

        stm.append(SQL.FROM + SQL.SPACE + projectModel.getQualifiedSourceTableName(tableName));

        if (projectModel.getTableFilter(tableName) != null) {
            stm.append(SQL.SPACE + SQL.WHERE + SQL.SPACE + projectModel.getTableFilter(tableName).getAttributeValue(XMLTags.VALUE));
        }

        return stm.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param projectModel DOCUMENT ME!
     * @param tableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static String getSelectAllStatement(ProjectModel projectModel,
                                               String       tableName) throws Exception {
        StringBuffer stm = new StringBuffer();

        stm.append(SQL.SELECT + SQL.SPACE + "*" + SQL.SPACE);

        stm.append(SQL.FROM + SQL.SPACE + projectModel.getQualifiedSourceTableName(tableName));

        if (projectModel.getTableFilter(tableName) != null) {
            stm.append(SQL.SPACE + SQL.WHERE + SQL.SPACE + projectModel.getTableFilter(tableName).getAttributeValue(XMLTags.VALUE));
        }

        return stm.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param qualifiedTableName DOCUMENT ME!
     * @param processColumns DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String getInsertPreparedStatement(String qualifiedTableName,
                                                    List   processColumns) {
        StringBuffer pstmInsert = new StringBuffer();
        int          nbrColumns = 0;

        pstmInsert.append(SQL.INSERT_INTO + qualifiedTableName + SQL.SPACE + SQL.LPAREN);

        Iterator itColumns = processColumns.iterator();

        while (itColumns.hasNext()) {
            pstmInsert.append(((Element) itColumns.next()).getAttributeValue(XMLTags.DESTINATION_DB) + SQL.COMMA);
            nbrColumns++;
        }

        // remove last comma and add RPAREN instead
        pstmInsert.replace(pstmInsert.length() - 1, pstmInsert.length(), SQL.RPAREN);

        pstmInsert.append(" " + SQL.VALUES + SQL.SPACE + SQL.LPAREN);

        // iterate until the end minus 1
        for (int i = 0; i < (nbrColumns - 1); i++) {
            pstmInsert.append("?, ");
        }

        // now add the last question mark
        pstmInsert.append("?");

        pstmInsert.append(SQL.RPAREN);

        return pstmInsert.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param qualifiedTableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static String getDeleteTableStatement(String qualifiedTableName) throws Exception {
        StringBuffer stm = new StringBuffer();

        stm.append(SQL.DELETE + SQL.SPACE);

        stm.append(SQL.FROM + SQL.SPACE + qualifiedTableName);

        return stm.toString();
    }
}
