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
package opendbcopy.connection;

import opendbcopy.config.XMLTags;

import org.jdom.Element;

import java.sql.Connection;
import java.sql.DriverManager;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class DBConnection {
    /**
     * DOCUMENT ME!
     *
     * @param connection DOCUMENT ME!
     * @param commit DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static final void closeConnection(Connection connection,
                                             boolean    commit) throws Exception {
        if (!connection.isClosed()) {
            if (commit) {
                connection.commit();
            }

            connection.close();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param connection DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static final Connection getConnection(Element connection) throws Exception {
        // check if driver class is available
        if (connection.getAttributeValue(XMLTags.DRIVER_CLASS).length() > 0) {
            Class.forName(connection.getAttributeValue(XMLTags.DRIVER_CLASS));
        }

        // retrieve connection
        Connection conn = DriverManager.getConnection(connection.getAttributeValue(XMLTags.URL), connection.getAttributeValue(XMLTags.USERNAME), connection.getAttributeValue(XMLTags.PASSWORD));

        // set auto commit to false
        conn.setAutoCommit(false);

        return conn;
    }

    /**
     * DOCUMENT ME!
     *
     * @param connection DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static final boolean testConnection(Element connection) throws Exception {
        Connection conn = getConnection(connection);

        if (conn != null) {
            conn.close();

            return true;
        } else {
            return false;
        }
    }
}
