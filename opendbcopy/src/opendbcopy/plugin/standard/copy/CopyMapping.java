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
package opendbcopy.plugin.standard.copy;

import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;

import opendbcopy.io.Writer;

import opendbcopy.model.ProjectModel;

import opendbcopy.plugin.ExecuteSkeleton;

import opendbcopy.task.TaskExecute;

import org.apache.log4j.Logger;

import org.jdom.Element;

import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
public class CopyMapping extends ExecuteSkeleton {
    private static Logger     logger = Logger.getLogger(CopyMapping.class.getName());
    private TaskExecute       task;
    private Element           conf;
    private Connection        connSource;
    private Connection        connDestination;
    private Statement         stmSource;
    private PreparedStatement pstmtDestination;
    private ResultSet         rs;
    private StringBuffer      recordBuffer; // to hold records contents
    private StringBuffer      recordErrorBuffer; // in case of errors records are written to file
    private String            stmSelect = "";
    private String            stmInsert = "";
    private String            sourceTableName = "";
    private String            destinationTableName = "";
    private String            newLine = "";
    private String            path = "";
    private String            fileName = "";
    private String            fileType = "";
    private String            delimiter = "";
    private boolean           log_error = false;
    private boolean           errorLogSetup = false;
    private int               counterRecords = 0;
    private int               counterTables = 0;
    private List              processColumns;
    private List              processTables;

    /**
     * Creates a new CopyMapping object.
     *
     * @param task DOCUMENT ME!
     * @param projectModel DOCUMENT ME!
     */
    public CopyMapping(TaskExecute  task,
                       ProjectModel projectModel) {
        this.task     = task;

        newLine     = System.getProperty("line.separator");

        // read the plugin configuration
        conf          = projectModel.getPlugin().getChild(XMLTags.CONF);
        path          = conf.getChild(XMLTags.PATH).getAttributeValue(XMLTags.VALUE);
        fileType      = conf.getChild(XMLTags.FILE_TYPE).getAttributeValue(XMLTags.VALUE);
        delimiter     = conf.getChild(XMLTags.DELIMITER).getAttributeValue(XMLTags.VALUE);
        log_error     = Boolean.valueOf(conf.getChild(XMLTags.LOG_ERROR).getAttributeValue(XMLTags.VALUE)).booleanValue();

        if (log_error) {
            recordBuffer          = new StringBuffer();
            recordErrorBuffer     = new StringBuffer();
        }

        try {
            // get connections
            connSource          = DBConnection.getConnection(projectModel.getSourceConnection());
            connDestination     = DBConnection.getConnection(projectModel.getDestinationConnection());

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
     * @throws SocketTimeoutException DOCUMENT ME!
     * @throws SocketException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public final void doExecute(TaskExecute  task,
                                ProjectModel projectModel) throws SocketTimeoutException, SocketException, Exception {
        // extract the tables to copy
        processTables = projectModel.getDestinationTablesToProcessOrdered();

        // now set the number of tables that need to be copied
        task.setLengthOfTaskTable(processTables.size());

        try {
            Iterator itColumns;
            Element  tableProcess;
            Element  columnDestination;
            int      colCounter;
            Object   input;

            stmSource = connSource.createStatement();

            Iterator itProcessTables = processTables.iterator();

            while (!task.isInterrupted() && itProcessTables.hasNext()) {
                tableProcess     = (Element) itProcessTables.next();

                sourceTableName          = tableProcess.getAttributeValue(XMLTags.SOURCE_DB);
                destinationTableName     = tableProcess.getAttributeValue(XMLTags.DESTINATION_DB);

                if (log_error) {
                    fileName = path + destinationTableName + "_ERRORS" + "." + fileType;
                }

                processColumns = projectModel.getMappingColumnsToProcessByDestinationTable(destinationTableName);

                // setting record counter to minimum of progress bar
                task.setCurrentRecord(0);
                task.setLengthOfTaskRecord(0);

                // Reading number of records for progress bar
                task.setLengthOfTaskRecord(ExecuteSkeleton.getNumberOfRecordsFiltered(stmSource, projectModel, XMLTags.SOURCE_DB, sourceTableName));

                // get Select Statement
                stmSelect     = ExecuteSkeleton.getSelectStatement(projectModel, sourceTableName, XMLTags.SOURCE_DB, processColumns);

                // get Insert Statement
                stmInsert     = ExecuteSkeleton.getInsertPreparedStatement(projectModel.getQualifiedDestinationTableName(destinationTableName), processColumns);

                pstmtDestination = connDestination.prepareStatement(stmInsert);

                task.setCurrentTable(counterTables);

                // Execute SELECT
                rs = stmSource.executeQuery(stmSelect);

                task.setMessage("Copying " + projectModel.getQualifiedSourceTableName(sourceTableName) + " into " + projectModel.getQualifiedDestinationTableName(destinationTableName) + " ...");

                logger.info("Copying " + projectModel.getQualifiedSourceTableName(sourceTableName) + " into " + projectModel.getQualifiedDestinationTableName(destinationTableName) + " ...");

                while (!task.isInterrupted() && rs.next()) {
                    colCounter     = 1;

                    // process columns
                    itColumns = processColumns.iterator();

                    while (itColumns.hasNext()) {
                        columnDestination     = projectModel.getDestinationColumn(destinationTableName, ((Element) itColumns.next()).getAttributeValue(XMLTags.DESTINATION_DB));

                        input = rs.getObject(colCounter);

                        if (input != null) {
                            input = ExecuteSkeleton.applyStringFilters(input, Boolean.valueOf(columnDestination.getAttributeValue(XMLTags.NULLABLE)).booleanValue(), projectModel);

                            if (input != null) {
                                if (log_error) {
                                    recordBuffer.append(input + delimiter);
                                }

                                pstmtDestination.setObject(colCounter, input);
                            } else {
                                if (log_error) {
                                    recordBuffer.append("null" + delimiter);
                                }

                                pstmtDestination.setNull(colCounter, Integer.parseInt(columnDestination.getAttributeValue(XMLTags.DATA_TYPE)));
                            }
                        } else {
                            if (log_error) {
                                recordBuffer.append("null" + delimiter);
                            }

                            pstmtDestination.setNull(colCounter, Integer.parseInt(columnDestination.getAttributeValue(XMLTags.DATA_TYPE)));
                        }

                        colCounter++;
                    }

                    // execute the prepared statement and log the error ... and continue without disturbing other business
                    try {
                        // Execute INSERT
                        pstmtDestination.executeUpdate();
                        pstmtDestination.clearParameters();

                        counterRecords++;
                        task.setCurrentRecord(counterRecords);
                    } catch (SQLException e) {
                        connDestination.rollback();

                        if (log_error && !errorLogSetup) {
                            initErrorLog(processColumns);
                        }

                        if (log_error) {
                            recordErrorBuffer.append(recordBuffer + e.toString() + newLine);
                        }
                    } finally {
                        // reset recordBuffer
                        if (log_error) {
                            recordBuffer = new StringBuffer();
                        }
                    }
                }

                if (!task.isInterrupted()) {
                    connDestination.commit();
                    rs.close();
                    logger.info(counterRecords + " records inserted into table " + destinationTableName);
                    counterRecords = 0;

                    // required in case of last table that had to be copied
                    counterTables++;
                    task.setCurrentTable(counterTables);

                    // set processed
                    tableProcess.setAttribute(XMLTags.PROCESSED, "true");
                } else {
                    connDestination.rollback();
                    rs.close();
                    counterRecords = 0;
                }

                if (log_error) {
                    if (recordErrorBuffer.length() > 0) {
                        Writer.write(recordErrorBuffer, fileName);
                        logger.error(fileName + " contains records which could not be processed");
                        recordErrorBuffer     = new StringBuffer();
                        errorLogSetup         = false;
                    }
                }
            }

            stmSource.close();
            pstmtDestination.close();

            DBConnection.closeConnection(connSource, true);
            DBConnection.closeConnection(connDestination, true);

            if (!task.isInterrupted()) {
                logger.info(counterTables + " table(s) processed");
                this.task.setTaskStatus(this.task.done);
            } else {
                logger.info("interrupted by user");
                this.task.setTaskStatus(this.task.interrupted);
            }
        } catch (Exception e) {
            DBConnection.closeConnection(connSource, false);
            DBConnection.closeConnection(connDestination, false);
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
    private void initErrorLog(List processColumns) throws Exception {
        Iterator itColumns = processColumns.iterator();

        // set the column headings for the possible error log
        while (itColumns.hasNext()) {
            recordErrorBuffer.append(((Element) itColumns.next()).getAttributeValue(XMLTags.DESTINATION_DB) + delimiter);
        }

        recordErrorBuffer.append("ERROR" + newLine);
        errorLogSetup = true;
    }
}
