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
package opendbcopy.connection;

import opendbcopy.config.XMLTags;

import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.connection.exception.DriverNotFoundException;
import opendbcopy.connection.exception.OpenConnectionException;

import opendbcopy.plugin.model.exception.MissingAttributeException;

import org.jdom.Element;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


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
     *
     * @throws CloseConnectionException DOCUMENT ME!
     */
    public static final void closeConnection(Connection connection) throws CloseConnectionException {
        try {
            if (!connection.isClosed()) {
                try {
                    if (!connection.getAutoCommit()) {
                        connection.commit();
                    }
                } catch (SQLException e2) {
                    // ok, driver does not support auto commit ... leave it as it is
                }

                connection.close();
            }
        } catch (SQLException e1) {
            throw new CloseConnectionException(e1);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param connection DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws DriverNotFoundException DOCUMENT ME!
     * @throws OpenConnectionException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static final Connection getConnection(Element connection) throws MissingAttributeException, DriverNotFoundException, OpenConnectionException {
        if (connection == null) {
            throw new IllegalArgumentException("Missing connection element");
        }

        if (connection.getAttributeValue(XMLTags.DRIVER_CLASS) == null) {
            throw new MissingAttributeException(connection, XMLTags.DRIVER_CLASS);
        }

        if (connection.getAttributeValue(XMLTags.URL) == null) {
            throw new MissingAttributeException(connection, XMLTags.URL);
        }

        if (connection.getAttributeValue(XMLTags.USERNAME) == null) {
            throw new MissingAttributeException(connection, XMLTags.USERNAME);
        }

        if (connection.getAttributeValue(XMLTags.PASSWORD) == null) {
            throw new MissingAttributeException(connection, XMLTags.PASSWORD);
        }

        Connection conn = null;

        // check if driver is available. Some drivers do not require to specifically provide DRIVER_CLASS
        if (connection.getAttributeValue(XMLTags.DRIVER_CLASS).length() > 0) {
            try {
                Class.forName(connection.getAttributeValue(XMLTags.DRIVER_CLASS));
            } catch (ClassNotFoundException e) {
                throw new DriverNotFoundException("Could not load JDBC driver. The driver (jar, zip or class file(s)) must either be in the opendbcopy/lib directory or specified in your system's classpath.");
            }
        }

        // retrieve connection and handle possible exceptions
        try {
            conn = DriverManager.getConnection(connection.getAttributeValue(XMLTags.URL), connection.getAttributeValue(XMLTags.USERNAME), connection.getAttributeValue(XMLTags.PASSWORD));
        }
        // maybe that an SQLException is thrown but the connection is still valid!
         catch (SQLException e1) {
            if (conn == null) {
                throw new OpenConnectionException(e1);
            }
        }

        // set auto commit to false
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            // bad luck, the connection is valid but does not support autoCommit function
        }

        return conn;
    }

    /**
     * DOCUMENT ME!
     *
     * @param connection DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws MissingAttributeException DOCUMENT ME!
     * @throws DriverNotFoundException DOCUMENT ME!
     * @throws OpenConnectionException DOCUMENT ME!
     * @throws CloseConnectionException DOCUMENT ME!
     */
    public static final boolean testConnection(Element connection) throws MissingAttributeException, DriverNotFoundException, OpenConnectionException, CloseConnectionException {
        Connection conn = getConnection(connection);

        if (conn != null) {
            closeConnection(conn);

            return true;
        } else {
            return false;
        }
    }
}
