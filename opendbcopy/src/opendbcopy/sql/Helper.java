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
package opendbcopy.sql;

import opendbcopy.config.XMLTags;

import opendbcopy.filter.StringConverter;

import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.exception.MissingAttributeException;
import opendbcopy.plugin.model.exception.MissingElementException;

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
public final class Helper {
    /**
     * DOCUMENT ME!
     *
     * @param in DOCUMENT ME!
     * @param returnNullWhenEmpty DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static Object trimObject(Object  in,
                                    boolean returnNullWhenEmpty) {
        if (in == null) {
            throw new IllegalArgumentException("Missing in object");
        }

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
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static Object trimAndRemoveMultipleIntermediateWhitespaces(Object  in,
                                                                      boolean returnNullWhenEmpty) {
        if (in == null) {
            throw new IllegalArgumentException("Missing in object");
        }

        if (in instanceof String || in instanceof Character) {
            return StringConverter.trimAndRemoveMultipleIntermediateWhitespaces(in, returnNullWhenEmpty);
        } else {
            return in;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param qs DOCUMENT ME!
     * @param model DOCUMENT ME!
     * @param database DOCUMENT ME!
     * @param tableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static int getNumberOfRecordsFiltered(Statement     qs,
                                                 DatabaseModel model,
                                                 String        database,
                                                 String        tableName) throws SQLException, Exception {
        if ((qs == null) || (model == null) || (database == null) || (tableName == null)) {
            throw new IllegalArgumentException("Missing arguments values: qs=" + qs + " model=" + model + " database=" + database + " tableName=" + tableName);
        }

        int    numberRecords = 0;
        String stm = "";
        String qualifiedTableName = "";

        if (database.compareTo(XMLTags.SOURCE_DB) == 0) {
            qualifiedTableName = model.getQualifiedSourceTableName(tableName);
        } else {
            qualifiedTableName = model.getQualifiedDestinationTableName(tableName);
        }

        // no table filters specified
        if (model.getTableFilter(tableName) == null) {
            stm = SQL.SELECT + "COUNT(*)" + SQL.FROM + qualifiedTableName;
        }
        // table filters specified
        else {
            stm = SQL.SELECT + "COUNT(*)" + SQL.FROM + qualifiedTableName + SQL.SPACE + SQL.WHERE + SQL.SPACE + model.getTableFilter(tableName).getAttributeValue(XMLTags.VALUE);
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
     * @param model DOCUMENT ME!
     * @param database DOCUMENT ME!
     * @param tableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static int getNumberOfRecords(Statement     qs,
                                         DatabaseModel model,
                                         String        database,
                                         String        tableName) throws MissingAttributeException, MissingElementException, SQLException {
        if ((qs == null) || (model == null) || (database == null) || (tableName == null)) {
            throw new IllegalArgumentException("Missing arguments values: qs=" + qs + " model=" + model + " database=" + database + " tableName=" + tableName);
        }

        int    numberRecords = 0;
        String qualifiedTableName = "";

        if (database.compareTo(XMLTags.SOURCE_DB) == 0) {
            qualifiedTableName = model.getQualifiedSourceTableName(tableName);
        } else {
            qualifiedTableName = model.getQualifiedDestinationTableName(tableName);
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
     * @param model DOCUMENT ME!
     * @param tableName DOCUMENT ME!
     * @param attributeName DOCUMENT ME!
     * @param processColumns DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static String getSelectStatement(DatabaseModel model,
                                            String        tableName,
                                            String        attributeName,
                                            List          processColumns) throws MissingAttributeException, MissingElementException {
        if ((model == null) || (tableName == null) || (attributeName == null) || (processColumns == null)) {
            throw new IllegalArgumentException("Missing arguments values: model=" + model + " tableName=" + tableName + " attributeName=" + attributeName + " processColumns=" + processColumns);
        }

        StringBuffer stm = new StringBuffer();

        stm.append(SQL.SELECT + SQL.SPACE);

        Iterator itColumns = processColumns.iterator();

        while (itColumns.hasNext()) {
            stm.append(((Element) itColumns.next()).getAttributeValue(attributeName) + SQL.COMMA);
        }

        // replace last COMMA with SPACE
        stm.replace(stm.length() - 1, stm.length(), SQL.SPACE);

        stm.append(SQL.FROM + SQL.SPACE + model.getQualifiedSourceTableName(tableName));

        if (model.getTableFilter(tableName) != null) {
            stm.append(SQL.SPACE + SQL.WHERE + SQL.SPACE + model.getTableFilter(tableName).getAttributeValue(XMLTags.VALUE));
        }

        return stm.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param model DOCUMENT ME!
     * @param tableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws MissingElementException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static String getSelectAllStatement(DatabaseModel model,
                                               String        tableName) throws MissingAttributeException, MissingElementException {
        if ((model == null) || (tableName == null)) {
            throw new IllegalArgumentException("Missing arguments values: model=" + model + " tableName=" + tableName);
        }

        StringBuffer stm = new StringBuffer();

        stm.append(SQL.SELECT + SQL.SPACE + "*" + SQL.SPACE);

        stm.append(SQL.FROM + SQL.SPACE + model.getQualifiedSourceTableName(tableName));

        if (model.getTableFilter(tableName) != null) {
            stm.append(SQL.SPACE + SQL.WHERE + SQL.SPACE + model.getTableFilter(tableName).getAttributeValue(XMLTags.VALUE));
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
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static String getInsertPreparedStatement(String qualifiedTableName,
                                                    List   processColumns) {
        if ((qualifiedTableName == null) || (processColumns == null)) {
            throw new IllegalArgumentException("Missing arguments values: qualifiedTableName=" + qualifiedTableName + " processColumns=" + processColumns);
        }

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
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static String getDeleteTableStatement(String qualifiedTableName) {
        if (qualifiedTableName == null) {
            throw new IllegalArgumentException("Missing qualifiedTableName");
        }

        StringBuffer stm = new StringBuffer();

        stm.append(SQL.DELETE + SQL.SPACE);

        stm.append(SQL.FROM + SQL.SPACE + qualifiedTableName);

        return stm.toString();
    }
}
