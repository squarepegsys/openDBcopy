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
package opendbcopy.config;

/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class OperationType {
    /** Project and Application Operations */
    public static final String NEW_PROJECT = "new_project";
    public static final String ADD_PLUGIN = "add_plugin";
    public static final String REMOVE_PLUGIN = "remove_plugin";
    public static final String EXPORT_PROJECT = "export_project";
    public static final String IMPORT_PROJECT = "import_project";
    public static final String EXIT = "exit";

    public static final String NEW_PLUGIN = "new_plugin";
    public static final String EXPORT_PLUGIN = "export_plugin";
    public static final String IMPORT_PLUGIN = "import_plugin";

    /** Capture Operations */
    public static final String READ_METADATA = "read_metadata";
    public static final String CAPTURE_SOURCE_MODEL = "capture_source_model";
    public static final String CAPTURE_DESTINATION_MODEL = "capture_destination_model";

    /** Execute Menu */
    public static final String EXECUTE = "execute";
    public static final String CANCEL = "cancel";

    /** Test Menu */
    public static final String TEST_SOURCE_CONNECTION = "test_source_connection";
    public static final String TEST_DESTINATION_CONNECTION = "test_destination_connection";
    public static final String TEST_TABLE_FILTER = "test_table_filter";
}
