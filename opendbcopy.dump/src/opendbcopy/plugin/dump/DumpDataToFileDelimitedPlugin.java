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

import opendbcopy.config.APM;
import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;

import opendbcopy.connection.exception.CloseConnectionException;

import opendbcopy.controller.MainController;

import opendbcopy.plugin.model.DynamicPluginThread;
import opendbcopy.plugin.model.Model;
import opendbcopy.plugin.model.database.DatabaseModel;
import opendbcopy.plugin.model.exception.PluginException;

import opendbcopy.sql.Helper;

import opendbcopy.util.InputOutputHelper;

import org.jdom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
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
    private String        delimiter;
    private String        fileType = "";
    private boolean       show_header = false;
    private boolean       show_null_values = false;
    List                  processTables = null;
    private File          outputPath = null;

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
        // read the plugins configuration
        Element conf = model.getConf();

        try {
            outputPath = new File(conf.getChild(XMLTags.DIR).getAttributeValue(XMLTags.VALUE));

            if (!outputPath.exists()) {
                boolean mkDirOk = outputPath.mkdir();

                if (!mkDirOk) {
                    throw new PluginException("Could not create " + outputPath.getAbsolutePath());
                }
            }

            fileType                      = conf.getChild(XMLTags.FILE_TYPE).getAttributeValue(XMLTags.VALUE);
            show_header                   = Boolean.valueOf(conf.getChild(XMLTags.SHOW_HEADER).getAttributeValue(XMLTags.VALUE)).booleanValue();
            show_null_values              = Boolean.valueOf(conf.getChild("show_null_values").getAttributeValue(XMLTags.VALUE)).booleanValue();
            append_file_after_records     = Integer.parseInt(conf.getChild(XMLTags.APPEND_FILE_AFTER_RECORDS).getAttributeValue(XMLTags.VALUE));

            delimiter = conf.getChild(XMLTags.DELIMITER).getAttributeValue(XMLTags.VALUE);

            // transform \t for tabulator into unicode representation
            if (delimiter.compareToIgnoreCase("\\t") == 0) {
                delimiter = "\u0009";
            }

            // get connection
            connSource     = DBConnection.getConnection(model.getSourceConnection());

            // extract the tables to dump
            processTables = model.getSourceTablesToProcessOrdered();
        } catch (Exception e) {
            throw new PluginException(e);
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

            String    stmSelect = "";

            ArrayList generatedFiles = new ArrayList();

            Iterator  itProcessTables = processTables.iterator();

            while (itProcessTables.hasNext() && !isInterrupted()) {
                Element tableProcess = (Element) itProcessTables.next();

                String  sourceTableName = tableProcess.getAttributeValue(XMLTags.NAME);

                File    file = new File(outputPath.getAbsolutePath() + APM.FILE_SEP + getFileName(sourceTableName));

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
                model.setLengthProgressRecord(Helper.getNumberOfRecordsFiltered(stmSource, model, XMLTags.SOURCE_DB, sourceTableName));

                // get Select Statement
                stmSelect = Helper.getSelectStatement(model, sourceTableName, XMLTags.NAME, processColumns);

                model.setCurrentProgressTable(counterTables);

                // Execute SELECT
                rs = stmSource.executeQuery(stmSelect);

                model.setProgressMessage("Reading " + sourceTableName + " ...");

                logger.info("Reading " + sourceTableName + " ...");

                // open file writer				
                OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file), MainController.getEncoding());

                while (rs.next()) {
                    model.setCurrentProgressRecord(++counterRecords);

                    // process columns
                    for (int colCounter = 1; colCounter < (processColumns.size() + 1); colCounter++) {
                        String input = rs.getString(colCounter);

                        if (!rs.wasNull()) {
                            recordBuffer.append(input + delimiter);
                        } else {
                            if (show_null_values) {
                                recordBuffer.append("null" + delimiter);
                            } else {
                                recordBuffer.append("" + delimiter);
                            }
                        }
                    }

                    recordBuffer.append(APM.LINE_SEP);

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

                generatedFiles.add(file);

                logger.info(counterRecords + " records written to file " + file.getAbsolutePath());
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

                File[] outputFiles = new File[generatedFiles.size()];
                outputFiles = (File[]) generatedFiles.toArray(outputFiles);

                Element outputConf = model.getConf().getChild(XMLTags.OUTPUT);

                model.appendToOutput(InputOutputHelper.createFileListElement(outputFiles, outputConf.getChild(XMLTags.FILELIST).getAttributeValue(XMLTags.VALUE)));
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

            throw new PluginException(e1);
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

        recordBuffer.append(APM.LINE_SEP);
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

        return tableName + "." + fileType;
    }
}
