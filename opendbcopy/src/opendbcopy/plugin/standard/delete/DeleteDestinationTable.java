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
package opendbcopy.plugin.standard.delete;

import opendbcopy.config.XMLTags;

import opendbcopy.connection.DBConnection;

import opendbcopy.model.ProjectModel;

import opendbcopy.plugin.ExecuteSkeleton;

import opendbcopy.task.TaskExecute;

import org.apache.log4j.Logger;

import org.jdom.Element;

import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.sql.Connection;
import java.sql.Statement;

import java.util.Iterator;
import java.util.List;


/**
 * class description
 *
 * @author Anthony Smith
 * @version $Revision$
 */
public class DeleteDestinationTable extends ExecuteSkeleton {
    private static Logger logger = Logger.getLogger(DeleteDestinationTable.class.getName());
    private TaskExecute   task = null;
    private List          processTables = null;
    private Connection    connDestination = null;
    private Statement     stmDestination = null;
    private String        deleteString = null;
    private int           counterTables = 0;

    /**
     * Creates a new CopyMapping object.
     *
     * @param task DOCUMENT ME!
     * @param projectModel DOCUMENT ME!
     */
    public DeleteDestinationTable(TaskExecute  task,
                                  ProjectModel projectModel) {
        this.task = task;

        try {
            // get connection to destination database
            connDestination = DBConnection.getConnection(projectModel.getDestinationConnection());

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
            Iterator itProcessTables = processTables.iterator();

            while (!task.isInterrupted() && itProcessTables.hasNext()) {
                Element tableProcess = (Element) itProcessTables.next();

                String  destinationTableName = tableProcess.getAttributeValue(XMLTags.DESTINATION_DB);

                // setting record counter to minimum of progress bar
                task.setCurrentRecord(0);
                task.setLengthOfTaskRecord(0);

                // get Delete Statement
                stmDestination     = connDestination.createStatement();
                deleteString       = ExecuteSkeleton.getDeleteTableStatement(projectModel.getQualifiedDestinationTableName(destinationTableName));

                task.setCurrentTable(counterTables);

                // Execute Delete
                int recordsDeleted = stmDestination.executeUpdate(deleteString);

                task.setLengthOfTaskRecord(recordsDeleted);
                task.setCurrentRecord(recordsDeleted);

                task.setMessage("Deleted " + recordsDeleted + " records in " + projectModel.getQualifiedDestinationTableName(destinationTableName));
                logger.info("Deleted " + recordsDeleted + " records in " + projectModel.getQualifiedDestinationTableName(destinationTableName));

                // required in case of last table that had to be copied
                counterTables++;
                task.setCurrentTable(counterTables);

                // set processed
                tableProcess.setAttribute(XMLTags.PROCESSED, "true");
            }

            if (!task.isInterrupted()) {
                if (processTables.size() > 0) {
                    connDestination.commit();
                    stmDestination.close();
                    DBConnection.closeConnection(connDestination, true);
                    logger.info(counterTables + " table(s) deleted and commited.");
                } else {
                    logger.warn("no tables to process!");
                }

                this.task.setTaskStatus(this.task.done);
            } else {
                if (processTables.size() > 0) {
                    connDestination.rollback();
                    stmDestination.close();
                }

                DBConnection.closeConnection(connDestination, false);
                logger.info("execution cancelled by user. A Rollback has been made.");
            }
        } catch (Exception e) {
            DBConnection.closeConnection(connDestination, false);
            throw e;
        }
    }
}
