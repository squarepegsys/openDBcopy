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
package opendbcopy.plugin.dump;

import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;

import opendbcopy.connection.exception.CloseConnectionException;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.model.DynamicPluginThread;
import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.exception.PluginException;

import opendbcopy.sql.Helper;

import org.jdom.Element;

import java.io.File;
import java.io.FileWriter;

import java.sql.Connection;
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
public class DumpDataToFileDelimitedPlugin extends DynamicPluginThread {
    private DatabaseModel model;
    private Connection    connSource = null;
    private Statement     stmSource = null;
    private ResultSet     rs = null;
    private StringBuffer  recordBuffer = new StringBuffer(); // to hold records contents
    private int           counterRecords = 0;
    private int           counterTables = 0;
    private int           append_file_after_records = 0;
    private String        delimiter = "";
    private String        delimiterOriginal = "";
    private String        newLine = "";
    private String        path = "";
    private String        fileType = "";
    private boolean       show_header = false;
    private boolean       append_delimiter_after_data = false;
    private boolean       show_null_values = false;
    List                  processTables = null;

    /**
     * Creates a new CopyMappingPlugin object.
     *
     * @param controller DOCUMENT ME!
     * @param baseModel DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public DumpDataToFileDelimitedPlugin(MainController controller,
                                         Model          baseModel) throws PluginException {
        super(controller, baseModel);
        this.model = (DatabaseModel) baseModel;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    protected final void setUp() throws PluginException {
        // use the system's newline character instead of \n
        newLine = System.getProperty("line.separator");

        // read the plugins configuration
        Element conf = model.getConf();

        path                          = conf.getChild(XMLTags.PATH).getAttributeValue(XMLTags.VALUE);
        fileType                      = conf.getChild(XMLTags.FILE_TYPE).getAttributeValue(XMLTags.VALUE);
        delimiterOriginal             = conf.getChild(XMLTags.DELIMITER).getAttributeValue(XMLTags.VALUE);
        delimiter                     = delimiterOriginal;
        show_header                   = Boolean.valueOf(conf.getChild(XMLTags.SHOW_HEADER).getAttributeValue(XMLTags.VALUE)).booleanValue();
        show_null_values              = Boolean.valueOf(conf.getChild("show_null_values").getAttributeValue(XMLTags.VALUE)).booleanValue();
        append_file_after_records     = Integer.parseInt(conf.getChild(XMLTags.APPEND_FILE_AFTER_RECORDS).getAttributeValue(XMLTags.VALUE));

        if (conf.getChild(XMLTags.DELIMITER_POSITION).getAttributeValue(XMLTags.VALUE).compareTo("after") == 0) {
            append_delimiter_after_data = true;
        }

        try {
            // get connection
            connSource     = DBConnection.getConnection(model.getSourceConnection());

            // extract the tables to dump
            processTables = model.getSourceTablesToProcessOrdered();
        } catch (Exception e) {
            throw new PluginException(e.getMessage());
        }

        // now set the number of tables that need to be copied
        model.setLengthProgressTable(processTables.size());
    }

    /**
     * DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public void execute() throws PluginException {
        try {
            stmSource = connSource.createStatement();

            String   stmSelect = "";

            Iterator itProcessTables = processTables.iterator();

            while (itProcessTables.hasNext() && !isInterrupted()) {
                Element tableProcess = (Element) itProcessTables.next();

                String  sourceTableName = tableProcess.getAttributeValue(XMLTags.NAME);

                String  fileName = getFileName(sourceTableName);

                counterRecords = 0;

                List processColumns = model.getSourceColumnsToProcess(sourceTableName);

                if (show_header) {
                    initHeader(processColumns);
                } else {
                    recordBuffer = new StringBuffer();
                }

                // setting record counter to minimum of progress bar
                model.setCurrentProgressRecord(0);
                model.setCurrentProgressTable(0);

                // Reading number of records for progress bar
                model.setLengthProgressRecord(Helper.getNumberOfRecordsFiltered(stmSource, model, XMLTags.NAME, sourceTableName));

                // get Select Statement
                stmSelect = Helper.getSelectStatement(model, sourceTableName, XMLTags.NAME, processColumns);

                model.setCurrentProgressTable(counterTables);

                // Execute SELECT
                rs = stmSource.executeQuery(stmSelect);

                model.setProgressMessage("Reading " + sourceTableName + " ...");

                logger.info("Reading " + sourceTableName + " ...");

                // open file writer					
                FileWriter fileWriter = new FileWriter(new File(fileName));

                while (rs.next()) {
                    model.setCurrentProgressRecord(++counterRecords);

                    // process columns
                    for (int colCounter = 1; colCounter < (processColumns.size() + 1); colCounter++) {
                        String input = rs.getString(colCounter);

                        // treat first column different
                        if ((colCounter == 1) && !append_delimiter_after_data) {
                            delimiter = "";
                        } else {
                            delimiter = delimiterOriginal;
                        }

                        if (!rs.wasNull()) {
                            if (append_delimiter_after_data) {
                                recordBuffer.append(input + delimiter);
                            } else {
                                recordBuffer.append(delimiter + input);
                            }
                        } else {
                            if (show_null_values) {
                                if (append_delimiter_after_data) {
                                    recordBuffer.append("null" + delimiter);
                                } else {
                                    recordBuffer.append(delimiter + "null");
                                }
                            } else {
                                if (append_delimiter_after_data) {
                                    recordBuffer.append("" + delimiter);
                                } else {
                                    recordBuffer.append(delimiter + "");
                                }
                            }
                        }
                    }

                    recordBuffer.append(newLine);

                    if ((counterRecords % append_file_after_records) == 0) {
                        fileWriter.write(recordBuffer.toString());
                        recordBuffer = new StringBuffer();
                    }
                }

                // append rest and close file
                if (recordBuffer.length() > 0) {
                    fileWriter.write(recordBuffer.toString());
                    fileWriter.close();
                }

                rs.close();
                logger.info(counterRecords + " records written to file " + fileName);
                counterRecords = 0;

                // required in case of last table that had to be copied
                model.setCurrentProgressTable(++counterTables);

                // set processed
                tableProcess.setAttribute(XMLTags.PROCESSED, "true");
            }

            stmSource.close();

            DBConnection.closeConnection(connSource);

            if (!isInterrupted()) {
                logger.info(counterTables + " table(s) processed");
            }
        } catch (SQLException sqle) {
            throw new PluginException(sqle);
        } catch (Exception e1) {
            // clean up
            try {
                DBConnection.closeConnection(connSource);
            } catch (CloseConnectionException e2) {
                // bad luck ... don't worry
            }

            throw new PluginException(e1.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param processColumns DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private void initHeader(List processColumns) throws IllegalArgumentException {
        if (processColumns == null) {
            throw new IllegalArgumentException("Missing processColumns");
        }

        recordBuffer = new StringBuffer();

        Iterator itProcessColumns = processColumns.iterator();

        // set the column headings
        while (itProcessColumns.hasNext()) {
            recordBuffer.append(((Element) itProcessColumns.next()).getAttributeValue(XMLTags.NAME) + delimiter);
        }

        recordBuffer.append(newLine);
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private String getFileName(String tableName) throws IllegalArgumentException {
        if (tableName == null) {
            throw new IllegalArgumentException("Missing tableName");
        }

        return path + tableName + "." + fileType;
    }
}
