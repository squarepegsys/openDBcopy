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
package opendbcopy.connection.exception;

import java.sql.SQLException;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class CloseConnectionException extends SQLException {
    private String message;
    private String toString;
    private String sqlState;
    private int    errorCode;

    /**
     * Creates a new CloseConnectionException object.
     *
     * @param message DOCUMENT ME!
     * @param toString DOCUMENT ME!
     * @param sqlState DOCUMENT ME!
     * @param errorCode DOCUMENT ME!
     */
    public CloseConnectionException(String message,
                                    String toString,
                                    String sqlState,
                                    int    errorCode) {
        this.message       = message;
        this.toString      = toString;
        this.sqlState      = sqlState;
        this.errorCode     = errorCode;
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
     * @return Returns the message.
     */
    public String getMessage() {
        return "MESSAGE:\n" + message + "\n\nTO STRING:\n" + toString + "\n\nERROR CODE:\n" + errorCode + "\n\nSQL STATE:\n" + sqlState;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the sqlState.
     */
    public String getSqlState() {
        return sqlState;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the toString.
     */
    public String getToString() {
        return toString;
    }
}
