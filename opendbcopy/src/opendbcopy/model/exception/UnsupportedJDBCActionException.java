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
package opendbcopy.model.exception;

import java.sql.SQLException;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class UnsupportedJDBCActionException extends SQLException {
    private String unsupportedAction;
    private String message;
    private String sqlState;
    private int    errorCode;

    /**
     * Creates a new UnsupportedJDBCActionException object.
     *
     * @param unsupportedAction DOCUMENT ME!
     * @param message DOCUMENT ME!
     * @param sqlState DOCUMENT ME!
     * @param errorCode DOCUMENT ME!
     */
    public UnsupportedJDBCActionException(String unsupportedAction,
                                          String message,
                                          String sqlState,
                                          int    errorCode) {
        this.unsupportedAction     = unsupportedAction;
        this.message               = message;
        this.sqlState              = sqlState;
        this.errorCode             = errorCode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getMessage() {
        return unsupportedAction + "\n\n" + message + "\n\n" + sqlState + "\n\n" + errorCode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getUnsupportedAction() {
        return unsupportedAction;
    }
}
