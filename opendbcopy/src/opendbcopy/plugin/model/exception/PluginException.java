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
package opendbcopy.plugin.model.exception;

import opendbcopy.connection.exception.DriverNotFoundException;

import java.lang.reflect.InvocationTargetException;

import java.sql.SQLException;


/**
 * This class shall help to catch all sorts of "allowed" exceptions to be formatted and passed with all available information to higher levels as a
 * single exception - pluginException
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class PluginException extends SQLException {
    private Throwable cause;
    private String    message;
    private String    sqlState;
    private int       errorCode;

    /**
     * Creates a new PluginException object.
     *
     * @param message DOCUMENT ME!
     */
    public PluginException(String message) {
        this.message = message;
    }

    /**
     * Creates a new PluginException object.
     *
     * @param e DOCUMENT ME!
     */
    public PluginException(SQLException e) {
        this.cause         = e.getCause();
        this.message       = e.getMessage();
        this.sqlState      = e.getSQLState();
        this.errorCode     = e.getErrorCode();
    }

    /**
     * Creates a new PluginException object.
     *
     * @param e DOCUMENT ME!
     */
    public PluginException(ClassNotFoundException e) {
        this.cause       = e.getCause();
        this.message     = e.getMessage();
    }

    /**
     * Creates a new PluginException object.
     *
     * @param e DOCUMENT ME!
     */
    public PluginException(InvocationTargetException e) {
        this.cause       = e.getCause();
        this.message     = e.getMessage();
    }

    /**
     * Creates a new PluginException object.
     *
     * @param e DOCUMENT ME!
     */
    public PluginException(IllegalAccessException e) {
        this.cause       = e.getCause();
        this.message     = e.getMessage();
    }

    /**
     * Creates a new PluginException object.
     *
     * @param e DOCUMENT ME!
     */
    public PluginException(InstantiationException e) {
        this.cause       = e.getCause();
        this.message     = e.getMessage();
    }

    /**
     * Creates a new PluginException object.
     *
     * @param e DOCUMENT ME!
     */
    public PluginException(MissingAttributeException e) {
        this.cause       = e.getCause();
        this.message     = e.getMessage();
    }

    /**
     * Creates a new PluginException object.
     *
     * @param e DOCUMENT ME!
     */
    public PluginException(MissingElementException e) {
        this.cause       = e.getCause();
        this.message     = e.getMessage();
    }

    /**
     * Creates a new PluginException object.
     *
     * @param e DOCUMENT ME!
     */
    public PluginException(UnsupportedAttributeValueException e) {
        this.cause       = e.getCause();
        this.message     = e.getMessage();
    }

    /**
     * Creates a new PluginException object.
     *
     * @param e DOCUMENT ME!
     */
    public PluginException(DriverNotFoundException e) {
        this.cause       = e.getCause();
        this.message     = e.getMessage();
    }

    /**
     * Creates a new PluginException object.
     *
     * @param e DOCUMENT ME!
     */
    public PluginException(Exception e) {
        this.cause       = e.getCause();
        this.message     = e.getMessage();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getMessage() {
        return message;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the errorCode.
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the sqlState.
     */
    public String getSqlState() {
        return sqlState;
    }
}
