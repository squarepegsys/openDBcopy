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
package opendbcopy.config;

/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class OperationType {
    /** Project and Application Operations */
    public static final String NEW = "New";
    public static final String EXPORT_PROJECT = "Export Project ...";
    public static final String IMPORT_PROJECT = "Import Project ...";
    public static final String OPEN = "open";
    public static final String EXIT = "Exit";

    /** Capture Operations */
    public static final String READ_METADATA = "read_metadata";
    public static final String CAPTURE_SOURCE_MODEL = "Capture Source Model";
    public static final String CAPTURE_DESTINATION_MODEL = "Capture Destination Model";

    /** Execute Menu */
    public static final String EXECUTE = "Execute";
    public static final String CANCEL = "Cancel";

    /** Test Menu */
    public static final String TEST_SOURCE = "Test Source Connection";
    public static final String TEST_DESTINATION = "Test Destination Connection";
    public static final String TEST_TABLE_FILTER = "Test Table Filter";
}
