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
 * Known XML tags for opendbcopy. See user manual for details.  DatabaseMetaData details can be found in the official Java APIs
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public final class XMLTags {
    // root node
    public static final String PROJECT = "project";

    // children of root or specific database
    public static final String OPERATION = "operation";
    public static final String CONNECTION = "connection";
    public static final String DRIVER = "driver";
    public static final String TYPE_INFO = "type_info";
    public static final String MODEL = "model";
    public static final String MAPPING = "mapping";
    public static final String FILTER = "filter";
    public static final String PLUGIN = "plugin";
    public static final String STATISTICS = "statistics";
    public static final String SOURCE_DB = "source_db";
    public static final String DESTINATION_DB = "destination_db";
    public static final String CATALOG = "catalog";
    public static final String SCHEMA = "schema";
    public static final String METADATA = "metadata";
    public static final String TABLE_PATTERN = "table_pattern";
    public static final String TABLE = "table";
    public static final String VIEW = "view";
    public static final String COLUMN = "column";
    public static final String STRING = "string";
    public static final String EXPORTED_KEY = "exported_key";
    public static final String IMPORTED_KEY = "imported_key";
    public static final String PRIMARY_KEY = "primary_key";
    public static final String INDEX = "index";
    public static final String ELEMENT = "element";

    // general attributes
    public static final String DB_MODE = "db_mode";
    public static final String SINGLE_DB = "single";
    public static final String DUAL_DB = "dual";
    public static final String BOTH_DB = "both";
    public static final String DRIVER_CLASS = "driver_class";
    public static final String URL = "url";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String PATTERN = "pattern";
    public static final String NAME = "name";
    public static final String TABLE_TYPE = "table_type";
    public static final String LOCAL_TYPE_NAME = "local_type_name";
    public static final String TYPE_NAME = "type_name";
    public static final String DATA_TYPE = "data_type";
    public static final String PRECISION = "precision";
    public static final String LITERAL_PREFIX = "literal_prefix";
    public static final String LITERAL_SUFFIX = "literal_suffix";
    public static final String CASE_SENSITIVE = "case_sensitive";
    public static final String COLUMN_SIZE = "column_size";
    public static final String DECIMAL_DIGITS = "decimal_digits";
    public static final String NULLABLE = "nullable";
    public static final String CAPTURE_DATE = "capture_date";
    public static final String MAPPED = "mapped";
    public static final String PROCESS = "process";
    public static final String PROCESSED = "processed";
    public static final String PROCESS_ORDER = "process_order";
    public static final String FILE = "file";
    public static final String FILE_TYPE = "file_type";
    public static final String VALUE = "value";
    public static final String RECORDS = "records";
    public static final String IDENTIFIER_QUOTE_STRING = "identifier_quote_string";
    public static final String CATALOG_SEPARATOR = "catalog_separator";
    public static final String DB_PRODUCT_NAME = "db_product_name";
    public static final String DB_PRODUCT_VERSION = "db_product_version";
    public static final String JDBC_MAJOR_VERSION = "jdbc_major_version";
    public static final String DRIVER_VERSION = "driver_version";
    public static final String READ_PRIMARY_KEYS = "read_primary_keys";
    public static final String READ_FOREIGN_KEYS = "read_foreign_keys";
    public static final String READ_INDEXES = "read_indexes";
    public static final String NUMBER = "number";

    // primary key attributes
    public static final String TABLE_CAT = "table_cat"; // String => table catalog (may be null)
    public static final String TABLE_SCHEM = "table_schem"; // String => table schema (may be null)
    public static final String TABLE_NAME = "table_name";
    public static final String COLUMN_NAME = "column_name";
    public static final String KEY_SEQ = "key_seq"; // short => sequence number within primary key
    public static final String PK_NAME = "pk_name"; // String => primary key name (may be null)

    // foreign key attributes
    public static final String PKTABLE_CAT = "pktable_cat"; // String => primary key table catalog being imported (may be null)
    public static final String PKTABLE_SCHEM = "pktable_schem"; // String => primary key table schema being imported (may be null)
    public static final String PKTABLE_NAME = "pktable_name"; // String => primary key table name being imported
    public static final String PKCOLUMN_NAME = "pkcolumn_name"; // String => primary key column name being imported
    public static final String FKTABLE_CAT = "fktable_cat"; // String => foreign key table catalog (may be null)
    public static final String FKTABLE_SCHEM = "fktable_schem"; // String => foreign key table schema (may be null)
    public static final String FKTABLE_NAME = "fktable_name"; // String => foreign key table name
    public static final String FKCOLUMN_NAME = "fkcolumn_name"; // String => foreign key column name
    public static final String UPDATE_RULE = "update_rule"; // short => What happens to a foreign key when the primary key is updated:
    public static final String DELETE_RULE = "delete_rule"; // short => What happens to the foreign key when primary is deleted.
    public static final String FK_NAME = "fk_name"; // String => foreign key name (may be null)
    public static final String DEFERRABILITY = "deferrability"; // short => can the evaluation of foreign key constraints be deferred until commit

    // index attributes
    public static final String NON_UNIQUE = "non_unique"; // boolean => Can index values be non-unique. false when TYPE is tableIndexStatistic
    public static final String INDEX_QUALIFIER = "index_qualifier"; // String => index catalog (may be null); null when TYPE is tableIndexStatistic
    public static final String INDEX_NAME = "index_name"; // String => index name; null when TYPE is tableIndexStatistic
    public static final String ORDINAL_POSITION = "ordinal_position"; // short => column sequence number within index; zero when TYPE is tableIndexStatistic
    public static final String ASC_OR_DESC = "asc_or_desc"; // String => column sort sequence, "A" => ascending, "D" => descending, may be null if sort sequence is not supported; null when TYPE is tableIndexStatistic
    public static final String CARDINALITY = "cardinality"; // int => When TYPE is tableIndexStatistic, then this is the number of rows in the table; otherwise, it is the number of unique values in the index.
    public static final String PAGES = "pages"; // int => When TYPE is tableIndexStatisic then this is the number of pages used for the table, otherwise it is the number of pages used for the current index.
    public static final String FILTER_CONDITION = "filter_condition"; // String => Filter condition, if any. (may be null)	
    public static final String TYPE = "type"; // short => index type:

    // filer attributes
    public static final String TRIM = "trim";
    public static final String REMOVE_INTERMEDIATE_WHITESPACES = "remove_intermediate_whitespaces";
    public static final String SET_NULL = "set_null";

    // plugin attributes and nodes
    public static final String PLUGIN_CONFIG = "plugin_config";
    public static final String CLASS = "class";
    public static final String DESCRIPTION = "description";
    public static final String AUTHOR = "author";
    public static final String CONF = "conf";
    public static final String DELIMITER = "delimiter";
    public static final String DELIMITER_POSITION = "delimiter_position";
    public static final String APPEND_FILE_AFTER_RECORDS = "append_file_after_records";
    public static final String LOG_ERROR = "log_error";
    public static final String ON_ERROR_RELOAD = "on_error_reload";
    public static final String RELOAD_TIMEOUT_MS = "reload_timeout_ms";
    public static final String RESOURCE = "resource";
    public static final String PATH = "path";
    public static final String SHOW_HEADER = "show_header";
    public static final String SHOW_NULL_VALUES = "show_null_values";

    // application arguments
    public static final String ARGUMENTS = "arguments";
    public static final String RUNLEVEL = "runlevel";

    // SQLDriver tags
    public static final String DRIVERS = "drivers";

    // Working Mode
    public static final String WORKING_MODE = "working_mode";
    public static final String WORKING_MODES = "working_modes";
    public static final String IDENTIFIER = "identifier";
    public static final String TITLE = "title";
    public static final String PANELS = "panels";
    public static final String PANEL = "panel";
    public static final String REGISTER_AS_OBSERVER = "register_as_observer";
}
