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
package opendbcopy.plugin.standard.dump;

import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;

import opendbcopy.model.ProjectModel;

import opendbcopy.plugin.ExecuteSkeleton;

import opendbcopy.task.TaskExecute;

import org.apache.log4j.Logger;

import org.jdom.Element;

import java.io.File;
import java.io.FileWriter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.Iterator;
import java.util.List;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class DumpToFileDelimited extends ExecuteSkeleton {
    private static Logger logger = Logger.getLogger(DumpToFileDelimited.class.getName());
    private TaskExecute   task = null;
    private Connection    connSource = null;
    private Statement     stmSource = null;
    private ResultSet     rs = null;
    private StringBuffer  recordBuffer = new StringBuffer(); // to hold records contents
    private Element       conf = null;
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

    /**
     * Creates a new CopyMapping object.
     *
     * @param task DOCUMENT ME!
     * @param projectModel DOCUMENT ME!
     */
    public DumpToFileDelimited(TaskExecute  task,
                               ProjectModel projectModel) {
        this.task     = task;

        // read the plugins configuration
        this.conf                          = projectModel.getPlugin().getChild(XMLTags.CONF);
        this.path                          = conf.getChild(XMLTags.PATH).getAttributeValue(XMLTags.VALUE);
        this.fileType                      = conf.getChild(XMLTags.FILE_TYPE).getAttributeValue(XMLTags.VALUE);
        this.delimiterOriginal             = this.conf.getChild(XMLTags.DELIMITER).getAttributeValue(XMLTags.VALUE);
        this.delimiter                     = this.delimiterOriginal;
        this.show_header                   = Boolean.valueOf(conf.getChild(XMLTags.SHOW_HEADER).getAttributeValue(XMLTags.VALUE)).booleanValue();
        this.show_null_values              = Boolean.valueOf(conf.getChild("show_null_values").getAttributeValue(XMLTags.VALUE)).booleanValue();
        this.append_file_after_records     = Integer.parseInt(conf.getChild(XMLTags.APPEND_FILE_AFTER_RECORDS).getAttributeValue(XMLTags.VALUE));

        // use the system's newline character instead of \n
        this.newLine = System.getProperty("line.separator");

        if (this.conf.getChild(XMLTags.DELIMITER_POSITION).getAttributeValue(XMLTags.VALUE).compareTo("after") == 0) {
            append_delimiter_after_data = true;
        }

        try {
            // get connection
            connSource = DBConnection.getConnection(projectModel.getSourceConnection());

            // execute unprocessed tables
            doExecute(task, projectModel);
        }
        // hummm ... check error log
         catch (Exception e) {
            logger.error(e.toString());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param task DOCUMENT ME!
     * @param projectModel DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final void doExecute(TaskExecute  task,
                                ProjectModel projectModel) throws Exception {
        // extract the tables to dump
        List processTables = projectModel.getSourceTablesToProcessOrdered();

        int  nbrTables = processTables.size();

        // now set the number of tables that need to be copied
        task.setLengthOfTaskTable(nbrTables);

        try {
            stmSource = connSource.createStatement();

            String   stmSelect = "";

            Iterator itProcessTables = processTables.iterator();

            while (itProcessTables.hasNext() && !task.isInterrupted()) {
                Element tableProcess = (Element) itProcessTables.next();

                String  sourceTableName = tableProcess.getAttributeValue(XMLTags.NAME);

                String  fileName = getFileName(sourceTableName);

                counterRecords = 0;

                List processColumns = projectModel.getSourceColumnsToProcess(sourceTableName);

                if (show_header) {
                    initHeader(processColumns);
                } else {
                    recordBuffer = new StringBuffer();
                }

                // setting record counter to minimum of progress bar
                task.setCurrentRecord(0);
                task.setLengthOfTaskRecord(0);

                // Reading number of records for progress bar
                task.setLengthOfTaskRecord(ExecuteSkeleton.getNumberOfRecordsFiltered(stmSource, projectModel, XMLTags.NAME, sourceTableName));

                // get Select Statement
                stmSelect = ExecuteSkeleton.getSelectStatement(projectModel, sourceTableName, XMLTags.NAME, processColumns);

                task.setCurrentTable(counterTables);

                // Execute SELECT
                rs = stmSource.executeQuery(stmSelect);

                task.setMessage("Reading " + sourceTableName + " ...");

                logger.info("Reading " + sourceTableName + " ...");

                // open file writer					
                FileWriter fileWriter = new FileWriter(new File(fileName));

                while (rs.next()) {
                    task.setCurrentRecord(++counterRecords);

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
                task.setCurrentTable(++counterTables);

                // set processed
                tableProcess.setAttribute(XMLTags.PROCESSED, "true");
            }

            stmSource.close();

            DBConnection.closeConnection(connSource, true);

            if (!task.isInterrupted()) {
                logger.info(counterTables + " table(s) processed");
                this.task.setTaskStatus(this.task.done);
            } else {
                this.task.setTaskStatus(this.task.interrupted);
                logger.info("interrupted by user");
            }
        } catch (Exception e) {
            DBConnection.closeConnection(connSource, false);
            throw e;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param processColumns DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void initHeader(List processColumns) throws Exception {
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
     * @throws Exception DOCUMENT ME!
     */
    private String getFileName(String tableName) throws Exception {
        return path + tableName + "." + fileType;
    }
}
