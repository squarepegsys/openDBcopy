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
package opendbcopy.plugin.copy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import opendbcopy.config.XMLTags;
import opendbcopy.connection.DBConnection;
import opendbcopy.connection.exception.CloseConnectionException;
import opendbcopy.controller.MainController;
import opendbcopy.filter.StringConverter;
import opendbcopy.plugin.model.DynamicPluginThread;
import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.exception.PluginException;
import opendbcopy.sql.Helper;
import opendbcopy.util.InputOutputHelper;

import org.jdom.Element;


/**
 * Copies records of selected source tables and columns into selected destination tables. If the underlying 
 * RDBMS supports Referential Integrity Constraints, the order of tables to process is respected. If possible
 * errors shall be logged, those are logged into comma separated value files.
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class CopyMappingPlugin extends DynamicPluginThread {
    private static final String            fileType = "csv";
    private DatabaseModel     model; // this plugin's model
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
    private File              outputPath = null;
    private String            fileName = "";
    private String            delimiter = "";
    private boolean           log_error = false;
    private boolean           errorLogSetup = false;
    private int               counterRecords = 0;
    private int               counterTables = 0;
    private List              processColumns;
    private List              processTables;
    boolean                   trimString = false;
    boolean                   trimAndRemoveMultipleIntermediateWhitespaces = false;
    boolean                   trimAndReturnNullWhenEmpty = false;

    /**
     * Creates a new CopyMappingPlugin object.
     *
     * @param controller DOCUMENT ME!
     * @param baseModel DOCUMENT ME!
     *
     * @throws PluginException DOCUMENT ME!
     */
    public CopyMappingPlugin(MainController controller,
                             Model          baseModel) throws PluginException {
        // Call the super constructor
    	super(controller, baseModel);
    	
    	// cast the super base model into a specific database model
        this.model = (DatabaseModel) baseModel;
    }

    /**
     * Read configuration and setup database connections
     *
     * @throws PluginException DOCUMENT ME!
     */
    protected final void setUp() throws PluginException {
        // set the new line character as this differs from OS to OS
    	newLine = MainController.lineSep;

        // get the plugin configuration
        Element conf = model.getConf();

        if (conf == null) {
            throw new PluginException("Missing conf element");
        }

        try {
        	// set the output path selected by user
            outputPath = new File(conf.getChild(XMLTags.DIR).getAttributeValue(XMLTags.VALUE));

            // create the output directory if it does not yet exist
            if (!outputPath.exists()) {
                boolean mkDirOk = outputPath.mkdir();

                if (!mkDirOk) {
                    throw new PluginException("Could not create " + outputPath.getAbsolutePath());
                }
            }

            // shall errors be logged?
            log_error                     = Boolean.valueOf(conf.getChild(XMLTags.LOG_ERROR).getAttributeValue(XMLTags.VALUE)).booleanValue();

            if (log_error) {
                recordBuffer          = new StringBuffer();
                recordErrorBuffer     = new StringBuffer();
            }

            // check string filters
            if (model.getStringFilterTrim().getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0) {
                trimString = true;
            }

            if (model.getStringFilterRemoveIntermediateWhitespaces().getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0) {
                trimAndRemoveMultipleIntermediateWhitespaces = true;
            }

            if (model.getStringFilterSetNull().getAttributeValue(XMLTags.PROCESS).compareTo("true") == 0) {
                trimAndReturnNullWhenEmpty = true;
            }

            // get connections
            connSource          = DBConnection.getConnection(model.getSourceConnection());
            connDestination     = DBConnection.getConnection(model.getDestinationConnection());

            // extract the tables to copy
            processTables = model.getDestinationTablesToProcessOrdered();
        } catch (Exception e) {
            throw new PluginException(e);
        }

        // now set the number of tables that need to be copied
        model.setLengthProgressTable(processTables.size());
    }

    /**
     * Copies records of selected source tables and columns into selected destination tables. 
     * If requested and occuring, errors are logged
     *
     * @throws PluginException DOCUMENT ME!
     */
    public final void execute() throws PluginException {
        Iterator itColumns;
        Element  tableProcess;
        Element  columnDestination;
        int      colCounter;
        Object   input;

        try {
            stmSource = connSource.createStatement();

            Iterator itProcessTables = processTables.iterator();
            
            ArrayList generatedFiles = new ArrayList();

            while (!isInterrupted() && itProcessTables.hasNext()) {
                tableProcess     = (Element) itProcessTables.next();

                sourceTableName          = tableProcess.getAttributeValue(XMLTags.SOURCE_DB);
                destinationTableName     = tableProcess.getAttributeValue(XMLTags.DESTINATION_DB);

                // file name for error logs
                if (log_error) {
                    fileName = destinationTableName + "_ERRORS" + "." + fileType;
                }

                // get the columns to process
                processColumns = model.getMappingColumnsToProcessByDestinationTable(destinationTableName);

                // setting record counter to minimum of progress bar
                model.setCurrentProgressRecord(0);
                model.setLengthProgressRecord(0);

                // Reading number of records for progress bar
                model.setLengthProgressRecord(Helper.getNumberOfRecordsFiltered(stmSource, model, XMLTags.SOURCE_DB, sourceTableName));

                // get Select Statement for source model
                stmSelect     = Helper.getSelectStatement(model, sourceTableName, XMLTags.SOURCE_DB, processColumns);

                // get Insert Statement for destination model
                stmInsert     = Helper.getInsertPreparedStatement(model.getQualifiedDestinationTableName(destinationTableName), processColumns);

                pstmtDestination = connDestination.prepareStatement(stmInsert);

                model.setCurrentProgressTable(counterTables);

                // Execute SELECT
                rs = stmSource.executeQuery(stmSelect);

                // do some logging
                model.setProgressMessage("Copying " + model.getQualifiedSourceTableName(sourceTableName) + " into " + model.getQualifiedDestinationTableName(destinationTableName) + " ...");

                logger.info("Copying " + model.getQualifiedSourceTableName(sourceTableName) + " into " + model.getQualifiedDestinationTableName(destinationTableName) + " ...");

                // while there are more records to process and the process is not interrupted by the user
                while (!isInterrupted() && rs.next()) {
                    colCounter     = 1;

                    // process columns
                    itColumns = processColumns.iterator();

                    while (itColumns.hasNext()) {
                        columnDestination     = model.getDestinationColumn(destinationTableName, ((Element) itColumns.next()).getAttributeValue(XMLTags.DESTINATION_DB));

                        input = rs.getObject(colCounter);

                        if (input != null) {
                            input = applyStringFilters(input, Boolean.valueOf(columnDestination.getAttributeValue(XMLTags.NULLABLE)).booleanValue());

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
                        model.setCurrentProgressRecord(counterRecords);
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

                if (!isInterrupted()) {
                	// commit INSERTs. Commit behaviour depends on RDBMS used
                    connDestination.commit();
                    
                    // close the result set
                    rs.close();
                    logger.info(counterRecords + " records inserted into table " + destinationTableName);
                    counterRecords = 0;

                    // required in case of last table that had to be copied
                    counterTables++;
                    model.setCurrentProgressTable(counterTables);

                    // set processed
                    tableProcess.setAttribute(XMLTags.PROCESSED, "true");
                } else {
                	// rollback insert in case the user interrupts the process
                    connDestination.rollback();
                    
                    // close the result set
                    rs.close();
                    counterRecords = 0;
                }

                if (log_error) {
                    if (recordErrorBuffer.length() > 0) {
                        // open file writer			
                    	File errorFile = new File(outputPath.getAbsolutePath() + MainController.fileSep + fileName);
                        OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(errorFile), MainController.getEncoding());
                        fileWriter.write(recordErrorBuffer.toString());
                        fileWriter.close();
                        generatedFiles.add(errorFile);

                        logger.error(errorFile + " contains records which could not be processed");
                        recordErrorBuffer     = new StringBuffer();
                        errorLogSetup         = false;
                    }
                }
            }

            stmSource.close();
            pstmtDestination.close();

            // close database connections
            DBConnection.closeConnection(connSource);
            DBConnection.closeConnection(connDestination);

            if (!isInterrupted()) {
            	if (generatedFiles != null && generatedFiles.size() > 0) {
                    File[] outputFiles = new File[generatedFiles.size()];
                    outputFiles = (File[]) generatedFiles.toArray(outputFiles);

                    Element outputConf = model.getConf().getChild(XMLTags.OUTPUT);

                    model.appendToOutput(InputOutputHelper.createFileListElement(outputFiles, outputConf.getChild(XMLTags.FILELIST).getAttributeValue(XMLTags.VALUE)));
            	}

                logger.info(counterTables + " table(s) processed");
            }
        } catch (SQLException sqle) {
            throw new PluginException(sqle);
        } catch (Exception e1) {
            // clean up if required
            try {
                DBConnection.closeConnection(connSource);
                DBConnection.closeConnection(connDestination);
            } catch (CloseConnectionException e2) {
                // bad luck ... don't worry
            }

            throw new PluginException(e1);
        }
    }

    /**
     * If global string filters were selected by user, those are applied using this method
     *
     * @param in DOCUMENT ME!
     * @param returnNullWhenEmpty DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Object applyStringFilters(Object  in,
                                      boolean returnNullWhenEmpty) {
        if (in instanceof String || in instanceof Character) {
            if (trimAndRemoveMultipleIntermediateWhitespaces && trimAndReturnNullWhenEmpty) {
                return StringConverter.trimAndRemoveMultipleIntermediateWhitespaces(in, returnNullWhenEmpty);
            } else if (trimAndRemoveMultipleIntermediateWhitespaces && !trimAndReturnNullWhenEmpty) {
                return StringConverter.trimAndRemoveMultipleIntermediateWhitespaces(in, false);
            } else if (trimString && trimAndReturnNullWhenEmpty) {
                return StringConverter.trimString(in, returnNullWhenEmpty);
            } else if (trimString && !trimAndReturnNullWhenEmpty) {
                return StringConverter.trimString(in, false);
            } else {
                return in;
            }
        } else {
            return in;
        }
    }

    /**
     * Called to write a nice row header of column names in possible error logs
     *
     * @param processColumns DOCUMENT ME!
     */
    private void initErrorLog(List processColumns) {
        Iterator itColumns = processColumns.iterator();

        // set the column headings for the possible error log
        while (itColumns.hasNext()) {
            recordErrorBuffer.append(((Element) itColumns.next()).getAttributeValue(XMLTags.DESTINATION_DB) + delimiter);
        }

        recordErrorBuffer.append("ERROR" + newLine);
        errorLogSetup = true;
    }
}
