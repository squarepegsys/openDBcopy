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
 * --------------------------------------------------------------------------*/
package opendbcopy.model;

import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;

import opendbcopy.sql.SQL;

import org.jdom.Element;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class Test {
    /**
     * DOCUMENT ME!
     *
     * @param projectModel DOCUMENT ME!
     * @param tableName DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static final void testTableFilter(ProjectModel projectModel,
                                             String       tableName) throws Exception {
        String     stm = "";

        Connection conn = null;
        Statement  stmSource = null;
        ResultSet  rs = null;

        try {
            conn = DBConnection.getConnection(projectModel.getSourceConnection());

            Element tableFilter = projectModel.getTableFilter(tableName);

            if (tableFilter != null) {
                stm     = SQL.SELECT_COUNT + SQL.LPAREN + "*" + SQL.RPAREN + SQL.FROM + SQL.SPACE + projectModel.getQualifiedSourceTableName(tableName) + SQL.SPACE + SQL.WHERE + SQL.SPACE + tableFilter.getAttributeValue(XMLTags.VALUE);

                stmSource     = conn.createStatement();

                rs = stmSource.executeQuery(stm);

                boolean recordsFound = false;

                while (rs.next()) {
                    // set the number of matches for filter
                    tableFilter.setAttribute(XMLTags.RECORDS, rs.getString(1));
                    recordsFound = true;
                }

                if (!recordsFound) {
                    tableFilter.setAttribute(XMLTags.RECORDS, Integer.toString(0));
                }

                rs.close();
                DBConnection.closeConnection(conn, true);
            }
        } catch (SQLException e) {
            if (rs != null) {
                rs.close();
            }

            if (conn != null) {
                DBConnection.closeConnection(conn, false);
            }

            throw e;
        }
    }
}
