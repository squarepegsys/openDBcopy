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
package opendbcopy.plugin.standard.script;

import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;

import opendbcopy.io.Writer;

import opendbcopy.model.ProjectModel;

import opendbcopy.model.typeinfo.TypeInfo;
import opendbcopy.model.typeinfo.TypeInfoHelper;

import opendbcopy.plugin.ExecuteSkeleton;

import opendbcopy.task.TaskExecute;

import opendbcopy.util.IntHashMap;

import org.apache.log4j.Logger;

import org.jdom.Element;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class CreateDataScript extends ExecuteSkeleton {
    private static Logger logger = Logger.getLogger(CreateDataScript.class.getName());
    private TaskExecute   task;
    private ProjectModel  projectModel;
    private Connection    connSource;
    private Statement     stmSource;
    private ResultSet     rs;
    StringBuffer          sbScript;
    Element               conf;
    String                path = "";
    String                newLine = "";
    String                database = "";
    String                identifierQuoteStringOut = "";
    //char identifierQuoteStringOut = '';
    private boolean       show_qualified_table_name = false;
    private int           counterRecords = 0;
    private int           counterTables = 0;

    /**
     * Creates a new CopyMapping object.
     *
     * @param task DOCUMENT ME!
     * @param projectModel DOCUMENT ME!
     */
    public CreateDataScript(TaskExecute  task,
                            ProjectModel projectModel) {
        this.task             = task;
        this.projectModel     = projectModel;

        try {
            newLine     = System.getProperty("line.separator");

            // read the plugin configuration
            conf                          = projectModel.getPlugin().getChild(XMLTags.CONF);
            path                          = conf.getChild(XMLTags.PATH).getAttributeValue(XMLTags.VALUE);
            show_qualified_table_name     = Boolean.valueOf(conf.getChild("show_qualified_table_name").getAttributeValue(XMLTags.VALUE)).booleanValue();

            if (projectModel.getDbMode() == projectModel.DUAL_MODE) {
                identifierQuoteStringOut     = projectModel.getDestinationMetadata().getChild(XMLTags.IDENTIFIER_QUOTE_STRING).getAttributeValue(XMLTags.VALUE);
                database                     = projectModel.getDestinationDb().getName();
            } else {
                identifierQuoteStringOut     = projectModel.getSourceMetadata().getChild(XMLTags.IDENTIFIER_QUOTE_STRING).getAttributeValue(XMLTags.VALUE);
                database                     = projectModel.getSourceDb().getName();
            }

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
        List processTables = null;

        // extract the tables to dump
        if (projectModel.getDbMode() == projectModel.DUAL_MODE) {
            processTables = projectModel.getDestinationTablesToProcessOrdered();
        } else {
            processTables = projectModel.getSourceTablesToProcessOrdered();
        }

        int nbrTables = processTables.size();

        // now set the number of tables that need to be copied
        task.setLengthOfTaskTable(nbrTables);

        try {
            stmSource = connSource.createStatement();

            String   sourceTableName = "";
            String   destinationTableName = "";
            String   qualifiedTableName = "";
            String   tableName = "";
            String   fileName = "";
            String   selectStm = "";
            Iterator itProcessTables = processTables.iterator();

            while (itProcessTables.hasNext() && !task.isInterrupted()) {
                Element tableProcess = (Element) itProcessTables.next();
                List    processColumns = null;
                counterRecords = 0;

                if (projectModel.getDbMode() == projectModel.DUAL_MODE) {
                    sourceTableName          = tableProcess.getAttributeValue(XMLTags.SOURCE_DB);
                    destinationTableName     = tableProcess.getAttributeValue(XMLTags.DESTINATION_DB);
                    processColumns           = projectModel.getMappingColumnsToProcessByDestinationTable(destinationTableName);
                    fileName                 = path + counterTables + "_" + destinationTableName + ".sql";
                    selectStm                = ExecuteSkeleton.getSelectStatement(projectModel, sourceTableName, XMLTags.SOURCE_DB, processColumns);

                    if (show_qualified_table_name) {
                        qualifiedTableName = projectModel.getQualifiedDestinationTableName(destinationTableName);
                    } else {
                        qualifiedTableName = destinationTableName;
                    }

                    tableName = destinationTableName;
                } else {
                    sourceTableName     = tableProcess.getAttributeValue(XMLTags.NAME);
                    processColumns      = projectModel.getSourceColumnsToProcess(sourceTableName);
                    fileName            = path + counterTables + "_" + sourceTableName + ".sql";
                    selectStm           = ExecuteSkeleton.getSelectStatement(projectModel, sourceTableName, XMLTags.NAME, processColumns);

                    if (show_qualified_table_name) {
                        qualifiedTableName = projectModel.getQualifiedSourceTableName(sourceTableName);
                    } else {
                        qualifiedTableName = sourceTableName;
                    }

                    tableName = sourceTableName;
                }

                // Reading number of records for progress bar
                task.setLengthOfTaskRecord(ExecuteSkeleton.getNumberOfRecordsFiltered(stmSource, projectModel, XMLTags.SOURCE_DB, sourceTableName));
                task.setCurrentRecord(counterRecords);

                task.setMessage("Reading " + qualifiedTableName + " ...");

                logger.info("Reading " + qualifiedTableName + " ...");

                // initalise stringBuffer sbScript and script header
                initScriptHeader(qualifiedTableName, counterTables);

                ResultSet srcResult = stmSource.executeQuery(selectStm);

                genInserts(srcResult, tableName, qualifiedTableName, processColumns, sbScript);

                srcResult.close();

                Writer.write(sbScript, fileName);

                task.setCurrentTable(++counterTables);

                logger.info(counterRecords + " records written to file " + fileName);
            }

            if (!task.isInterrupted()) {
                logger.info(counterTables + " table(s) processed");
                this.task.setTaskStatus(this.task.done);
            } else {
                this.task.setTaskStatus(this.task.interrupted);
            }
        } catch (Exception e) {
            DBConnection.closeConnection(connSource, false);
            throw e;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param srcResult DOCUMENT ME!
     * @param tableName DOCUMENT ME!
     * @param qualifiedTableName DOCUMENT ME!
     * @param processColumns DOCUMENT ME!
     * @param sbScript DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    private void genInserts(ResultSet    srcResult,
                            String       tableName,
                            String       qualifiedTableName,
                            List         processColumns,
                            StringBuffer sbScript) throws SQLException, Exception {
        Iterator     itProcessColumns = processColumns.iterator();
        StringBuffer sbColumnNames = new StringBuffer();

        int          nbrCols = 1;
        IntHashMap   colTypeInfo = new IntHashMap();

        while (itProcessColumns.hasNext()) {
            Element column = (Element) itProcessColumns.next();

            if (projectModel.getDbMode() == projectModel.DUAL_MODE) {
                column = projectModel.getDestinationColumn(tableName, column.getAttributeValue(XMLTags.DESTINATION_DB));
            }

            sbColumnNames.append(column.getAttributeValue(XMLTags.NAME));

            colTypeInfo.put(nbrCols, new TypeInfo(projectModel.getTypeInfoByTypeName(column.getAttributeValue(XMLTags.TYPE_NAME), database)));

            if (nbrCols < processColumns.size()) {
                sbColumnNames.append(", ");
                nbrCols++;
            }
        }

        String in = null;

        while (srcResult.next() && !task.isInterrupted()) {
            sbScript.append("insert into ");
            sbScript.append(qualifiedTableName);
            sbScript.append(" (" + sbColumnNames.toString() + ")");
            sbScript.append(" values (");

            // read columns and append them
            for (int i = 1; i < (nbrCols + 1); i++) {
                in = srcResult.getString(i);

                if (srcResult.wasNull()) {
                    sbScript.append("null");
                } else {
                    sbScript.append(TypeInfoHelper.getFormattedString(in, (TypeInfo) colTypeInfo.get(i), identifierQuoteStringOut));
                }

                if (i < (nbrCols)) {
                    sbScript.append(", ");
                }
            }

            sbScript.append(");");
            sbScript.append(newLine);

            task.setCurrentRecord(++counterRecords);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param tableName DOCUMENT ME!
     * @param processOrder DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void initScriptHeader(String tableName,
                                  int    processOrder) throws Exception {
        sbScript = new StringBuffer();

        sbScript.append("#############################################################################" + newLine);
        sbScript.append("#" + newLine);
        sbScript.append("# Script for table " + tableName + newLine);
        sbScript.append("#" + newLine);
        sbScript.append("# Load this script as #" + processOrder + " if there are referential integrity constraints set" + newLine);
        sbScript.append("#" + newLine);
        sbScript.append("# generated on " + new Date().toString() + newLine);
        sbScript.append("#" + newLine);
        sbScript.append("#############################################################################" + newLine);
    }
}
